package com.kimbersmod.securitycams;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ModNetworking {
	/** How far from a camera a player may still control it, as a sanity limit for the server. */
	private static final double MAX_CONTROL_DISTANCE = 256.0;

	private ModNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(TabletActionPayload.TYPE, TabletActionPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MotionAlertPayload.TYPE, MotionAlertPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(TabletActionPayload.TYPE, (payload, context) -> handle(context.player(), payload));
	}

	private static void handle(ServerPlayer player, TabletActionPayload payload) {
		ItemStack stack = player.getItemInHand(payload.offHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		if (!stack.is(ModItems.TABLET)) {
			return;
		}
		List<LinkedCamera> cameras = new ArrayList<>(stack.getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of()));
		int index = payload.index();
		if (index < 0 || index >= cameras.size()) {
			return;
		}
		LinkedCamera camera = cameras.get(index);

		switch (payload.action()) {
			case TabletActionPayload.ACTION_REMOVE -> {
				cameras.remove(index);
				stack.set(ModComponents.LINKED_CAMERAS, List.copyOf(cameras));
			}
			case TabletActionPayload.ACTION_RENAME -> {
				String name = TabletItem.sanitizeName(payload.text());
				if (!name.isEmpty()) {
					cameras.set(index, camera.withName(name));
					stack.set(ModComponents.LINKED_CAMERAS, List.copyOf(cameras));
				}
			}
			case TabletActionPayload.ACTION_SWEEP -> withCamera(player, camera, be -> be.setSweeping(payload.value() != 0));
			case TabletActionPayload.ACTION_AIM -> withCamera(player, camera, be -> be.nudgeAim(Integer.signum(payload.value()) * CameraBlockEntity.AIM_STEP));
			default -> {
			}
		}
	}

	private static void withCamera(ServerPlayer player, LinkedCamera camera, java.util.function.Consumer<CameraBlockEntity> action) {
		if (!(player.level() instanceof ServerLevel level) || !camera.pos().dimension().equals(level.dimension())) {
			return;
		}
		BlockPos pos = camera.pos().pos();
		if (!level.hasChunkAt(pos) || player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_CONTROL_DISTANCE * MAX_CONTROL_DISTANCE) {
			return;
		}
		if (level.getBlockEntity(pos) instanceof CameraBlockEntity be) {
			action.accept(be);
		}
	}
}
