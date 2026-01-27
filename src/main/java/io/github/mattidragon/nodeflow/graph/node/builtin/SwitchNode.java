package io.github.mattidragon.nodeflow.graph.node.builtin;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.builtin.base.TypedNode;
import java.util.List;
import net.minecraft.network.chat.Component;

public class SwitchNode extends TypedNode {
    public SwitchNode(Graph graph) {
        super(NodeType.SWITCH, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[]{ type.makeRequiredOutput("result", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[]{
                DataType.BOOLEAN.makeRequiredInput("isFirst", this),
                type.makeRequiredInput("first", this),
                type.makeRequiredInput("second", this)
        };
    }

    @Override
    protected Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.left(new DataValue<?>[]{ inputs[0].getAs(DataType.BOOLEAN) ? inputs[1] : inputs[2] });
    }
}
