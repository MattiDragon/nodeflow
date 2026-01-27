package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node groups are collections of similar nodes that are grouped together in the gui. A single node can be in multiple groups. You are free to add your own nodes to any group as long as they fit the group.
 */
public interface NodeGroup {
    Map<Identifier, StreamCodec<? super RegistryFriendlyByteBuf, ? extends NodeGroup>> CODECS = new HashMap<>();
    StreamCodec<RegistryFriendlyByteBuf, NodeGroup> CODEC = Identifier.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().dispatch(NodeGroup::getCodecId, NodeGroup.CODECS::get);

    /**
     * Registers a packet decoder for a group type. The id must match that returned by {@link #getCodecId() getDecoderId} for this to work correctly.
     */
    static void registerCodec(Identifier id, StreamCodec<? super RegistryFriendlyByteBuf, ? extends NodeGroup> codec) {
        CODECS.put(id, codec);
    }

    /**
     * Gets the name of the group, for display in the gui.
     */
    Component getName();

    /**
     * Gets the nodes in the group.
     */
    List<NodeType<?>> getTypes();

    /**
     * Gets the id used to get the packet decoder for this group.
     */
    Identifier getCodecId();
}
