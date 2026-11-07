package com.kimbersmod.securitycams.follower;

import com.kimbersmod.securitycams.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** An axolotl-sized chameleon that changes colour to match the block it is standing on. */
public class VeiledChameleon extends FollowerEntity {
	/**
	 * The map colours each camouflage look is meant to blend into, in the same order as the textures:
	 * grass, sand, stone, snow, dirt, nether.
	 */
	private static final int[] CAMOUFLAGE_COLORS = {
		MapColor.GRASS.col, MapColor.SAND.col, MapColor.STONE.col, MapColor.SNOW.col, MapColor.DIRT.col, MapColor.NETHER.col
	};
	public static final int CAMOUFLAGE_LOOKS = CAMOUFLAGE_COLORS.length;

	private static final EntityDataAccessor<Integer> DATA_CAMOUFLAGE = SynchedEntityData.defineId(VeiledChameleon.class, EntityDataSerializers.INT);

	public VeiledChameleon(EntityType<? extends VeiledChameleon> type, Level level) {
		super(type, level);
	}

	@Override
	protected Item figurine() {
		return ModItems.CHAMELEON_FIGURINE;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(DATA_CAMOUFLAGE, 0);
	}

	/** Which camouflage look the chameleon currently has (an index into the textures). */
	public int getCamouflage() {
		return entityData.get(DATA_CAMOUFLAGE);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("camouflage", getCamouflage());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(DATA_CAMOUFLAGE, Math.floorMod(input.getIntOr("camouflage", 0), CAMOUFLAGE_LOOKS));
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel serverLevel && tickCount % 10 == 0 && onGround()) {
			updateCamouflage(serverLevel);
		}
	}

	private void updateCamouflage(ServerLevel level) {
		BlockPos below = blockPosition().below();
		BlockState state = level.getBlockState(below);
		MapColor color = state.getMapColor(level, below);
		if (color == MapColor.NONE) {
			return;
		}
		int look = nearestLook(color.col);
		if (look != getCamouflage()) {
			entityData.set(DATA_CAMOUFLAGE, look);
		}
	}

	/** The camouflage look whose colour is closest to the given map colour. */
	public static int nearestLook(int rgb) {
		int best = 0;
		long bestDistance = Long.MAX_VALUE;
		for (int i = 0; i < CAMOUFLAGE_COLORS.length; i++) {
			int c = CAMOUFLAGE_COLORS[i];
			long dr = ((rgb >> 16) & 255) - ((c >> 16) & 255);
			long dg = ((rgb >> 8) & 255) - ((c >> 8) & 255);
			long db = (rgb & 255) - (c & 255);
			long distance = dr * dr + dg * dg + db * db;
			if (distance < bestDistance) {
				bestDistance = distance;
				best = i;
			}
		}
		return best;
	}
}
