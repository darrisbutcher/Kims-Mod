package com.kimbersmod.securitycams;

import com.kimbersmod.securitycams.follower.FollowerEntity;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** A figurine that summons its follower where you place it. Sneak and right-click the follower to get the figurine back. */
public class FigurineItem<T extends FollowerEntity> extends Item {
	private final Supplier<EntityType<T>> followerType;

	public FigurineItem(Item.Properties properties, Supplier<EntityType<T>> followerType) {
		super(properties);
		this.followerType = followerType;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		Player player = context.getPlayer();
		BlockPos clicked = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockState state = level.getBlockState(clicked);
		BlockPos spawnPos = state.getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(face);

		T follower = followerType.get().spawn(serverLevel, entity -> {
			entity.setTame(true, false);
			if (player != null) {
				entity.setOwner(player);
				entity.setYRot(player.getYRot() + 180.0F);
				entity.yRotO = entity.getYRot();
				entity.setYHeadRot(entity.getYRot());
			}
			entity.setPersistenceRequired();
		}, spawnPos, EntitySpawnReason.SPAWN_ITEM_USE, true, !clicked.equals(spawnPos) && face == Direction.UP);
		if (follower == null) {
			return InteractionResult.FAIL;
		}

		serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, follower.getX(), follower.getY() + 0.4, follower.getZ(), 6, 0.2, 0.2, 0.2, 0.0);
		serverLevel.playSound(null, spawnPos, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.6F, 0.8F);
		if (player == null || !player.hasInfiniteMaterials()) {
			context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
