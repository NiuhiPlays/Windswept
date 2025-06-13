package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class WaveSystem {
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 40; // Base interval for checking wave spawns (2 seconds)
    private static final int MIN_WATER_BLOCKS = 15; // Minimum water blocks for large water body
    private static final int CHECK_RADIUS = 2; // 5x5 area
    private static final int PARTICLES_PER_GROUP = 4; // Number of particles in a group
    private static final float GROUP_SPREAD = 0.3f; // Base spread along shoreline (in blocks)
    private static final float RANDOM_OFFSET = 0.15f; // Random offset for less uniformity
    private static final float Y_OFFSET = 0.01f; // Max Y variation to prevent Z-fighting
    private static final float SPLASH_HEIGHT_THRESHOLD = 5.0f; // Threshold for BIGSPLASH vs SPLASH
    private static final int WAVE_SPAWN_DELAY_RANGE = 20; // Random delay for wave groups (0-20 ticks)
    private static final int SPLASH_SPAWN_DELAY_RANGE = 10; // Random delay for splashes (0-10 ticks)

    // List to store pending particle spawns
    private static final List<PendingSpawn> pendingSpawns = new ArrayList<>();

    // Class to represent a pending particle spawn
        private record PendingSpawn(ParticleEffect particleType, double x, double y, double z, double velocityX,
                                    double velocityY, double velocityZ, int spawnTick) {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            // Process pending spawns every tick
            processPendingSpawns(client);
            // Check for new wave spawns at interval
            if (tickCounter >= TICK_INTERVAL) {
                tickCounter = 0;
                scheduleWaveParticles(client);
            }
        });
    }

    private static void processPendingSpawns(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) {
            return;
        }

        // Use iterator to safely remove entries while iterating
        Iterator<PendingSpawn> iterator = pendingSpawns.iterator();
        while (iterator.hasNext()) {
            PendingSpawn spawn = iterator.next();
            if (tickCounter >= spawn.spawnTick) {
                world.addParticleClient(spawn.particleType, spawn.x, spawn.y, spawn.z,
                        spawn.velocityX, spawn.velocityY, spawn.velocityZ);
                iterator.remove(); // Remove processed spawn
            }
        }
    }

    private static void scheduleWaveParticles(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();
        Random random = world.random;
        int radius = 32;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int worldX = playerPos.getX() + x;
                int worldZ = playerPos.getZ() + z;
                // Get surface height to limit Y checks
                int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
                BlockPos checkPos = new BlockPos(worldX, topY - 1, worldZ);
                BlockState state = world.getBlockState(checkPos);

                if (state.isOf(Blocks.WATER)) {
                    // Check for surface water (air or non-water above)
                    BlockState aboveState = world.getBlockState(checkPos.up());
                    if (!aboveState.isAir() && aboveState.isOf(Blocks.WATER)) {
                        continue;
                    }

                    // Check sky exposure
                    if (checkPos.getY() < topY - 1) {
                        continue; // Skip if not at surface height
                    }

                    // Check for land edge and compute direction
                    boolean isEdge = false;
                    int waterNeighbors = 0;
                    float normalX = 0.0f;
                    float normalZ = 0.0f;
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos neighborPos = checkPos.offset(dir);
                        BlockState neighborState = world.getBlockState(neighborPos);
                        if (neighborState.isSolidBlock(world, neighborPos) && !neighborState.isOf(Blocks.WATER)) {
                            isEdge = true;
                            // Add opposite direction to normal (pointing into water)
                            normalX -= dir.getOffsetX();
                            normalZ -= dir.getOffsetZ();
                        } else if (neighborState.isOf(Blocks.WATER)) {
                            waterNeighbors++;
                        }
                    }

                    // Normalize direction vector
                    float length = (float) Math.sqrt(normalX * normalX + normalZ * normalZ);
                    if (length > 0) {
                        normalX /= length;
                        normalZ /= length;
                    } else {
                        normalX = random.nextFloat() - 0.5f; // Fallback random direction
                        normalZ = random.nextFloat() - 0.5f;
                        length = (float) Math.sqrt(normalX * normalX + normalZ * normalZ);
                        if (length > 0) {
                            normalX /= length;
                            normalZ /= length;
                        }
                    }

                    // Schedule wave group if it's an edge, not surrounded, and in a large water body
                    if (isEdge && waterNeighbors < 4 && isLargeWaterBody(world, checkPos)) {
                        // Calculate base spawn position (center of block, offset toward shore)
                        double baseX = checkPos.getX() + 0.5 - normalX * 0.25; // Offset toward shore
                        double baseY = checkPos.getY() + 1.0; // At water surface
                        double baseZ = checkPos.getZ() + 0.5 - normalZ * 0.25;

                        // Schedule a group of particles with random delay
                        int waveDelay = tickCounter + random.nextInt(WAVE_SPAWN_DELAY_RANGE);
                        for (int i = 0; i < PARTICLES_PER_GROUP; i++) {
                            // Offset along shoreline
                            float offset = (i - (PARTICLES_PER_GROUP - 1) / 2.0f) * GROUP_SPREAD;
                            // Random offsets for less uniformity
                            float randomAlong = (random.nextFloat() - 0.5f) * RANDOM_OFFSET;
                            float randomNormal = (random.nextFloat() - 0.5f) * RANDOM_OFFSET;
                            // Y variation to prevent Z-fighting
                            double yOffset = (random.nextFloat() - 0.5f) * Y_OFFSET;
                            // Calculate spawn position
                            double px = baseX + -normalZ * (offset + randomAlong) + normalX * randomNormal;
                            double py = baseY + yOffset;
                            double pz = baseZ + normalX * (offset + randomAlong) + normalZ * randomNormal;

                            // Per-particle cliff check and height calculation
                            double cliffHeight = 0.0;
                            BlockPos particlePos = new BlockPos((int)px, (int)py, (int)pz);
                            for (Direction dir : Direction.Type.HORIZONTAL) {
                                BlockPos neighborPos = particlePos.offset(dir);
                                BlockState neighborState = world.getBlockState(neighborPos);
                                if (neighborState.isSolidBlock(world, neighborPos) && !neighborState.isOf(Blocks.WATER)) {
                                    // Calculate height difference
                                    int neighborTopY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, neighborPos.getX(), neighborPos.getZ());
                                    int heightDiff = neighborTopY - particlePos.getY();
                                    if (heightDiff >= 2) {
                                        cliffHeight = Math.max(cliffHeight, heightDiff);
                                    }
                                }
                            }

                            // Schedule Wave particle
                            pendingSpawns.add(new PendingSpawn(
                                    WaterParticleTypes.WAVE, px, py, pz,
                                    normalX, cliffHeight, normalZ, waveDelay));
                            // Schedule Foam particle
                            pendingSpawns.add(new PendingSpawn(
                                    WaterParticleTypes.FOAM, px, py, pz,
                                    normalX, cliffHeight, normalZ, waveDelay));

                            // Schedule splash particles for cliffs less frequently
                            if (cliffHeight > 0 && random.nextFloat() < 0.2f) {
                                int splashCount = Math.min(2, (int) (cliffHeight * 0.25)); // 0.25 particles per block, capped at 2
                                for (int j = 0; j < splashCount; j++) {
                                    // Spawn 1 block above water with slight randomization
                                    double splashX = px + random.nextGaussian() * 0.2;
                                    double splashY = baseY + 1.0; // 1 block above water surface
                                    double splashZ = pz + random.nextGaussian() * 0.2;
                                    // Choose splash type based on cliff height
                                    ParticleEffect splashType = (cliffHeight >= SPLASH_HEIGHT_THRESHOLD && random.nextFloat() < 0.3f)
                                            ? WaterParticleTypes.BIGSPLASH : WaterParticleTypes.SPLASH;
                                    // Schedule splash with random delay
                                    int splashDelay = waveDelay + random.nextInt(SPLASH_SPAWN_DELAY_RANGE);
                                    pendingSpawns.add(new PendingSpawn(
                                            splashType, splashX, splashY, splashZ,
                                            0, 0, 0, splashDelay));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isLargeWaterBody(ClientWorld world, BlockPos pos) {
        int waterCount = 0;

        for (int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
            for (int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                BlockPos checkPos = pos.add(x, 0, z);
                BlockState state = world.getBlockState(checkPos);
                if (state.isOf(Blocks.WATER)) {
                    BlockState aboveState = world.getBlockState(checkPos.up());
                    if (aboveState.isAir() || !aboveState.isOf(Blocks.WATER)) {
                        waterCount++;
                    }
                }
            }
        }

        return waterCount >= MIN_WATER_BLOCKS;
    }
}