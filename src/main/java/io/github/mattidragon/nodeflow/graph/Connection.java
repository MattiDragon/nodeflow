package io.github.mattidragon.nodeflow.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record Connection(UUID targetUuid, String targetName, UUID sourceUuid, String sourceName) {
    public static final Codec<Connection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.AUTHLIB_CODEC.fieldOf("inputUuid").forGetter(Connection::targetUuid),
            Codec.STRING.fieldOf("inputName").forGetter(Connection::targetName),
            UUIDUtil.AUTHLIB_CODEC.fieldOf("outputUuid").forGetter(Connection::sourceUuid),
            Codec.STRING.fieldOf("outputName").forGetter(Connection::sourceName)
    ).apply(instance, Connection::new));

    public @Nullable Connector<?> getTargetConnector(Graph graph) {
        for (var input : graph.getNode(targetUuid).getInputs()) {
            if (input.id().equals(targetName))
                return input;
        }
        return null;
    }

    public @Nullable Connector<?> getSourceConnector(Graph graph) {
        for (var output : graph.getNode(sourceUuid).getOutputs()) {
            if (output.id().equals(sourceName))
                return output;
        }
        return null;
    }
}
