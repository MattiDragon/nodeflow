package io.github.mattidragon.nodeflow.misc;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.screen.EditorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * This should to be implemented on a block entity class to provide graphs for a {@link io.github.mattidragon.nodeflow.client.ui.screen.HandledEditorScreen HandledEditorScreen}.
 * If you for some reason can't do it or are somehow using a screen handler without a block entity then you can subclass {@link EditorMenu EditorScreenHandler} and override the close logic.
 */
public interface GraphProvider {
    void setGraph(Graph graph, Level world, BlockPos pos);
    Graph getGraph(Level world, BlockPos pos);
}
