package io.github.mattidragon.nodeflow.client.ui.widget;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.graph.node.NodeTag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ContextMenuWidget extends AbstractParentElement implements Drawable {
    private final EditorAreaWidget area;
    public NodeWidget node;
    public final List<ClickableWidget> widgets = new ArrayList<>();

    public ContextMenuWidget(EditorAreaWidget area) {
        this.area = area;
    }

    public void show(int x, int y, @Nullable NodeWidget node) {
        hide();

        widgets.add(ButtonWidget.builder(ScreenTexts.CANCEL, __ -> hide()).size(100, 12).build());
        if (node != null) {
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.duplicate"), __ -> area.duplicateNode())
                    .size(100, 12)
                    .build());
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.delete"), __ -> area.deleteNode())
                    .size(100, 12)
                    .build());
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.copy"), __ -> area.copyNode())
                    .size(100, 12)
                    .build());
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.paste"), __ -> area.pasteNode(x, y))
                    .size(100, 12)
                    .build());
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.cut"), __ -> area.cutNode())
                    .size(100, 12)
                    .build());
            if (NodeConfigScreenRegistry.hasConfig(node.node)) {
                widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.configure"), __ -> area.configureNode())
                        .size(100, 12)
                        .build());
            }
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.tag"), __ -> setupTagging())
                    .size(100, 12)
                    .build());
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.name"), __ -> setupNaming())
                    .size(100, 12)
                    .build());
        } else {
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.paste"), __ -> area.pasteNode(x, y))
                    .size(100, 12)
                    .build());
        }
        this.node = node;

        positionWidgets(x, y);
        area.setFocused(this);
    }

    private void positionWidgets(int x, int y) {
        var totalHeight = widgets.stream().mapToInt(ClickableWidget::getHeight).sum();
        var screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        var currentY = Math.min(y, screenHeight - totalHeight - 10);
        for (var button : widgets) {
            button.setX(x);
            button.setY(currentY);
            currentY += button.getHeight();
        }

        if (!widgets.isEmpty()) {
            this.setFocused(widgets.get(0));
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
        var x = widgets.get(0).getX();
        var y = widgets.get(0).getY();
        widgets.clear();
        widgets.add(ButtonWidget.builder(ScreenTexts.BACK, __ -> show(x, y, node))
                .size(100, 12)
                .build());

        for (var tag : NodeTag.values()) {
            var text = Text.empty().append(Text.literal("■ ").setStyle(Style.EMPTY.withColor(tag.getColor()))).append(Text.translatable("nodeflow.editor.node_tag." + tag.asString()));
            widgets.add(ButtonWidget.builder(text, __ -> area.tagNode(tag))
                    .size(100, 12)
                    .build());
        }
        positionWidgets(x, y);
    }

    private void setupNaming() {
        if (widgets.isEmpty()) {
            NodeFlow.LOGGER.warn("Tried to setup naming without showing context menu");
            return;
        }
        var x = widgets.get(0).getX();
        var y = widgets.get(0).getY();
        widgets.clear();
        widgets.add(ButtonWidget.builder(ScreenTexts.BACK, __ -> show(x, y, node))
                .size(100, 12)
                .build());
        if (node.node.nickname != null) {
            widgets.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.clear_name"), __ -> area.renameNode(null))
                    .size(100, 12)
                    .build());
        }
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        var textField = new TextFieldWidget(textRenderer, 100, 20, Text.empty());
        textField.setPlaceholder(Text.translatable("nodeflow.editor.button.nick_placeholder").formatted(Formatting.GRAY));
        textField.setMaxLength(16);
        if (node.node.nickname != null) {
            textField.setText(node.node.nickname);
        }
        widgets.add(textField);

        widgets.add(ButtonWidget.builder(ScreenTexts.DONE, __ -> area.renameNode(textField.getText()))
                .size(100, 12)
                .build());
        positionWidgets(x, y);
    }

    public boolean isVisible() {
        return !children().isEmpty();
    }

    @Override
    public List<? extends Element> children() {
        return widgets;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (var button : widgets) {
            button.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (widgets.isEmpty()) return false;

        if (!super.mouseClicked(mouseX, mouseY, button)) {
            // Hide if clicked outside
            hide();
        }
        // Either one of the children consumed the click or we close
        return true;
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        var path = super.getNavigationPath(navigation);
        if (path == null) hide();
        return path;
    }
}
