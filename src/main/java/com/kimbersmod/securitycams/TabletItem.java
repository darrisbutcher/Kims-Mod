package com.kimbersmod.securitycams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class TabletItem extends Item {
	public static final int MAX_CAMERAS = 16;

	/** Set by the client entrypoint. Opens the tablet screen; does nothing on a dedicated server. */
	public static volatile BiConsumer<ItemStack, InteractionHand> openScreen = (stack, hand) -> {
	};

	public TabletItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			openScreen.accept(player.getItemInHand(hand), hand);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(
		ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag
	) {
		int count = stack.getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of()).size();
		builder.accept(Component.translatable("securitycams.tablet.tooltip", count, MAX_CAMERAS));
	}

	/** Links the camera at pos to this tablet (server side). */
	public static void link(Level level, BlockPos pos, ItemStack stack, Player player) {
		GlobalPos target = GlobalPos.of(level.dimension(), pos);
		List<LinkedCamera> cameras = new ArrayList<>(stack.getOrDefault(ModComponents.LINKED_CAMERAS, List.<LinkedCamera>of()));
		for (LinkedCamera existing : cameras) {
			if (existing.pos().equals(target)) {
				player.sendOverlayMessage(Component.translatable("securitycams.tablet.already_linked"));
				return;
			}
		}
		if (cameras.size() >= MAX_CAMERAS) {
			player.sendOverlayMessage(Component.translatable("securitycams.tablet.full"));
			return;
		}
		String name = nextFreeName(cameras);
		cameras.add(new LinkedCamera(target, name));
		stack.set(ModComponents.LINKED_CAMERAS, List.copyOf(cameras));
		player.sendOverlayMessage(Component.translatable("securitycams.tablet.linked", name));
	}

	private static String nextFreeName(List<LinkedCamera> cameras) {
		for (int n = 1; ; n++) {
			String candidate = "Camera " + n;
			boolean taken = false;
			for (LinkedCamera camera : cameras) {
				if (camera.name().equals(candidate)) {
					taken = true;
					break;
				}
			}
			if (!taken) {
				return candidate;
			}
		}
	}

	/** Trims, drops control characters and caps the length of a player-typed camera name. */
	public static String sanitizeName(String raw) {
		StringBuilder cleaned = new StringBuilder();
		for (char c : raw.toCharArray()) {
			if (c >= 32 && c != 127 && c != '§') {
				cleaned.append(c);
			}
		}
		String trimmed = cleaned.toString().strip();
		return trimmed.length() > LinkedCamera.MAX_NAME_LENGTH ? trimmed.substring(0, LinkedCamera.MAX_NAME_LENGTH) : trimmed;
	}
}
