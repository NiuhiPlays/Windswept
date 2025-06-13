package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public class WaveSystem {
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 20; // Spawn every 10 ticks
    private static final int MIN_WATER_BLOCKS = 15; // Minimum water blocks for large water body
    private static final int CHECK_RADIUS = 2; // 5x5 area
    private static final int PARTICLES_PER_GROUP = 4; // Number of particles in a group
    private static final float GROUP_SPREAD = 0.3f; // Base spread along shoreline (in blocks)
    private static final float RANDOM_OFFSET = 0.15f; // Random offset for less uniformity
    private static final float Y_OFFSET = 0.01f; // Max Y variation to prevent Z-fighting

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= TICK_INTERVAL) {
                tickCounter = 0;
                spawnWaveParticles(client);
            }
        });
    }

    private static void spawnWaveParticles(MinecraftClient client) {
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

                    // Spawn only if it's an edge, not surrounded, and in a large water body
                    if (isEdge && waterNeighbors < 4 && isLargeWaterBody(world, checkPos)) {
                        if (random.nextFloat() < 0.3f) {
                            // Calculate base spawn position (center of block, offset toward shore)
                            double baseX = checkPos.getX() + 0.5 - normalX * 0.25; // Offset toward shore
                            double baseY = checkPos.getY() + 1.0; // At water surface
                            double baseZ = checkPos.getZ() + 0.5 - normalZ * 0.25;

                            // Spawn a group of particles along the shoreline
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

                                // Per-particle cliff check
                                boolean particleIsCliff = false;
                                BlockPos particlePos = new BlockPos((int)px, (int)py, (int)pz);
                                for (Direction dir : Direction.Type.HORIZONTAL) {
                                    BlockPos neighborPos = particlePos.offset(dir);
                                    BlockState neighborState = world.getBlockState(neighborPos);
                                    if (neighborState.isSolidBlock(world, neighborPos) && !neighborState.isOf(Blocks.WATER)) {
                                        // Check height difference (at least 2 blocks taller)
                                        int neighborTopY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, neighborPos.getX(), neighborPos.getZ());
                                        if (neighborTopY >= particlePos.getY() + 2) {
                                            particleIsCliff = true;
                                            break;
                                        }
                                    }
                                }

                                // Spawn Wave particle with per-particle cliff flag
                                world.addParticleClient(WaterParticleTypes.WAVE, px, py, pz, normalX, particleIsCliff ? 1.0 : 0.0, normalZ);
                                // Spawn Foam particle with per-particle cliff flag
                                world.addParticleClient(WaterParticleTypes.FOAM, px, py, pz, normalX, particleIsCliff ? 1.0 : 0.0, normalZ);
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