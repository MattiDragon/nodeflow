package io.github.mattidragon.nodeflow.graph.data;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public record DataType<T>(int color, boolean splittable) {
    public static final DefaultedRegistry<DataType<?>> REGISTRY = FabricRegistryBuilder.<DataType<?>>createDefaulted(null, NodeFlow.id("data_type"), NodeFlow.id("number")).buildAndRegister();

    public static final DataType<Double> NUMBER = register(new DataType<>(0x5555ff, true), NodeFlow.id("number"));
    public static final DataType<Boolean> BOOLEAN = register(new DataType<>(0xff5555, true), NodeFlow.id("boolean"));
    public static final DataType<String> STRING = register(new DataType<>(0x55ff55, true), NodeFlow.id("string"));

    public static void register() {}

    public DataValue<T> makeValue(T value) {
        return new DataValue<>(this, value);
    }

    public Connector<T> makeRequiredInput(String name, Node parent) {
        return new Connector<>(this, name, false, false, parent);
    }

    public Connector<T> makeRequiredOutput(String name, Node parent) {
        return new Connector<>(this, name, true, false, parent);
    }

    public Connector<T> makeOptionalInput(String name , Node parent) {
        return new Connector<>(this, name, false, true, parent);
    }

    public Connector<T> makeOptionalOutput(String name, Node parent) {
        return new Connector<>(this, name, true, true, parent);
    }

    public static <T> DataType<T> register(DataType<T> type, Identifier id) {
        Registry.register(REGISTRY, id, type);
        return type;
    }

    public Text name() {
        return Text.translatable("data_type." + REGISTRY.getId(this).toTranslationKey());
    }

    @Override
    public String toString() {
        return REGISTRY.getId(this).toString();
    }
}
