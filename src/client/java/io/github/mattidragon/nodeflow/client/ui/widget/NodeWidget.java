package io.github.mattidragon.nodeflow.client.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class NodeWidget extends ClickableWidget {
    public static final int ROW_HEIGHT = 12;

    public final Node node;
    private final EditorScreen parent;
    private int dragX;
    private int dragY;
    @Nullable
    private Tooltip tooltip;

    public NodeWidget(Node node, EditorScreen parent) {
        super(node.guiX, node.guiY, calcWidth(node, Screens.getTextRenderer(parent)), 24 + 8 + (node.getInputs().length + node.getOutputs().length) * ROW_HEIGHT, node.getName());
        setX(getX() - width / 2);
        setY(getY() - height / 2);

        this.node = node;
        this.parent = parent;

        updateTooltip();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    private static int calcWidth(Node node, TextRenderer renderer) {
        var fieldMax = Stream.concat(Arrays.stream(node.getInputs()), Arrays.stream(node.getOutputs()))
                .map(Connector::id)
                .mapToInt(renderer::getWidth)
                .map(width -> width + 24)
                .max()
                .orElse(0);
        return Math.max(fieldMax, renderer.getWidth(node.getName()) + 32);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Stolen from PressableWidget and tweaked
        if (!this.active || !this.visible) return false;
        if (KeyCodes.isToggle(keyCode)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            var area = parent.getArea();
            area.setContextMenu((int) area.reverseModifyX(getX()), (int) area.reverseModifyY(getY()), this);
            return true;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                setX(getX() - 10);
                updateNodePos();
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                setY(getY() + 10);
                updateNodePos();
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                setY(getY() - 10);
                updateNodePos();
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                setX(getX() + 10);
                updateNodePos();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (parent.isDeletingNode()) {
            parent.removeNode(this);
            return;
        }

        dragX = (int) (getX() - mouseX);
        dragY = (int) (getY() - mouseY);

        if (NodeConfigScreenRegistry.hasConfig(node) && isMouseOnButton(mouseX, mouseY)) {
            MinecraftClient.getInstance().setScreen(NodeConfigScreenRegistry.createScreen(node, parent));
            return;
        }

        for (Segment row : calculateSegments()) {
            if (row.hasConnectorAt(mouseX, mouseY)) {
                parent.connectingConnector = row.connector;
                break;
            }
        }
    }

    private boolean isMouseOnButton(double mouseX, double mouseY) {
        return mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (parent.connectingConnector != null) return;

        setX((int) (mouseX + dragX));
        setY((int) (mouseY + dragY));

        updateNodePos();
    }

    private void updateNodePos() {
        node.guiX = getX() + width / 2;
        node.guiY = getY() + height / 2;
    }

    public void updateTooltip() {
        var tooltip = new ArrayList<Text>();
        var hasError = !node.validate().isEmpty() || !node.isFullyConnected();

        if (NodeConfigScreenRegistry.hasConfig(node))
            tooltip.add(Text.translatable("nodeflow.editor.button.config.tooltip").formatted(Formatting.WHITE));
        if (hasError)
            tooltip.add(Text.translatable("nodeflow.editor.button.config.tooltip.errors").formatted(Formatting.RED));

        if (!node.validate().isEmpty())
            tooltip.add(Text.literal("  ").append(Text.translatable("nodeflow.editor.button.config.tooltip.invalid_config").formatted(Formatting.RED)));
        if (!node.isFullyConnected())
            tooltip.add(Text.literal("  ").append(Text.translatable("nodeflow.editor.button.config.tooltip.not_connected").formatted(Formatting.RED)));

        this.tooltip = Tooltip.of(Texts.join(tooltip, Text.literal("\n")));
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var textRenderer = Screens.getTextRenderer(parent);

        var texture = isFocused() ? NodeFlow.id("node_selected") : NodeFlow.id("node");
        var tagColor = node.tag.getColor();
        RenderSystem.setShaderColor((tagColor >> 16 & 0xff) / 256f, (tagColor >> 8 & 0xff) / 256f, (tagColor & 0xff) / 256f, 1);
        context.drawGuiTexture(texture, getX(), getY(), width, height);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Status indicator / config button
        if (!node.isFullyConnected())
            context.setShaderColor(1, 2 / 3f, 1 / 3f, 1);
        if (!node.validate().isEmpty())
            context.setShaderColor(1, 2 / 3f, 1 / 3f, 1);
        if (NodeConfigScreenRegistry.hasConfig(node) && mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20)
            context.setShaderColor(0.6f, 0.6f, 1, 1);

        if (!node.isFullyConnected() || !node.validate().isEmpty()) {
            context.drawGuiTexture(NodeFlow.id("config_button_error"), getX() + width - 20, getY() + 4, 16, 16);
        } else if (NodeConfigScreenRegistry.hasConfig(node)) {
            context.drawGuiTexture(NodeFlow.id("config_button"), getX() + width - 20, getY() + 4, 16, 16);
        }
        context.setShaderColor(1, 1, 1, 1);

        for (var segment : calculateSegments()) {
            segment.render(context, mouseX, mouseY);
        }

        context.drawText(textRenderer, getMessage(), getX() + 7, getY() + 7, 0x404040, false);

        if (isMouseOnButton(mouseX, mouseY) && tooltip != null) {
            tooltip.render(hovered, isFocused(), new ScreenRect(getX() + width - 20, getY() + 4, 16, 16));
        }
    }

    @Override
    public Text getMessage() {
        return node.getName();
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY);
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        var area = parent.getArea();
        if (area.reverseModifyX(getX() + width) < area.x ||
            area.reverseModifyY(getY() + height) < area.y ||
            area.reverseModifyX(getX()) > area.x + area.width ||
            area.reverseModifyY(getY()) > area.y + area.height) return null;

        return super.getNavigationPath(navigation);
    }

    public EditorScreen getParent() {
        return parent;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        var area = parent.getArea();
        return new ScreenRect((int) area.reverseModifyX(this.getX()),
                (int) area.reverseModifyY(this.getY()),
                (int) area.reverseModifyDeltaX(this.getWidth()),
                (int) area.reverseModifyDeltaY(this.getHeight()));
    }

    public void updateWidth() {
        this.width = calcWidth(node, Screens.getTextRenderer(parent));
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

        public void render(DrawContext context, int mouseX, int mouseY) {
            var textRenderer = Screens.getTextRenderer(parent);

            var brightness = hasConnectorAt(mouseX, mouseY) ? 2 : 1;
            var color = this.connector.type().color();
            var red = ((color & 0xff0000) >> 16) / 256f * brightness;
            var green = ((color & 0x00ff00) >> 8) / 256f * brightness;
            var blue = (color & 0x0000ff) / 256f * brightness;

            context.setShaderColor(red, green, blue, 1);
            context.drawGuiTexture(NodeFlow.id("connector"), getConnectorX(), getConnectorY(), 4, 4);
            context.setShaderColor(1, 1, 1, 1);

            if (!isOutput)
                context.drawText(textRenderer, this.connector.id(), x + 16, y + 2, 0x404040, false);
            else
                context.drawText(textRenderer, this.connector.id(), x + width - 16 - textRenderer.getWidth(this.connector.id()), y + 2, 0x404040, false);
        }
    }
}
