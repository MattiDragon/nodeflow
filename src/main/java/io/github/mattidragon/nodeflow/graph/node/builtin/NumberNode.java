package io.github.mattidragon.nodeflow.graph.node.builtin;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NumberNode extends Node {
    // We use string so we can represent error states
    private String value = "";

    public NumberNode(Graph graph) {
        super(NodeType.NUMBER, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] {DataType.NUMBER.makeRequiredOutput("value", this)};
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    public List<Component> validate() {
        try {
            Double.parseDouble(value);
            return List.of();
        } catch (NumberFormatException e) {
            return List.of(Component.translatable("node.nodeflow.number.invalid", value));
        }
    }

    @Override
    protected Either<DataValue<?>[], Component> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.left(new DataValue<?>[]{DataType.NUMBER.makeValue(Double.valueOf(value))});
    }

    public boolean hasConfig() {
        return true;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void readData(ValueInput view) {
        super.readData(view);
        value = view.getStringOr("value", "");
    }

    @Override
    public void writeData(ValueOutput view) {
        super.writeData(view);
        view.putString("value", value);
    }
}
