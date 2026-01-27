package io.github.mattidragon.nodeflow.graph;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.misc.EvaluationError;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Graph {
    public static final StreamCodec<RegistryFriendlyByteBuf, Graph> PACKET_CODEC =
            StreamCodec.of((buf, graph) -> {
                GraphEnvironment.PACKET_CODEC.encode(buf, graph.env);
                buf.writeNbt(graph.writeNbt(buf.registryAccess()));
            }, buf -> {
                var environment = GraphEnvironment.PACKET_CODEC.decode(buf);
                var graph = new Graph(environment);
                graph.readNbt(Objects.requireNonNull(buf.readNbt(), "Missing nbt in packet"), buf.registryAccess());
                return graph;
            });
    
    private final Map<UUID, Node> nodes = new LinkedHashMap<>();
    private final Set<Connection> connections = new LinkedHashSet<>();
    public final GraphEnvironment env;

    public Graph(GraphEnvironment env) {
        this.env = env;
    }

    public Graph copy() {
        var staticRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var graph = new Graph(env);
        graph.readNbt(writeNbt(staticRegistries), staticRegistries);
        return graph;
    }

    public void addNode(Node node) {
        if (!env.isAllowedNodeType(node.type))
            throw new IllegalArgumentException("This graph doesn't support that node type: %s".formatted(node.type));
        if (nodes.containsKey(node.id)) {
            NodeFlow.LOGGER.warn("Tried to add node that already is in graph. (id: {}, type: {})", node.id, node.type);
            return;
        }
        nodes.put(node.id, node);
    }

    public Node getNode(UUID id) {
        return nodes.get(id);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public void removeNode(UUID id) {
        nodes.remove(id);
        connections.removeAll(getConnections(id));
    }

    /**
     * Removes all connections to the specified connector
     */
    public void removeConnections(Connector<?> connector) {
        connections.removeAll(getConnections(connector));
    }

    public void addConnection(Connector<?> target, Connector<?> source) {
        if (target.isOutput() == source.isOutput())
            throw new IllegalArgumentException("Adding connection target graph.");

        // swap target and source if necessary
        if (target.isOutput()) {
            var tmp = target;
            target = source;
            source = tmp;
        }

        connections.add(new Connection(target.parent().id, target.id(), source.parent().id, source.id()));
    }

    public void cleanConnections(Node node) {
        getConnections(node.id).stream().filter(connection -> {
            var input = connection.getTargetConnector(this);
            var output = connection.getSourceConnector(this);

            return input == null || output == null || input.type() != output.type();
        }).forEach(connections::remove);
    }

    /**
     * Returns all the connections in the graph.
     */
    public Set<Connection> getConnections() {
        return Collections.unmodifiableSet(connections);
    }

    /**
     * Returns all connections to and from the node with the specified UUID.
     */
    public Set<Connection> getConnections(UUID node) {
        return connections.stream().filter(connection -> connection.targetUuid().equals(node) || connection.sourceUuid().equals(node)).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Returns all the nodes connected to the specified connector. Normally 0 or 1, but for outputs of splittable types there can be more.
     */
    public Set<Connection> getConnections(Connector<?> connector) {
        if (connector.isOutput()) {
            return connections.stream().filter(connection -> connector.equals(connection.getSourceConnector(this))).collect(Collectors.toCollection(HashSet::new));
        }
        for (var connection : connections) {
            if (connector.equals(connection.getTargetConnector(this))) return Set.of(connection);
        }
        return Set.of();
    }

    public CompoundTag writeNbt(HolderLookup.Provider registries) {
        try (var logging = new ProblemReporter.ScopedCollector(() -> "Graph NBT serializer", NodeFlow.LOGGER)) {
            var view = TagValueOutput.createWithContext(logging, registries);
            writeData(view);
            return view.buildResult();
        }
    }

    public void readNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        try (var logging = new ProblemReporter.ScopedCollector(() -> "Graph NBT deserializer", NodeFlow.LOGGER)) {
            var view = TagValueInput.create(logging, registries, nbt);
            readData(view);
        }
    }

    public void writeData(ValueOutput view) {
        var nodeData = view.childrenList("nodes");
        nodes.values().forEach(node -> node.writeData(nodeData.addChild()));

        view.store("connections", Connection.CODEC.listOf(), List.copyOf(connections));
    }

    public void readData(ValueInput view) {
        var ignoredIds = new ArrayList<UUID>();

        nodes.clear();
        for (var element : view.childrenListOrEmpty("nodes")) {
            var typeString = element.getStringOr("type", "<missing>");
            var type = element.getString("type")
                    .map(Identifier::tryParse)
                    .flatMap(NodeType.REGISTRY::getOptional);
            if (type.isEmpty()) {
                NodeFlow.LOGGER.warn("Unknown node type: {}. Ignoring node", typeString);
                element.read("id", UUIDUtil.AUTHLIB_CODEC).ifPresent(ignoredIds::add);
                continue;
            }
            if (!env.isAllowedNodeType(type.get())) {
                NodeFlow.LOGGER.warn("Unsupported node type: {}. Ignoring node", typeString);
                element.read("id", UUIDUtil.AUTHLIB_CODEC).ifPresent(ignoredIds::add);
                continue;
            }
            var node = type.get().generator().apply(this);
            node.readData(element);
            nodes.put(node.id, node);
        }

        connections.clear();
        for (var connection : view.read("connections", Connection.CODEC.listOf()).orElse(List.of())) {
            if (validateConnection(connection, ignoredIds)) {
                connections.add(connection);
            }
        }
    }

    private boolean validateConnection(Connection connection, ArrayList<UUID> ignoredIds) {
        // Silently remove connections to removed nodes
        if (ignoredIds.contains(connection.targetUuid()) || ignoredIds.contains(connection.sourceUuid()))
            return false;

        if (!nodes.containsKey(connection.targetUuid())) {
            NodeFlow.LOGGER.warn("Found connections to non-existent node. Id: {}.", connection.targetUuid());
            ignoredIds.add(connection.targetUuid());
            return false;
        }
        if (!nodes.containsKey(connection.sourceUuid())) {
            NodeFlow.LOGGER.warn("Found connections to non-existent node. Id: {}.", connection.sourceUuid());
            ignoredIds.add(connection.sourceUuid());
            return false;
        }

        if (Arrays.stream(nodes.get(connection.targetUuid()).getInputs()).noneMatch(input -> input.id().equals(connection.targetName()))) {
            NodeFlow.LOGGER.warn("Found connection to non-existent input. Name: {}, Node: {}.", connection.targetName(), connection.targetUuid());
            return false;
        }

        if (Arrays.stream(nodes.get(connection.sourceUuid()).getOutputs()).noneMatch(output -> output.id().equals(connection.sourceName()))) {
            NodeFlow.LOGGER.warn("Found connection to non-existent output. Name: {}, Node: {}.", connection.sourceName(), connection.sourceUuid());
            return false;
        }

        return true;
    }

    /**
     * Evaluates the graph with the given context. Even if an error is detected when running one node another node that has side effects might have run. Ensure that no issues can be caused by this in your environment, by for example delaying application of nodes until after evaluation.
     * @param context Additional context for nodes to use.
     * @return Any errors that might have happened.
     * @implNote Currently only returns one error at a time, but in the future some situation might allow multiple errors.
     */
    public List<EvaluationError> evaluate(Context context) {
        if (nodes.values().stream().anyMatch(Predicate.not(Node::isFullyConnected)))
            return List.of(EvaluationError.Type.NOT_CONNECTED.error());

        for (Node node : nodes.values()) {
            var errors = node.validate();
            if (!errors.isEmpty()) {
                return List.of(EvaluationError.Type.INVALID_CONFIG.error(errors.getFirst().copy().withStyle(ChatFormatting.YELLOW)));
            }
        }

        // Make sure all nodes are processed
        var nodesProcessed = 0;
        var inputCounts = new Object2IntOpenHashMap<Node>(nodes.size());
        inputCounts.defaultReturnValue(0);
        var availableInputs = new HashMap<Node, Map<String, DataValue<?>>>();

        // Count connected inputs of all nodes
        for (var connection : connections) {
            inputCounts.addTo(nodes.get(connection.targetUuid()), 1);
        }

        var readyNodes = nodes.values().stream().filter(node -> inputCounts.getInt(node) == 0).toList();

        while (!readyNodes.isEmpty()) {
            var nextNodes = new ArrayList<Node>();

            for (var node : readyNodes) {
                var inputValues = availableInputs.computeIfAbsent(node, __ -> new HashMap<>());
                var inputConnectors = node.getInputs();

                // TODO: move to other node checks (why is it here?)
                var missingContexts = node.contexts.stream().filter(Predicate.not(context::contains)).toList();
                if (!missingContexts.isEmpty()) {
                    return List.of(EvaluationError.Type.MISSING_CONTEXTS.error(missingContexts));
                }

                // get values for inputs in order
                var values = Arrays.stream(inputConnectors)
                        .map(connector -> inputValues.get(connector.id()))
                        .toArray(DataValue[]::new);

                // validate types of inputs to avoid ugly errors
                for (int i = 0; i < values.length; i++) {
                    if (inputConnectors[i].type() != values[i].type()) {
                        return List.of(EvaluationError.Type.MISMATCHED_CONNECTION_TYPES.error());
                    }
                }

                Either<DataValue<?>[], Component> either;
                try {
                    either = node.process(values, context);
                } catch (RuntimeException e) {
                    NodeFlow.LOGGER.warn("Unexpected error while evaluating node", e);
                    return List.of(EvaluationError.Type.EVALUATION_ERROR.error(e.getMessage()));
                }

                // Propagate node errors
                if (either.left().isEmpty()) {
                    // Intellij doesn't understand either
                    //noinspection OptionalGetWithoutIsPresent
                    return List.of(EvaluationError.Type.EVALUATION_ERROR.error(either.right().get()));
                }

                var results = either.left().get();
                var expectedOutputs = node.getOutputs();

                if (expectedOutputs.length != results.length) {
                    return List.of(EvaluationError.Type.UNEXPECTED_OUTPUT_COUNT.error(expectedOutputs.length, results.length));
                }

                for (int i = 0; i < expectedOutputs.length; i++) {
                    var connector = expectedOutputs[i];
                    var value = results[i];

                    if (value.type() != connector.type()) {
                        return List.of(EvaluationError.Type.UNEXPECTED_OUTPUT_TYPE.error(i, value.type(), connector.type()));
                    }

                    var connections = getConnections(connector);
                    for (var connection : connections) {
                        var target = getNode(connection.targetUuid());
                        var map = availableInputs.computeIfAbsent(target, __ -> new HashMap<>());
                        map.put(connection.targetName(), value);

                        // If the node has gotten all of its inputs, schedule it for the next round
                        if (map.size() == inputCounts.getInt(target))
                            nextNodes.add(target);
                    }
                }
                nodesProcessed++;
            }
            readyNodes = nextNodes;
        }
        if (nodesProcessed != nodes.size())
            return List.of(EvaluationError.Type.UNRESOLVABLE_NODES.error(nodesProcessed, nodes.size()));
        return List.of();
    }

}
