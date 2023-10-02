package io.github.mattidragon.nodeflow.client.compat.controlify;

import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import net.fabricmc.loader.api.FabricLoader;

public interface ControlifyProxy {
    ControlifyProxy INSTANCE = FabricLoader.getInstance().isModLoaded("controlify") ? new ControlifyPlugin() : new ControlifyProxy() {
        @Override
        public void register() {
        }

        @Override
        public void registerScreenType(Class<? extends EditorScreen> clazz) {
        }
    };

    void register();

    // This method is used by advanced networking
    void registerScreenType(Class<? extends EditorScreen> clazz);
}
