package io.github.mattidragon.nodeflow.ui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class EditorAreaWidget extends ZoomableAreaWidget<NodeWidget> {
    private static final String CLIPBOARD_PREFIX = "nodeflow-node-v1:";
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

    private void copyNode() {
        if (clickedNode == null) {
            NodeFlow.LOGGER.warn("Clicked copy key without clicking node");
            return;
        }
        var nbt = new NbtCompound();
        clickedNode.node.writeNbt(nbt);
        nbt.remove("guiX");
        nbt.remove("guiY");
        nbt.remove("id");

        var bytes = new ByteArrayOutputStream();
        try (var out = Base64.getEncoder().wrap(bytes)) {
            NbtIo.writeCompressed(nbt, out);
        } catch (IOException e) {
            NodeFlow.LOGGER.warn("Failed to copy node", e);
            return;
        }

        MinecraftClient.getInstance().keyboard.setClipboard(CLIPBOARD_PREFIX + bytes);

        closeMenu();
    }

    private void cutNode() {
        if (clickedNode == null) {
            NodeFlow.LOGGER.warn("Clicked cut key without clicking node");
            return;
        }
        var nbt = new NbtCompound();
        clickedNode.node.writeNbt(nbt);
        nbt.remove("guiX");
        nbt.remove("guiY");
        nbt.remove("id");

        var bytes = new ByteArrayOutputStream();
        try (var out = Base64.getEncoder().wrap(bytes)) {
            NbtIo.writeCompressed(nbt, out);
        } catch (IOException e) {
            NodeFlow.LOGGER.warn("Failed to cut node", e);
            return;
        }
        MinecraftClient.getInstance().keyboard.setClipboard(CLIPBOARD_PREFIX + bytes);

        parent.removeNode(clickedNode);
        closeMenu();
    }

    private void pasteNode(double mouseX, double mouseY) {
        var clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        if (!clipboard.startsWith(CLIPBOARD_PREFIX))
            return;

        NbtCompound nbt;
        var bytes = new ByteArrayInputStream(clipboard.substring(CLIPBOARD_PREFIX.length()).getBytes());
        try (var in = Base64.getDecoder().wrap(bytes)) {
            nbt = NbtIo.readCompressed(in);
        } catch (IOException e) {
            NodeFlow.LOGGER.warn("Failed to paste node", e);
            return;
        }

        if (!nbt.contains("type", NbtElement.STRING_TYPE) || !Identifier.isValid(nbt.getString("type"))) {
            NodeFlow.LOGGER.warn("Failed to paste node, invalid type");
            return;
        }
        var node = NodeType.REGISTRY.get(new Identifier(nbt.getString("type"))).generator().apply(parent.graph);
        node.readNbt(nbt);
        node.guiX = (int) modifyX(mouseX);
        node.guiY = (int) modifyY(mouseY);

        parent.graph.addNode(node);
        add(new NodeWidget(node, parent));
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
            for (var node : this.children()) {
                if (node.clicked(modifyX(mouseX), modifyY(mouseY))) {
                    var buttons = new ArrayList<ButtonWidget>();
                    buttons.add(new ButtonWidget((int) mouseX, (int) mouseY, 100, 12, ScreenTexts.CANCEL, __ -> closeMenu()));
                    buttons.add(new ButtonWidget((int) mouseX, (int) mouseY + 12, 100, 12, Text.translatable("nodeflow.editor.button.duplicate"), __ -> duplicateNode()));
                    buttons.add(new ButtonWidget((int) mouseX, (int) mouseY + 24, 100, 12, Text.translatable("nodeflow.editor.button.delete"), __ -> deleteNode()));
                    buttons.add(new ButtonWidget((int) mouseX, (int) mouseY + 36, 100, 12, Text.translatable("nodeflow.editor.button.copy"), __ -> copyNode()));
                    buttons.add(new ButtonWidget((int) mouseX, (int) mouseY + 48, 100, 12, Text.translatable("nodeflow.editor.button.cut"), __ -> cutNode()));
                    if (node.node.hasConfig())
                        buttons.add(new ButtonWidget((int) mouseX, (int) mouseY + 90, 100, 10, Text.translatable("nodeflow.editor.button.configure"), __ -> configureNode()));
                    contextButtons = buttons.toArray(new ButtonWidget[0]);
                    clickedNode = node;
                    return true;
                }
            }
            var buttons = new ArrayList<ButtonWidget>();
            buttons.add(new ButtonWidget((int) mouseX, (int) mouseY, 100, 12, ScreenTexts.CANCEL, __ -> closeMenu()));
            buttons.add(new ButtonWidget((int) mouseX, (int) mouseY + 12, 100, 12, Text.translatable("nodeflow.editor.button.paste"), __ -> pasteNode(mouseX, mouseY)));
            contextButtons = buttons.toArray(new ButtonWidget[0]);
            return true;
        }
        if (contextButtons.length != 0) {
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isPaste(keyCode)) {
            pasteNode(x + width / 2.0, x + height / 2.0);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
