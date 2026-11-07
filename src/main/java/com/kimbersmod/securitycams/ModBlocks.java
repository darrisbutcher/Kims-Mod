package com.kimbersmod.securitycams;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
	public static final ResourceKey<Block> CAMERA_KEY = ResourceKey.create(Registries.BLOCK, SecurityCams.id("camera"));

	public static final Block CAMERA = Registry.register(
		BuiltInRegistries.BLOCK,
		CAMERA_KEY,
		new CameraBlock(BlockBehaviour.Properties.of()
			.setId(CAMERA_KEY)
			.mapColor(MapColor.METAL)
			.strength(2.0F, 6.0F)
			.sound(SoundType.METAL)
			.noOcclusion()
			.requiresCorrectToolForDrops())
	);

	/**
	 * Never placed in the world and has no item. It only exists so the turning head of a camera has a
	 * block model that the block entity renderer can draw (see CameraBlockEntityRenderer).
	 */
	public static final ResourceKey<Block> CAMERA_HEAD_KEY = ResourceKey.create(Registries.BLOCK, SecurityCams.id("camera_head"));

	public static final Block CAMERA_HEAD = Registry.register(
		BuiltInRegistries.BLOCK,
		CAMERA_HEAD_KEY,
		new Block(BlockBehaviour.Properties.of().setId(CAMERA_HEAD_KEY).noOcclusion().noLootTable())
	);

	public static final BlockEntityType<CameraBlockEntity> CAMERA_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		SecurityCams.id("camera"),
		new BlockEntityType<>(CameraBlockEntity::new, Set.of(CAMERA))
	);

	private ModBlocks() {
	}

	public static void init() {
	}
}
