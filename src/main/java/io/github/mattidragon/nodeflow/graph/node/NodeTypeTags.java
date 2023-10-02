package io.github.mattidragon.nodeflow.graph.node;

import io.github.mattidragon.nodeflow.NodeFlow;
import net.minecraft.registry.tag.TagKey;

public class NodeTypeTags {
    public static final TagKey<NodeType<?>> DEBUG = TagKey.of(NodeType.KEY, NodeFlow.id("debug"));
    public static final TagKey<NodeType<?>> FLOW = TagKey.of(NodeType.KEY, NodeFlow.id("flow"));
    public static final TagKey<NodeType<?>> LOGIC = TagKey.of(NodeType.KEY, NodeFlow.id("logic"));
    public static final TagKey<NodeType<?>> MATH = TagKey.of(NodeType.KEY, NodeFlow.id("math"));
    public static final TagKey<NodeType<?>> COMPARE_NUMBER = TagKey.of(NodeType.KEY, NodeFlow.id("compare_number"));
    public static final TagKey<NodeType<?>> CONSTANTS = TagKey.of(NodeType.KEY, NodeFlow.id("constant"));
    public static final TagKey<NodeType<?>> ADVANCED_MATH = TagKey.of(NodeType.KEY, NodeFlow.id("advanced_math"));

    private NodeTypeTags() {}
}
