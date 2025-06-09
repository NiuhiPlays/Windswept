package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;

public class CascadeSystem {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            World world = client.world;
            if (world == null || client.player == null) return;

            BlockPos playerPos = client.player.getBlockPos();
            int radius = 32;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        BlockPos impactPos = findWaterfallImpact(world, pos);
                        if (impactPos != null) {
                            List<BlockPos> openSides = getOpenSides(world, impactPos);
                            float baseSpawnChance = 0.05f;
                            float waterfallIntensity = getWaterfallIntensity(world, pos);
                            float pondSize = getPondSizeMultiplier(world, impactPos);
                            float spawnChance = baseSpawnChance * waterfallIntensity * pondSize * (1 + openSides.size() * 0.10f);
                            spawnChance = Math.min(spawnChance, 0.3f);

                            if (world.random.nextFloat() < spawnChance) {
                                float scale = 0.3f + (waterfallIntensity - 1.0f) * 0.15f + (openSides.size() * 0.05f);
                                scale = Math.min(scale, 1.0f);
                                List<Vec3d> spawnPositions = getImpactSpawnPositions(world, impactPos, openSides);
                                int maxParticlesPerSpawn = 2 + world.random.nextInt(3);
                                for (int i = 0; i < Math.min(spawnPositions.size(), maxParticlesPerSpawn); i++) {
                                    Vec3d spawnPos = spawnPositions.get(i);
                                    world.addParticleClient(WaterParticleTypes.CASCADE,
                                            spawnPos.x, spawnPos.y, spawnPos.z,
                                            scale, 0.0, 0.0);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    // Find if flowing water at this position eventually hits a still water pond below
    public static BlockPos findWaterfallImpact(World world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);

        // Current position must have flowing water
        if (!fluidState.isOf(Fluids.FLOWING_WATER)) {
            return null;
        }

        // Trace the waterfall downward to find where it hits still water
        BlockPos checkPos = pos.down();
        int maxDepth = 25; // Reasonable search depth for waterfalls

        for (int depth = 0; depth < maxDepth; depth++) {
            FluidState checkFluidState = world.getFluidState(checkPos);

            if (checkFluidState.isOf(Fluids.WATER)) {
                // Found still water - check if it's a proper pond
                if (isValidPond(world, checkPos)) {
                    return findPondSurface(world, checkPos);
                }
            } else if (checkFluidState.isOf(Fluids.FLOWING_WATER)) {
                // Continue through flowing water (part of the waterfall)
                checkPos = checkPos.down();
                continue;
            } else if (world.getBlockState(checkPos).isAir()) {
                // Waterfall going through air, continue downward
                checkPos = checkPos.down();
                continue;
            } else {
                // Hit solid block without finding a pond
                break;
            }

            checkPos = checkPos.down();
        }

        return null;
    }

    // Check if a still water position is part of a valid pond
    private static boolean isValidPond(World world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);

        // Must be still water
        if (!fluidState.isOf(Fluids.WATER)) {
            return false;
        }

        // Check for connected still water blocks to confirm it's a pond, not isolated
        int connectedStillWaterBlocks = 0;
        BlockPos[] adjacentPositions = {
                pos.north(), pos.south(), pos.east(), pos.west(),
                pos.north().east(), pos.north().west(),
                pos.south().east(), pos.south().west()
        };

        for (BlockPos adjacentPos : adjacentPositions) {
            FluidState adjacentFluid = world.getFluidState(adjacentPos);
            if (adjacentFluid.isOf(Fluids.WATER)) {
                connectedStillWaterBlocks++;
            }
        }

        // A pond should have at least 2 connected still water blocks
        // This helps distinguish from single isolated water blocks
        return connectedStillWaterBlocks >= 2;
    }

    // Find the surface (topmost still water block) of a pond
    private static BlockPos findPondSurface(World world, BlockPos pondPos) {
        BlockPos surface = pondPos;

        // Look upward to find the topmost still water block
        BlockPos checkPos = pondPos.up();
        while (world.getFluidState(checkPos).isOf(Fluids.WATER) &&
                isValidPond(world, checkPos)) {
            surface = checkPos;
            checkPos = checkPos.up();
        }

        return surface;
    }

    // Calculate waterfall intensity based on the amount and flow of water above
    public static float getWaterfallIntensity(World world, BlockPos flowingWaterPos) {
        int flowingBlocksAbove = 0;
        int maxHeight = 15; // Check up to 15 blocks above

        BlockPos checkPos = flowingWaterPos.up();
        for (int i = 0; i < maxHeight; i++) {
            FluidState fluidState = world.getFluidState(checkPos);
            if (fluidState.isOf(Fluids.FLOWING_WATER)) {
                flowingBlocksAbove++;
            } else if (fluidState.isOf(Fluids.WATER)) {
                // Still water source feeding the waterfall
                flowingBlocksAbove += 2; // Weight source blocks more heavily
            } else if (!world.getBlockState(checkPos).isAir()) {
                // Hit solid block, stop checking
                break;
            }
            checkPos = checkPos.up();
        }

        // Convert to multiplier (1.0 = base, higher = more intense)
        return Math.min(1.0f + (flowingBlocksAbove * 0.15f), 2.5f);
    }

    // Calculate pond size multiplier for spawn chance
    private static float getPondSizeMultiplier(World world, BlockPos pondCenter) {
        int stillWaterBlocks = 0;
        int searchRadius = 4; // Check 4 blocks in each direction

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = -2; y <= 2; y++) { // Check a few blocks up and down
                    BlockPos checkPos = pondCenter.add(x, y, z);
                    if (world.getFluidState(checkPos).isOf(Fluids.WATER)) {
                        stillWaterBlocks++;
                    }
                }
            }
        }

        // Larger ponds get higher multipliers (more spray)
        return Math.min(1.0f + (stillWaterBlocks * 0.1f), 1.8f);
    }

    // Renamed to getImpactSpawnPositions to return multiple positions
    private static List<Vec3d> getImpactSpawnPositions(World world, BlockPos impactPos, List<BlockPos> openSides) {
        List<Vec3d> spawnPositions = new ArrayList<>();
        double baseX = impactPos.getX();
        double baseY = impactPos.getY() + 1.0; // Spawn above the water surface
        double baseZ = impactPos.getZ();

        // Add vertical randomness to create spray effect
        double verticalOffset = world.random.nextDouble() * 0.4; // 0.0 to 0.3 blocks higher
        baseY += verticalOffset;

        // If no open sides, skip to avoid spawning inside water
        if (openSides.isEmpty()) {
            return spawnPositions; // Empty list, no particles
        }

        // Spawn one particle per open side in 8 directions
        for (BlockPos openSide : openSides) {
            // Calculate direction vector to open side
            double dirX = openSide.getX() - impactPos.getX();
            double dirZ = openSide.getZ() - impactPos.getZ();

            // Normalize diagonal directions to keep within block bounds
            double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
            double offsetScale = (length > 1.0) ? 0.45 : 0.5;
            dirX *= offsetScale / length;
            dirZ *= offsetScale / length;

            // Add small randomness along the edge
            double offsetX = dirX + (world.random.nextDouble() - 0.5) * 0.3; // ±0.2 variation
            double offsetZ = dirZ + (world.random.nextDouble() - 0.5) * 0.3;

            // Clamp to stay near the edge
            offsetX = Math.max(-0.75, Math.min(0.75, offsetX));
            offsetZ = Math.max(-0.75, Math.min(0.75, offsetZ));

            spawnPositions.add(new Vec3d(baseX + 0.5 + offsetX, baseY, baseZ + 0.5 + offsetZ));
        }

        return spawnPositions;
    }

    // Updated getOpenSides to check 8 cardinal directions
    private static List<BlockPos> getOpenSides(World world, BlockPos pos) {
        List<BlockPos> openSides = new ArrayList<>();
        BlockPos[] sides = {
                pos.north(), pos.south(), pos.east(), pos.west(),
                pos.north().east(), pos.north().west(),
                pos.south().east(), pos.south().west()
        };

        for (BlockPos side : sides) {
            if (world.getBlockState(side).isAir() ||
                    !world.getBlockState(side).isSolidBlock(world, side)) {
                openSides.add(side);
            }
        }
        return openSides;
    }
}