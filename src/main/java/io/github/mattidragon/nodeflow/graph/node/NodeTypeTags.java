package io.github.mattidragon.nodeflow.graph.node;

import io.github.mattidragon.nodeflow.NodeFlow;
import net.minecraft.tags.TagKey;

public class NodeTypeTags {
    public static final TagKey<NodeType<?>> DEBUG = TagKey.create(NodeType.KEY, NodeFlow.id("debug"));
    public static final TagKey<NodeType<?>> FLOW = TagKey.create(NodeType.KEY, NodeFlow.id("flow"));
    public static final TagKey<NodeType<?>> LOGIC = TagKey.create(NodeType.KEY, NodeFlow.id("logic"));
    public static final TagKey<NodeType<?>> MATH = TagKey.create(NodeType.KEY, NodeFlow.id("math"));
    public static final TagKey<NodeType<?>> COMPARE_NUMBER = TagKey.create(NodeType.KEY, NodeFlow.id("compare_number"));
    public static final TagKey<NodeType<?>> CONSTANTS = TagKey.create(NodeType.KEY, NodeFlow.id("constant"));
    public static final TagKey<NodeType<?>> ADVANCED_MATH = TagKey.create(NodeType.KEY, NodeFlow.id("advanced_math"));

    private NodeTypeTags() {}
}
