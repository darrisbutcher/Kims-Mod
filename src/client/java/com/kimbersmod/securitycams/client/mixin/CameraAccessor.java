package com.kimbersmod.securitycams.client.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The game camera eases its eye height toward the camera entity's eye height over a few ticks. When we
 * switch to a security camera (eye height 0) and back (eye height ~1.6) that easing would show up as the
 * view sliding up or down, so the tablet screen sets the value directly instead.
 */
@Mixin(Camera.class)
public interface CameraAccessor {
	@Accessor("eyeHeight")
	void securitycams$setEyeHeight(float eyeHeight);

	@Accessor("eyeHeightOld")
	void securitycams$setEyeHeightOld(float eyeHeightOld);
}
