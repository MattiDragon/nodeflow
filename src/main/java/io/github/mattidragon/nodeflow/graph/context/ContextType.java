package io.github.mattidragon.nodeflow.graph.context;

import io.github.mattidragon.nodeflow.NodeFlow;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record ContextType<T>(Class<T> type, ContextType<?>[] parents) {
    public static final DefaultedRegistry<ContextType<?>> REGISTRY = FabricRegistryBuilder.<ContextType<?>>createDefaulted(null, NodeFlow.id("context_type"), NodeFlow.id("dummy")).buildAndRegister();

    public static final ContextType<MinecraftServer> SERVER = register(new ContextType<>(MinecraftServer.class), NodeFlow.id("server"));
    public static final ContextType<World> WORLD = register(new ContextType<>(World.class), NodeFlow.id("world"));
    public static final ContextType<ServerWorld> SERVER_WORLD = register(new ContextType<>(ServerWorld.class, new ContextType[]{ WORLD }), NodeFlow.id("server_world"));
    public static final ContextType<BlockPos> BLOCK_POS = register(new ContextType<>(BlockPos.class), NodeFlow.id("block_pos"));
    private static final ContextType<Void> DUMMY = register(new ContextType<>(Void.class), NodeFlow.id("dummy"));

    @Environment(EnvType.CLIENT)
    public static final ContextType<MinecraftClient> CLIENT;
    @Environment(EnvType.CLIENT)
    public static final ContextType<ClientWorld> CLIENT_WORLD;

    static {
        // They should only be referenced on the client anyway
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            CLIENT = register(new ContextType<>(MinecraftClient.class), NodeFlow.id("client"));
            CLIENT_WORLD = register(new ContextType<>(ClientWorld.class, new ContextType[]{ WORLD }), NodeFlow.id("client_world"));
        } else {
            // This somehow doesn't cause NoSuchFieldErrors on servers (maybe fabric strips null values)?
            CLIENT = null;
            CLIENT_WORLD = null;
        }
    }

    public ContextType(Class<T> type, ContextType<?>[] parents) {
        this.type = type;
        for (var parent : parents) {
            if (parent == null)
                throw new IllegalArgumentException("Null parent");
            if (!parent.type.isAssignableFrom(type))
                throw new IllegalArgumentException("Not a subclass of parent");
        }
        this.parents = parents;
    }

    public ContextType(Class<T> type) {
        this(type, new ContextType[0]);
    }

    public static void register() {}

    public static <T> ContextType<T> register(ContextType<T> type, Identifier id) {
        Registry.register(REGISTRY, id, type);
        return type;
    }

    @Override
    public String toString() {
        return REGISTRY.getId(this).toString();
    }
}
