package io.github.mattidragon.nodeflow.graph.node.builtin.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;

public class UnaryOperationNode<T, R> extends Node {
    private final DataType<T> inputType;
    private final DataType<R> resultType;
    private final Function<T, R> function;

    public UnaryOperationNode(NodeType<UnaryOperationNode<T, R>> type, Graph graph, DataType<T> inputType, DataType<R> resultType, Function<T, R> function) {
        super(type, List.of(), graph);
        this.inputType = inputType;
        this.resultType = resultType;
        this.function = function;
    }

    public static <T, R> NodeType<UnaryOperationNode<T, R>> makeType(DataType<T> inputType, DataType<R> resultType, Function<T, R> function) {
        // avoid reference errors with holder object (can't use array due to generics)
        var type = new Object() {
            NodeType<UnaryOperationNode<T, R>> type;
        };
        return type.type = new NodeType<>(graph -> new UnaryOperationNode<>(type.type, graph, inputType, resultType, function));
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { resultType.makeRequiredOutput("result", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { inputType.makeRequiredInput("input", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.<DataValue<?>[], Text>left(new DataValue[]{resultType.makeValue(function.apply(inputs[0].getAs(inputType)))});
    }
}
