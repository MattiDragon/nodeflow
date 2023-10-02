package io.github.mattidragon.nodeflow.client;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.client.compat.controlify.ControlifyProxy;
import io.github.mattidragon.nodeflow.client.graph.ClientTagNodeGroup;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.HandledEditorScreen;
import io.github.mattidragon.nodeflow.client.ui.widget.ZoomableAreaWidget;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.GraphEnvironment;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.NodeTypeTags;
import io.github.mattidragon.nodeflow.graph.node.group.DirectNodeGroup;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.group.TagNodeGroup;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
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
        NodeConfigScreenRegistry.registerDefaults();
        NodeGroup.registerDecoder(ClientTagNodeGroup.DECODER_ID, ClientTagNodeGroup::new);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            var debugEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.nodeflow.debug", GLFW.GLFW_KEY_K, "key.categories.nodeflow"));
            var devKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.nodeflow.dev", GLFW.GLFW_KEY_M, "key.categories.nodeflow"));
            var graph = new Graph(new GraphEnvironment(DataType.REGISTRY.stream().toList(),
                    ContextType.REGISTRY.stream().toList(),
                    List.of(new TagNodeGroup(NodeTypeTags.LOGIC),
                            new TagNodeGroup(NodeTypeTags.DEBUG),
                            new TagNodeGroup(NodeTypeTags.MATH),
                            new TagNodeGroup(NodeTypeTags.FLOW),
                            new TagNodeGroup(NodeTypeTags.ADVANCED_MATH),
                            new TagNodeGroup(NodeTypeTags.CONSTANTS),
                            new TagNodeGroup(NodeTypeTags.COMPARE_NUMBER),
                            DirectNodeGroup.misc(NodeType.REGISTRY.stream().toArray(NodeType[]::new)))));

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
