package io.github.mattidragon.nodeflow.graph.node.builtin.base;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.List;

public abstract class TypedNode extends Node {
    protected DataType<?> type;

    protected TypedNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
        this.type = getDefaultType();
    }

    protected DataType<?> getDefaultType() {
        var allowedDataTypes = this.graph.env.allowedDataTypes();
        return allowedDataTypes.contains(DataType.NUMBER) || allowedDataTypes.isEmpty() ? DataType.NUMBER : allowedDataTypes.getFirst();
    }

    public DataType<?> getType() {
        return type;
    }

    public void setType(DataType<?> type) {
        this.type = type;
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        type = view.read("data_type", DataType.REGISTRY.getCodec()).orElseGet(this::getDefaultType);
    }

    @Override
    public void writeData(WriteView view) {
        super.writeData(view);
        view.putString("data_type", DataType.REGISTRY.getId(type).toString());
    }
}
