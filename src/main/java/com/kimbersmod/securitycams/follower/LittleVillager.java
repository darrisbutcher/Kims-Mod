package com.kimbersmod.securitycams.follower;

import com.kimbersmod.securitycams.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** A half-size villager in a junior security guard cap. */
public class LittleVillager extends FollowerEntity {
	public LittleVillager(EntityType<? extends LittleVillager> type, Level level) {
		super(type, level);
	}

	@Override
	protected Item figurine() {
		return ModItems.LITTLE_VILLAGER_FIGURINE;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.VILLAGER_AMBIENT;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 400;
	}
}
