package com.kimbersmod.securitycams.client.follower;

import com.kimbersmod.securitycams.SecurityCams;
import com.kimbersmod.securitycams.follower.LittleVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class LittleVillagerRenderer extends MobRenderer<LittleVillager, FollowerRenderState, LittleVillagerModel> {
	private static final Identifier TEXTURE = SecurityCams.id("textures/entity/little_villager.png");
	/** The model is authored at adult-villager size; this brings it down to a baby villager's height. */
	private static final float SIZE = 0.59F;

	public LittleVillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new LittleVillagerModel(context.bakeLayer(ModModelLayers.LITTLE_VILLAGER)), 0.25F);
	}

	@Override
	public Identifier getTextureLocation(FollowerRenderState state) {
		return TEXTURE;
	}

	@Override
	public FollowerRenderState createRenderState() {
		return new FollowerRenderState();
	}

	@Override
	public void extractRenderState(LittleVillager entity, FollowerRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.sitting = entity.isInSittingPose();
	}

	@Override
	protected void scale(FollowerRenderState state, PoseStack poseStack) {
		poseStack.scale(SIZE, SIZE, SIZE);
	}
}
