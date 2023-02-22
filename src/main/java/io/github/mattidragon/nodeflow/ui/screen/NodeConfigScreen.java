package io.github.mattidragon.nodeflow.ui.screen;

import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NodeConfigScreen extends Screen {
    private final Node owner;
    private final EditorScreen parent;

    protected NodeConfigScreen(Node owner, EditorScreen parent) {
        super(Text.translatable("node.nodeflow.generic.config.title"));
        this.owner = owner;
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        var isGray = new MutableBoolean(false);

        var validation = owner.validate();
        var texts = validation.stream()
                .map(text -> {
                    var formatting = isGray.booleanValue() ? Formatting.GRAY : Formatting.WHITE;
                    isGray.setValue(!isGray.booleanValue());

                    return text.copy().formatted(formatting);
                })
                .flatMap(text -> textRenderer.wrapLines(text, 180).stream())
                .toList();

        var x = width - 200;
        var y = 29;

        fill(matrices, x - 2, 18, width - 18, height - 18, 0x33ffffff);

        if (texts.size() > 0)
            textRenderer.draw(matrices, Text.translatable("node.nodeflow.generic.config.errors"), x, 20, 0xff5555);
        else
            textRenderer.draw(matrices, Text.translatable("node.nodeflow.generic.config.no_errors"), x, 20, 0x55ff55);

        for (var text : texts) {
            textRenderer.draw(matrices, text, x, y, 0xffffff);
            y += 9;
        }

        var text = Text.translatable("node.nodeflow.generic.config.title", owner.getName());
        textRenderer.draw(matrices, text, (width - 200) / 2f - textRenderer.getWidth(text.asOrderedText()) / 2f, 10, 0xffffff);
    }

    @Override
    public void close() {
        parent.graph.cleanConnections(owner);
        parent.syncGraph();
        client.setScreen(parent);
    }
}
