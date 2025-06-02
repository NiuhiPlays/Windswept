package com.niuhi.wind;

import com.niuhi.particle.WindParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class WindSystem {
    private final ClientWorld world;
    private final Random random;
    private final MinecraftClient client;

    public WindSystem(ClientWorld world, Vec3d windDirection, float windStrength) {
        this.world = world;
        this.random = world.random;
        this.client = MinecraftClient.getInstance();
    }

    public ClientWorld getWorld() {
        return world;
    }

    public void spawnWindParticles(BlockPos center, int radius, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            double x = center.getX() + (random.nextGaussian() * radius);
            double y = center.getY() + 10 + random.nextDouble() * 2; // Spawns from ground level
            double z = center.getZ() + (random.nextGaussian() * radius);

            if (!world.getBlockState(BlockPos.ofFloored(x, y, z)).isAir()) {
                y = center.getY() + 2;
            }

            client.particleManager.addParticle(WindParticleTypes.WIND, x, y, z, 0, 0, 0);
        }
    }

    public void tickWindArea(BlockPos center, int radius) {
        if (world.getTime() % 20 == 0) { // Spawn every 20 ticks (once per second)
            spawnWindParticles(center, radius, 1 + random.nextInt(25)); // Particle Amount
        }
    }

    public void setWindDirection(Vec3d newDirection) {
        // For future expansion
    }
}