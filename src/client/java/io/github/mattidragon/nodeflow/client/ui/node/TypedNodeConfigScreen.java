package io.github.mattidragon.nodeflow.client.ui.node;

import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.builtin.base.TypedNode;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class TypedNodeConfigScreen extends NodeConfigScreen<TypedNode> {
    public TypedNodeConfigScreen(TypedNode owner, EditorScreen parent) {
        super(owner, parent);
    }

    @Override
    protected void init() {
        super.init();
        var x = ((width - 200) / 2) - 50;
        addDrawableChild(CyclingButtonWidget.<DataType<?>>builder(DataType::name)
                .values(owner.getGraph().env.allowedDataTypes())
                .build(x, 70, 100, 20, Text.translatable("node.nodeflow.switch.type"), (button, type) -> owner.setType(type)))
                .setValue(owner.getType());
    }
}
