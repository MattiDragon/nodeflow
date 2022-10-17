package io.github.mattidragon.nodeflow.graph;

import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.Node;

public record Connector<T>(DataType<T> type, String id, boolean isOutput, boolean isOptional, Node parent) {
}
