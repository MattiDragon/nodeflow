package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node groups are collections of similar nodes that are grouped together in the gui. A single node can be in multiple groups. You are free to add your own nodes to any group as long as they fit the group.
 */
public interface NodeGroup {
    Map<Identifier, PacketCodec<? super RegistryByteBuf, ? extends NodeGroup>> CODECS = new HashMap<>();
    PacketCodec<RegistryByteBuf, NodeGroup> CODEC = Identifier.PACKET_CODEC.<RegistryByteBuf>cast().dispatch(NodeGroup::getCodecId, NodeGroup.CODECS::get);

    /**
     * Registers a packet decoder for a group type. The id must match that returned by {@link #getCodecId() getDecoderId} for this to work correctly.
     */
    static void registerCodec(Identifier id, PacketCodec<? super RegistryByteBuf, ? extends NodeGroup> codec) {
        CODECS.put(id, codec);
    }

    /**
     * Gets the name of the group, for display in the gui.
     */
    Text getName();

    /**
     * Gets the nodes in the group.
     */
    List<NodeType<?>> getTypes();

    /**
     * Gets the id used to get the packet decoder for this group.
     */
    Identifier getCodecId();
}
