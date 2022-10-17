package io.github.mattidragon.nodeflow.ui.screen;

import io.github.mattidragon.nodeflow.misc.GraphSyncPacket;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
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
        GraphSyncPacket.send(this.handler.syncId, graph);
    }

    @Override
    public EditorScreenHandler getScreenHandler() {
        return handler;
    }
}
