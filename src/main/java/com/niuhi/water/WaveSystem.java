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
    private static final int TICK_INTERVAL = 10; // Spawn every 10 ticks
    private static final int MIN_WATER_BLOCKS = 15; // Minimum water blocks for large water body
    private static final int CHECK_RADIUS = 2; // 5x5 area

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
        int radius = 16;

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

                    // Check for land edge
                    boolean isEdge = false;
                    int waterNeighbors = 0;
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos neighborPos = checkPos.offset(dir);
                        BlockState neighborState = world.getBlockState(neighborPos);
                        if (neighborState.isSolidBlock(world, neighborPos) && !neighborState.isOf(Blocks.WATER)) {
                            isEdge = true;
                        } else if (neighborState.isOf(Blocks.WATER)) {
                            waterNeighbors++;
                        }
                    }

                    // Spawn only if it's an edge, not surrounded, and in a large water body
                    if (isEdge && waterNeighbors < 4 && isLargeWaterBody(world, checkPos)) {
                        double px = checkPos.getX() + random.nextDouble();
                        double py = checkPos.getY() + 1.05; // Slightly higher spawn
                        double pz = checkPos.getZ() + random.nextDouble();

                        if (random.nextFloat() < 0.3f) {
                            world.addParticleClient(WaterParticleTypes.WAVE, px, py, pz, 0, 0, 0);
                            world.addParticleClient(WaterParticleTypes.FOAM, px, py, pz, 0, 0, 0);
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