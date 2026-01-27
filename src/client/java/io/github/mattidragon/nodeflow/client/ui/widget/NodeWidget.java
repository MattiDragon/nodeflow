package io.github.mattidragon.nodeflow.client.ui.widget;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class NodeWidget extends AbstractWidget {
    public static final int ROW_HEIGHT = 12;

    public final Node node;
    private final EditorScreen parent;
    private int dragX;
    private int dragY;

    public NodeWidget(Node node, EditorScreen parent) {
        super(node.guiX, node.guiY, calcWidth(node, parent.getFont()), 24 + 8 + (node.getInputs().length + node.getOutputs().length) * ROW_HEIGHT, node.getName());
        setX(getX() - width / 2);
        setY(getY() - height / 2);

        this.node = node;
        this.parent = parent;

        updateTooltip();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }

    private static int calcWidth(Node node, Font renderer) {
        var fieldMax = Stream.concat(Arrays.stream(node.getInputs()), Arrays.stream(node.getOutputs()))
                .map(Connector::id)
                .mapToInt(renderer::width)
                .map(width -> width + 24)
                .max()
                .orElse(0);
        return Math.max(fieldMax, renderer.width(node.getName()) + 32);
    }

    public Segment[] calculateSegments() {
        Segment[] segments = new Segment[node.getInputs().length + node.getOutputs().length];
        var i = 0;

        for (var input : node.getInputs())
            segments[i++] = new Segment(getX(), getY() + 12 + i * ROW_HEIGHT, false, input);
        for (var output : node.getOutputs())
            segments[i++] = new Segment(getX(), getY() + 12 + i * ROW_HEIGHT, true, output);

        return segments;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Stolen from PressableWidget and tweaked
        if (!this.active || !this.visible) return false;
        if (event.isSelection()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            var area = parent.getArea();
            area.setContextMenu((int) area.reverseModifyX(getX()), (int) area.reverseModifyY(getY()), this);
            return true;
        }

        if (event.isLeft()) {
            setX(getX() - 10);
            updateNodePos();
            return true;
        } else if (event.isRight()) {
            setX(getX() + 10);
            updateNodePos();
            return true;
        } else if (event.isUp()) {
            setY(getY() - 10);
            updateNodePos();
            return true;
        } else if (event.isDown()) {
            setY(getY() + 10);
            updateNodePos();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (parent.isDeletingNode()) {
            parent.removeNode(this);
            return;
        }

        dragX = (int) (getX() - event.x());
        dragY = (int) (getY() - event.y());

        if (NodeConfigScreenRegistry.hasConfig(node) && isMouseOnButton(event.x(), event.y())) {
            Minecraft.getInstance().setScreen(NodeConfigScreenRegistry.createScreen(node, parent));
            return;
        }

        for (Segment row : calculateSegments()) {
            if (row.hasConnectorAt(event.x(), event.y())) {
                parent.connectingConnector = row.connector;
                break;
            }
        }
    }

    private boolean isMouseOnButton(double mouseX, double mouseY) {
        return mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20;
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (parent.connectingConnector != null) return;

        setX((int) (event.x() + dragX));
        setY((int) (event.y() + dragY));

        updateNodePos();
    }

    private void updateNodePos() {
        node.guiX = getX() + width / 2;
        node.guiY = getY() + height / 2;
    }

    public void updateTooltip() {
        var tooltip = new ArrayList<Component>();
        var hasError = !node.validate().isEmpty() || !node.isFullyConnected();

        if (NodeConfigScreenRegistry.hasConfig(node))
            tooltip.add(Component.translatable("nodeflow.editor.button.config.tooltip").withStyle(ChatFormatting.WHITE));
        if (hasError)
            tooltip.add(Component.translatable("nodeflow.editor.button.config.tooltip.errors").withStyle(ChatFormatting.RED));

        if (!node.validate().isEmpty())
            tooltip.add(Component.literal("  ").append(Component.translatable("nodeflow.editor.button.config.tooltip.invalid_config").withStyle(ChatFormatting.RED)));
        if (!node.isFullyConnected())
            tooltip.add(Component.literal("  ").append(Component.translatable("nodeflow.editor.button.config.tooltip.not_connected").withStyle(ChatFormatting.RED)));

         setTooltip(Tooltip.create(ComponentUtils.formatList(tooltip, Component.literal("\n"))));
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        var font = parent.getFont();

        var texture = isFocused() ? NodeFlow.id("node_selected") : NodeFlow.id("node");
        var tagColor = node.tag.getColor();
        context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), width, height, tagColor | 0xff000000);

        var color = 0xffffffff;
        // Status indicator / config button
        if (!node.isFullyConnected())
            color = 0xffffaa55;
        if (!node.validate().isEmpty())
            color = 0xffffaa55;
        if (NodeConfigScreenRegistry.hasConfig(node) && isMouseOver(mouseX, mouseY))
            color = 0xff9999ff;

        if (!node.isFullyConnected() || !node.validate().isEmpty()) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, NodeFlow.id("config_button_error"), getX() + width - 20, getY() + 4, 16, 16, color);
        } else if (NodeConfigScreenRegistry.hasConfig(node)) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, NodeFlow.id("config_button"), getX() + width - 20, getY() + 4, 16, 16, color);
        }

        for (var segment : calculateSegments()) {
            segment.render(context, mouseX, mouseY);
        }

        context.drawString(font, getMessage(), getX() + 7, getY() + 7, 0xff404040, false);

        // Used to hide tooltip
        if (!isMouseOnButton(mouseX, mouseY)) {
            isHovered = false;
        }
    }

    @Override
    public Component getMessage() {
        return node.getName();
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        var area = parent.getArea();
        if (area.reverseModifyX(getX() + width) < area.x ||
            area.reverseModifyY(getY() + height) < area.y ||
            area.reverseModifyX(getX()) > area.x + area.width ||
            area.reverseModifyY(getY()) > area.y + area.height) return null;

        return super.nextFocusPath(navigation);
    }

    public EditorScreen getParent() {
        return parent;
    }

    @Override
    public ScreenRectangle getRectangle() {
        var area = parent.getArea();
        return new ScreenRectangle((int) area.reverseModifyX(this.getX()),
                (int) area.reverseModifyY(this.getY()),
                (int) area.reverseModifyDeltaX(this.getWidth()),
                (int) area.reverseModifyDeltaY(this.getHeight()));
    }

    public void updateWidth() {
        this.width = calcWidth(node, parent.getFont());
    }

    public class Segment {
        public final int x;
        public final int y;
        public final Connector<?> connector;
        public final boolean isOutput;

        public Segment(int x, int y, boolean isOutput, Connector<?> connector) {
            this.x = x;
            this.y = y;
            this.isOutput = isOutput;
            this.connector = connector;
        }

        public int getConnectorX() {
            return isOutput ? x + width - 12 : x + 8;
        }

        public int getConnectorY() {
            return y + 4;
        }

        public boolean hasConnectorAt(double mouseX, double mouseY) {
            return mouseX > getConnectorX() - 2 && mouseX < getConnectorX() + 6 && mouseY > getConnectorY() - 2 && mouseY < getConnectorY() + 6;
        }

        public void render(GuiGraphics context, int mouseX, int mouseY) {
            var font = parent.getFont();

            var brightness = hasConnectorAt(mouseX, mouseY) ? 2 : 1;
            var color = this.connector.type().color();
            color = 0xff000000 |
                    Math.min((color >> 16 & 0xff) * brightness, 0xff) << 16 |
                    Math.min((color >> 8 & 0xff) * brightness, 0xff) << 8 |
                    Math.min((color & 0xff) * brightness, 0xff);

            context.blitSprite(RenderPipelines.GUI_TEXTURED, NodeFlow.id("connector"), getConnectorX(), getConnectorY(), 4, 4, color);

            if (!isOutput)
                context.drawString(font, this.connector.id(), x + 16, y + 2, 0xff404040, false);
            else
                context.drawString(font, this.connector.id(), x + width - 16 - font.width(this.connector.id()), y + 2, 0xff404040, false);
        }
    }
}
