package com.kimbersmod.securitycams.client;

import com.kimbersmod.securitycams.ModBlocks;
import com.kimbersmod.securitycams.ModEntities;
import com.kimbersmod.securitycams.MotionAlertPayload;
import com.kimbersmod.securitycams.TabletItem;
import com.kimbersmod.securitycams.client.follower.ChameleonModel;
import com.kimbersmod.securitycams.client.follower.ChameleonRenderer;
import com.kimbersmod.securitycams.client.follower.LittleVillagerModel;
import com.kimbersmod.securitycams.client.follower.LittleVillagerRenderer;
import com.kimbersmod.securitycams.client.follower.ModModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;

public class SecurityCamsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlocks.CAMERA_ENTITY, context -> new CameraBlockEntityRenderer());

		ModelLayerRegistry.registerModelLayer(ModModelLayers.LITTLE_VILLAGER, LittleVillagerModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(ModModelLayers.VEILED_CHAMELEON, ChameleonModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.LITTLE_VILLAGER, LittleVillagerRenderer::new);
		EntityRendererRegistry.register(ModEntities.VEILED_CHAMELEON, ChameleonRenderer::new);

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
