package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * This class is useful for those implementing a block entity with a graph screen. It implements sending the required info to the client and creating the screen handler.
 * You don't have to use this class; you only have to implement {@link GraphProvider} and {@link ExtendedScreenHandlerFactory} for everything to work.
 */
public abstract class GraphProvidingBlockEntity extends BlockEntity implements GraphProvider, ExtendedScreenHandlerFactory {
    public GraphProvidingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        var graphNbt = new NbtCompound();
        var graph = getGraph(world, pos);
        graph.env.toPacket(buf);
        graph.writeNbt(graphNbt);
        buf.writeNbt(graphNbt);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new EditorScreenHandler(syncId, this, ScreenHandlerContext.create(world, pos));
    }
}
