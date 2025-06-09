package com.niuhi.wind;

import com.niuhi.Windswept;
import com.niuhi.particle.wind.WindParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public class WindSystem {
    private final ClientWorld world;
    private Vec3d windDirection;
    private WindType windType;
    private final Random random;
    private final MinecraftClient client;
    private long lastWindChangeDay;

    public enum WindType {
        NONE(0.0f), SOFT(0.5f), NORMAL(1.0f), HEAVY(2.0f), STORM(4.0f);

        private final float strength;

        WindType(float strength) {
            this.strength = strength;
        }

        public float getStrength() {
            return strength;
        }
    }

    public WindSystem(ClientWorld world, Vec3d initialWindDirection, WindType initialWindType) {
        this.world = world;
        this.windDirection = initialWindDirection.normalize();
        this.windType = initialWindType;
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

    public WindType getWindType() {
        return windType;
    }

    public float getWindStrength() {
        return windType.getStrength();
    }

    private void updateWind() {
        if (world == null || client.player == null) return;
        long currentDay = world.getTimeOfDay() / 24000;
        if (currentDay > lastWindChangeDay) {
            // Update direction (9 options: 8 cardinal + no wind)
            int directionIndex = random.nextInt(9);
            if (directionIndex == 8) {
                // No wind day (~11.1% chance)
                this.windDirection = Vec3d.ZERO;
                this.windType = WindType.NONE;
            } else {
                // Cardinal direction
                double angle = directionIndex * Math.PI / 4; // 45-degree increments
                double x = Math.cos(angle);
                double z = Math.sin(angle);
                this.windDirection = new Vec3d(x, 0.1, z).normalize();

                // Update wind type
                if (world.isThundering()) {
                    this.windType = WindType.STORM;
                } else if (world.isRaining()) {
                    this.windType = random.nextFloat() < 0.6f ? WindType.HEAVY : (random.nextFloat() < 0.5f ? WindType.SOFT : WindType.NORMAL);
                } else {
                    // Get biome-based wind type probabilities
                    BlockPos playerPos = client.player.getBlockPos();
                    BiomeWinds.WindTypeWeights weights = BiomeWinds.getWindTypeWeights(world, playerPos);
                    float roll = random.nextFloat();
                    if (roll < weights.softChance()) {
                        this.windType = WindType.SOFT;
                    } else if (roll < weights.softChance() + weights.normalChance()) {
                        this.windType = WindType.NORMAL;
                    } else {
                        this.windType = WindType.HEAVY;
                    }
                }
            }
            this.lastWindChangeDay = currentDay;
        }
    }

    public void spawnWindParticles(BlockPos center, int radius, int particleCount) {
        if (windType == WindType.NONE) return; // No particles on no-wind days
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
        updateWind();
        if (world.getTime() % 20 == 0 && windType != WindType.NONE) {
            int particleCount = switch (windType) {
                case SOFT -> 1 + random.nextInt(5);
                case NORMAL -> 1 + random.nextInt(8);
                case HEAVY -> 2 + random.nextInt(15);
                case STORM -> 3 + random.nextInt(20); // More particles for STORM
                default -> 0;
            };
            spawnWindParticles(center, radius, particleCount);
        }
    }

    public void setWind(Vec3d newDirection, WindType newType) {
        this.windDirection = newDirection.normalize();
        this.windType = newType;
        this.lastWindChangeDay = world != null ? world.getTimeOfDay() / 24000 : 0;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                // Initialize WindSystem if not set or world has changed
                if (Windswept.WIND_SYSTEM == null || Windswept.WIND_SYSTEM.getWorld() != client.world) {
                    Windswept.WIND_SYSTEM = new WindSystem(client.world, new Vec3d(1, 0.1, 0), WindType.NORMAL);
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
        });
    }
}