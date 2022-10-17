package io.github.mattidragon.nodeflow.graph;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record Connection(UUID targetUuid, String targetName, UUID sourceUuid, String sourceName) {
    @Nullable
    public static Connection fromNbt(NbtCompound data) {
        if (data.containsUuid("inputUuid") && data.containsUuid("outputUuid"))
            return new Connection(data.getUuid("inputUuid"), data.getString("inputName"), data.getUuid("outputUuid"), data.getString("outputName"));
        return null;
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        nbt.putUuid("inputUuid", targetUuid);
        nbt.putString("inputName", targetName);
        nbt.putUuid("outputUuid", sourceUuid);
        nbt.putString("outputName", sourceName);
        return nbt;
    }

    public Connector<?> getTargetConnector(Graph graph) {
        for (var input : graph.getNode(targetUuid).getInputs()) {
            if (input.id().equals(targetName))
                return input;
        }
        return null;
    }

    public Connector<?> getSourceConnector(Graph graph) {
        for (var output : graph.getNode(sourceUuid).getOutputs()) {
            if (output.id().equals(sourceName))
                return output;
        }
        return null;
    }
}
