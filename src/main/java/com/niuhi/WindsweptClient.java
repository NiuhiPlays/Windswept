package com.niuhi;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.niuhi.wind.WindSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WindsweptClient implements ClientModInitializer {
    private WindSystem windSystem;

    @Override
    public void onInitializeClient() {
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                // Initialize WindSystem only when world is available
                if (windSystem == null || windSystem.getWorld() != client.world) {
                    windSystem = new WindSystem(
                            client.world,
                            new Vec3d(1, 0.1, 0), // Default wind direction
                            0.05f // Wind strength
                    );
                }
                // Update wind system with player's position
                BlockPos playerPos = client.player.getBlockPos();
                windSystem.tickWindArea(playerPos, 16); // Spawn particles in a 16-block radius around player
            }
        });
    }
}