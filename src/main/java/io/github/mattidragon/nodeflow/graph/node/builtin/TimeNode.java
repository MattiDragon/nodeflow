package io.github.mattidragon.nodeflow.graph.node.builtin;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

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
    protected Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, ContextProvider context) {
        Level world = context.get(ContextType.WORLD);
        return Either.left(new DataValue<?>[]{
                DataType.NUMBER.makeValue((double) world.getGameTime()),
                DataType.NUMBER.makeValue((double) world.getDefaultClockTime() % 24000L),
                DataType.NUMBER.makeValue((double) world.getGameTime() / 24000L)
        });
    }
}
