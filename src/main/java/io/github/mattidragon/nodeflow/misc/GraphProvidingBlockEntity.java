package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * This class is useful for those implementing a block entity with a graph screen. It implements sending the required info to the client and creating the screen handler.
 * You don't have to use this class; you only have to implement {@link GraphProvider} and {@link ExtendedScreenHandlerFactory} for everything to work.
 */
public abstract class GraphProvidingBlockEntity extends BlockEntity implements GraphProvider, ExtendedScreenHandlerFactory<Graph> {
    public GraphProvidingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Graph getScreenOpeningData(ServerPlayer player) {
        return getGraph(level, worldPosition);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new EditorScreenHandler(syncId, this, ContainerLevelAccess.create(level, worldPosition));
    }
}
