package io.github.mattidragon.nodeflow.graph.node.builtin.base;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.builtin.SwitchNode;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class TypedNode extends Node {
    protected DataType<?> type;

    protected TypedNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new TypedNode.ConfigScreen(parent);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        type = DataType.REGISTRY.get(new Identifier(data.getString("data_type")));
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("data_type", DataType.REGISTRY.getId(type).toString());
    }

    private class ConfigScreen extends NodeConfigScreen {
        public ConfigScreen(EditorScreen parent) {
            super(TypedNode.this, parent);
        }

        @Override
        protected void init() {
            super.init();
            var x = ((width - 200) / 2) - 50;
            addDrawableChild(CyclingButtonWidget.<DataType<?>>builder(DataType::name)
                    .values(graph.env.allowedDataTypes())
                    .build(x, 70, 100, 20, Text.translatable("node.nodeflow.switch.type"), (button, value) -> type = value))
                    .setValue(type);
        }
    }
}
