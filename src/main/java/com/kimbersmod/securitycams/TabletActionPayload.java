package com.kimbersmod.securitycams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Something the player did on the tablet screen that the server has to carry out, because the
 * tablet item and the cameras only exist for real on the server.
 *
 * @param action  one of the ACTION_* constants
 * @param index   which of the tablet's linked cameras this is about
 * @param value   action argument (sweep on/off, or aim direction -1 / +1)
 * @param text    new name, for ACTION_RENAME
 * @param offHand whether the tablet is in the off hand
 */
public record TabletActionPayload(int action, int index, int value, String text, boolean offHand) implements CustomPacketPayload {
	public static final int ACTION_REMOVE = 0;
	public static final int ACTION_RENAME = 1;
	public static final int ACTION_SWEEP = 2;
	public static final int ACTION_AIM = 3;

	public static final CustomPacketPayload.Type<TabletActionPayload> TYPE = new CustomPacketPayload.Type<>(SecurityCams.id("tablet_action"));

	public static final StreamCodec<RegistryFriendlyByteBuf, TabletActionPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, TabletActionPayload::action,
		ByteBufCodecs.VAR_INT, TabletActionPayload::index,
		ByteBufCodecs.VAR_INT, TabletActionPayload::value,
		ByteBufCodecs.stringUtf8(LinkedCamera.MAX_NAME_LENGTH * 4), TabletActionPayload::text,
		ByteBufCodecs.BOOL, TabletActionPayload::offHand,
		TabletActionPayload::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
