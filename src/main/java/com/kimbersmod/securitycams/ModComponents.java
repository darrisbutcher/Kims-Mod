package com.kimbersmod.securitycams;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModComponents {
	/** The cameras a tablet has been linked to. Stored on the tablet item itself. */
	public static final DataComponentType<List<LinkedCamera>> LINKED_CAMERAS = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		SecurityCams.id("linked_cameras"),
		DataComponentType.<List<LinkedCamera>>builder()
			.persistent(LinkedCamera.CODEC.listOf())
			.networkSynchronized(LinkedCamera.STREAM_CODEC.apply(ByteBufCodecs.list()))
			.build()
	);

	private ModComponents() {
	}

	public static void init() {
	}
}
