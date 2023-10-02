package io.github.mattidragon.nodeflow.client.ui;

import io.github.mattidragon.nodeflow.client.ui.node.NumberNodeConfigScreen;
import io.github.mattidragon.nodeflow.client.ui.node.TypedNodeConfigScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class NodeConfigScreenRegistry {
    private static final Map<NodeType<?>, Factory<?>> FACTORIES = new HashMap<>();

    public static void registerDefaults() {
        register(NumberNodeConfigScreen::new, NodeType.NUMBER);
        register(TypedNodeConfigScreen::new, NodeType.SWITCH);
    }

    @SafeVarargs
    public static <T extends Node> void register(Factory<T> factory, NodeType<? extends T>... types) {
        for (var type : types) {
            FACTORIES.put(type, factory);
        }
    }

    public static boolean hasConfig(Node node) {
        return FACTORIES.containsKey(node.type);
    }

    public static <T extends Node> NodeConfigScreen<T> createScreen(T node, EditorScreen parent) {
        /*
         Javas type system can't express the relationship in the map properly,
         so we need to cast when retrieving. Unfortunately, java doesn't allow us
         to cast to wildcard types, that is unless we use a generic method to do it
        */
        return unsafeCast(FACTORIES.get(node.type).apply(unsafeCast(node), parent));
    }

    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(Object object) {
        return (T) object;
    }

    public interface Factory<T extends Node> extends BiFunction<T, EditorScreen, NodeConfigScreen<T>> {
        @Override
        NodeConfigScreen<T> apply(T node, EditorScreen parent);
    }
}
