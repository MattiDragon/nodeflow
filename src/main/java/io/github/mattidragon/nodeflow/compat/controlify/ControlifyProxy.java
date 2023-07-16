package io.github.mattidragon.nodeflow.compat.controlify;

import net.fabricmc.loader.api.FabricLoader;

public interface ControlifyProxy {
    ControlifyProxy INSTANCE = FabricLoader.getInstance().isModLoaded("controlify") ? new ControlifyPlugin() : new ControlifyProxy() {
        @Override
        public void register() {
        }
    };

    void register();
}
