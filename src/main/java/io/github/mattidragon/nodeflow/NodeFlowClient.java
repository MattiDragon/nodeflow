package io.github.mattidragon.nodeflow;

import io.github.mattidragon.nodeflow.compat.controlify.ControlifyProxy;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.GraphEnvironment;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import io.github.mattidragon.nodeflow.ui.widget.ZoomableAreaWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Environment(EnvType.CLIENT)
public class NodeFlowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.<EditorScreenHandler, HandledEditorScreen>register(NodeFlow.SCREEN_HANDLER, HandledEditorScreen::new);
        ControlifyProxy.INSTANCE.register();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            var debugEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.nodeflow.debug", GLFW.GLFW_KEY_K, "key.categories.nodeflow"));
            var devKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.nodeflow.dev", GLFW.GLFW_KEY_M, "key.categories.nodeflow"));
            var graph = new Graph(new GraphEnvironment(NodeType.REGISTRY.stream().toList(), DataType.REGISTRY.stream().toList(), ContextType.REGISTRY.stream().toList(), List.of(NodeGroup.LOGIC, NodeGroup.DEBUG, NodeGroup.MATH, NodeGroup.FLOW, NodeGroup.ADVANCED_MATH, NodeGroup.CONSTANTS, NodeGroup.COMPARE_NUMBER)));

            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (debugEditorKey.wasPressed())
                    client.setScreen(new EditorScreen(Text.literal("Test Editor"), graph));

                while (devKey.wasPressed()) {
                    var screen = new Screen(Text.literal("Test Zoom Areas")) {
                        @Override
                        protected void init() {
                            super.init();
                            var widget = addDrawableChild(new ZoomableAreaWidget<ButtonWidget>(64, 64, width - 128, height - 128));

                            widget.add(ButtonWidget.builder(Text.literal("1"), button -> System.out.println("1")).dimensions(0, 0, 20, 20).build());
                            widget.add(ButtonWidget.builder(Text.literal("2"), button -> System.out.println("2")).dimensions(0, 30, 20, 20).build());
                            widget.add(ButtonWidget.builder(Text.literal("3"), button -> System.out.println("3")).dimensions(30, 0, 20, 20).build());

                            //addDrawableChild(new ButtonWidget(0, 0, 20, 20, Text.literal("+"), button -> {}));

                            focusOn(widget);
                        }
                    };
                    client.setScreen(screen);
                }
            });
        }
    }
}
