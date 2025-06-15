package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.Map;

public class EntitySplashSystem {
    private static final int CHECK_INTERVAL = 2; // Check every 2 ticks
    private static final long SPAWN_COOLDOWN = 10; // 10-tick cooldown per entity
    private static final Map<Entity, Long> lastSpawnTime = new HashMap<>();
    private static final Map<Entity, Boolean> wasInWater = new HashMap<>(); // Track previous water state
    private static final double MIN_VELOCITY_THRESHOLD = 0.3; // Minimum velocity to create splash

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientWorld world = client.world;
            if (world == null || client.player == null) {
                return;
            }

            // Check all entities in the world
            for (Entity entity : world.getEntities()) {
                // Skip if it's the player in spectator mode
                if (entity == client.player && client.player.isSpectator()) {
                    continue;
                }

                // Check every CHECK_INTERVAL ticks
                if (world.getTime() % CHECK_INTERVAL != 0) {
                    continue;
                }

                boolean currentlyInWater = isEntityInWater(entity);
                boolean previouslyInWater = wasInWater.getOrDefault(entity, false);

                // Only spawn particles when entity ENTERS water (transition from not in water to in water)
                if (currentlyInWater && !previouslyInWater) {
                    // Check velocity threshold - only splash if moving fast enough
                    Vec3d velocity = entity.getVelocity();
                    double velocityMagnitude = velocity.length();

                    if (velocityMagnitude < MIN_VELOCITY_THRESHOLD) {
                        wasInWater.put(entity, currentlyInWater);
                        continue;
                    }

                    // Check cooldown
                    long currentTime = world.getTime();
                    long lastSpawn = lastSpawnTime.getOrDefault(entity, 0L);
                    if (currentTime - lastSpawn < SPAWN_COOLDOWN) {
                        wasInWater.put(entity, currentlyInWater);
                        continue;
                    }

                    // Update last spawn time
                    lastSpawnTime.put(entity, currentTime);

                    // Calculate splash size based on entity's bounding box
                    Box boundingBox = entity.getBoundingBox();
                    double width = boundingBox.getLengthX();
                    double depth = boundingBox.getLengthZ();

                    // Use the larger dimension (width or depth) as the exact size
                    double baseSplashSize = Math.max(width, depth);

                    // Clamp size to reasonable limits
                    double sizeMultiplier = Math.max(0.5, Math.min(2.0, baseSplashSize));

                    // Find water surface position
                    BlockPos pos = new BlockPos((int) Math.floor(entity.getX()),
                            (int) Math.floor(entity.getY()),
                            (int) Math.floor(entity.getZ()));
                    double waterSurfaceY = getWaterSurfaceHeight(world, pos);

                    if (waterSurfaceY != Double.MIN_VALUE) {
                        // Spawn single particles at the water surface
                        spawnSplashParticles(world, entity.getX(), waterSurfaceY + 0.02, entity.getZ(), sizeMultiplier);
                    }
                }

                // Update water state tracking
                wasInWater.put(entity, currentlyInWater);
            }

            // Clean up old entries for dead entities
            lastSpawnTime.entrySet().removeIf(entry -> !entry.getKey().isAlive());
            wasInWater.entrySet().removeIf(entry -> !entry.getKey().isAlive());
        });
    }

    private static boolean isEntityInWater(Entity entity) {
        // Check if entity's center is in water
        BlockPos pos = new BlockPos((int) Math.floor(entity.getX()),
                (int) Math.floor(entity.getY()),
                (int) Math.floor(entity.getZ()));
        FluidState fluidState = entity.getWorld().getFluidState(pos);
        return fluidState.isIn(FluidTags.WATER);
    }

    private static double getWaterSurfaceHeight(ClientWorld world, BlockPos pos) {
        // Get the topmost exposed block using heightmap
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos surfacePos = new BlockPos(pos.getX(), topY - 1, pos.getZ());

        // Check if the surface position is water
        FluidState fluidState = world.getFluidState(surfacePos);
        if (fluidState.isOf(Fluids.WATER)) {
            // Ensure the block above is air or non-solid to confirm exposure
            BlockPos abovePos = surfacePos.up();
            if (world.getBlockState(abovePos).isAir() || !world.getBlockState(abovePos).isSolidBlock(world, abovePos)) {
                return surfacePos.getY() + fluidState.getHeight(world, surfacePos);
            }
        }

        // If not water at topY - 1, check one block below
        surfacePos = surfacePos.down();
        fluidState = world.getFluidState(surfacePos);
        if (fluidState.isOf(Fluids.WATER)) {
            BlockPos abovePos = surfacePos.up();
            if (world.getBlockState(abovePos).isAir() || !world.getBlockState(abovePos).isSolidBlock(world, abovePos)) {
                return surfacePos.getY() + fluidState.getHeight(world, surfacePos);
            }
        }

        return Double.MIN_VALUE; // No valid water surface found
    }

    private static void spawnSplashParticles(ClientWorld world, double x, double y, double z, double sizeMultiplier) {
        // Spawn exactly one of each particle type at the center position
        world.addParticleClient(WaterParticleTypes.WATERSPLASH,
                x, y, z,
                sizeMultiplier, 0.0, 0.0);

        world.addParticleClient(WaterParticleTypes.WATERSPLASHFOAM,
                x, y, z,
                sizeMultiplier, 0.0, 0.0);

        world.addParticleClient(WaterParticleTypes.WATERSPLASHRING,
                x, y, z,
                sizeMultiplier, 0.0, 0.0);
    }
}