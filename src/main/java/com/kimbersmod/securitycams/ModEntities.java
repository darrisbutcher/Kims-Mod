package com.kimbersmod.securitycams;

import com.kimbersmod.securitycams.follower.FollowerEntity;
import com.kimbersmod.securitycams.follower.LittleVillager;
import com.kimbersmod.securitycams.follower.VeiledChameleon;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final ResourceKey<EntityType<?>> LITTLE_VILLAGER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, SecurityCams.id("little_villager"));
	public static final ResourceKey<EntityType<?>> VEILED_CHAMELEON_KEY = ResourceKey.create(Registries.ENTITY_TYPE, SecurityCams.id("veiled_chameleon"));

	/** Half the size of a villager, like a baby villager. */
	public static final EntityType<LittleVillager> LITTLE_VILLAGER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		LITTLE_VILLAGER_KEY,
		EntityType.Builder.of(LittleVillager::new, MobCategory.CREATURE)
			.sized(0.49F, 0.975F)
			.eyeHeight(0.8F)
			.clientTrackingRange(10)
			.build(LITTLE_VILLAGER_KEY)
	);

	/** About the size of an axolotl. */
	public static final EntityType<VeiledChameleon> VEILED_CHAMELEON = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		VEILED_CHAMELEON_KEY,
		EntityType.Builder.of(VeiledChameleon::new, MobCategory.CREATURE)
			.sized(0.7F, 0.45F)
			.eyeHeight(0.35F)
			.clientTrackingRange(10)
			.build(VEILED_CHAMELEON_KEY)
	);

	private ModEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(LITTLE_VILLAGER, FollowerEntity.createAttributes(0.3));
		FabricDefaultAttributeRegistry.register(VEILED_CHAMELEON, FollowerEntity.createAttributes(0.28));
	}
}
