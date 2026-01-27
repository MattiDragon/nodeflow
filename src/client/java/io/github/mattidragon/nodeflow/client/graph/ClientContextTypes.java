package io.github.mattidragon.nodeflow.client.graph;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class ClientContextTypes {
    public static final ContextType<Minecraft> CLIENT = ContextType.register(new ContextType<>(Minecraft.class), NodeFlow.id("client"));
    public static final ContextType<ClientLevel> CLIENT_LEVEL = ContextType.register(new ContextType<>(ClientLevel.class, new ContextType[]{ ContextType.LEVEL}), NodeFlow.id("client_level"));
}
