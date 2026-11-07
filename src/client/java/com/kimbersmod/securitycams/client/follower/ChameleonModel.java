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
 * A veiled chameleon: a flat-sided body with a dorsal crest, a head with a tall casque, eyes that look
 * around on their own, four gripping legs and a curled tail. Authored at full size; the renderer shrinks it.
 */
public class ChameleonModel extends EntityModel<FollowerRenderState> {
	private final ModelPart all;
	private final ModelPart head;
	private final ModelPart eyeRight;
	private final ModelPart eyeLeft;
	private final ModelPart tail;
	private final ModelPart legFrontRight;
	private final ModelPart legFrontLeft;
	private final ModelPart legBackRight;
	private final ModelPart legBackLeft;

	public ChameleonModel(ModelPart root) {
		super(root);
		this.all = root.getChild("all");
		this.head = all.getChild("head");
		this.eyeRight = head.getChild("eye_right");
		this.eyeLeft = head.getChild("eye_left");
		this.tail = all.getChild("tail");
		this.legFrontRight = all.getChild("leg_front_right");
		this.legFrontLeft = all.getChild("leg_front_left");
		this.legBackRight = all.getChild("leg_back_right");
		this.legBackLeft = all.getChild("leg_back_left");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -7.5F, -3.5F, 3.0F, 5.0F, 8.0F), PartPose.ZERO);
		all.addOrReplaceChild(
			"crest",
			CubeListBuilder.create()
				.texOffs(40, 27).addBox(-0.5F, -8.5F, -2.5F, 1.0F, 1.0F, 1.0F)
				.texOffs(40, 27).addBox(-0.5F, -8.5F, -0.5F, 1.0F, 1.0F, 1.0F)
				.texOffs(40, 27).addBox(-0.5F, -8.5F, 1.5F, 1.0F, 1.0F, 1.0F)
				.texOffs(40, 27).addBox(-0.5F, -8.5F, 3.5F, 1.0F, 1.0F, 1.0F),
			PartPose.ZERO
		);

		PartDefinition head = all.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -2.0F, -4.5F, 4.0F, 4.0F, 5.0F), PartPose.offset(0.0F, -6.0F, -3.5F)
		);
		head.addOrReplaceChild(
			"casque",
			CubeListBuilder.create()
				.texOffs(18, 13).addBox(-0.5F, -4.0F, -3.5F, 1.0F, 2.0F, 4.0F)
				.texOffs(28, 13).addBox(-0.5F, -6.0F, -1.5F, 1.0F, 2.0F, 2.0F),
			PartPose.ZERO
		);
		CubeListBuilder eye = CubeListBuilder.create().texOffs(34, 13).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F);
		head.addOrReplaceChild("eye_right", eye, PartPose.offset(-2.5F, -1.0F, -2.5F));
		head.addOrReplaceChild("eye_left", eye, PartPose.offset(2.5F, -1.0F, -2.5F));

		CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F).texOffs(6, 22).addBox(-1.0F, 3.0F, -1.5F, 2.0F, 1.0F, 3.0F);
		all.addOrReplaceChild("leg_front_right", leg, PartPose.offset(-2.0F, -4.0F, -2.0F));
		all.addOrReplaceChild("leg_front_left", leg, PartPose.offset(2.0F, -4.0F, -2.0F));
		all.addOrReplaceChild("leg_back_right", leg, PartPose.offset(-2.0F, -4.0F, 3.0F));
		all.addOrReplaceChild("leg_back_left", leg, PartPose.offset(2.0F, -4.0F, 3.0F));

		all.addOrReplaceChild(
			"tail",
			CubeListBuilder.create()
				.texOffs(0, 27).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F)
				.texOffs(12, 27).addBox(-1.0F, -3.0F, 4.0F, 2.0F, 4.0F, 2.0F)
				.texOffs(20, 27).addBox(-1.0F, -5.0F, 2.0F, 2.0F, 2.0F, 4.0F)
				.texOffs(32, 27).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 3.0F, 2.0F),
			PartPose.offset(0.0F, -4.0F, 4.5F)
		);

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(FollowerRenderState state) {
		super.setupAnim(state);
		head.xRot = state.xRot * Mth.DEG_TO_RAD;
		head.yRot = state.yRot * Mth.DEG_TO_RAD;
		// A chameleon's eyes move independently of each other.
		eyeRight.yRot = Mth.sin(state.ageInTicks * 0.07F) * 0.6F;
		eyeLeft.yRot = Mth.sin(state.ageInTicks * 0.05F + 1.5F) * 0.6F;

		float speed = state.walkAnimationSpeed;
		float stride = state.walkAnimationPos * 0.9F;
		legFrontRight.xRot = Mth.cos(stride) * speed;
		legBackLeft.xRot = Mth.cos(stride) * speed;
		legFrontLeft.xRot = Mth.cos(stride + (float) Math.PI) * speed;
		legBackRight.xRot = Mth.cos(stride + (float) Math.PI) * speed;
		tail.yRot = Mth.sin(state.ageInTicks * 0.08F) * 0.2F + Mth.cos(state.walkAnimationPos * 0.6F) * 0.25F * speed;
		if (state.sitting) {
			all.y += 1.0F;
		}
	}
}
