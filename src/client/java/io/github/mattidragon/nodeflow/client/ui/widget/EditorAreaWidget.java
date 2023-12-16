package io.github.mattidragon.nodeflow.client.ui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.graph.Connection;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class EditorAreaWidget extends ZoomableAreaWidget<NodeWidget> {
    private static final String CLIPBOARD_PREFIX = "nodeflow-node-v1:";
    private final EditorScreen parent;

    private final ContextMenuWidget contextMenu = new ContextMenuWidget();

    public EditorAreaWidget(int x, int y, int width, int height, EditorScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
        contextMenu.hide();
    }

    private void deleteNode() {
        if (contextMenu.node == null) {
            NodeFlow.LOGGER.warn("Clicked delete key without clicking node");
            return;
        }
        parent.removeNode(contextMenu.node);
        contextMenu.hide();
    }

    private void duplicateNode() {
        if (contextMenu.node == null) {
            NodeFlow.LOGGER.warn("Clicked dupe key without clicking node");
            return;
        }
        var nbt = new NbtCompound();
        var oldNode = contextMenu.node.node;
        oldNode.writeNbt(nbt);

        var newNode = oldNode.type.generator().apply(parent.graph);
        newNode.readNbt(nbt);
        newNode.id = UUID.randomUUID();
        newNode.guiX = oldNode.guiX + 10;
        newNode.guiY = oldNode.guiY + 10;

        parent.graph.addNode(newNode);
        add(new NodeWidget(newNode, parent));
        parent.syncGraph();
        contextMenu.hide();
    }

    private void copyNode() {
        if (contextMenu.node == null) {
            NodeFlow.LOGGER.warn("Clicked copy key without clicking node");
            return;
        }
        var nbt = new NbtCompound();
        contextMenu.node.node.writeNbt(nbt);
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

        contextMenu.hide();
    }

    private void cutNode() {
        if (contextMenu.node == null) {
            NodeFlow.LOGGER.warn("Clicked cut key without clicking node");
            return;
        }
        var nbt = new NbtCompound();
        contextMenu.node.node.writeNbt(nbt);
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

        parent.removeNode(contextMenu.node);
        contextMenu.hide();
    }

    private void pasteNode(double mouseX, double mouseY) {
        var clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        if (!clipboard.startsWith(CLIPBOARD_PREFIX))
            return;

        NbtCompound nbt;
        var bytes = new ByteArrayInputStream(clipboard.substring(CLIPBOARD_PREFIX.length()).getBytes());
        try (var in = Base64.getDecoder().wrap(bytes)) {
            nbt = NbtIo.readCompressed(in, NbtSizeTracker.ofUnlimitedBytes());
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
        contextMenu.hide();
    }

    private void configureNode() {
        if (contextMenu.node == null) {
            NodeFlow.LOGGER.warn("Clicked dupe key without clicking node");
            return;
        }
        MinecraftClient.getInstance().setScreen(NodeConfigScreenRegistry.createScreen(contextMenu.node.node, parent));
        contextMenu.hide();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            for (var node : Lists.reverse(this.children())) {
                if (node.clicked(modifyX(mouseX), modifyY(mouseY))) {
                    setContextMenu((int) mouseX, (int) mouseY, node);
                    return true;
                }
            }
            setContextMenu((int) mouseX, (int) mouseY, null);
            return true;
        }

        return contextMenu.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Shows the context menu
     * @param x The x coordinate for the position of the menu
     * @param y The y coordinate for the position of the menu
     * @param node An optional node that the menu opens for. Makes node specific actions available
     */
    public void setContextMenu(int x, int y, @Nullable NodeWidget node) {
        contextMenu.show(x, y, node);
        setFocused(contextMenu);
        contextMenu.setFocused(contextMenu.buttons.get(0));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isPaste(keyCode)) {
            pasteNode(x + width / 2.0, x + height / 2.0);
            return true;
        }

        if (contextMenu.isVisible() && (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN))
            return false;
        if (contextMenu.isVisible() && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
            contextMenu.hide();
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (this.getFocused() != null) {
            this.getFocused().setFocused(focused);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        contextMenu.render(context, mouseX, mouseY, delta);
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

        for (Connection connection : parent.graph.getConnections()) {
            var input = parent.findSegment(Objects.requireNonNull(connection.getTargetConnector(parent.graph)));
            var output = parent.findSegment(Objects.requireNonNull(connection.getSourceConnector(parent.graph)));

            renderConnectorLine(matrices, input.getConnectorX(), input.getConnectorY(), output.getConnectorX(), output.getConnectorY(), input.connector.type().color() | 0xaa000000);
        }

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

    public class ContextMenuWidget extends AbstractParentElement implements Drawable {
        private NodeWidget node;
        private final List<ButtonWidget> buttons = new ArrayList<>();

        public void show(int x, int y, @Nullable NodeWidget node) {
            hide();

            buttons.add(ButtonWidget.builder(ScreenTexts.CANCEL, __ -> contextMenu.hide()).size(100, 12).build());
            if (node != null) {
                buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.duplicate"), __ -> duplicateNode())
                        .size(100, 12)
                        .build());
                buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.delete"), __ -> deleteNode())
                        .size(100, 12)
                        .build());
                buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.copy"), __ -> copyNode())
                        .size(100, 12)
                        .build());
                buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.paste"), __ -> pasteNode(x, y))
                        .size(100, 12)
                        .build());
                buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.cut"), __ -> cutNode())
                        .size(100, 12)
                        .build());
                if (NodeConfigScreenRegistry.hasConfig(node.node)) {
                    buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.configure"), __ -> configureNode())
                            .size(100, 12)
                            .build());
                }
            } else {
                buttons.add(ButtonWidget.builder(Text.translatable("nodeflow.editor.button.paste"), __ -> pasteNode(x, y))
                        .size(100, 12)
                        .build());
            }
            this.node = node;

            var currentY = y;
            for (var button : buttons) {
                button.setX(x);
                button.setY(currentY);
                currentY += button.getHeight();
            }
        }

        public void hide() {
            buttons.clear();
        }

        public boolean isVisible() {
            return !children().isEmpty();
        }

        @Override
        public List<? extends Element> children() {
            return buttons;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            for (var button : buttons) {
                button.render(context, mouseX, mouseY, delta);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (buttons.isEmpty()) return false;

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
}
