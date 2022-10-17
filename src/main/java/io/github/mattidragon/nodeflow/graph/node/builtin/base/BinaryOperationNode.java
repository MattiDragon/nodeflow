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
import java.util.function.BiFunction;

public class BinaryOperationNode<T, R> extends Node {
    private final DataType<T> inputType;
    private final DataType<R> resultType;
    private final BiFunction<T, T, R> function;

    public BinaryOperationNode(NodeType<BinaryOperationNode<T, R>> type, Graph graph, DataType<T> inputType, DataType<R> resultType, BiFunction<T, T, R> function) {
        super(type, List.of(), graph);
        this.inputType = inputType;
        this.resultType = resultType;
        this.function = function;
    }

    public static <T, R> NodeType<BinaryOperationNode<T, R>> makeType(DataType<T> inputType, DataType<R> resultType, BiFunction<T, T, R> function) {
        // avoid reference errors with holder object (can't use array due to generics)
        var type = new Object() {
            NodeType<BinaryOperationNode<T, R>> type;
        };
        return type.type = new NodeType<>(graph -> new BinaryOperationNode<>(type.type, graph, inputType, resultType, function));
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { resultType.makeRequiredOutput("result", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] {
                inputType.makeRequiredInput("first", this),
                inputType.makeRequiredInput("second", this)
        };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.<DataValue<?>[], Text>left(new DataValue[]{resultType.makeValue(function.apply(inputs[0].getAs(inputType), inputs[1].getAs(inputType)))});
    }
}
