package com.kimbersmod.securitycams.follower;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A small companion that follows its owner around. Shared by the Little Villager and the veiled chameleon.
 *
 * <p>Right-click your follower to make it stay put (or follow you again). Sneak and right-click to pack it
 * back into its figurine. Followers can't be hurt, and they teleport to you if you get too far away.
 */
public abstract class FollowerEntity extends TamableAnimal {
	protected FollowerEntity(EntityType<? extends FollowerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes(double movementSpeed) {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.MOVEMENT_SPEED, movementSpeed);
	}

	/** The figurine item this follower turns back into when packed away. */
	protected abstract Item figurine();

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.1, 4.0F, 2.0F));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean isInvulnerable() {
		return true;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceSquared) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!isOwnedBy(player)) {
			return super.mobInteract(player, hand);
		}
		if (level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (player.isSecondaryUseActive()) {
			packAway(player);
		} else {
			boolean stay = !isOrderedToSit();
			setOrderedToSit(stay);
			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.sendOverlayMessage(Component.translatable(stay ? "securitycams.follower.stay" : "securitycams.follower.follow", getName()));
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** Turns the follower back into its figurine and hands it to the player. */
	protected void packAway(Player player) {
		ItemStack figurine = new ItemStack(figurine());
		if (level() instanceof ServerLevel serverLevel) {
			if (!player.getInventory().add(figurine)) {
				spawnAtLocation(serverLevel, figurine);
			}
			serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY() + getBbHeight() / 2.0, getZ(), 8, 0.2, 0.2, 0.2, 0.02);
			serverLevel.playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.6F, 1.2F);
		}
		discard();
	}
}
