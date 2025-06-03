package com.niuhi.wind;

import com.niuhi.particle.wind.WindParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public class WindSystem {
    private final ClientWorld world;
    private Vec3d windDirection;
    private final float windStrength;
    private final Random random;
    private final MinecraftClient client;
    private long lastWindChangeDay;

    public WindSystem(ClientWorld world, Vec3d initialWindDirection, float windStrength) {
        this.world = world;
        this.windDirection = initialWindDirection.normalize();
        this.windStrength = windStrength;
        this.random = world != null ? world.random : Random.create();
        this.client = MinecraftClient.getInstance();
        this.lastWindChangeDay = world != null ? world.getTimeOfDay() / 24000 : 0;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public Vec3d getWindDirection() {
        return windDirection;
    }

    public float getWindStrength() {
        return windStrength;
    }

    private void updateWindDirection() {
        if (world == null) return; // Prevent updates if world is null
        long currentDay = world.getTimeOfDay() / 24000;
        if (currentDay > lastWindChangeDay) {
            int directionIndex = random.nextInt(8);
            double angle = directionIndex * Math.PI / 4; // 45-degree increments
            double x = Math.cos(angle);
            double z = Math.sin(angle);
            this.windDirection = new Vec3d(x, 0.1, z).normalize();
            this.lastWindChangeDay = currentDay;
        }
    }

    public void spawnWindParticles(BlockPos center, int radius, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            int x = center.getX() + (int) (random.nextGaussian() * radius);
            int z = center.getZ() + (int) (random.nextGaussian() * radius);

            int groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            double y = groundY + 1 + random.nextDouble() * 5;

            if (!world.getBlockState(BlockPos.ofFloored(x, y, z)).isAir()) {
                y = groundY + 1;
            }
            client.particleManager.addParticle(WindParticleTypes.WIND, x, y, z, 0, 0, 0);
        }
    }

    public void tickWindArea(BlockPos center, int radius) {
        updateWindDirection();
        if (world.getTime() % 20 == 0) {
            spawnWindParticles(center, radius, 1 + random.nextInt(15));
        }
    }

    public void setWindDirection(Vec3d newDirection) {
        this.windDirection = newDirection.normalize();
        this.lastWindChangeDay = world != null ? world.getTimeOfDay() / 24000 : 0;
    }
}