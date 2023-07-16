package io.github.mattidragon.nodeflow.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
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
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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

        if (node.hasConfig() && mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20) {
            MinecraftClient.getInstance().setScreen(node.createConfigScreen(parent));
            return;
        }

        for (Segment row : calculateSegments()) {
            if (row.hasConnectorAt(mouseX, mouseY)) {
                parent.connectingConnector = row.connector;
                break;
            }
        }
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

        if (node.hasConfig())
            tooltip.add(Text.translatable("nodeflow.editor.button.config.tooltip").formatted(Formatting.WHITE));
        if (hasError)
            tooltip.add(Text.translatable("nodeflow.editor.button.config.tooltip.errors").formatted(Formatting.RED));

        if (!node.validate().isEmpty())
            tooltip.add(Text.literal("  ").append(Text.translatable("nodeflow.editor.button.config.tooltip.invalid_config").formatted(Formatting.RED)));
        if (!node.isFullyConnected())
            tooltip.add(Text.literal("  ").append(Text.translatable("nodeflow.editor.button.config.tooltip.not_connected").formatted(Formatting.RED)));

        setTooltip(Tooltip.of(Texts.join(tooltip, Text.literal("\n"))));
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        var textRenderer = Screens.getTextRenderer(parent);
        var matrix = context.getMatrices().peek().getPositionMatrix();
        var segments = calculateSegments();

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, parent.texture);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        var uvOffset = this.isFocused() ? 64 : 32;
        var columnCount = width / 16 - 1;
        // Top bar
        for (int i = 0; i < columnCount; i++) {
            addQuad(matrix, getX() + 8 + i * 16, getY(), uvOffset + 8, 0, 16, 24);
        }
        addQuad(matrix, getX() + 8 + columnCount * 16, getY(), uvOffset + 8, 0, width - (16 + columnCount * 16), 24);

        // Top corners
        addQuad(matrix, getX(), getY(), uvOffset, 0, 8, 24);
        addQuad(matrix, getX() + width - 8, getY(), uvOffset + 24, 0, 8, 24);

        var status = 0xffffffff;
        if (!node.isFullyConnected())
            status = 0xffffaa55;
        if (!node.validate().isEmpty())
            status = 0xffffaa55;
        if (node.hasConfig() && mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20)
            status = 0xff9999ff;

        // Status indicator / config button
        if (!node.isFullyConnected() || !node.validate().isEmpty()) {
            addQuad(matrix, getX() + width - 20, getY() + 4, 112, 4, 16, 16, status);
        } else if (node.hasConfig()) {
            addQuad(matrix, getX() + width - 20, getY() + 4, 96, 4, 16, 16, status);
        }

        // Center rows
        for (var segment : segments) {
            // Fill
            for (int i = 0; i < columnCount; i++) {
                addQuad(matrix, getX() + 8 + i * 16, segment.y, uvOffset + 8, 8, 16, 12);
            }
            addQuad(matrix, getX() + 8 + columnCount * 16, segment.y, uvOffset + 8, 8, width - (16 + columnCount * 16), 12);

            // Sides
            addQuad(matrix, getX(), segment.y, uvOffset, 8, 8, 12);
            addQuad(matrix, getX() + width - 8, segment.y, uvOffset + 24, 8, 8, 12);
        }

        // Bottom bar
        for (int i = 0; i < columnCount; i++) {
            addQuad(matrix, getX() + 8 + i * 16, getY() + 24 + segments.length * ROW_HEIGHT, uvOffset + 8, 24, 16, 8);
        }
        addQuad(matrix, getX() + 8 + columnCount * 16, getY() + 24 + segments.length * ROW_HEIGHT, uvOffset + 8, 24, width - (16 + columnCount * 16), 8);

        // Bottom corners
        addQuad(matrix, getX(), getY() + 24 + segments.length * ROW_HEIGHT, uvOffset, 24, 8, 8);
        addQuad(matrix, getX() + width - 8, getY() + 24 + segments.length * ROW_HEIGHT, uvOffset + 24, 24, 8, 8);


        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        for (var segment : segments) {
            segment.render(context, mouseX, mouseY);
        }

        context.drawText(textRenderer, getMessage(), getX() + 7, getY() + 7, 0x404040, false);
    }

    private static void addQuad(Matrix4f matrix, int x, int y, int u, int v, int width, int height) {
        addQuad(matrix, x, y, u, v, width, height, 0xffffffff);
    }

    public static void addQuad(Matrix4f matrix, int x1, int y1, int u, int v, int width, int height, int color) {
        int x2 = x1 + width;
        int y2 = y1 + height;
        float u1 = u / 256f;
        float u2 = (u + width) / 256f;
        float v1 = v / 256f;
        float v2 = (v + height) / 256f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        bufferBuilder.vertex(matrix, x1, y2, 0).texture(u1, v2).color(color).next();
        bufferBuilder.vertex(matrix, x2, y2, 0).texture(u2, v2).color(color).next();
        bufferBuilder.vertex(matrix, x2, y1, 0).texture(u2, v1).color(color).next();
        bufferBuilder.vertex(matrix, x1, y1, 0).texture(u1, v1).color(color).next();
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
            var matrices = context.getMatrices();
            var matrix = matrices.peek().getPositionMatrix();
            boolean hovered = hasConnectorAt(mouseX, mouseY);

            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.setShaderTexture(0, parent.texture);
            RenderSystem.setShaderColor(hovered ? 2 : 1, hovered ? 2 : 1, hovered ? 2 : 1, 1);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            addQuad(matrix, getConnectorX(), getConnectorY(), 96, 0, 4, 4, this.connector.type().color() | 0xff000000);

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            RenderSystem.setShaderColor(1, 1, 1, 1);

            if (!isOutput)
                context.drawText(textRenderer, this.connector.id(), x + 16, y + 2, 0x404040, false);
            else
                context.drawText(textRenderer, this.connector.id(), x + width - 16 - textRenderer.getWidth(this.connector.id()), y + 2, 0x404040, false);
        }
    }
}
