package io.github.mattidragon.nodeflow.screen;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.misc.GraphProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class EditorScreenHandler extends AbstractContainerMenu {
    public final Graph graph;
    private final ContainerLevelAccess context;

    /**
     * Used to create the screen handler on the server.
     * @param syncId The sync id of this screen handler, should be provided by minecraft for you.
     * @param provider The block entity that is providing the graph.
     * @param context A screen handler context. Create one using {@link ContainerLevelAccess#create}.
     */
    public EditorScreenHandler(int syncId, GraphProvider provider, ContainerLevelAccess context) {
        super(NodeFlow.SCREEN_HANDLER, syncId);
        // Context should always be present
        this.graph = context.evaluate((world, pos) -> provider.getGraph(world, pos).copy()).orElseThrow(IllegalStateException::new);
        this.context = context;
    }

    /**
     * Used to create a screen handler on the client. There shouldn't really be any reason for you to use this, but it's internally needed.
     * @param syncId The sync id of this screen handler, should be provided by minecraft for you.
     * @param inv Ignored. Exist for convenient lambda usage.
     * @param graph The graph that the screen will be editing.
     */
    public EditorScreenHandler(int syncId, Inventory inv, Graph graph) {
        super(NodeFlow.SCREEN_HANDLER, syncId);
        this.graph = graph;
        context = ContainerLevelAccess.NULL;
    }

    // Minecraft assumes screen handlers have slots. We don't, so we don't implement shift clicking.
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.context.evaluate((world, pos) -> player.distanceToSqr((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 64.0, true);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        context.execute((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof GraphProvider provider) {
                provider.setGraph(graph.copy(), world, pos);
            }
        });
    }
}
