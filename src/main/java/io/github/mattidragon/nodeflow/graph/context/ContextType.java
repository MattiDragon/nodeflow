package io.github.mattidragon.nodeflow.graph.context;

import io.github.mattidragon.nodeflow.NodeFlow;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public record ContextType<T>(Class<T> type, ContextType<?>[] parents) {
    public static final ResourceKey<Registry<ContextType<?>>> KEY = ResourceKey.createRegistryKey(NodeFlow.id("context_type"));
    public static final DefaultedRegistry<ContextType<?>> REGISTRY = FabricRegistryBuilder.createDefaulted(KEY, NodeFlow.id("dummy")).buildAndRegister();

    public static final ContextType<MinecraftServer> SERVER = register(new ContextType<>(MinecraftServer.class), NodeFlow.id("server"));
    public static final ContextType<Level> WORLD = register(new ContextType<>(Level.class), NodeFlow.id("world"));
    public static final ContextType<ServerLevel> SERVER_WORLD = register(new ContextType<>(ServerLevel.class, new ContextType[]{ WORLD }), NodeFlow.id("server_world"));
    public static final ContextType<BlockPos> BLOCK_POS = register(new ContextType<>(BlockPos.class), NodeFlow.id("block_pos"));
    private static final ContextType<Void> DUMMY = register(new ContextType<>(Void.class), NodeFlow.id("dummy"));

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

    public static <T> ContextType<T> register(ContextType<T> type, ResourceLocation id) {
        Registry.register(REGISTRY, id, type);
        return type;
    }

    @Override
    public String toString() {
        return REGISTRY.getKey(this).toString();
    }
}
