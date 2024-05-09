package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public record DirectNodeGroup(Text name, List<NodeType<?>> types) implements NodeGroup {
    public static final Identifier DECODER_ID = NodeFlow.id("direct");
    public static final PacketCodec<RegistryByteBuf, DirectNodeGroup> CODEC =
            PacketCodec.tuple(TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, DirectNodeGroup::name, 
                    PacketCodecs.registryValue(NodeType.REGISTRY.getKey()).collect(PacketCodecs.toList()), DirectNodeGroup::types,
                    DirectNodeGroup::new);

    public DirectNodeGroup(Text name, NodeType<?>... types) {
        this(name, Arrays.asList(types));
    }

    public static DirectNodeGroup misc(NodeType<?>... types) {
        return new DirectNodeGroup(Text.translatable("group.nodeflow.misc"), types);
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
    public Identifier getCodecId() {
        return DECODER_ID;
    }
}
