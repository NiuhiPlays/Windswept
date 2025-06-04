package com.niuhi;

import com.niuhi.water.CascadeSystem;
import com.niuhi.water.RippleSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import com.niuhi.wind.WindSystem;
import net.minecraft.util.math.Vec3d;

public class WindsweptClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickHandler());

        // Water related Logic
        CascadeSystem.register();
        RippleSystem.register();
    }

    private static class ClientTickHandler implements ClientTickEvents.EndTick {
        @Override
        public void onEndTick(MinecraftClient client) {
            if (client.world != null) {
                // Initialize WindSystem if not set or world has changed
                if (Windswept.WIND_SYSTEM == null || Windswept.WIND_SYSTEM.getWorld() != client.world) {
                    Windswept.WIND_SYSTEM = new WindSystem(client.world, new Vec3d(1, 0.1, 0), 1.0f);
                }
                // Update wind and spawn particles around player
                if (client.player != null) {
                    BlockPos playerPos = client.player.getBlockPos();
                    Windswept.WIND_SYSTEM.tickWindArea(playerPos, 16); // 16-block radius
                }
            } else {
                // Reset WindSystem when leaving world
                Windswept.WIND_SYSTEM = null;
            }
        }
    }
}