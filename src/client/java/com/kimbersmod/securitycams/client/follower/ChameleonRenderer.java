package com.kimbersmod.securitycams.client.follower;

import com.kimbersmod.securitycams.SecurityCams;
import com.kimbersmod.securitycams.follower.VeiledChameleon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ChameleonRenderer extends MobRenderer<VeiledChameleon, FollowerRenderState, ChameleonModel> {
	/** In the same order as the camouflage colours: grass, sand, stone, snow, dirt, nether. */
	private static final String[] LOOKS = {"grass", "sand", "stone", "snow", "dirt", "nether"};
	private static final Identifier[] TEXTURES = new Identifier[LOOKS.length];

	static {
		for (int i = 0; i < LOOKS.length; i++) {
			TEXTURES[i] = SecurityCams.id("textures/entity/veiled_chameleon_" + LOOKS[i] + ".png");
		}
	}

	/** The model is authored larger than an axolotl; this brings it to axolotl size. */
	private static final float SIZE = 0.65F;

	public ChameleonRenderer(EntityRendererProvider.Context context) {
		super(context, new ChameleonModel(context.bakeLayer(ModModelLayers.VEILED_CHAMELEON)), 0.2F);
	}

	@Override
	public Identifier getTextureLocation(FollowerRenderState state) {
		return TEXTURES[Mth.clamp(state.camouflage, 0, TEXTURES.length - 1)];
	}

	@Override
	public FollowerRenderState createRenderState() {
		return new FollowerRenderState();
	}

	@Override
	public void extractRenderState(VeiledChameleon entity, FollowerRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.sitting = entity.isInSittingPose();
		state.camouflage = entity.getCamouflage();
	}

	@Override
	protected void scale(FollowerRenderState state, PoseStack poseStack) {
		poseStack.scale(SIZE, SIZE, SIZE);
	}
}
