package com.kimbersmod.securitycams;

import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
	public static final ResourceKey<Item> CAMERA_KEY = ResourceKey.create(Registries.ITEM, SecurityCams.id("camera"));
	public static final ResourceKey<Item> TABLET_KEY = ResourceKey.create(Registries.ITEM, SecurityCams.id("tablet"));

	public static final Item CAMERA = Registry.register(
		BuiltInRegistries.ITEM,
		CAMERA_KEY,
		new BlockItem(ModBlocks.CAMERA, new Item.Properties().setId(CAMERA_KEY).useBlockDescriptionPrefix())
	);

	public static final Item TABLET = Registry.register(
		BuiltInRegistries.ITEM,
		TABLET_KEY,
		new TabletItem(new Item.Properties()
			.setId(TABLET_KEY)
			.stacksTo(1)
			.component(ModComponents.LINKED_CAMERAS, List.of()))
	);

	private ModItems() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> output.accept(CAMERA));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(TABLET));
	}
}
