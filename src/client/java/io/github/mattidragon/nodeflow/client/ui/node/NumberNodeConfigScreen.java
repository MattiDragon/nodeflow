package io.github.mattidragon.nodeflow.client.ui.node;

import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.node.builtin.NumberNode;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NumberNodeConfigScreen extends NodeConfigScreen<NumberNode> {
    public NumberNodeConfigScreen(NumberNode node, EditorScreen parent) {
        super(node, parent);
    }

    @Override
    protected void init() {
        super.init();
        var x = ((width - 200) / 2) - 50;
        var field = addDrawableChild(new TextFieldWidget(textRenderer, x, 70, 100, 20, Text.empty()));
        field.setChangedListener(owner::setValue);
        field.setText(owner.getValue());
    }
}
