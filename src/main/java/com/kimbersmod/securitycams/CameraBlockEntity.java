package com.kimbersmod.securitycams;

import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Holds a camera's aim and runs its motion detector.
 *
 * <p>The view direction is a pure function of the block state, the saved aim, and the game time, so
 * the server and every client agree on where the camera is looking without any per-tick syncing.
 */
public class CameraBlockEntity extends BlockEntity {
	/** How far (degrees) a sweeping camera swings to each side of its aim. */
	public static final float SWEEP_AMPLITUDE = 40.0F;
	/** Ticks for one full left-right-left sweep. */
	public static final float SWEEP_PERIOD_TICKS = 240.0F;
	/** How far the owner can manually turn a camera away from where it was placed. */
	public static final float MAX_AIM = 60.0F;
	public static final float AIM_STEP = 15.0F;

	public static final double DETECT_RANGE = 12.0;
	private static final double DETECT_HALF_ANGLE_COS = Math.cos(Math.toRadians(45.0));
	private static final int DETECT_INTERVAL_TICKS = 10;
	private static final int ALERT_COOLDOWN_TICKS = 100;

	private float baseYaw;
	private float aimOffset;
	private boolean sweeping;
	private int cooldown;
	private int tickCounter;

	public CameraBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.CAMERA_ENTITY, pos, state);
	}

	// ---- aim -----------------------------------------------------------------------------------

	public static float basePitch(Direction facing) {
		return switch (facing) {
			case DOWN -> 50.0F;
			case UP -> -50.0F;
			default -> 15.0F;
		};
	}

	/** How far behind the middle of the block the head's pivot sits, toward the mounting surface. */
	private static final double PIVOT_DEPTH = 3.0 / 16.0;
	/** Wall-mounted heads sit half a pixel above the block's middle (matches the head model). */
	private static final double PIVOT_LIFT = 0.5 / 16.0;
	/** Distance from the pivot to just in front of the lens. */
	private static final double LENS_DISTANCE = 0.6;

	/** The point the head turns around, in block-local coordinates (0 to 1 on each axis). */
	public static Vec3 localPivot(Direction facing) {
		double lift = facing.getAxis().isHorizontal() ? PIVOT_LIFT : 0.0;
		return new Vec3(
			0.5 - facing.getStepX() * PIVOT_DEPTH,
			0.5 + lift - facing.getStepY() * PIVOT_DEPTH,
			0.5 - facing.getStepZ() * PIVOT_DEPTH
		);
	}

	/** Unit vector for a view yaw/pitch in degrees (Minecraft convention: yaw 0 looks south, positive pitch looks down). */
	public static Vec3 lookVector(float yaw, float pitch) {
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float pitchRad = pitch * Mth.DEG_TO_RAD;
		return new Vec3(-Mth.sin(yawRad) * Mth.cos(pitchRad), -Mth.sin(pitchRad), Mth.cos(yawRad) * Mth.cos(pitchRad));
	}

	/** Where the camera sees from: just in front of the lens, wherever the head is currently pointing. */
	public static Vec3 eyePosition(BlockPos pos, Direction facing, float yaw, float pitch) {
		return Vec3.atLowerCornerOf(pos).add(localPivot(facing)).add(lookVector(yaw, pitch).scale(LENS_DISTANCE));
	}

	public Vec3 eyePosition(float time) {
		return eyePosition(worldPosition, getBlockState().getValue(CameraBlock.FACING), viewYaw(time), viewPitch());
	}

	/** The view yaw in degrees at the given game time (use gameTime + partialTick on the client). */
	public float viewYaw(float time) {
		float yaw = baseYaw + aimOffset;
		if (sweeping) {
			yaw += SWEEP_AMPLITUDE * Mth.sin(time * (float) (Math.PI * 2.0) / SWEEP_PERIOD_TICKS);
		}
		return yaw;
	}

	public float viewPitch() {
		return basePitch(getBlockState().getValue(CameraBlock.FACING));
	}

	public boolean isSweeping() {
		return sweeping;
	}

	public float getAimOffset() {
		return aimOffset;
	}

	public float getBaseYaw() {
		return baseYaw;
	}

	public void setBaseYaw(float baseYaw) {
		this.baseYaw = baseYaw;
		syncToClients();
	}

	public void setSweeping(boolean sweeping) {
		this.sweeping = sweeping;
		syncToClients();
	}

	public void nudgeAim(float degrees) {
		this.aimOffset = Mth.clamp(this.aimOffset + degrees, -MAX_AIM, MAX_AIM);
		syncToClients();
	}

	private void syncToClients() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
		}
	}

	// ---- saving / syncing ---------------------------------------------------------------------

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putFloat("base_yaw", baseYaw);
		output.putFloat("aim_offset", aimOffset);
		output.putBoolean("sweeping", sweeping);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		baseYaw = input.getFloatOr("base_yaw", 0.0F);
		aimOffset = input.getFloatOr("aim_offset", 0.0F);
		sweeping = input.getBooleanOr("sweeping", false);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	// ---- motion detection (server only) -------------------------------------------------------

	public static void serverTick(Level level, BlockPos pos, BlockState state, CameraBlockEntity camera) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (++camera.tickCounter < DETECT_INTERVAL_TICKS) {
			return;
		}
		camera.tickCounter = 0;
		if (camera.cooldown > 0) {
			camera.cooldown = Math.max(0, camera.cooldown - DETECT_INTERVAL_TICKS);
			return;
		}
		camera.detectMotion(serverLevel, pos, state);
	}

	private void detectMotion(ServerLevel level, BlockPos pos, BlockState state) {
		Direction facing = state.getValue(CameraBlock.FACING);
		float yaw = viewYaw(level.getGameTime());
		float pitch = viewPitch();
		Vec3 eye = eyePosition(pos, facing, yaw, pitch);
		Vec3 look = lookVector(yaw, pitch);

		AABB area = AABB.ofSize(eye.add(look.scale(DETECT_RANGE / 2.0)), DETECT_RANGE, DETECT_RANGE, DETECT_RANGE);
		List<LivingEntity> seen = level.getEntitiesOfClass(LivingEntity.class, area, entity ->
			entity.isAlive()
				&& (entity instanceof Monster || (entity instanceof Player player && !player.isSpectator()))
				&& canSee(level, eye, look, entity));
		if (seen.isEmpty()) {
			return;
		}

		GlobalPos here = GlobalPos.of(level.dimension(), pos);
		boolean alerted = false;
		for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
			String name = linkedName(player, here);
			if (name == null) {
				continue;
			}
			// Don't alert someone about only themselves.
			LivingEntity culprit = null;
			for (LivingEntity entity : seen) {
				if (entity != player) {
					culprit = entity;
					break;
				}
			}
			if (culprit == null) {
				continue;
			}
			ServerPlayNetworking.send(player, new MotionAlertPayload(name, culprit.getName()));
			player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
			alerted = true;
		}
		if (alerted) {
			cooldown = ALERT_COOLDOWN_TICKS;
		}
	}

	private static boolean canSee(ServerLevel level, Vec3 eye, Vec3 look, LivingEntity entity) {
		Vec3 target = entity.getEyePosition();
		Vec3 toTarget = target.subtract(eye);
		double distance = toTarget.length();
		if (distance < 0.5 || distance > DETECT_RANGE) {
			return false;
		}
		if (toTarget.scale(1.0 / distance).dot(look) < DETECT_HALF_ANGLE_COS) {
			return false;
		}
		HitResult hit = level.clip(new ClipContext(eye, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
		return hit.getType() == HitResult.Type.MISS;
	}

	/** The nickname this player's tablet gives the camera, or null if they have no tablet linked to it. */
	private static String linkedName(ServerPlayer player, GlobalPos camera) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.is(ModItems.TABLET)) {
				continue;
			}
			for (LinkedCamera linked : stack.getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of())) {
				if (linked.pos().equals(camera)) {
					return linked.name();
				}
			}
		}
		return null;
	}
}
