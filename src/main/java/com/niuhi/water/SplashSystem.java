package com.niuhi.water;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class SplashSystem {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SplashSystem::tick);
    }

    private static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            return;
        }

    }
}