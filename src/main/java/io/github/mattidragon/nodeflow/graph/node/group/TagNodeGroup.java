package io.github.mattidragon.nodeflow.graph.node.group;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A node group that is defined in a tag file from datapacks.
 */
public record TagNodeGroup(TagKey<NodeType<?>> tag) implements NodeGroup {
    public static final Identifier DECODER_ID = NodeFlow.id("tag");

    public TagNodeGroup(PacketByteBuf buf) {
        this(TagKey.of(NodeType.KEY, buf.readIdentifier()));
    }

    @Override
    public Text getName() {
        return Text.translatable(tag.id().toTranslationKey("group"));
    }

    @Override
    public List<NodeType<?>> getTypes() {
        return StreamSupport.stream(NodeType.REGISTRY.iterateEntries(tag).spliterator(), false)
                .map(RegistryEntry::value)
                .collect(Collectors.toList()); // toList gives generics error
    }

    @Override
    public Identifier getDecoderId() {
        return DECODER_ID;
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        buf.writeIdentifier(tag.id());
    }
}
