package io.github.mattidragon.nodeflow.client.compat.controlify;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.MenuEditorScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class ControlifyPlugin implements ControlifyProxy {
    private static final Component NODEFLOW_CATEGORY = Component.translatable("key.categories.nodeflow");
    private InputBindingSupplier editorUpKey;
    private InputBindingSupplier editorDownKey;
    private InputBindingSupplier editorLeftKey;
    private InputBindingSupplier editorRightKey;

    @Override
    public void register() {
        registerScreenType(EditorScreen.class);
        registerScreenType(MenuEditorScreen.class);
        editorUpKey = ControlifyBindApi.get().registerBinding(builder ->
                builder.defaultInput(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_UP))
                        .id(NodeFlow.id("editor_up"))
                        .category(NODEFLOW_CATEGORY)
                        .allowedContexts(BindContext.V_MOUSE_CURSOR));
        editorDownKey = ControlifyBindApi.get().registerBinding(builder ->
                builder.defaultInput(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_DOWN))
                        .id(NodeFlow.id("editor_down"))
                        .category(NODEFLOW_CATEGORY)
                        .allowedContexts(BindContext.V_MOUSE_CURSOR));
        editorLeftKey = ControlifyBindApi.get().registerBinding(builder ->
                builder.defaultInput(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_LEFT))
                        .id(NodeFlow.id("editor_left"))
                        .category(NODEFLOW_CATEGORY)
                        .allowedContexts(BindContext.V_MOUSE_CURSOR));
        editorRightKey = ControlifyBindApi.get().registerBinding(builder ->
                builder.defaultInput(GamepadInputs.getBind(GamepadInputs.RIGHT_STICK_AXIS_RIGHT))
                        .id(NodeFlow.id("editor_right"))
                        .category(NODEFLOW_CATEGORY)
                        .allowedContexts(BindContext.V_MOUSE_CURSOR));
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
            ButtonGuidePredicate<Button> predicate = _ -> !Controlify.instance().virtualMouseHandler().isVirtualMouseEnabled();

            ButtonGuideApi.addGuideToButton(screen.plusButton, ControlifyBindings.GUI_ABSTRACT_ACTION_1, predicate);
            ButtonGuideApi.addGuideToButton(screen.deleteButton, ControlifyBindings.GUI_ABSTRACT_ACTION_2, predicate);
            ButtonGuideApi.addGuideToButton(screen.backButton, ControlifyBindings.GUI_BACK, predicate);
        }

        @Override
        protected void handleButtons(ControllerEntity controller) {
            var iwm = new InputWithModifiers() {
                @Override
                public @InputConstants.Value int input() {
                    return 0;
                }

                @Override
                public @Modifiers int modifiers() {
                    return 0;
                }
            };

            if (screen.plusButton.active && ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).justPressed()) screen.plusButton.onPress(iwm);
            if (screen.deleteButton.active && ControlifyBindings.GUI_ABSTRACT_ACTION_2.on(controller).justPressed()) screen.deleteButton.onPress(iwm);
            if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
                if (screen.backButton.active) {
                    screen.backButton.onPress(iwm);
                } else {
                    screen.onClose();
                }
            }

            if (ControlifyBindings.GUI_PRESS.on(controller).justPressed())
                screen.keyPressed(new KeyEvent(GLFW.GLFW_KEY_ENTER, 0, 0));

            var area = screen.getArea();
            var impulseX = editorRightKey.on(controller).analogueNow() - editorLeftKey.on(controller).analogueNow();
            var impulseY = editorDownKey.on(controller).analogueNow() - editorUpKey.on(controller).analogueNow();
            area.setViewX(area.getViewX() + impulseX * Mth.abs(impulseX) * -10f);
            area.setViewY(area.getViewY() + impulseY * Mth.abs(impulseY) * -10f);
        }

        @Override
        protected void handleScreenVMouse(ControllerEntity controller, VirtualMouseHandler vmouse) {
            var area = screen.getArea();
            var impulseX = editorRightKey.on(controller).analogueNow() - editorLeftKey.on(controller).analogueNow();
            var impulseY = editorDownKey.on(controller).analogueNow() - editorUpKey.on(controller).analogueNow();
            area.setViewX(area.getViewX() + impulseX * Mth.abs(impulseX) * -10f);
            area.setViewY(area.getViewY() + impulseY * Mth.abs(impulseY) * -10f);
        }
    }
}
