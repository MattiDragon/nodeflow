package io.github.mattidragon.nodeflow.client.ui.screen;

import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NodeConfigScreen<T extends Node> extends Screen {
    protected final T owner;
    private final EditorScreen parent;

    protected NodeConfigScreen(T owner, EditorScreen parent) {
        super(Component.translatable("node.nodeflow.generic.config.title"));
        this.owner = owner;
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        var isGray = new MutableBoolean(false);

        var validation = owner.validate();
        var texts = validation.stream()
                .map(text -> {
                    var formatting = isGray.booleanValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE;
                    isGray.setValue(!isGray.booleanValue());

                    return text.copy().withStyle(formatting);
                })
                .flatMap(text -> font.split(text, 180).stream())
                .toList();

        var x = width - 200;
        var y = 29;

        context.fill(x - 2, 18, width - 18, height - 18, 0x33ffffff);

        if (!texts.isEmpty()) {
            context.drawString(font, Component.translatable("node.nodeflow.generic.config.errors"), x, 20, 0xffff5555, false);
        } else {
            context.drawString(font, Component.translatable("node.nodeflow.generic.config.no_errors"), x, 20, 0xff55ff55, false);
        }

        for (var text : texts) {
            context.drawString(font, text, x, y, 0xffffffff, false);
            y += 9;
        }

        var text = Component.translatable("node.nodeflow.generic.config.title", owner.getName());
        context.drawString(font, text, (width - 200 - font.width(text.getVisualOrderText())) / 2, 10, 0xffffffff, false);
    }

    @Override
    public void onClose() {
        parent.graph.cleanConnections(owner);
        parent.syncGraph();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
