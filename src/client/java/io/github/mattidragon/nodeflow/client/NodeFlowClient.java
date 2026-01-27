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
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Environment(EnvType.CLIENT)
public class NodeFlowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.<EditorScreenHandler, HandledEditorScreen>register(NodeFlow.SCREEN_HANDLER, HandledEditorScreen::new);
        ControlifyProxy.INSTANCE.register();
        NodeConfigScreenRegistry.registerDefaults();
        NodeGroup.registerCodec(ClientTagNodeGroup.DECODER_ID, ClientTagNodeGroup.CODEC);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            var category = KeyMapping.Category.register(NodeFlow.id("debug"));
            var debugEditorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.nodeflow.debug", GLFW.GLFW_KEY_K, category));
            var devKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.nodeflow.dev", GLFW.GLFW_KEY_M, category));
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
                while (debugEditorKey.consumeClick())
                    client.setScreen(new EditorScreen(Component.literal("Test Editor"), graph));

                while (devKey.consumeClick()) {
                    var screen = new Screen(Component.literal("Test Zoom Areas")) {
                        private ZoomableAreaWidget<Button> widget;

                        @Override
                        protected void init() {
                            super.init();
                            widget = addRenderableWidget(new ZoomableAreaWidget<>(64, 64, width - 128, height - 128));

                            widget.add(Button.builder(Component.literal("1"), button -> System.out.println("1")).bounds(0, 0, 20, 20).build());
                            widget.add(Button.builder(Component.literal("2"), button -> System.out.println("2")).bounds(0, 30, 20, 20).build());
                            widget.add(Button.builder(Component.literal("3"), button -> System.out.println("3")).bounds(30, 0, 20, 20).build());

                            //addDrawableChild(new Button(0, 0, 20, 20, Component.literal("+"), button -> {}));
                        }

                        @Override
                        protected void setInitialFocus() {
                            setInitialFocus(widget);
                        }
                    };
                    client.setScreen(screen);
                }
            });
        }
    }
}
