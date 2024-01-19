package io.github.mattidragon.nodeflow.client.ui.widget;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A widget that contains other widgets in a zoomable and movable field.
 * @param <T> The type of elements contained.
 * @author MattiDragon
 * @version 1.0.2
 */
public class ZoomableAreaWidget<T extends Element & Drawable & Narratable> extends AbstractParentElement implements Drawable, Selectable {
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
        zoom = MathHelper.clamp(zoom, -5, 5);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY) || !active || !visible) return false;
        /*
         Super returns true if a child accepts the click and sets that child as focused.
         We read the focused element and move it to the top.
         This is done here and not when the focus is set because keyboard nav doesn't like the child order changing.
        */
        if (super.mouseClicked(modifyX(mouseX), modifyY(mouseY), button)) {
            // Only children should be able to get focused
            //noinspection unchecked
            var focused = (T) getFocused();
            // If the node was deleted due to the click we don't want to add it back
            if (children.remove(focused)) {
                children.add(0, focused);
            }
        } else {
            setFocused(null);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY) || !active || !visible) return false;
        return super.mouseReleased(modifyX(mouseX), modifyY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isMouseOver(mouseX, mouseY) || !active || !visible) return false;
        if (super.mouseDragged(modifyX(mouseX), modifyY(mouseY), button, modifyDeltaX(deltaX), modifyDeltaY(deltaY)))
            return true;

        viewX += deltaX;
        viewY += deltaY;

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

    @Override
    public Optional<Element> hoveredElement(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY) || !active || !visible) return Optional.empty();
        return super.hoveredElement(modifyX(mouseX), modifyY(mouseY));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) return false;
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> viewY += 10;
            case GLFW.GLFW_KEY_DOWN -> viewY -= 10;
            case GLFW.GLFW_KEY_RIGHT -> viewX -= 10;
            case GLFW.GLFW_KEY_LEFT -> viewX += 10;
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> zoom(-1, x + width / 2.0, y + height / 2.0);
            case GLFW.GLFW_KEY_KP_ADD -> zoom(1, x + width / 2.0, y + height / 2.0);
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        var matrices = context.getMatrices();
        context.enableScissor(x, y, x + width, y + height);
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.translate(viewX + width / 2.0, viewY + height / 2.0, 0);
        var scale = getScale();
        matrices.scale(scale, scale, scale);

        for (var child : Lists.reverse(children)) {
            child.render(context, (int) Math.floor(modifyX(mouseX)), (int) Math.floor(modifyY(mouseY)), delta);
        }

        renderExtras(matrices, mouseX, mouseY, delta);

        matrices.pop();
        context.disableScissor();
    }

    protected void renderExtras(MatrixStack matrices, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return new ScreenRect(x, y, width, height);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        if (getFocused() instanceof Narratable narratable)
            narratable.appendNarrations(builder);
    }

    @Override
    public boolean isNarratable() {
        return active && visible;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.FOCUSED;
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
    public GuiNavigationPath getFocusedPath() {
        return this.getFocused() == null ? GuiNavigationPath.of(this) : super.getFocusedPath();
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        if (!active) return null;
        if (!this.isFocused()) return GuiNavigationPath.of(this);
        if (getFocused() == null && navigation.equals(new GuiNavigation.Tab(false))) return null;

        return super.getNavigationPath(navigation);
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
