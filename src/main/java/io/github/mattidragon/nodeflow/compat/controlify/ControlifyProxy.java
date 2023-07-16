package io.github.mattidragon.nodeflow.compat.controlify;

import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
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

    void registerScreenType(Class<? extends EditorScreen> clazz);
}
