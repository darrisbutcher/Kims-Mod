package com.kimbersmod.securitycams;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** One camera that a tablet is linked to: where it is, and the nickname this tablet gives it. */
public record LinkedCamera(GlobalPos pos, String name) {
	public static final int MAX_NAME_LENGTH = 24;

	public static final Codec<LinkedCamera> CODEC = RecordCodecBuilder.create(i -> i.group(
		GlobalPos.CODEC.fieldOf("pos").forGetter(LinkedCamera::pos),
		Codec.STRING.fieldOf("name").forGetter(LinkedCamera::name)
	).apply(i, LinkedCamera::new));

	public static final StreamCodec<ByteBuf, LinkedCamera> STREAM_CODEC = StreamCodec.composite(
		GlobalPos.STREAM_CODEC, LinkedCamera::pos,
		ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH * 4), LinkedCamera::name,
		LinkedCamera::new
	);

	public LinkedCamera withName(String newName) {
		return new LinkedCamera(pos, newName);
	}
}
