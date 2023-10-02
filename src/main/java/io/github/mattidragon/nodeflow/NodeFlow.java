package io.github.mattidragon.nodeflow;

import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.group.DirectNodeGroup;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.group.TagNodeGroup;
import io.github.mattidragon.nodeflow.misc.GraphSyncPacket;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeFlow implements ModInitializer {
    public static final String MOD_ID = "nodeflow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ExtendedScreenHandlerType<EditorScreenHandler> SCREEN_HANDLER = new ExtendedScreenHandlerType<>(EditorScreenHandler::new);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.SCREEN_HANDLER, id("editor_screen"), SCREEN_HANDLER);
        GraphSyncPacket.register();
        NodeType.register();
        DataType.register();
        ContextType.register();

        NodeGroup.registerDecoder(TagNodeGroup.DECODER_ID, TagNodeGroup::new);
        NodeGroup.registerDecoder(DirectNodeGroup.DECODER_ID, DirectNodeGroup::new);
    }
}
