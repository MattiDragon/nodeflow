package io.github.mattidragon.nodeflow.client.ui.widget;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.graph.node.NodeTag;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextMenuWidget extends AbstractContainerEventHandler implements Renderable {
    private final EditorAreaWidget area;
    public @Nullable NodeWidget node;
    public final List<AbstractWidget> widgets = new ArrayList<>();

    public ContextMenuWidget(EditorAreaWidget area) {
        this.area = area;
    }

    public void show(int x, int y, @Nullable NodeWidget node) {
        hide();

        widgets.add(Button.builder(CommonComponents.GUI_CANCEL, _ -> hide()).size(100, 12).build());
        if (node != null) {
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.duplicate"), _ -> area.duplicateNode())
                    .size(100, 12)
                    .build());
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.delete"), _ -> area.deleteNode())
                    .size(100, 12)
                    .build());
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.copy"), _ -> area.copyNode())
                    .size(100, 12)
                    .build());
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.paste"), _ -> area.pasteNode(x, y))
                    .size(100, 12)
                    .build());
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.cut"), _ -> area.cutNode())
                    .size(100, 12)
                    .build());
            if (NodeConfigScreenRegistry.hasConfig(node.node)) {
                widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.configure"), _ -> area.configureNode())
                        .size(100, 12)
                        .build());
            }
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.tag"), _ -> setupTagging())
                    .size(100, 12)
                    .build());
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.name"), _ -> setupNaming())
                    .size(100, 12)
                    .build());
        } else {
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.paste"), _ -> area.pasteNode(x, y))
                    .size(100, 12)
                    .build());
        }
        this.node = node;

        positionWidgets(x, y);
        area.setFocused(this);
    }

    private void positionWidgets(int x, int y) {
        var totalHeight = widgets.stream().mapToInt(AbstractWidget::getHeight).sum();
        var screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        var currentY = Math.min(y, screenHeight - totalHeight - 10);
        for (var button : widgets) {
            button.setX(x);
            button.setY(currentY);
            currentY += button.getHeight();
        }

        if (!widgets.isEmpty()) {
            this.setFocused(widgets.getFirst());
        }
    }

    public void hide() {
        widgets.clear();
    }

    private void setupTagging() {
        if (widgets.isEmpty()) {
            NodeFlow.LOGGER.warn("Tried to setup tagging without showing context menu");
            return;
        }
        var x = widgets.getFirst().getX();
        var y = widgets.getFirst().getY();
        widgets.clear();
        widgets.add(Button.builder(CommonComponents.GUI_BACK, _ -> show(x, y, node))
                .size(100, 12)
                .build());

        for (var tag : NodeTag.values()) {
            var text = Component.empty().append(Component.literal("■ ").setStyle(Style.EMPTY.withColor(tag.getColor()))).append(Component.translatable("nodeflow.editor.node_tag." + tag.asString()));
            widgets.add(Button.builder(text, _ -> area.tagNode(tag))
                    .size(100, 12)
                    .build());
        }
        positionWidgets(x, y);
    }

    private void setupNaming() {
        if (widgets.isEmpty() || node == null) {
            NodeFlow.LOGGER.warn("Tried to setup naming without showing context menu");
            return;
        }
        var x = widgets.getFirst().getX();
        var y = widgets.getFirst().getY();
        widgets.clear();
        widgets.add(Button.builder(CommonComponents.GUI_BACK, _ -> show(x, y, node))
                .size(100, 12)
                .build());
        if (node.node.nickname != null) {
            widgets.add(Button.builder(Component.translatable("nodeflow.editor.button.clear_name"), _ -> area.renameNode(null))
                    .size(100, 12)
                    .build());
        }
        var textRenderer = Minecraft.getInstance().font;
        var textField = new EditBox(textRenderer, 100, 20, Component.empty());
        textField.setHint(Component.translatable("nodeflow.editor.button.nick_placeholder").withStyle(ChatFormatting.GRAY));
        textField.setMaxLength(16);
        if (node.node.nickname != null) {
            textField.setValue(node.node.nickname);
        }
        widgets.add(textField);

        widgets.add(Button.builder(CommonComponents.GUI_DONE, _ -> area.renameNode(textField.getValue()))
                .size(100, 12)
                .build());
        positionWidgets(x, y);
    }

    public boolean isVisible() {
        return !children().isEmpty();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return widgets;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        for (var button : widgets) {
            button.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (widgets.isEmpty()) return false;

        if (!super.mouseClicked(event, doubleClick)) {
            // Hide if clicked outside
            hide();
        }
        // Either one of the children consumed the click or we close
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (var widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        var path = super.nextFocusPath(navigation);
        if (path == null) hide();
        return path;
    }
}
