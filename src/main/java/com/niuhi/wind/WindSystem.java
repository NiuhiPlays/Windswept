package com.niuhi.wind;

import com.niuhi.particle.WindParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

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

    public ClientWorld getWorld() {
        return world;
    }

    public void spawnWindParticles(BlockPos center, int radius, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            // Random position within radius in XZ plane
            int x = center.getX() + (int) (random.nextGaussian() * radius);
            int z = center.getZ() + (int) (random.nextGaussian() * radius);

            // Get ground level using heightmap
            int groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            double y = groundY + 5 + random.nextDouble() * 3; // 1-3 blocks above ground

            // Ensure spawn position is in air
            if (!world.getBlockState(BlockPos.ofFloored(x, y, z)).isAir()) {
                y = groundY + 5; // Adjust to just above ground if blocked
            }

            client.particleManager.addParticle(WindParticleTypes.WIND, x, y, z, 0, 0, 0);
        }
    }

    public void tickWindArea(BlockPos center, int radius) {
        if (world.getTime() % 20 == 0) { // Spawn every 20 ticks (once per second)
            spawnWindParticles(center, radius, 1 + random.nextInt(15)); // Amount
        }
    }

    public void setWindDirection(Vec3d newDirection) {
        // For future expansion
    }
}