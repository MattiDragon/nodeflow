package io.github.mattidragon.nodeflow.client.ui.screen;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.MessageToast;
import io.github.mattidragon.nodeflow.client.ui.widget.EditorAreaWidget;
import io.github.mattidragon.nodeflow.client.ui.widget.NodeWidget;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class EditorScreen extends Screen {
    private static final Identifier DEFAULT_TEXTURE = NodeFlow.id("textures/gui/editor.png");
    public static final int TILE_SIZE = 16;
    public static final int BORDER_SIZE = 8;
    public static final int BORDER_OFFSET = 32;
    public static final int GRID_OFFSET = BORDER_OFFSET + BORDER_SIZE;

    public final Graph graph;
    public final Identifier texture;

    protected boolean isAddingNode = false;
    protected boolean isDeletingNode = false;
    protected @Nullable Connector<?> lastHoveredConnector = null;
    protected long lastHoveredTimestamp = 0;
    public @Nullable Connector<?> connectingConnector;

    public Button backButton;
    protected final Map<NodeGroup, List<Button>> nodeButtons = new HashMap<>();
    protected final List<Button> groupButtons = new ArrayList<>();
    @Nullable
    protected NodeGroup activeGroup = null;

    public Button plusButton;
    public Button deleteButton;
    private AddNodesWidget addNodesWidget;
    protected EditorAreaWidget area;

    public EditorScreen(Component title, Graph graph) {
        this(title, graph, DEFAULT_TEXTURE);
    }

    public EditorScreen(Component title, Graph graph, Identifier texture) {
        super(title);
        this.graph = graph;
        this.texture = texture;

        // Moved to right spot before usage, no need to reinit
        for (NodeGroup group : graph.env.groups()) {
            addGroup(graph, group);
        }
    }

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T drawableElement) {
        return super.addRenderableWidget(drawableElement);
    }

    private void addGroup(Graph graph, NodeGroup group) {
        groupButtons.add(Button.builder(group.getName(), _ -> {
            activeGroup = group;
            updateAddButtons();
            updateVisibility();
        }).bounds(0, 0, 100, 20).build());

        var buttons = new ArrayList<Button>();
        for (var type : group.getTypes()) {
            buttons.add(Button.builder(type.name(), _ -> {
                toggleAddingMode();
                var node = type.newNode(graph);
                node.guiX = (int) area.modifyX(width / 2.0);
                node.guiY = (int) area.modifyY(height / 2.0);

                graph.addNode(node);
                var widget = new NodeWidget(node, this);
                area.add(widget);
                syncGraph();
            }).bounds(0, 0, 100, 20).build());
        }
        nodeButtons.put(group, buttons);
    }

    @Override
    protected void init() {
        area = addRenderableWidget(new EditorAreaWidget(GRID_OFFSET, GRID_OFFSET, getBoxWidth(), getBoxHeight(), this));
        area.children().clear();
        for (var node : graph.getNodes()) {
            var widget = new NodeWidget(node, this);
            area.add(widget);
        }

        plusButton = addRenderableWidget(Button.builder(Component.empty(), _ -> toggleAddingMode()).bounds(GRID_OFFSET, BORDER_OFFSET - 20, 100, 20).build());
        deleteButton = addRenderableWidget(Button.builder(Component.empty(), _ -> toggleDeletingMode()).bounds(GRID_OFFSET + 110, BORDER_OFFSET - 20, 100, 20).build());
        backButton = addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, _ -> {
            activeGroup = null;
            updateAddButtons();
            updateVisibility();
        }).bounds(GRID_OFFSET + 110, BORDER_OFFSET - 20, 100, 20).build());

        addNodesWidget = addRenderableWidget(new AddNodesWidget(minecraft, getBoxWidth(), getBoxHeight() - 10, GRID_OFFSET + 10));
        updateAddButtons();
        updateVisibility();
    }

    private void updateAddButtons() {
        var buttons = activeGroup == null ? groupButtons : nodeButtons.get(activeGroup);

        var entries = new ArrayList<AddNodesWidget.Entry>();
        var currentButtons = new ArrayList<Button>();
        for (var button : buttons) {
            currentButtons.add(button);
            if (currentButtons.size() >= addNodesWidget.getButtonCount()) {
                entries.add(new AddNodesWidget.Entry(currentButtons));
                currentButtons = new ArrayList<>();
            }
        }
        if (!currentButtons.isEmpty()) {
            entries.add(new AddNodesWidget.Entry(currentButtons));
        }
        addNodesWidget.replaceEntries(entries);
        addNodesWidget.setScrollAmount(0);
    }

    public void syncGraph() {}

    private void toggleDeletingMode() {
        isDeletingNode = !isDeletingNode;
        updateVisibility();
    }

    private void toggleAddingMode() {
        isAddingNode = !isAddingNode;
        activeGroup = null;
        updateAddButtons();
        updateVisibility();
    }

    private void updateVisibility() {
        area.children().forEach(node -> {
            node.active = !isAddingNode;
            //node.visible = !isAddingNode;
        });
        backButton.active = isAddingNode && activeGroup != null;
        backButton.visible = isAddingNode && activeGroup != null;
        addNodesWidget.active = isAddingNode;
        addNodesWidget.visible = isAddingNode;
        deleteButton.active = !isAddingNode;
        deleteButton.visible = !isAddingNode;
        plusButton.active = !isDeletingNode;
        plusButton.visible = !isDeletingNode;
        area.active = !isAddingNode;
        deleteButton.setMessage(isDeletingNode ? CommonComponents.GUI_CANCEL : Component.translatable("nodeflow.editor.button.delete_nodes"));
        plusButton.setMessage(isAddingNode ? CommonComponents.GUI_CANCEL : Component.translatable("nodeflow.editor.button.add_node"));
    }

    public void removeNode(NodeWidget node) {
        graph.removeNode(node.node.id);
        area.children().remove(node);
        area.remove(node);
        syncGraph();
    }

    private void tryFindConnection(double mouseX, double mouseY) {
        var row = findConnectorAt(mouseX, mouseY);
        if (row == null) return;
        if (connectingConnector == null) return;
        if (row.equals(connectingConnector)) {
            graph.removeConnections(connectingConnector);
            return;
        }

        if (connectingConnector.isOutput() == row.isOutput()) {
            if (row.isOutput()) {
                showToast(Component.translatable("nodeflow.editor.toast.two_outputs").withStyle(ChatFormatting.RED));
            } else {
                showToast(Component.translatable("nodeflow.editor.toast.two_inputs").withStyle(ChatFormatting.RED));
            }
            return;
        }

        if (connectingConnector.type() != row.type()) {
            showToast(Component.translatable("nodeflow.editor.toast.different_type").withStyle(ChatFormatting.RED));
            return;
        }

        if (!row.type().splittable() || !row.isOutput()) {
            graph.removeConnections(row);
        }
        graph.addConnection(connectingConnector, row);

        var stack = new ArrayDeque<Connector<?>>();
        var searchTarget = connectingConnector.isOutput() ? connectingConnector : row;
        var searchStarter = connectingConnector.isOutput() ? row : connectingConnector;

        Arrays.stream(searchStarter.parent().getOutputs())
                .map(graph::getConnections)
                .flatMap(Set::stream)
                .map(connection -> connection.getTargetConnector(graph))
                .filter(Objects::nonNull)
                .forEach(stack::push);

        while (!stack.isEmpty()) {
            var element = stack.pop();
            if (element.equals(searchTarget) || element.equals(searchStarter)) {
                showToast(Component.translatable("nodeflow.editor.toast.recursion").withStyle(ChatFormatting.RED));
                graph.removeConnections(connectingConnector);
                return;
            }

            Arrays.stream(element.parent().getOutputs())
                    .map(graph::getConnections)
                    .flatMap(Set::stream)
                    .map(connection -> connection.getTargetConnector(graph))
                    .filter(Objects::nonNull)
                    .forEach(stack::push);
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));

    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (connectingConnector != null && event.button() == 0) {
            if (!connectingConnector.type().splittable() || !connectingConnector.isOutput())
                graph.removeConnections(connectingConnector);

            tryFindConnection(event.x(), event.y());

            area.children().forEach(NodeWidget::updateTooltip);

            connectingConnector = null;
        }
//        setFocused(null);
        // Sync node movement and connector changes
        syncGraph();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (isAddingNode) {
            addNodesWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return true;
        }
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isAddingNode) return;
        var connector = findConnectorAt(mouseX, mouseY);
        if (connector == null) return;

        long time = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        if (!connector.equals(lastHoveredConnector) || time - lastHoveredTimestamp > 10) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.7f));
        }
        lastHoveredConnector = connector;
        lastHoveredTimestamp = time;
    }

    public @Nullable NodeWidget findWidget(Node node) {
        for (var widget : area.children()) {
            if (widget.node == node)
                return widget;
        }
        return null;
    }

    public NodeWidget.@Nullable Segment findSegment(Connector<?> connector) {
        var widget = findWidget(connector.parent());
        if (widget == null) return null;

        for (var segment : widget.calculateSegments()) {
            if (segment.connector.equals(connector))
                return segment;
        }
        return null;
    }

    private @Nullable Connector<?> findConnectorAt(double mouseX, double mouseY) {
        for (var node : area.children()) {
            for (var segment : node.calculateSegments()) {
                if (segment.hasConnectorAt(area.modifyX(mouseX), area.modifyY(mouseY))) {
                    return segment.connector;
                }
            }
        }
        return null;
    }

    public NodeWidget.@Nullable Segment findSegmentAt(double mouseX, double mouseY) {
        for (var node : area.children()) {
            for (var segment : node.calculateSegments()) {
                if (segment.hasConnectorAt(area.modifyX(mouseX), area.modifyY(mouseY))) {
                    return segment;
                }
            }
        }
        return null;
    }

    public int getBoxWidth() {
        return (this.width - GRID_OFFSET * 2) / TILE_SIZE * TILE_SIZE;
    }

    public int getBoxHeight() {
        return (this.height - GRID_OFFSET * 2) / TILE_SIZE * TILE_SIZE;
    }

    public void showToast(Component message) {
        Minecraft.getInstance().getToastManager().addToast(new MessageToast(message));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (minecraft.player != null) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        renderArea(context);
    }

    private void renderArea(GuiGraphics context) {
        var texture = area.isFocused() && minecraft.getLastInputType().isKeyboard() ? NodeFlow.id("editor_selected") : NodeFlow.id("editor");
        context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, BORDER_OFFSET, BORDER_OFFSET, getBoxWidth() + BORDER_SIZE * 2, getBoxHeight() + BORDER_SIZE * 2);
    }

    public boolean isDeletingNode() {
        return isDeletingNode;
    }

    public EditorAreaWidget getArea() {
        return area;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (focused == getFocused()) return;
        super.setFocused(focused);
    }

    private static class AddNodesWidget extends ContainerObjectSelectionList<AddNodesWidget.Entry> {
        public AddNodesWidget(Minecraft client, int width, int height, int y) {
            super(client, width, height, y, 30);
            centerListVertically = true;
            setX(GRID_OFFSET);
        }

        @Override
        public void replaceEntries(Collection<io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen.AddNodesWidget.Entry> newEntries) {
            super.replaceEntries(newEntries);
        }

        @Override
        public int addEntry(io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen.AddNodesWidget.Entry entry) {
            return super.addEntry(entry);
        }

        @Override
        protected void renderListSeparators(GuiGraphics context) {
            // Overridden to disable background
        }

        @Override
        protected void renderListBackground(GuiGraphics context) {
            // Overridden to disable background
        }

        @Override
        public int getRowWidth() {
            return getButtonCount() * 110 - 10;
        }

        public int getButtonCount() {
            return (width - 50) / 110;
        }

        @Override
        protected int scrollBarX() {
            return (width / 2) + (getRowWidth() / 2) + GRID_OFFSET + 10;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!active) return false;
            return super.mouseClicked(event, doubleClick);
        }

        @Override
        protected void renderListItems(GuiGraphics context, int mouseX, int mouseY, float delta) {
            context.enableScissor(0, getY(), width + GRID_OFFSET, height + getY());
            super.renderListItems(context, mouseX, mouseY, delta);
            context.disableScissor();
        }

        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
            if (!active) return null;

            return super.nextFocusPath(navigation);
        }

        private static class Entry extends ContainerObjectSelectionList.Entry<io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen.AddNodesWidget.Entry> {
            private final List<Button> buttons;

            public Entry(List<Button> buttons) {
                super();
                this.buttons = buttons;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return buttons;
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return buttons;
            }

            @Override
            public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float a) {
                for (int i = 0; i < buttons.size(); i++) {
                    var button = buttons.get(i);
                    button.setY(getContentY());
                    button.setX(getContentX() + i * 110);
                    button.render(graphics, mouseX, mouseY, a);
                }
            }
        }
    }
}
