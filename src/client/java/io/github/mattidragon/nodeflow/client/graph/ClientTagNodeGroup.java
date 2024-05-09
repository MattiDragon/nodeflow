package io.github.mattidragon.nodeflow.client.graph;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.group.TagNodeGroup;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A node group that is defined in a tag files. Loaded using the fabric client tag api.
 * This is only safe to use for fully client side graph environments.
 */
public record ClientTagNodeGroup(TagKey<NodeType<?>> tag) implements NodeGroup {
    public static final Identifier DECODER_ID = NodeFlow.id("client_tag");
    public static final PacketCodec<PacketByteBuf, TagNodeGroup> CODEC =
            PacketCodec.tuple(Identifier.PACKET_CODEC.xmap(id -> TagKey.of(NodeType.KEY, id), TagKey::id), TagNodeGroup::tag, TagNodeGroup::new);

    @Override
    public Text getName() {
        return Text.translatable(tag.id().toTranslationKey("group"));
    }

    @Override
    public List<NodeType<?>> getTypes() {
        return ClientTags.getOrCreateLocalTag(tag)
                .stream()
                .map(NodeType.REGISTRY::get)
                .distinct()
                .collect(Collectors.toList()); // toList gives generics error
    }

    @Override
    public Identifier getCodecId() {
        return DECODER_ID;
    }
}
