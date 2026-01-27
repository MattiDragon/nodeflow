package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GraphSyncPacket(CompoundTag nbt, byte syncId) implements CustomPacketPayload {
    public static final ResourceLocation GRAPH_SYNC_ID = NodeFlow.id("graph_sync");
    public static final Type<GraphSyncPacket> ID = new Type<>(GRAPH_SYNC_ID);
    public static final StreamCodec<FriendlyByteBuf, GraphSyncPacket> CODEC = 
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, GraphSyncPacket::nbt, ByteBufCodecs.BYTE, GraphSyncPacket::syncId, GraphSyncPacket::new);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            var player = context.player();
            if (player.containerMenu.containerId == payload.syncId && player.containerMenu instanceof EditorScreenHandler screenHandler) {
                screenHandler.graph.readNbt(payload.nbt, player.level().registryAccess());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
