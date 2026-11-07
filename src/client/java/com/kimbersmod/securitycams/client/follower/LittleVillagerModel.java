package com.kimbersmod.securitycams.client.follower;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A villager-shaped body in a robe with folded arms, holding a tablet, and a big head with a security cap.
 * Authored at full size; the renderer shrinks it to baby-villager size.
 */
public class LittleVillagerModel extends EntityModel<FollowerRenderState> {
	private final ModelPart all;
	private final ModelPart head;

	public LittleVillagerModel(ModelPart root) {
		super(root);
		this.all = root.getChild("all");
		this.head = all.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -14.0F, -3.0F, 8.0F, 14.0F, 6.0F), PartPose.ZERO);
		all.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(0, 20).addBox(-4.5F, -12.0F, -6.0F, 9.0F, 4.0F, 3.0F), PartPose.ZERO);
		all.addOrReplaceChild("tablet", CubeListBuilder.create().texOffs(24, 20).addBox(-2.5F, -12.0F, -7.0F, 5.0F, 4.0F, 1.0F), PartPose.ZERO);

		PartDefinition head = all.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(28, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F), PartPose.offset(0.0F, -14.0F, 0.0F)
		);
		head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(68, 0).addBox(-1.0F, -6.0F, -8.0F, 2.0F, 5.0F, 3.0F), PartPose.ZERO);
		head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 40).addBox(-5.5F, -12.0F, -5.5F, 11.0F, 2.0F, 11.0F), PartPose.ZERO);
		head.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(44, 40).addBox(-5.5F, -11.0F, -8.5F, 11.0F, 1.0F, 3.0F), PartPose.ZERO);

		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(FollowerRenderState state) {
		super.setupAnim(state);
		head.xRot = state.xRot * Mth.DEG_TO_RAD;
		head.yRot = state.yRot * Mth.DEG_TO_RAD;
		if (state.sitting) {
			// Sink into the ground a little, like sitting down in the robe.
			all.y += 3.0F;
		} else {
			float stride = state.walkAnimationPos * 0.6662F;
			all.y -= Math.abs(Mth.sin(stride)) * state.walkAnimationSpeed * 1.5F;
			all.zRot = Mth.cos(stride) * 0.08F * state.walkAnimationSpeed;
		}
	}
}
