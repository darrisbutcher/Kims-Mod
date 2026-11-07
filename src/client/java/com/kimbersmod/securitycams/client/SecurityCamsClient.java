package com.kimbersmod.securitycams.client;

import com.kimbersmod.securitycams.ModBlocks;
import com.kimbersmod.securitycams.MotionAlertPayload;
import com.kimbersmod.securitycams.TabletItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;

public class SecurityCamsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlocks.CAMERA_ENTITY, context -> new CameraBlockEntityRenderer());

		TabletItem.openScreen =(stack, hand) -> Minecraft.getInstance().setScreenAndShow(new TabletScreen(hand));

		ClientPlayNetworking.registerGlobalReceiver(MotionAlertPayload.TYPE, (payload, context) -> {
			Component message = payload.message();
			if (context.client().gui.screen() instanceof TabletScreen tablet) {
				// The HUD is hidden while watching a feed, so show the alert on the feed itself.
				tablet.showAlert(message);
			} else {
				context.client().gui.hud.setOverlayMessage(message, false);
			}
		});
	}
}
