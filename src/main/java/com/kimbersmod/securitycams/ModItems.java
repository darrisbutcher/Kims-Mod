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

	public static final ResourceKey<Item> LITTLE_VILLAGER_FIGURINE_KEY = ResourceKey.create(Registries.ITEM, SecurityCams.id("little_villager_figurine"));
	public static final ResourceKey<Item> CHAMELEON_FIGURINE_KEY = ResourceKey.create(Registries.ITEM, SecurityCams.id("veiled_chameleon_figurine"));

	public static final Item LITTLE_VILLAGER_FIGURINE = Registry.register(
		BuiltInRegistries.ITEM,
		LITTLE_VILLAGER_FIGURINE_KEY,
		new FigurineItem<>(new Item.Properties().setId(LITTLE_VILLAGER_FIGURINE_KEY).stacksTo(16), () -> ModEntities.LITTLE_VILLAGER)
	);

	public static final Item CHAMELEON_FIGURINE = Registry.register(
		BuiltInRegistries.ITEM,
		CHAMELEON_FIGURINE_KEY,
		new FigurineItem<>(new Item.Properties().setId(CHAMELEON_FIGURINE_KEY).stacksTo(16), () -> ModEntities.VEILED_CHAMELEON)
	);

	private ModItems() {
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> output.accept(CAMERA));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(TABLET));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.accept(LITTLE_VILLAGER_FIGURINE);
			output.accept(CHAMELEON_FIGURINE);
		});
	}
}
