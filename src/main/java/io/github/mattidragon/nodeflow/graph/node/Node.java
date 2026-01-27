package io.github.mattidragon.nodeflow.graph.node;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    public final Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, Context context) {
        return process(inputs, new ContextProvider(context));
    }

    protected abstract Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, ContextProvider context);

    /**
     * Validates this nodes configuration as well as possible.
     *
     * @return A list of all errors detected, may only contain some errors as long as others appear once they are fixed.
     */
    public List<Component> validate() {
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

    public void readData(ValueInput view) {
        view.read("id", UUIDUtil.AUTHLIB_CODEC).ifPresent(newId -> id = newId);
        guiX = view.getIntOr("guiX", 0);
        guiY = view.getIntOr("guiY", 0);
        tag = NodeTag.fromString(view.getStringOr("tag", ""));
        nickname = view.getStringOr("nickname", nickname);
    }

    public void writeData(ValueOutput view) {
        view.putString("type", NodeType.REGISTRY.getKey(type).toString());
        view.store("id", UUIDUtil.AUTHLIB_CODEC, id);
        view.putInt("guiX", guiX);
        view.putInt("guiY", guiY);
        view.putString("tag", tag.asString());
        if (nickname != null)
            view.putString("nickname", nickname);
    }

    public final Component getName() {
        if (nickname != null) {
            return Component.literal(nickname).withStyle(ChatFormatting.ITALIC);
        }
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
