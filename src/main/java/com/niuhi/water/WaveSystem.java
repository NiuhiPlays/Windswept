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

public class WaveSystem {
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 5;

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
                BlockPos pos = playerPos.add(x, 0, z);
                for (int y = world.getBottomY(); y <= world.getBottomY() + world.getHeight(); y++) {
                    BlockPos checkPos = pos.withY(y);
                    BlockState state = world.getBlockState(checkPos);

                    if (state.isOf(Blocks.WATER)) {
                        // Check for surface water (air or non-water above)
                        BlockState aboveState = world.getBlockState(checkPos.up());
                        if (!aboveState.isAir() && aboveState.isOf(Blocks.WATER)) {
                            continue;
                        }

                        // Check for land edge (at least one solid non-water block horizontally)
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

                        // Spawn only if it's an edge and not surrounded by water
                        if (isEdge && waterNeighbors < 4) {
                            double px = checkPos.getX() + random.nextDouble();
                            double py = checkPos.getY() + 1.0;
                            double pz = checkPos.getZ() + random.nextDouble();

                            if (random.nextFloat() < 0.3f) {
                                world.addParticleClient(WaterParticleTypes.WAVE, px, py, pz, 0, 0, 0);

                                if (random.nextFloat() < 0.5f) {
                                    world.addParticleClient(WaterParticleTypes.FOAM, px + random.nextGaussian() * 0.1,
                                            py, pz + random.nextGaussian() * 0.1, 0, 0, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}