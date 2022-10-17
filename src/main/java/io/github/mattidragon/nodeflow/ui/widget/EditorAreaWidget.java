package io.github.mattidragon.nodeflow.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Objects;

public class EditorAreaWidget extends ZoomableAreaWidget<NodeWidget> {
    private final EditorScreen parent;

    public EditorAreaWidget(int x, int y, int width, int height, EditorScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
    }

    @Override
    protected void renderExtras(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderConnectors(matrices, mouseX, mouseY);
    }

    private void renderConnectors(MatrixStack matrices, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        if (parent.connectingConnector != null) {
            var row = parent.findSegmentAt(mouseX, mouseY);
            var targetX = (int) modifyX(mouseX);
            var targetY = (int) modifyY(mouseY);

            if (row != null) {
                targetX = row.getConnectorX();
                targetY = row.getConnectorY();
            }

            var connectingSegment = parent.findSegment(parent.connectingConnector);

            renderConnectorLine(matrices, targetX, targetY, connectingSegment.getConnectorX(), connectingSegment.getConnectorY(), parent.connectingConnector.type().color() | 0xaa000000);
        }

        parent.graph.getConnections().forEach(connection -> {
            var input = parent.findSegment(Objects.requireNonNull(connection.getTargetConnector(parent.graph)));
            var output = parent.findSegment(Objects.requireNonNull(connection.getSourceConnector(parent.graph)));

            renderConnectorLine(matrices, input.getConnectorX(), input.getConnectorY(), output.getConnectorX(), output.getConnectorY(), input.connector.type().color() | 0xaa000000);
        });

        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    private static void renderConnectorLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        var matrix = matrices.peek().getPositionMatrix();
        var xOffset = x1 - x2;
        var yOffset = y1 - y2;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        if (xOffset > 0) {
            bufferBuilder.vertex(matrix, x2, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2, 0).color(color).next();
            bufferBuilder.vertex(matrix, x2, y2, 0).color(color).next();
        } else {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x2 + 4, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x2 + 4, y2, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2, 0).color(color).next();
        }

        if (yOffset < 0) {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1 + 4, 0).color(color).next();
        } else {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2, 0).color(color).next();
        }

        if (xOffset > 0) {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1, 0).color(color).next();
        } else {
            bufferBuilder.vertex(matrix, x1, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(color).next();
        }

    }
}
