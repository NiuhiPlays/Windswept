package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class RippleSystem {
    private static final float RIPPLE_CHANCE = 0.1f; // Reduced chance to avoid excessive particles for rain
    private static int tickCounter = 0;

    // Define settings for raindrop ripples
    private record RippleSettings(float sizeMultiplier, float maxAge, float animationSpeed) {
        static final RippleSettings RAINDROP = new RippleSettings(0.8f, 20.0f, 0.6f); // Small, quick ripples for rain
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter % 5 == 0) { // Run every 5 ticks to reduce performance impact
                spawnRipplesForRain(client);
            }
        });
    }

    private static void spawnRipplesForRain(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || !world.isRaining()) return; // Only spawn ripples during rain

        // Get the player to define a radius around them for performance
        if (client.player == null) return;
        BlockPos playerPos = client.player.getBlockPos();
        int radius = 32; // Process blocks within 16-block radius of player

        // Iterate over a square area around the player
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (world.random.nextFloat() > RIPPLE_CHANCE) continue; // Randomize to reduce particle density

                BlockPos pos = playerPos.add(x, 0, z);
                // Get the topmost exposed block (rain falls from the sky)
                int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
                BlockPos surfacePos = new BlockPos(pos.getX(), topY - 1, pos.getZ());

                // Check if the block at the surface is water
                if (world.getFluidState(surfacePos).isOf(Fluids.WATER)) {
                    // Ensure the block above is air or non-colliding to confirm exposure to rain
                    BlockPos abovePos = surfacePos.up();
                    if (world.getBlockState(abovePos).isAir() || !world.getBlockState(abovePos).isSolidBlock(world, abovePos)) {
                        spawnRippleAtPosition(world, surfacePos);
                    }
                }
            }
        }
    }

    private static void spawnRippleAtPosition(ClientWorld world, BlockPos pos) {
        // Use raindrop-specific ripple settings
        RippleSettings settings = RippleSettings.RAINDROP;

        // Calculate water surface height
        double waterHeight = pos.getY() + world.getFluidState(pos).getHeight(world, pos);

        // Randomize position slightly within the block for natural variation
        double offsetX = world.random.nextDouble() * 0.6 - 0.3;
        double offsetZ = world.random.nextDouble() * 0.6 - 0.3;

        // Spawn the ripple particle
        world.addParticleClient(
                WaterParticleTypes.RIPPLE,
                pos.getX() + 0.5 + offsetX,
                waterHeight + 0.01, // Slightly above water surface
                pos.getZ() + 0.5 + offsetZ,
                settings.sizeMultiplier,
                settings.maxAge,
                settings.animationSpeed
        );
    }
}