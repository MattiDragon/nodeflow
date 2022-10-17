package io.github.mattidragon.nodeflow.graph.node.builtin;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

public class TimeNode extends Node {
    private final Connector<?>[] outputs = {
            DataType.NUMBER.makeOptionalOutput("gametime", this),
            DataType.NUMBER.makeOptionalOutput("daytime", this),
            DataType.NUMBER.makeOptionalOutput("day", this)
    };

    public TimeNode(Graph graph) {
        super(NodeType.TIME, List.of(ContextType.WORLD), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return outputs;
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        World world = context.get(ContextType.WORLD);
        return Either.left(new DataValue<?>[]{
                DataType.NUMBER.makeValue((double) world.getTime()),
                DataType.NUMBER.makeValue((double) world.getTimeOfDay() % 24000L),
                DataType.NUMBER.makeValue((double) world.getTime() / 24000L)
        });
    }
}
