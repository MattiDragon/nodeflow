package io.github.mattidragon.nodeflow.graph.node.builtin.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.text.Text;

import java.util.List;

public class ConstantNode extends Node {
    private final DataValue<?> value;

    private ConstantNode(NodeType<?> type, Graph graph, DataValue<?> value) {
        super(type, List.of(), graph);
        this.value = value;
    }

    public static NodeType<ConstantNode> makeType(DataValue<?> value) {
        // avoid reference errors with holder object (can't use array due to generics)
        var type = new Object() {
            NodeType<ConstantNode> type;
        };
        return type.type = new NodeType<>(graph -> new ConstantNode(type.type, graph, value));
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
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.<DataValue<?>[], Text>left(new DataValue[]{value});
    }
}
