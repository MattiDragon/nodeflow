package io.github.mattidragon.nodeflow.client.ui.widget;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A widget that contains other widgets in a zoomable and movable field.
 * @param <T> The type of elements contained.
 * @author MattiDragon
 * @version 1.0.2
 */
public class ZoomableAreaWidget<T extends GuiEventListener & Renderable & NarrationSupplier> extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
    private final List<T> children = new ArrayList<>();

    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public boolean visible = true;
    public boolean active = true;
    private boolean focused = false;

    private int zoom;
    private double viewX;
    private double viewY;

    private float scale = Float.NaN;

    public ZoomableAreaWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public List<T> children() {
        return children;
    }

    public void remove(T child) {
        children.remove(child);
    }

    public void add(T child) {
        children.add(child);
    }

    public int getZoom() {
        return zoom;
    }

    public float getScale() {
        if (Float.isNaN(scale))
            scale = (float) Math.pow(2, zoom / 2.0);
        return scale;
    }

    public void zoom(int amount, double x, double y) {
        var anchorX = modifyX(x);
        var anchorY = modifyY(y);
        zoom += amount;
        zoom = Mth.clamp(zoom, -5, 5);
        scale = Float.NaN;
        this.viewX = -(anchorX * getScale() - x + this.x + this.width / 2.0);
        this.viewY = -(anchorY * getScale() - y + this.y + this.height / 2.0);
    }

    public double modifyX(double originalX) {
        return modifyDeltaX(originalX - x - viewX - width / 2.0);
    }

    public double modifyY(double originalY) {
        return modifyDeltaY(originalY - y - viewY - height / 2.0);
    }

    public double modifyDeltaX(double originalX) {
        return originalX / getScale();
    }

    public double modifyDeltaY(double originalY) {
        return originalY / getScale();
    }

    public double reverseModifyX(double originalX) {
        return reverseModifyDeltaX(originalX) + x + viewX + width / 2.0;
    }

    public double reverseModifyY(double originalY) {
        return reverseModifyDeltaY(originalY) + y + viewY + height / 2.0;
    }

    public double reverseModifyDeltaX(double originalX) {
        return originalX * getScale();
    }

    public double reverseModifyDeltaY(double originalY) {
        return originalY * getScale();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!isMouseOver(event.x(), event.y()) || !active || !visible) return false;
        /*
         Super returns true if a child accepts the click and sets that child as focused.
         We read the focused element and move it to the top.
         This is done here and not when the focus is set because keyboard nav doesn't like the child order changing.
        */
        if (super.mouseClicked(modifyMouseEvent(event), doubleClick)) {
            // Only children should be able to get focused
            @SuppressWarnings("unchecked")
            T focused = (T) getFocused(); // IntelliJ bug with using var here, so we don't

            // If the node was deleted due to the click we don't want to add it back
            if (focused != null && children.remove(focused)) {
                children.addFirst(focused);
            }
        } else {
            setFocused(null);
        }
        return true;
    }

    private MouseButtonEvent modifyMouseEvent(MouseButtonEvent event) {
        return new MouseButtonEvent(modifyX(event.x()), modifyY(event.y()), event.buttonInfo());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!isMouseOver(event.x(), event.y()) || !active || !visible) return false;
        return super.mouseReleased(modifyMouseEvent(event));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!isMouseOver(event.x(), event.y()) || !active || !visible) return false;
        if (super.mouseDragged(modifyMouseEvent(event), modifyDeltaX(dx), modifyDeltaY(dy)))
            return true;

        viewX += dx;
        viewY += dy;

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY) || !active || !visible) return;
        super.mouseMoved(modifyX(mouseX), modifyY(mouseY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY) || !active || !visible)
            return false;
        if (super.mouseScrolled(modifyX(mouseX), modifyY(mouseY), horizontalAmount, verticalAmount))
            return true;
        zoom((int) verticalAmount, mouseX, mouseY);

        return true;
    }

//    @Override
//    public Optional<Element> hoveredElement(double mouseX, double mouseY) {
//        if (!isMouseOver(mouseX, mouseY) || !active || !visible) return Optional.empty();
//        return super.hoveredElement(mouseX, mouseY);
//    }


    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !visible) return false;
        if (super.keyPressed(event))
            return true;

        if (event.isUp()) {
            viewY += 10;
        } else if (event.isDown()) {
            viewY -= 10;
        } else if (event.isRight()) {
            viewX -= 10;
        } else if (event.isLeft()) {
            viewX += 10;
        } else if (event.key() == GLFW.GLFW_KEY_MINUS || event.key() == GLFW.GLFW_KEY_KP_SUBTRACT) {
            zoom(-1, x + width / 2.0, y + height / 2.0);
        } else if (event.key() == GLFW.GLFW_KEY_KP_ADD) {
            zoom(1, x + width / 2.0, y + height / 2.0);
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        var matrices = context.pose();
        context.enableScissor(x, y, x + width, y + height);
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.translate((float) (viewX + width / 2.0), (float) (viewY + height / 2.0));
        var scale = getScale();
        matrices.scale(scale, scale);

        for (var child : Lists.reverse(children)) {
            child.render(context, (int) Math.floor(modifyX(mouseX)), (int) Math.floor(modifyY(mouseY)), delta);
        }

        renderExtras(context, mouseX, mouseY, delta);

        matrices.popMatrix();
        context.disableScissor();
    }

    protected void renderExtras(GuiGraphics matrices, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(x, y, width, height);
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        if (getFocused() instanceof NarrationSupplier narratable)
            narratable.updateNarration(builder);
    }

    @Override
    public boolean isActive() {
        return active && visible;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.FOCUSED;
    }

    public double getViewX() {
        return viewX;
    }

    public double getViewY() {
        return viewY;
    }

    public void setViewX(double viewX) {
        this.viewX = viewX;
    }

    public void setViewY(double viewY) {
        this.viewY = viewY;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return this.getFocused() == null ? ComponentPath.leaf(this) : super.getCurrentFocusPath();
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigation) {
        if (!active) return null;
        if (!this.isFocused()) return ComponentPath.leaf(this);
        if (getFocused() == null && navigation.equals(new FocusNavigationEvent.TabNavigation(false))) return null;

        return super.nextFocusPath(navigation);
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (!focused) this.setFocused(null);
    }

    @Override
    public boolean isFocused() {
        return focused;
    }
}
