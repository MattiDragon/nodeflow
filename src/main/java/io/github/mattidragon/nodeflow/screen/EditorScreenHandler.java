package io.github.mattidragon.nodeflow.screen;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.GraphEnvironment;
import io.github.mattidragon.nodeflow.misc.GraphProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public class EditorScreenHandler extends ScreenHandler {
    public final Graph graph;
    private final ScreenHandlerContext context;

    /**
     * Used to create the screen handler on the server.
     * @param syncId The sync id of this screen handler, should be provided by minecraft for you.
     * @param provider The block entity that is providing the graph.
     * @param context A screen handler context. Create one using {@link ScreenHandlerContext#create}.
     */
    public EditorScreenHandler(int syncId, GraphProvider provider, ScreenHandlerContext context) {
        super(NodeFlow.SCREEN_HANDLER, syncId);
        // Context should always be present
        this.graph = context.get((world, pos) -> provider.getGraph(world, pos).copy()).orElseThrow(IllegalStateException::new);
        this.context = context;
    }

    /**
     * Used to create a screen handler on the client. There shouldn't really be any reason for you to use this, but it's internally needed.
     * @param syncId The sync id of this screen handler, should be provided by minecraft for you.
     * @param inv Ignored. Exist for convenient lambda usage.
     * @param buf The byte buffer with the graph data from the server.
     */
    @ApiStatus.Internal
    public EditorScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        super(NodeFlow.SCREEN_HANDLER, syncId);
        this.graph = new Graph(GraphEnvironment.fromPacket(buf));
        graph.readNbt(Objects.requireNonNull(buf.readNbt()));
        context = ScreenHandlerContext.EMPTY;
    }

    // Minecraft assumes screen handlers have slots. We don't, so we don't implement shift clicking.
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.context.get((world, pos) -> player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 64.0, true);
    }



    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof GraphProvider provider) {
                provider.setGraph(graph.copy(), world, pos);
            }
        });
    }
}
