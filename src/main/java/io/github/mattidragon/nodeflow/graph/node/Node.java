package io.github.mattidragon.nodeflow.graph.node;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class Node {
    public UUID id = UUID.randomUUID();
    public final NodeType<?> type;
    public final List<ContextType<?>> contexts;
    public int guiX = 0;
    public int guiY = 0;
    public NodeTag tag = NodeTag.WHITE;
    @Nullable
    public String nickname = null;
    protected final Graph graph;

    protected Node(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        this.type = type;
        this.contexts = contexts;
        this.graph = graph;
    }

    public abstract Connector<?>[] getOutputs();
    public abstract Connector<?>[] getInputs();

    public Graph getGraph() {
        return graph;
    }

    public final Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, Context context) {
        return process(inputs, new ContextProvider(context));
    }

    protected abstract Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context);

    /**
     * Validates this nodes configuration as well as possible.
     *
     * @return A list of all errors detected, may only contain some errors as long as others appear once they are fixed.
     */
    public List<Text> validate() {
        return List.of();
    }

    public final boolean isFullyConnected() {
        for (var input : getInputs()) {
            if (!input.isOptional() && graph.getConnections(input).isEmpty())
                return false;
        }

        for (var output : getOutputs()) {
            if (!output.isOptional() && graph.getConnections(output).isEmpty())
                return false;
        }

        return true;
    }

    public void readNbt(NbtCompound data) {
        if (data.containsUuid("id")) // Default to old/random
            id = data.getUuid("id");
        guiX = data.getInt("guiX");
        guiY = data.getInt("guiY");
        tag = NodeTag.fromString(data.getString("tag"));
        if (data.contains("nickname"))
            nickname = data.getString("nickname");
    }

    public void writeNbt(NbtCompound data) {
        data.putString("type", NodeType.REGISTRY.getId(type).toString());
        data.putUuid("id", id);
        data.putInt("guiX", guiX);
        data.putInt("guiY", guiY);
        data.putString("tag", tag.asString());
        if (nickname != null)
            data.putString("nickname", nickname);
    }

    public final Text getName() {
        return type.name();
    }

    protected final class ContextProvider {
        private final Context context;

        private ContextProvider(Context context) {
            this.context = context;
        }

        public <T> T get(ContextType<T> type) {
            if (!contexts.contains(type))
                throw new IllegalStateException("Node tried to use context it doesn't require");
            return context.get(type);
        }
    }
}
