package io.github.mattidragon.nodeflow.graph.node.builtin.base;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class TypedNode extends Node {
    protected DataType<?> type;

    protected TypedNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
    }

    public DataType<?> getType() {
        return type;
    }

    public void setType(DataType<?> type) {
        this.type = type;
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        type = DataType.REGISTRY.get(new Identifier(data.getString("data_type")));
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("data_type", DataType.REGISTRY.getId(type).toString());
    }

}
