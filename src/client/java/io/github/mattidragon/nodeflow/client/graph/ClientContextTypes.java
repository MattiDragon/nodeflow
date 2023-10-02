package io.github.mattidragon.nodeflow.client.graph;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class ClientContextTypes {
    public static final ContextType<MinecraftClient> CLIENT = ContextType.register(new ContextType<>(MinecraftClient.class), NodeFlow.id("client"));
    public static final ContextType<ClientWorld> CLIENT_WORLD = ContextType.register(new ContextType<>(ClientWorld.class, new ContextType[]{ ContextType.WORLD }), NodeFlow.id("client_world"));
}
