package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public record DirectNodeGroup(Text name, List<NodeType<?>> types) implements NodeGroup {
    public static final Identifier DECODER_ID = NodeFlow.id("direct");

    public DirectNodeGroup(PacketByteBuf buf) {
        this(buf.readText(), buf.readList(buf1 -> buf1.readRegistryValue(NodeType.REGISTRY)));
    }

    public DirectNodeGroup(Text name, NodeType<?>... types) {
        this(name, Arrays.asList(types));
    }

    public static DirectNodeGroup misc(NodeType<?>... types) {
        return new DirectNodeGroup(Text.translatable("group.nodeflow.misc"), Arrays.asList(types));
    }

    @Override
    public Text getName() {
        return name;
    }

    @Override
    public List<NodeType<?>> getTypes() {
        return types;
    }

    @Override
    public Identifier getDecoderId() {
        return DECODER_ID;
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        buf.writeText(name);
        buf.writeCollection(types, (buf1, type) -> buf1.writeRegistryValue(NodeType.REGISTRY, type));
    }
}
