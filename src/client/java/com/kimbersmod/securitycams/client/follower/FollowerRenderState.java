package com.kimbersmod.securitycams.client.follower;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** What the follower models need to know to pose themselves. */
public class FollowerRenderState extends LivingEntityRenderState {
	public boolean sitting;
	/** Which camouflage texture the chameleon wears. Unused by the villager. */
	public int camouflage;
}
