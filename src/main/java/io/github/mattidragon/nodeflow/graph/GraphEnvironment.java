package io.github.mattidragon.nodeflow.graph;

import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Contains info about an environment in which graphs exist. For most use cases there should only be need for a single environment per system using nodeflow, but if you, for example, would like some nodes to unlockable as part of you mods progressions you can make a new environment for each usage.
 * @param allowedDataTypes The list of allowed data types. Do not use the entire registry here; other mods might add their own data types that aren't obtainable in your graph. Used by nodes with configurable inputs and outputs to know possible data types.
 * @param availableContexts The list of contexts available to graphs in this environment
 * @param groups A list of groups nodes should be put in. Nodes without a group will be placed in a 'misc' group.
 */
public record GraphEnvironment(List<DataType<?>> allowedDataTypes, List<ContextType<?>> availableContexts, List<NodeGroup> groups) {
    public static final PacketCodec<RegistryByteBuf, GraphEnvironment> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(DataType.REGISTRY.getKey()).collect(PacketCodecs.toList()), GraphEnvironment::allowedDataTypes,
            PacketCodecs.registryValue(ContextType.KEY).collect(PacketCodecs.toList()), GraphEnvironment::availableContexts,
            NodeGroup.CODEC.collect(PacketCodecs.toList()), GraphEnvironment::groups,
            GraphEnvironment::new
    );
    
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
