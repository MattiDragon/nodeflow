package io.github.mattidragon.nodeflow;

import io.github.mattidragon.nodeflow.graph.Graph;
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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeFlow implements ModInitializer {
    public static final String MOD_ID = "nodeflow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ExtendedScreenHandlerType<EditorScreenHandler, Graph> SCREEN_HANDLER = new ExtendedScreenHandlerType<>(EditorScreenHandler::new, Graph.PACKET_CODEC);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.MENU, id("editor_screen"), SCREEN_HANDLER);
        GraphSyncPacket.register();
        NodeType.register();
        DataType.register();
        ContextType.register();
        
        NodeGroup.registerCodec(TagNodeGroup.DECODER_ID, TagNodeGroup.CODEC);
        NodeGroup.registerCodec(DirectNodeGroup.DECODER_ID, DirectNodeGroup.CODEC);
    }
}
