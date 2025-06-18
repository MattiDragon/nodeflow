package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GraphSyncPacket(NbtCompound nbt, byte syncId) implements CustomPayload {
    public static final Identifier GRAPH_SYNC_ID = NodeFlow.id("graph_sync");
    public static final Id<GraphSyncPacket> ID = new Id<>(GRAPH_SYNC_ID);
    public static final PacketCodec<PacketByteBuf, GraphSyncPacket> CODEC = 
            PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, GraphSyncPacket::nbt, PacketCodecs.BYTE, GraphSyncPacket::syncId, GraphSyncPacket::new);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            var player = context.player();
            if (player.currentScreenHandler.syncId == payload.syncId && player.currentScreenHandler instanceof EditorScreenHandler screenHandler) {
                screenHandler.graph.readNbt(payload.nbt, player.getWorld().getRegistryManager());
            }
        });
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
