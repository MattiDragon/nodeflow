package io.github.mattidragon.nodeflow.graph.node.builtin.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConstantNode extends Node {
    private final DataValue<?> value;

    public ConstantNode(NodeType<?> type, Graph graph, DataValue<?> value) {
        super(type, List.of(), graph);
        this.value = value;
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { value.type().makeRequiredOutput("value", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.<DataValue<?>[], Component>left(new DataValue[]{value});
    }
}
