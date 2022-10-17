package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * This should to be implemented on a block entity class to provide graphs for a {@link HandledEditorScreen HandledGraphScreen}.
 * If you for some reason can't do it or are somehow using a screen handler without a block entity then you can subclass {@link EditorScreenHandler GraphScreenHandler} and override the close logic.
 */
public interface GraphProvider {
    void setGraph(Graph graph, World world, BlockPos pos);
    Graph getGraph(World world, BlockPos pos);
}
