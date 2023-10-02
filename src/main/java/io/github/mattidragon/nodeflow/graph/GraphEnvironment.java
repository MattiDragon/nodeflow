package io.github.mattidragon.nodeflow.graph;

import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Contains info about an environment in which graphs exist. For most use cases there should only be need for a single environment per system using nodeflow, but if you, for example, would like some nodes to unlockable as part of you mods progressions you can make a new environment for each usage.
 * @param allowedNodeTypes The list of allowed types of nodes. Do not use the entire registry here; other mods might add nodes you can't provide context for.
 * @param allowedDataTypes The list of allowed data types. Do not use the entire registry here; other mods might add their own data types that aren't obtainable in your graph. Used by nodes with configurable inputs and outputs to know possible data types.
 * @param availableContexts The list of contexts available to graphs in this environment
 * @param groups A list of groups nodes should be put in. Nodes without a group will be placed in a 'misc' group.
 */
public record GraphEnvironment(List<DataType<?>> allowedDataTypes, List<ContextType<?>> availableContexts, List<NodeGroup> groups) {
    /**
     * Creates a graph environment, removing nodes that don't match the required types and contexts.
     */
    public GraphEnvironment(List<DataType<?>> allowedDataTypes, List<ContextType<?>> availableContexts, List<NodeGroup> groups) {
        if (allowedDataTypes.isEmpty()) throw new IllegalArgumentException("At least one data type has to be allowed");
        this.allowedDataTypes = List.copyOf(allowedDataTypes);
        this.availableContexts = List.copyOf(availableContexts);
        this.groups = List.copyOf(groups);
    }

    public boolean isAllowedNodeType(NodeType<?> type) {
        return groups.stream().map(NodeGroup::getTypes).flatMap(List::stream).anyMatch(type::equals);
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeCollection(allowedDataTypes.stream().map(DataType.REGISTRY::getId).toList(), PacketByteBuf::writeIdentifier);
        buf.writeCollection(availableContexts.stream().map(ContextType.REGISTRY::getId).toList(), PacketByteBuf::writeIdentifier);
        buf.writeCollection(groups, (buf1, group) -> {
            buf1.writeIdentifier(group.getDecoderId());
            group.toPacket(buf1);
        });
    }

    public static GraphEnvironment fromPacket(PacketByteBuf buf) {
        var allowedDataTypes = buf.readList(PacketByteBuf::readIdentifier).stream().<DataType<?>>map(DataType.REGISTRY::get).toList();
        var availableContexts = buf.readList(PacketByteBuf::readIdentifier).stream().<ContextType<?>>map(ContextType.REGISTRY::get).toList();
        var groups = buf.readList((buf1) -> NodeGroup.DECODERS.get(buf1.readIdentifier()).apply(buf1));
        return new GraphEnvironment(allowedDataTypes, availableContexts, groups);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<DataType<?>> allowedDataTypes = new ArrayList<>();
        private final List<ContextType<?>> availableContexts = new ArrayList<>();
        private final List<NodeGroup> groups = new ArrayList<>();

        private Builder() {}

        public Builder addDataTypes(DataType<?>... types) {
            Collections.addAll(allowedDataTypes, types);
            return this;
        }

        public Builder addContextTypes(ContextType<?>... types) {
            Collections.addAll(availableContexts, types);
            return this;
        }

        public Builder addDataTypes(List<DataType<?>> types) {
            allowedDataTypes.addAll(types);
            return this;
        }

        public Builder addContextTypes(List<ContextType<?>> types) {
            availableContexts.addAll(types);
            return this;
        }

        public Builder addNodeGroups(NodeGroup... groups) {
            this.groups.addAll(Arrays.asList(groups));
            return this;
        }

        public GraphEnvironment build() {
            return new GraphEnvironment(allowedDataTypes, availableContexts, groups);
        }
    }

    private static class DummyContext extends Context {
        private final List<ContextType<?>> list;

        public DummyContext(List<ContextType<?>> list) {
            super(Map.of());
            this.list = list;
        }

        @Override
        public <T> T get(ContextType<T> type) {
            return null;
        }

        @Override
        public boolean contains(ContextType<?> type) {
            if (list.contains(type)) return true;
            for (var entry : list) {
                if (ArrayUtils.contains(entry.parents(), type))
                    return true;
            }
            return false;
        }
    }
}
