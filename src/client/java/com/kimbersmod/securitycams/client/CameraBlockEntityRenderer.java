package com.kimbersmod.securitycams.client;

import com.kimbersmod.securitycams.CameraBlock;
import com.kimbersmod.securitycams.CameraBlockEntity;
import com.kimbersmod.securitycams.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the turning head of a security camera. The mount (plate and arm) is an ordinary block model;
 * the head is the model of a hidden helper block, drawn rotated around the pivot so it points wherever
 * the camera is currently looking.
 */
public class CameraBlockEntityRenderer implements BlockEntityRenderer<CameraBlockEntity, CameraBlockEntityRenderer.State> {
	/** The pivot of the head model as authored (pixels): the end of the arm of a north-facing camera. */
	private static final Vec3 AUTHORED_PIVOT = new Vec3(8.0 / 16.0, 8.5 / 16.0, 11.0 / 16.0);

	public static class State extends BlockEntityRenderState {
		public @Nullable MovingBlockRenderState head;
		public Direction facing = Direction.NORTH;
		public float yaw;
		public float pitch;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(
		CameraBlockEntity camera,
		State state,
		float partialTicks,
		Vec3 cameraPosition,
		ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(camera, state, partialTicks, cameraPosition, breakProgress);
		state.head = null;
		if (!(camera.getLevel() instanceof ClientLevel level)) {
			return;
		}
		BlockPos pos = camera.getBlockPos();
		MovingBlockRenderState head = new MovingBlockRenderState();
		head.randomSeedPos = pos;
		head.blockPos = pos;
		head.blockState = ModBlocks.CAMERA_HEAD.defaultBlockState();
		head.biome = level.getBiome(pos);
		head.cardinalLighting = level.cardinalLighting();
		head.lightEngine = level.getLightEngine();
		state.head = head;

		float time = level.getGameTime() + partialTicks;
		state.facing = camera.getBlockState().getValue(CameraBlock.FACING);
		state.yaw = camera.viewYaw(time);
		state.pitch = camera.viewPitch();
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.head == null) {
			return;
		}
		Vec3 pivot = CameraBlockEntity.localPivot(state.facing);
		poseStack.pushPose();
		// Move the pivot to where it is on this camera, turn the head around it, then undo the pivot's
		// position in the model so the model's own pivot lands exactly on that point.
		poseStack.translate(pivot.x, pivot.y, pivot.z);
		poseStack.rotateDegrees(Axis.YP, 180.0F - state.yaw);
		poseStack.rotateDegrees(Axis.XP, -state.pitch);
		poseStack.translate(-AUTHORED_PIVOT.x, -AUTHORED_PIVOT.y, -AUTHORED_PIVOT.z);
		submitNodeCollector.submitMovingBlock(poseStack, state.head, 0);
		poseStack.popPose();
	}
}
