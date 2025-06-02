package com.niuhi.wind;

import com.niuhi.particle.WindParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class WindSystem {
    private final ClientWorld world;
    private final Vec3d windDirection;
    private final float windStrength;
    private final Random random;
    private final MinecraftClient client;

    public WindSystem(ClientWorld world, Vec3d windDirection, float windStrength) {
        this.world = world;
        this.windDirection = windDirection.normalize();
        this.windStrength = windStrength;
        this.random = world.random;
        this.client = MinecraftClient.getInstance();
    }

    public void spawnWindParticles(BlockPos center, int radius, int particleCount) {
        if (!world.isClient || client.player == null) return; // Only spawn on client side

        for (int i = 0; i < particleCount; i++) {
            // Random position within radius
            double x = center.getX() + (random.nextGaussian() * radius);
            double y = center.getY() + (random.nextGaussian() * radius * 0.5); // Less vertical spread
            double z = center.getZ() + (random.nextGaussian() * radius);

            // Initial velocity with some randomness
            double velocityX = windDirection.x * windStrength + (random.nextGaussian() * 0.01);
            double velocityY = windDirection.y * windStrength + (random.nextGaussian() * 0.005);
            double velocityZ = windDirection.z * windStrength + (random.nextGaussian() * 0.01);

            // Spawn the particle using the particle manager
            client.particleManager.addParticle(WindParticleTypes.WIND,
                    x, y, z,
                    velocityX, velocityY, velocityZ);
        }
    }

    // Continuous wind effect in an area
    public void tickWindArea(BlockPos center, int radius) {
        if (world.getTime() % 4 == 0) { // Spawn every 4 ticks (5 times per second)
            spawnWindParticles(center, radius, 2 + random.nextInt(4));
        }
    }

    // Set new wind direction (for future expansion)
    public void setWindDirection(Vec3d newDirection) {
        // This will be used when you expand the system
    }
}