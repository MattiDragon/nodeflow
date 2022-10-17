package io.github.mattidragon.nodeflow.graph.node.builtin;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.List;

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
    public List<Text> validate() {
        try {
            Double.parseDouble(value);
            return List.of();
        } catch (NumberFormatException e) {
            return List.of(Text.translatable("node.nodeflow.number.invalid", value));
        }
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        return Either.<DataValue<?>[], Text>left(new DataValue[]{DataType.NUMBER.makeValue(Double.valueOf(value))});
    }

    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new ConfigScreen(parent);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        value = data.getString("value");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("value", value);
    }

    private class ConfigScreen extends NodeConfigScreen {
        public ConfigScreen(EditorScreen parent) {
            super(NumberNode.this, parent);
        }

        @Override
        protected void init() {
            super.init();
            var x = ((width - 200) / 2) - 50;
            var field = addDrawableChild(new TextFieldWidget(textRenderer, x, 70, 100, 20, Text.empty()));
            field.setChangedListener((value) -> NumberNode.this.value = (value));
            field.setText(value);
        }
    }
}
