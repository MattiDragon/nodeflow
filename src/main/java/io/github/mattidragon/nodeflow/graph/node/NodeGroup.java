package io.github.mattidragon.nodeflow.graph.node;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Node groups are collections of similar nodes that are grouped together in the gui. A single node can be in multiple groups. You are free to add your own nodes to any group as long as they fit the group.
 * @param name The name of the group, used in guis.
 * @param nodes The nodes contained in the group, guaranteed to mutable.
 */
public record NodeGroup(Text name, List<NodeType<?>> nodes) {
    /**
     * Useful nodes for debugging graphs. Consider not using this group in production as it could contain unwanted nodes in the future.
     */
    public static final NodeGroup DEBUG = new NodeGroup(Text.translatable("group.nodeflow.debug"), NodeType.BROADCAST);
    /**
     * Nodes that implement control flow. Currently only the switch
     */
    public static final NodeGroup FLOW = new NodeGroup(Text.translatable("group.nodeflow.flow"), NodeType.SWITCH);
    /**
     * Nodes that perform boolean operations
     */
    public static final NodeGroup LOGIC = new NodeGroup(Text.translatable("group.nodeflow.logic"), NodeType.AND, NodeType.OR, NodeType.XOR, NodeType.NAND, NodeType.NOR, NodeType.BOOL_EQL, NodeType.NOT);
    /**
     * Nodes that perform simple math
     */
    public static final NodeGroup MATH = new NodeGroup(Text.translatable("group.nodeflow.math"), NodeType.ADD, NodeType.SUBTRACT, NodeType.MULTIPLY, NodeType.DIVIDE, NodeType.MODULO, NodeType.NEGATE, NodeType.MIN, NodeType.MAX, NodeType.POW);
    public static final NodeGroup COMPARE_NUMBER = new NodeGroup(Text.translatable("group.nodeflow.compare_number"), NodeType.NUM_EQL, NodeType.NUM_NEQL, NodeType.LESS, NodeType.GREATER);
    public static final NodeGroup CONSTANTS = new NodeGroup(Text.translatable("group.nodeflow.constants"), NodeType.ZERO, NodeType.ONE, NodeType.NAN, NodeType.INFINITY, NodeType.TRUE, NodeType.FALSE, NodeType.NUMBER, NodeType.PI, NodeType.E);
    public static final NodeGroup ADVANCED_MATH = new NodeGroup(Text.translatable("group.nodeflow.advanced_math"), NodeType.SIN, NodeType.COS, NodeType.TAN, NodeType.SINH, NodeType.COSH, NodeType.TANH, NodeType.ASIN, NodeType.ACOS, NodeType.ATAN, NodeType.LOG10, NodeType.LOG, NodeType.CBRT, NodeType.SQRT, NodeType.CEIL, NodeType.FLOOR, NodeType.ABS, NodeType.SIGNUM);

    public NodeGroup(Text name, List<NodeType<?>> nodes) {
        this.name = name;
        this.nodes = new ArrayList<>(nodes);
    }

    public NodeGroup(Text name, NodeType<?>... nodes) {
        this(name, List.of(nodes));
    }
}
