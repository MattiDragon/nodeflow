package io.github.mattidragon.nodeflow.client.ui.screen;

import io.github.mattidragon.nodeflow.misc.GraphSyncPacket;
import io.github.mattidragon.nodeflow.screen.EditorMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * A networking screen that uses a screen handler to sync data. You shouldn't need to touch this class unless you want to add features to the screen.
 */
public class MenuEditorScreen extends EditorScreen implements MenuAccess<EditorMenu> {
    private final EditorMenu handler;

    public MenuEditorScreen(EditorMenu handler, Inventory inventory, Component title) {
        super(title, handler.graph.copy());
        this.handler = handler;
    }

    public MenuEditorScreen(EditorMenu handler, Inventory inventory, Component title, Identifier texture) {
        super(title, handler.graph.copy(), texture);
        this.handler = handler;
    }

    @Override
    public void syncGraph() {
        var nbt = graph.writeNbt(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        ClientPlayNetworking.send(new GraphSyncPacket(nbt, (byte) handler.containerId));
    }

    @Override
    public EditorMenu getMenu() {
        return handler;
    }
}
