package io.github.mattidragon.nodeflow.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.UUID;

public class EditorAreaWidget extends ZoomableAreaWidget<NodeWidget> {
    private final EditorScreen parent;

    private NodeWidget clickedNode;
    private ButtonWidget[] contextButtons = new ButtonWidget[0];

    public EditorAreaWidget(int x, int y, int width, int height, EditorScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
        closeMenu();
    }

    private void closeMenu() {
        contextButtons = new ButtonWidget[0];
        clickedNode = null;
    }

    private void deleteNode() {
        if (clickedNode == null) {
            NodeFlow.LOGGER.warn("Clicked delete key without clicking node");
            return;
        }
        parent.removeNode(clickedNode);
        closeMenu();
    }

    private void duplicateNode() {
        if (clickedNode == null) {
            NodeFlow.LOGGER.warn("Clicked dupe key without clicking node");
            return;
        }
        var nbt = new NbtCompound();
        var oldNode = clickedNode.node;
        oldNode.writeNbt(nbt);

        var newNode = oldNode.type.generator().apply(parent.graph);
        newNode.readNbt(nbt);
        newNode.id = UUID.randomUUID();
        newNode.guiX = oldNode.guiX + 10;
        newNode.guiY = oldNode.guiY + 10;

        parent.graph.addNode(newNode);
        add(new NodeWidget(newNode, parent));
        parent.syncGraph();
        closeMenu();
    }

    private void configureNode() {
        if (clickedNode == null) {
            NodeFlow.LOGGER.warn("Clicked dupe key without clicking node");
            return;
        }
        MinecraftClient.getInstance().setScreen(clickedNode.node.createConfigScreen(parent));
        closeMenu();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            for(var node : this.children()) {
                if (node.clicked(modifyX(mouseX), modifyY(mouseY))) {
                    if (node.node.hasConfig())
                        contextButtons = new ButtonWidget[] {
                                ButtonWidget.builder(ScreenTexts.CANCEL, __ -> closeMenu()).dimensions((int) mouseX, (int) mouseY, 100, 10).build(),
                                ButtonWidget.builder(Text.translatable("nodeflow.editor.button.duplicate"), __ -> duplicateNode()).dimensions((int) mouseX, (int) mouseY + 10, 100, 10).build(),
                                ButtonWidget.builder(Text.translatable("nodeflow.editor.button.configure"), __ -> configureNode()).dimensions((int) mouseX, (int) mouseY + 20, 100, 10).build(),
                                ButtonWidget.builder(Text.translatable("nodeflow.editor.button.delete"), __ -> deleteNode()).dimensions((int) mouseX, (int) mouseY + 30, 100, 10).build()};
                    else
                        contextButtons = new ButtonWidget[] {
                                ButtonWidget.builder(ScreenTexts.CANCEL, __ -> closeMenu()).dimensions((int) mouseX, (int) mouseY, 100, 10).build(),
                                ButtonWidget.builder(Text.translatable("nodeflow.editor.button.duplicate"), __ -> duplicateNode()).dimensions((int) mouseX, (int) mouseY + 10, 100, 10).build(),
                                ButtonWidget.builder(Text.translatable("nodeflow.editor.button.delete"), __ -> deleteNode()).dimensions((int) mouseX, (int) mouseY + 20, 100, 10).build()};
                    return true;
                }
            }
        }
        if (clickedNode != null) {
            var result = false;
            for (var contextButton : contextButtons) {
                result = contextButton.mouseClicked(mouseX, mouseY, button);
                if (result)
                    break;
            }
            if (!result)
                closeMenu();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        for (var contextButton : contextButtons) {
            contextButton.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void renderExtras(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderConnectors(matrices, mouseX, mouseY);
    }

    private void renderConnectors(MatrixStack matrices, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
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

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
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
