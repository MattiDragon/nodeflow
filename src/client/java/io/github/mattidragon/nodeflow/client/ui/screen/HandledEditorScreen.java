package io.github.mattidragon.nodeflow.client.ui.screen;

import io.github.mattidragon.nodeflow.misc.GraphSyncPacket;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A networking screen that uses a screen handler to sync data. You shouldn't need to touch this class unless you want to add features to the screen.
 */
public class HandledEditorScreen extends EditorScreen implements ScreenHandlerProvider<EditorScreenHandler> {
    private final EditorScreenHandler handler;

    public HandledEditorScreen(EditorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(title, handler.graph.copy());
        this.handler = handler;
    }

    public HandledEditorScreen(EditorScreenHandler handler, PlayerInventory inventory, Text title, Identifier texture) {
        super(title, handler.graph.copy(), texture);
        this.handler = handler;
    }

    @Override
    public void syncGraph() {
        var buf = PacketByteBufs.create();
        var nbt = new NbtCompound();
        graph.writeNbt(nbt);
        buf.writeNbt(nbt);
        buf.writeByte(this.handler.syncId);

        ClientPlayNetworking.send(GraphSyncPacket.GRAPH_SYNC_ID, buf);
    }

    @Override
    public EditorScreenHandler getScreenHandler() {
        return handler;
    }
}
