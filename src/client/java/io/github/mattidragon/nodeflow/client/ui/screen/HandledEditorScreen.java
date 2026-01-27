package io.github.mattidragon.nodeflow.client.ui.screen;

import io.github.mattidragon.nodeflow.misc.GraphSyncPacket;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * A networking screen that uses a screen handler to sync data. You shouldn't need to touch this class unless you want to add features to the screen.
 */
public class HandledEditorScreen extends EditorScreen implements MenuAccess<EditorScreenHandler> {
    private final EditorScreenHandler handler;

    public HandledEditorScreen(EditorScreenHandler handler, Inventory inventory, Component title) {
        super(title, handler.graph.copy());
        this.handler = handler;
    }

    public HandledEditorScreen(EditorScreenHandler handler, Inventory inventory, Component title, ResourceLocation texture) {
        super(title, handler.graph.copy(), texture);
        this.handler = handler;
    }

    @Override
    public void syncGraph() {
        var nbt = graph.writeNbt(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        ClientPlayNetworking.send(new GraphSyncPacket(nbt, (byte) handler.containerId));
    }

    @Override
    public @NotNull EditorScreenHandler getMenu() {
        return handler;
    }
}
