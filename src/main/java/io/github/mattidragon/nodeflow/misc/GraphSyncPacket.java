package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class GraphSyncPacket {
    public static final Identifier GRAPH_SYNC_ID = NodeFlow.id("graph_sync");

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
}
