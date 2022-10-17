package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class GraphSyncPacket {
    private static final Identifier GRAPH_SYNC_ID = NodeFlow.id("graph_sync");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(GRAPH_SYNC_ID, ((server, player, handler, buf, responseSender) -> {
            var nbt = buf.readNbt();
            var syncId = buf.readByte();

            if (nbt == null) return;

            server.execute(() -> {
                if (player.currentScreenHandler.syncId == syncId && player.currentScreenHandler instanceof EditorScreenHandler networking) {
                    networking.graph.readNbt(nbt);
                }
            });
        }));
    }

    public static void send(int syncId, Graph graph) {
        var buf = PacketByteBufs.create();
        var nbt = new NbtCompound();
        graph.writeNbt(nbt);
        buf.writeNbt(nbt);
        buf.writeByte(syncId);

        ClientPlayNetworking.send(GRAPH_SYNC_ID, buf);
    }
}
