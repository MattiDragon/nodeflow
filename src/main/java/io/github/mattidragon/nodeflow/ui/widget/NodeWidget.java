package io.github.mattidragon.nodeflow.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;

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
        setPos(getX() - width / 2, getY() - height / 2);

        this.node = node;
        this.parent = parent;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        //TODO: implement
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

        node.guiX = getX() + width / 2;
        node.guiY = getY() + height / 2;
    }

    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20) {
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

            parent.renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        var textRenderer = Screens.getTextRenderer(parent);
        var matrix = matrices.peek().getPositionMatrix();
        var segments = calculateSegments();

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, parent.texture);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        int columnCount = width / 16 - 1;
        for (int i = 0; i < columnCount; i++) {
            addQuad(matrix, getX() + 8 + i * 16, getY(), 32 + 8, 0, 16, 24);
        }
        addQuad(matrix, getX() + 8 + columnCount * 16, getY(), 32 + 8, 0, width - (16 + columnCount * 16), 24);

        addQuad(matrix, getX(), getY(), 32, 0, 8, 24);
        addQuad(matrix, getX() + width - 8, getY(), 32 + 24, 0, 8, 24);

        var status = 0xffffffff;
        if (!node.isFullyConnected())
            status = 0xffffaa55;
        if (!node.validate().isEmpty())
            status = 0xffffaa55;
        if (node.hasConfig() && mouseX >= getX() + width - 20 && mouseX <= getX() + width - 4 && mouseY >= getY() + 4 && mouseY <= getY() + 20)
            status = 0xff9999ff;

        if (!node.isFullyConnected() || !node.validate().isEmpty())
            addQuad(matrix, getX() + width - 20, getY() + 4, 112, 4, 16, 16, status);
        else if (node.hasConfig())
            addQuad(matrix, getX() + width - 20, getY() + 4, 96, 4, 16, 16, status);

        for (var segment : segments) {
            for (int i = 0; i < columnCount; i++) {
                addQuad(matrix, getX() + 8 + i * 16, segment.y, 32 + 8, 8, 16, 12);
            }
            addQuad(matrix, getX() + 8 + columnCount * 16, segment.y, 32 + 8, 8, width - (16 + columnCount * 16), 12);

            addQuad(matrix, getX(), segment.y, 32, 8, 8, 12);
            addQuad(matrix, getX() + width - 8, segment.y, 32 + 24, 8, 8, 12);
        }

        for (int i = 0; i < columnCount; i++) {
            addQuad(matrix, getX() + 8 + i * 16, getY() + 24 + segments.length * ROW_HEIGHT, 32 + 8, 24, 16, 8);
        }
        addQuad(matrix, getX() + 8 + columnCount * 16, getY() + 24 + segments.length * ROW_HEIGHT, 32 + 8, 24, width - (16 + columnCount * 16), 8);

        addQuad(matrix, getX(), getY() + 24 + segments.length * ROW_HEIGHT, 32, 24, 8, 8);
        addQuad(matrix, getX() + width - 8, getY() + 24 + segments.length * ROW_HEIGHT, 32 + 24, 24, 8, 8);

        //addQuad(matrix, x, y + 24 + segments.length * ROW_SIZE, 32, 24, WIDTH, 8);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        for (var segment : segments) {
            segment.render(matrices, mouseX, mouseY);
        }

        textRenderer.draw(matrices, getMessage(), getX() + 7, getY() + 7, 0x404040);
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

    public class Segment extends DrawableHelper {
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

        public void render(MatrixStack matrices, int mouseX, int mouseY) {
            var textRenderer = Screens.getTextRenderer(parent);
            var matrix = matrices.peek().getPositionMatrix();
            boolean hovered = hasConnectorAt(mouseX, mouseY);

            RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
            RenderSystem.setShaderTexture(0, parent.texture);
            RenderSystem.setShaderColor(hovered ? 2 : 1, hovered ? 2 : 1, hovered ? 2 : 1, 1);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            addQuad(matrix, getConnectorX(), getConnectorY(), 96, 0, 4, 4, this.connector.type().color() | 0xff000000);

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            if (!isOutput)
                textRenderer.draw(matrices, this.connector.id(), (float) x + 16, (float) (y + 2), 0x404040);
            else
                textRenderer.draw(matrices, this.connector.id(), (float) (x + width - 16 - textRenderer.getWidth(this.connector.id())), (float) (y + 2), 0x404040);
        }
    }
}
