package io.github.mattidragon.nodeflow.graph;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Contains info about an environment in which graphs exist. For most use cases there should only be need for a single environment per system using nodeflow, but if you, for example, would like some nodes to unlockable as part of you mods progressions you can make a new environment for each usage.
 * @param allowedNodeTypes The list of allowed types of nodes. Do not use the entire registry here; other mods might add nodes you can't provide context for.
 * @param allowedDataTypes The list of allowed data types. Do not use the entire registry here; other mods might add their own data types that aren't obtainable in your graph. Used by nodes with configurable inputs and outputs to know possible data types.
 * @param availableContexts The list of contexts available to graphs in this environment
 * @param groups A list of groups nodes should be put in. Nodes without a group will be placed in a 'misc' group.
 */
public record GraphEnvironment(List<NodeType<?>> allowedNodeTypes, List<DataType<?>> allowedDataTypes, List<ContextType<?>> availableContexts, List<NodeGroup> groups) {
    /**
     * Creates a graph environment, removing nodes that don't match the required types and contexts.
     */
    public GraphEnvironment(List<NodeType<?>> allowedNodeTypes, List<DataType<?>> allowedDataTypes, List<ContextType<?>> availableContexts, List<NodeGroup> groups) {
        if (allowedDataTypes.isEmpty()) throw new IllegalArgumentException("At least one data type has to be allowed");
        allowedNodeTypes = new ArrayList<>(allowedNodeTypes);
        allowedDataTypes = new ArrayList<>(allowedDataTypes);
        availableContexts = new ArrayList<>(availableContexts);

        this.allowedNodeTypes = Collections.unmodifiableList(allowedNodeTypes);
        this.allowedDataTypes = List.copyOf(allowedDataTypes);
        this.availableContexts = List.copyOf(availableContexts);
        this.groups = List.copyOf(groups);

        var dummyGraph = new Graph(this);
        var dummyContext = new DummyContext(availableContexts);

        var errors = new ArrayList<RuntimeException>();

        var nodes = new ArrayList<Node>();
        for (var type : allowedNodeTypes) {
            var node = type.generator().apply(dummyGraph);
            if (node.type != type) {
                errors.add(new IllegalStateException("Node type %s produced a node with type %s which is different and not allowed!".formatted(type, node.type)));
                continue;
            }
            nodes.add(node);
        }

        outer:
        for (var node : nodes) {
            for (ContextType<?> contextType : node.contexts) {
                if (!dummyContext.contains(contextType)) {
                    errors.add(new IllegalArgumentException("Node with type %s requires context %s which is not available!".formatted(node.type, contextType)));
                    continue outer;
                }
            }

            for (Connector<?> connector : node.getInputs()) {
                if (!allowedDataTypes.contains(connector.type())) {
                    errors.add(new IllegalArgumentException("Node with type %s has input with type %s which is not available!".formatted(node.type, connector.type())));
                    continue outer;
                }
            }

            for (Connector<?> connector : node.getOutputs()) {
                if (!allowedDataTypes.contains(connector.type())) {
                    errors.add(new IllegalArgumentException("Node with type %s has output with type %s which is not available!".formatted(node.type, connector.type())));
                    continue outer;
                }
            }
        }
        if (!errors.isEmpty()) {
            var exception = new IllegalStateException("Failed to build graph environment", errors.get(0));
            for (int i = 1; i < errors.size(); i++) {
                exception.addSuppressed(errors.get(i));
            }
            throw exception;
        }
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeCollection(allowedNodeTypes.stream().map(NodeType.REGISTRY::getId).toList(), PacketByteBuf::writeIdentifier);
        buf.writeCollection(allowedDataTypes.stream().map(DataType.REGISTRY::getId).toList(), PacketByteBuf::writeIdentifier);
        buf.writeCollection(availableContexts.stream().map(ContextType.REGISTRY::getId).toList(), PacketByteBuf::writeIdentifier);
        buf.writeCollection(groups, (buf1, group) -> {
            buf1.writeText(group.name());
            buf1.writeCollection(group.nodes().stream().map(NodeType.REGISTRY::getId).toList(), PacketByteBuf::writeIdentifier);
        });
    }

    public static GraphEnvironment fromPacket(PacketByteBuf buf) {
        var allowedNodeTypes = buf.readList(PacketByteBuf::readIdentifier).stream().<NodeType<?>>map(NodeType.REGISTRY::get).toList();
        var allowedDataTypes = buf.readList(PacketByteBuf::readIdentifier).stream().<DataType<?>>map(DataType.REGISTRY::get).toList();
        var availableContexts = buf.readList(PacketByteBuf::readIdentifier).stream().<ContextType<?>>map(ContextType.REGISTRY::get).toList();
        var groups = buf.readList((buf1) -> {
            var name = buf1.readText();
            var nodes = buf1.readList(PacketByteBuf::readIdentifier).stream().<NodeType<?>>map(NodeType.REGISTRY::get).toList();
            return new NodeGroup(name, nodes);
        });
        return new GraphEnvironment(allowedNodeTypes, allowedDataTypes, availableContexts, groups);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<NodeType<?>> allowedNodeTypes = new ArrayList<>();
        private final List<DataType<?>> allowedDataTypes = new ArrayList<>();
        private final List<ContextType<?>> availableContexts = new ArrayList<>();
        private final List<NodeGroup> groups = new ArrayList<>();

        private Builder() {}

        public Builder addNodeTypes(NodeType<?>... types) {
            Collections.addAll(allowedNodeTypes, types);
            return this;
        }

        public Builder addDataTypes(DataType<?>... types) {
            Collections.addAll(allowedDataTypes, types);
            return this;
        }

        public Builder addContextTypes(ContextType<?>... types) {
            Collections.addAll(availableContexts, types);
            return this;
        }

        public Builder addNodeTypes(List<NodeType<?>> types) {
            allowedNodeTypes.addAll(types);
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
            for (var group : groups) {
                addNodeTypes(group.nodes());
                this.groups.add(group);
            }
            return this;
        }

        public Builder printDisableReasons() {
            var output = new StringBuilder("Graph environment debug info:\n");

            if (availableContexts.isEmpty())
                output.append("No contexts are available");
            else {
                output.append("Available contexts:\n");
                for (var context : availableContexts) {
                    output.append(" - [id: ")
                            .append(context)
                            .append(", parents: ")
                            .append(Arrays.toString(context.parents()))
                            .append(", class: ")
                            .append(context.type().getSimpleName())
                            .append("]\n");
                }
            }

            if (allowedDataTypes.isEmpty())
                output.append("No data types are allowed");
            else {
                output.append("Allowed data types:\n");
                for (var type : allowedDataTypes) {
                    output.append(" - [id: ")
                            .append(type)
                            .append(", splittable: ")
                            .append(type.splittable())
                            .append("]\n");
                }
            }

            if (allowedNodeTypes.isEmpty())
                output.append("No node types are allowed");
            else {
                output.append("Allowed node types:\n");
                for (var type : allowedNodeTypes) {
                    output.append(" - [id: ")
                            .append(type)
                            .append("]\n");
                }
            }

            try {
                var environment = build();
                var dummyGraph = new Graph(environment);
                var dummyContext = new DummyContext(availableContexts);

                var nodes = allowedNodeTypes.stream().map(NodeType::generator)
                        .map(generator -> generator.apply(dummyGraph))
                        .toList();

                var errors = new ArrayList<String>();

                outer:
                for (var node : nodes) {
                    for (ContextType<?> contextType : node.contexts) {
                        if (!dummyContext.contains(contextType)) {
                            errors.add("Removed node %s due to missing context %s".formatted(node.type, contextType));
                            continue outer;
                        }
                    }

                    for (Connector<?> connector : node.getInputs()) {
                        if (!allowedDataTypes.contains(connector.type())) {
                            errors.add("Removed node %s due to missing type for input %s, %s".formatted(node.type, connector.id(), connector.type()));
                            continue outer;
                        }
                    }

                    for (Connector<?> connector : node.getOutputs()) {
                        if (!allowedDataTypes.contains(connector.type())) {
                            errors.add("Removed node %s due to missing type for output %s, %s".formatted(node.type, connector.id(), connector.type()));
                            continue outer;
                        }
                    }
                }

                if (!errors.isEmpty()) {
                    output.append("\nThe following nodes where removed:\n");
                    for (var error : errors) {
                        output.append(" - ")
                                .append(error)
                                .append("\n");
                    }
                }
            } catch (IllegalArgumentException e) {
                output.append("\nCouldn't build environment: ")
                        .append(e)
                        .append("\n");
            }

            NodeFlow.LOGGER.info(output.toString());
            return this;
        }

        public GraphEnvironment build() {
            return new GraphEnvironment(allowedNodeTypes, allowedDataTypes, availableContexts, groups);
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
