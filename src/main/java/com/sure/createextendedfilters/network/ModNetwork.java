package com.sure.createextendedfilters.network;

import com.sure.createextendedfilters.CreateExtendedFilters;
import com.sure.createextendedfilters.filter.FilterMenuExtension;
import com.simibubi.create.content.logistics.filter.FilterMenu;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
	public static final ResourceLocation TOGGLE_COMPONENT_ID = ResourceLocation.fromNamespaceAndPath(CreateExtendedFilters.MOD_ID, "toggle_component_exclusion");

	private ModNetwork() {}

	public static void register(IEventBus modBus) {
		modBus.addListener(ModNetwork::registerPayloads);
	}

	private static void registerPayloads(RegisterPayloadHandlersEvent event) {
		var registrar = event.registrar(CreateExtendedFilters.MOD_ID);
		registrar.playToServer(ToggleComponentExclusionPacket.TYPE, ToggleComponentExclusionPacket.STREAM_CODEC, ToggleComponentExclusionPacket::handle);
	}

	public record ToggleComponentExclusionPacket(String exclusionPath) implements CustomPacketPayload {
		public static final Type<ToggleComponentExclusionPacket> TYPE = new Type<>(TOGGLE_COMPONENT_ID);
		public static final StreamCodec<ByteBuf, ToggleComponentExclusionPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, ToggleComponentExclusionPacket::exclusionPath,
			ToggleComponentExclusionPacket::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}

		public static void handle(ToggleComponentExclusionPacket packet, IPayloadContext context) {
			context.enqueueWork(() -> {
				if (!(context.player() instanceof ServerPlayer player))
					return;
				if (!(player.containerMenu instanceof FilterMenu menu))
					return;
				FilterMenuExtension extension = (FilterMenuExtension) menu;
				extension.createExtendedFilters$toggleExcludedPath(packet.exclusionPath());
			});
		}
	}
}
