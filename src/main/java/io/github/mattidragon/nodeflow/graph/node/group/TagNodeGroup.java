package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/**
 * A node group that is defined in a tag file from datapacks.
 */
public record TagNodeGroup(TagKey<NodeType<?>> tag) implements NodeGroup {
    public static final Identifier DECODER_ID = NodeFlow.id("tag");
    public static final StreamCodec<FriendlyByteBuf, TagNodeGroup> CODEC = 
            StreamCodec.composite(Identifier.STREAM_CODEC.map(id -> TagKey.create(NodeType.KEY, id), TagKey::location), TagNodeGroup::tag, TagNodeGroup::new);

    @Override
    public Component getName() {
        return Component.translatable(tag.location().toLanguageKey("group"));
    }

    @Override
    public List<NodeType<?>> getTypes() {
        return StreamSupport.stream(NodeType.REGISTRY.getTagOrEmpty(tag).spliterator(), false)
                .map(Holder::value)
                .collect(Collectors.toList()); // toList gives generics error
    }

    @Override
    public Identifier getCodecId() {
        return DECODER_ID;
    }
}
