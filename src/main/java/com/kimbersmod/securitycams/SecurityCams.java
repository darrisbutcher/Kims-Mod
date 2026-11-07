package com.kimbersmod.securitycams;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityCams implements ModInitializer {
	public static final String MOD_ID = "securitycams";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModComponents.init();
		ModBlocks.init();
		ModEntities.init();
		ModItems.init();
		ModNetworking.init();
		LOGGER.info("Security Cameras loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
