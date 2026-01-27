package io.github.mattidragon.nodeflow.graph.node.builtin;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SendNumberNode extends Node {
    private final Connector<?>[] inputs = new Connector[] { DataType.NUMBER.makeRequiredInput("input", this) };

    public SendNumberNode(Graph graph, NodeType<SendNumberNode> type) {
        super(type, List.of(ContextType.SERVER_LEVEL, ContextType.BLOCK_POS), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[0];
    }

    @Override
    public Connector<?>[] getInputs() {
        return inputs;
    }

    @Override
    protected Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, ContextProvider context) {
        context.get(ContextType.SERVER_LEVEL).getPlayers(player -> context.get(ContextType.BLOCK_POS).distToCenterSqr(player.position()) < 16 * 16).forEach(player ->
                player.sendSystemMessage(Component.translatable("node.nodeflow.broadcast.message", inputs[0].getAs(DataType.NUMBER))));

        return Either.left(new DataValue<?>[0]);
    }
}
