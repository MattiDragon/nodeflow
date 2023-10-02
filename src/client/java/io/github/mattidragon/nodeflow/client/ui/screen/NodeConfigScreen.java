package io.github.mattidragon.nodeflow.client.ui.screen;

import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NodeConfigScreen<T extends Node> extends Screen {
    protected final T owner;
    private final EditorScreen parent;

    protected NodeConfigScreen(T owner, EditorScreen parent) {
        super(Text.translatable("node.nodeflow.generic.config.title"));
        this.owner = owner;
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

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

        context.fill(x - 2, 18, width - 18, height - 18, 0x33ffffff);

        if (!texts.isEmpty()) {
            context.drawText(textRenderer, Text.translatable("node.nodeflow.generic.config.errors"), x, 20, 0xff5555, false);
        } else {
            context.drawText(textRenderer, Text.translatable("node.nodeflow.generic.config.no_errors"), x, 20, 0x55ff55, false);
        }

        for (var text : texts) {
            context.drawText(textRenderer, text, x, y, 0xffffff, false);
            y += 9;
        }

        var text = Text.translatable("node.nodeflow.generic.config.title", owner.getName());
        context.drawText(textRenderer, text, (width - 200 - textRenderer.getWidth(text.asOrderedText())) / 2, 10, 0xffffff, false);
    }

    @Override
    public void close() {
        parent.graph.cleanConnections(owner);
        parent.syncGraph();
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
