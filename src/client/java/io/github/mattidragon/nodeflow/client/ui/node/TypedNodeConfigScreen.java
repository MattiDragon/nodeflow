package io.github.mattidragon.nodeflow.client.ui.node;

import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.builtin.base.TypedNode;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

public class TypedNodeConfigScreen extends NodeConfigScreen<TypedNode> {
    public TypedNodeConfigScreen(TypedNode owner, EditorScreen parent) {
        super(owner, parent);
    }

    @Override
    protected void init() {
        super.init();
        var x = ((width - 200) / 2) - 50;
        addRenderableWidget(CycleButton.<DataType<?>>builder(DataType::name)
                .withValues(owner.getGraph().env.allowedDataTypes())
                .create(x, 70, 100, 20, Component.translatable("node.nodeflow.switch.type"), (button, type) -> owner.setType(type)))
                .setValue(owner.getType());
    }
}
