package io.github.mattidragon.nodeflow.graph.node;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.builtin.NumberNode;
import io.github.mattidragon.nodeflow.graph.node.builtin.SendNumberNode;
import io.github.mattidragon.nodeflow.graph.node.builtin.SwitchNode;
import io.github.mattidragon.nodeflow.graph.node.builtin.TimeNode;
import io.github.mattidragon.nodeflow.graph.node.builtin.base.BinaryOperationNode;
import io.github.mattidragon.nodeflow.graph.node.builtin.base.ConstantNode;
import io.github.mattidragon.nodeflow.graph.node.builtin.base.UnaryOperationNode;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public record NodeType<T extends Node>(Function<Graph, T> generator) {
    public static final RegistryKey<Registry<NodeType<?>>> KEY = RegistryKey.ofRegistry(NodeFlow.id("node_type"));
    public static final DefaultedRegistry<NodeType<?>> REGISTRY = FabricRegistryBuilder.createDefaulted(KEY, NodeFlow.id("time")).buildAndRegister();

    public static final NodeType<SendNumberNode> BROADCAST = register(new NodeType<>(SendNumberNode::new), NodeFlow.id("broadcast"));
    public static final NodeType<TimeNode> TIME = register(new NodeType<>(TimeNode::new), NodeFlow.id("time"));
    public static final NodeType<SwitchNode> SWITCH = register(new NodeType<>(SwitchNode::new), NodeFlow.id("switch"));
    public static final NodeType<NumberNode> NUMBER = register(new NodeType<>(NumberNode::new), NodeFlow.id("number"));

    public static final NodeType<ConstantNode> PI = register(ConstantNode.makeType(DataType.NUMBER.makeValue(Math.PI)), NodeFlow.id("pi"));
    public static final NodeType<ConstantNode> E = register(ConstantNode.makeType(DataType.NUMBER.makeValue(Math.E)), NodeFlow.id("e"));
    public static final NodeType<ConstantNode> ZERO = register(ConstantNode.makeType(DataType.NUMBER.makeValue(0.0)), NodeFlow.id("zero"));
    public static final NodeType<ConstantNode> ONE = register(ConstantNode.makeType(DataType.NUMBER.makeValue(1.0)), NodeFlow.id("one"));
    public static final NodeType<ConstantNode> NAN = register(ConstantNode.makeType(DataType.NUMBER.makeValue(Double.NaN)), NodeFlow.id("nan"));
    public static final NodeType<ConstantNode> INFINITY = register(ConstantNode.makeType(DataType.NUMBER.makeValue(Double.POSITIVE_INFINITY)), NodeFlow.id("infinity"));
    public static final NodeType<ConstantNode> TRUE = register(ConstantNode.makeType(DataType.BOOLEAN.makeValue(true)), NodeFlow.id("true"));
    public static final NodeType<ConstantNode> FALSE = register(ConstantNode.makeType(DataType.BOOLEAN.makeValue(false)), NodeFlow.id("false"));

    public static final NodeType<UnaryOperationNode<Boolean, Boolean>> NOT = register(UnaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, input -> !input), NodeFlow.id("not"));
    public static final NodeType<BinaryOperationNode<Boolean, Boolean>> AND = register(BinaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, Boolean::logicalAnd), NodeFlow.id("and"));
    public static final NodeType<BinaryOperationNode<Boolean, Boolean>> OR = register(BinaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, Boolean::logicalOr), NodeFlow.id("or"));
    public static final NodeType<BinaryOperationNode<Boolean, Boolean>> XOR = register(BinaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, Boolean::logicalXor), NodeFlow.id("xor"));
    public static final NodeType<BinaryOperationNode<Boolean, Boolean>> NAND = register(BinaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, (first, second) -> !(first && second)), NodeFlow.id("nand"));
    public static final NodeType<BinaryOperationNode<Boolean, Boolean>> NOR = register(BinaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, (first, second) -> !(first || second)), NodeFlow.id("nor"));
    public static final NodeType<BinaryOperationNode<Boolean, Boolean>> BOOL_EQL = register(BinaryOperationNode.makeType(DataType.BOOLEAN, DataType.BOOLEAN, Boolean::equals), NodeFlow.id("bool_eql"));

    public static final NodeType<UnaryOperationNode<Double, Double>> NEGATE = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, input -> -input), NodeFlow.id("negate"));
    public static final NodeType<BinaryOperationNode<Double, Double>> ADD = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Double::sum), NodeFlow.id("add"));
    public static final NodeType<BinaryOperationNode<Double, Double>> SUBTRACT = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, (first, second) -> first - second), NodeFlow.id("subtract"));
    public static final NodeType<BinaryOperationNode<Double, Double>> MULTIPLY = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, (first, second) -> first * second), NodeFlow.id("multiply"));
    public static final NodeType<BinaryOperationNode<Double, Double>> DIVIDE = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, (first, second) -> first / second), NodeFlow.id("divide"));
    public static final NodeType<BinaryOperationNode<Double, Double>> MODULO = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, (first, second) -> first % second), NodeFlow.id("modulo"));
    public static final NodeType<BinaryOperationNode<Double, Double>> MIN = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::min), NodeFlow.id("min"));
    public static final NodeType<BinaryOperationNode<Double, Double>> MAX = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::max), NodeFlow.id("max"));
    public static final NodeType<BinaryOperationNode<Double, Double>> POW = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::pow), NodeFlow.id("pow"));

    // Advanced math
    public static final NodeType<UnaryOperationNode<Double, Double>> SIN = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::sin), NodeFlow.id("sin"));
    public static final NodeType<UnaryOperationNode<Double, Double>> COS = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::cos), NodeFlow.id("cos"));
    public static final NodeType<UnaryOperationNode<Double, Double>> TAN = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::tan), NodeFlow.id("tan"));
    public static final NodeType<UnaryOperationNode<Double, Double>> SINH = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::sinh), NodeFlow.id("sinh"));
    public static final NodeType<UnaryOperationNode<Double, Double>> COSH = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::cosh), NodeFlow.id("cosh"));
    public static final NodeType<UnaryOperationNode<Double, Double>> TANH = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::tanh), NodeFlow.id("tanh"));
    public static final NodeType<UnaryOperationNode<Double, Double>> ASIN = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::asin), NodeFlow.id("asin"));
    public static final NodeType<UnaryOperationNode<Double, Double>> ACOS = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::acos), NodeFlow.id("acos"));
    public static final NodeType<UnaryOperationNode<Double, Double>> ATAN = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::atan), NodeFlow.id("atan"));
    public static final NodeType<UnaryOperationNode<Double, Double>> LOG10 = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::log10), NodeFlow.id("log10"));
    public static final NodeType<UnaryOperationNode<Double, Double>> LOG = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::log), NodeFlow.id("log"));
    public static final NodeType<UnaryOperationNode<Double, Double>> CBRT = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::cbrt), NodeFlow.id("cbrt"));
    public static final NodeType<UnaryOperationNode<Double, Double>> SQRT = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::sqrt), NodeFlow.id("sqrt"));
    public static final NodeType<UnaryOperationNode<Double, Double>> CEIL = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::ceil), NodeFlow.id("ceil"));
    public static final NodeType<UnaryOperationNode<Double, Double>> FLOOR = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::floor), NodeFlow.id("floor"));
    public static final NodeType<UnaryOperationNode<Double, Double>> ABS = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::abs), NodeFlow.id("abs"));
    public static final NodeType<UnaryOperationNode<Double, Double>> SIGNUM = register(UnaryOperationNode.makeType(DataType.NUMBER, DataType.NUMBER, Math::signum), NodeFlow.id("signum"));

    public static final NodeType<BinaryOperationNode<Double, Boolean>> NUM_EQL = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.BOOLEAN, (first, second) -> first == (double) second), NodeFlow.id("num_eql"));
    public static final NodeType<BinaryOperationNode<Double, Boolean>> NUM_NEQL = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.BOOLEAN, (first, second) -> first != (double) second), NodeFlow.id("num_neql"));
    public static final NodeType<BinaryOperationNode<Double, Boolean>> LESS = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.BOOLEAN, (first, second) -> first < second), NodeFlow.id("less"));
    public static final NodeType<BinaryOperationNode<Double, Boolean>> GREATER = register(BinaryOperationNode.makeType(DataType.NUMBER, DataType.BOOLEAN, (first, second) -> first > second), NodeFlow.id("greater"));

    public static void register() {}

    public static <T extends Node> NodeType<T> register(NodeType<T> type, Identifier id) {
        Registry.register(REGISTRY, id, type);
        return type;
    }

    public Text name() {
        return Text.translatable("node." + REGISTRY.getId(this).toTranslationKey());
    }

    @Override
    public String toString() {
        return REGISTRY.getId(this).toString();
    }
}
