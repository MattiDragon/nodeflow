package io.github.mattidragon.nodeflow.graph.data;

public record DataValue<T>(DataType<T> type, T value) {
    @SuppressWarnings("unchecked")
    public <O> O getAs(DataType<O> other) {
        if (type != other) throw new ClassCastException("Tried to get input as wrong type!");
        return (O) value;
    }
}
