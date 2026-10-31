package com.customesc;

import net.fabricmc.api.ClientModInitializer;

public class CustomEscClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Nothing to register: the pause screen is swapped in by MinecraftClientMixin.
    }
}
