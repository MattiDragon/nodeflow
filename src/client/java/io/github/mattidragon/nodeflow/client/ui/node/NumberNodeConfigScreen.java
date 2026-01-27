package io.github.mattidragon.nodeflow.client.ui.node;

import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.node.builtin.NumberNode;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class NumberNodeConfigScreen extends NodeConfigScreen<NumberNode> {
    public NumberNodeConfigScreen(NumberNode node, EditorScreen parent) {
        super(node, parent);
    }

    @Override
    protected void init() {
        super.init();
        var x = ((width - 200) / 2) - 50;
        var field = addRenderableWidget(new EditBox(font, x, 70, 100, 20, Component.empty()));
        field.setResponder(owner::setValue);
        field.setValue(owner.getValue());
    }
}
