package io.github.mattidragon.nodeflow.client.graph;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.graph.node.group.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.group.TagNodeGroup;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import java.util.List;

/**
 * A node group that is defined in a tag files. Loaded using the fabric client tag api.
 * This is only safe to use for fully client side graph environments.
 */
public record ClientTagNodeGroup(TagKey<NodeType<?>> tag) implements NodeGroup {
    public static final Identifier CODEC_ID = NodeFlow.id("client_tag");
    public static final StreamCodec<FriendlyByteBuf, TagNodeGroup> CODEC =
            StreamCodec.composite(Identifier.STREAM_CODEC.map(id -> TagKey.create(NodeType.KEY, id), TagKey::location), TagNodeGroup::tag, TagNodeGroup::new);

    @Override
    public Component getName() {
        return Component.translatable(tag.location().toLanguageKey("group"));
    }

    @Override
    public List<NodeType<?>> getTypes() {
        return ClientTags.getOrCreateLocalTag(tag)
                .stream()
                .<NodeType<?>>map(NodeType.REGISTRY::getValue)
                .distinct()
                .toList();
    }

    @Override
    public Identifier getCodecId() {
        return CODEC_ID;
    }
}
