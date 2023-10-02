package io.github.mattidragon.nodeflow.client.compat.controlify;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.buttonguide.ButtonRenderPosition;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.BindContexts;
import dev.isxander.controlify.bindings.ControllerBindings;
import dev.isxander.controlify.bindings.GamepadBinds;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.HandledEditorScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class ControlifyPlugin implements ControlifyProxy {
    private static final Text NODEFLOW_CATEGORY = Text.translatable("key.categories.nodeflow");
    private static final BindContext BIND_CONTEXT = new BindContext(NodeFlow.id("nodeflow"), Set.of(BindContexts.GUI_VMOUSE_CURSOR_ONLY));
    private BindingSupplier editorUpKey;
    private BindingSupplier editorDownKey;
    private BindingSupplier editorLeftKey;
    private BindingSupplier editorRightKey;

    @Override
    public void register() {
        registerScreenType(EditorScreen.class);
        registerScreenType(HandledEditorScreen.class);
        editorUpKey = ControllerBindings.Api.INSTANCE.registerBind(NodeFlow.id("editor_up"), builder ->
                builder.defaultBind(GamepadBinds.RIGHT_STICK_FORWARD)
                        .category(NODEFLOW_CATEGORY)
                        .context(BIND_CONTEXT));
        editorDownKey = ControllerBindings.Api.INSTANCE.registerBind(NodeFlow.id("editor_down"), builder ->
                builder.defaultBind(GamepadBinds.RIGHT_STICK_BACKWARD)
                        .category(NODEFLOW_CATEGORY)
                        .context(BIND_CONTEXT));
        editorLeftKey = ControllerBindings.Api.INSTANCE.registerBind(NodeFlow.id("editor_left"), builder ->
                builder.defaultBind(GamepadBinds.RIGHT_STICK_LEFT)
                        .category(NODEFLOW_CATEGORY)
                        .context(BIND_CONTEXT));
        editorRightKey = ControllerBindings.Api.INSTANCE.registerBind(NodeFlow.id("editor_right"), builder ->
                builder.defaultBind(GamepadBinds.RIGHT_STICK_RIGHT)
                        .category(NODEFLOW_CATEGORY)
                        .context(BIND_CONTEXT));
    }

    @Override
    public void registerScreenType(Class<? extends EditorScreen> clazz) {
        ScreenProcessorProvider.registerProvider(clazz, EditorScreenProcessor::new);
    }

    private class EditorScreenProcessor extends ScreenProcessor<EditorScreen> {
        public EditorScreenProcessor(EditorScreen screen) {
            super(screen);
        }

        @Override
        public void onWidgetRebuild() {
            super.onWidgetRebuild();
            ButtonGuidePredicate<ButtonWidget> predicate = button -> !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled();

            ButtonGuideApi.addGuideToButton(screen.plusButton, controller -> controller.bindings().GUI_ABSTRACT_ACTION_1, ButtonRenderPosition.TEXT, predicate);
            ButtonGuideApi.addGuideToButton(screen.deleteButton, controller -> controller.bindings().GUI_ABSTRACT_ACTION_2, ButtonRenderPosition.TEXT, predicate);
            ButtonGuideApi.addGuideToButton(screen.backButton, controller -> controller.bindings().GUI_BACK, ButtonRenderPosition.TEXT, predicate);
        }

        @Override
        protected void handleButtons(Controller<?, ?> controller) {
            if (screen.plusButton.active && controller.bindings().GUI_ABSTRACT_ACTION_1.justPressed()) screen.plusButton.onPress();
            if (screen.deleteButton.active && controller.bindings().GUI_ABSTRACT_ACTION_2.justPressed()) screen.deleteButton.onPress();
            if (controller.bindings().GUI_BACK.justPressed()) {
                if (screen.backButton.active) {
                    screen.backButton.onPress();
                } else {
                    screen.close();
                }
            }

            if (controller.bindings().GUI_PRESS.justPressed())
                screen.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);

            var area = screen.getArea();
            var impulseX = editorRightKey.onController(controller).state() - editorLeftKey.onController(controller).state();
            var impulseY = editorDownKey.onController(controller).state() - editorUpKey.onController(controller).state();
            area.setViewX(area.getViewX() + impulseX * MathHelper.abs(impulseX) * -10f);
            area.setViewY(area.getViewY() + impulseY * MathHelper.abs(impulseY) * -10f);
        }

        @Override
        protected void handleScreenVMouse(Controller<?, ?> controller, VirtualMouseHandler vmouse) {
            var area = screen.getArea();
            var impulseX = editorRightKey.onController(controller).state() - editorLeftKey.onController(controller).state();
            var impulseY = editorDownKey.onController(controller).state() - editorUpKey.onController(controller).state();
            area.setViewX(area.getViewX() + impulseX * MathHelper.abs(impulseX) * -10f);
            area.setViewY(area.getViewY() + impulseY * MathHelper.abs(impulseY) * -10f);
        }
    }
}
