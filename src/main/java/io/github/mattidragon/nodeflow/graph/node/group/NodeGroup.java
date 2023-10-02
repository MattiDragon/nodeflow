package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Node groups are collections of similar nodes that are grouped together in the gui. A single node can be in multiple groups. You are free to add your own nodes to any group as long as they fit the group.
 */
public interface NodeGroup {
    Map<Identifier, Function<PacketByteBuf, NodeGroup>> DECODERS = new HashMap<>();

    /**
     * Registers a packet decoder for a group type. The id must match that returned by {@link #getDecoderId() getDecoderId} for this to work correctly.
     */
    static void registerDecoder(Identifier id, Function<PacketByteBuf, NodeGroup> decoder) {
        DECODERS.put(id, decoder);
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
    Identifier getDecoderId();

    /**
     * Encodes this group to a packet. A matching decoder should be registered with {@link #registerDecoder(Identifier, Function) registerDecoder} at startup.
     */
    void toPacket(PacketByteBuf buf);
}
