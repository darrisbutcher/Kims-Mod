package com.kimbersmod.securitycams;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Tells a player that one of their linked cameras saw something.
 *
 * @param camera the nickname the player's tablet gives that camera
 * @param target what was seen (sent as a component so mob names translate on the client)
 */
public record MotionAlertPayload(String camera, Component target) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MotionAlertPayload> TYPE = new CustomPacketPayload.Type<>(SecurityCams.id("motion_alert"));

	public static final StreamCodec<RegistryFriendlyByteBuf, MotionAlertPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.stringUtf8(LinkedCamera.MAX_NAME_LENGTH * 4), MotionAlertPayload::camera,
		ComponentSerialization.STREAM_CODEC, MotionAlertPayload::target,
		MotionAlertPayload::new
	);

	public Component message() {
		return Component.translatable("securitycams.alert.motion", camera, target);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
