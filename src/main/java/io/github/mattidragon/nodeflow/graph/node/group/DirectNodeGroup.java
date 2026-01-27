package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record DirectNodeGroup(Component name, List<NodeType<?>> types) implements NodeGroup {
    public static final Identifier DECODER_ID = NodeFlow.id("direct");
    public static final StreamCodec<RegistryFriendlyByteBuf, DirectNodeGroup> CODEC =
            StreamCodec.composite(ComponentSerialization.TRUSTED_STREAM_CODEC, DirectNodeGroup::name, 
                    ByteBufCodecs.registry(NodeType.REGISTRY.key()).apply(ByteBufCodecs.list()), DirectNodeGroup::types,
                    DirectNodeGroup::new);

    public DirectNodeGroup(Component name, NodeType<?>... types) {
        this(name, Arrays.asList(types));
    }

    public static DirectNodeGroup misc(NodeType<?>... types) {
        return new DirectNodeGroup(Component.translatable("group.nodeflow.misc"), types);
    }

    @Override
    public Component getName() {
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
