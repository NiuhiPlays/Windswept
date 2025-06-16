package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
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
    private static final Map<Entity, Boolean> wasAtWaterSurface = new HashMap<>(); // Track if entity was at water surface
    private static final double MIN_VELOCITY_THRESHOLD = 0.25; // Minimum velocity to create splash
    private static final double MAX_HEIGHT_MULTIPLIER = 3.0; // Max height multiplier for high velocity
    private static final double HEIGHT_SCALING_FACTOR = 1.5; // Scales velocity to height multiplier

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

                boolean currentlyAtWaterSurface = isEntityAtWaterSurface(entity);
                boolean previouslyAtWaterSurface = wasAtWaterSurface.getOrDefault(entity, false);

                // Only spawn particles when entity ENTERS water surface (transition to surface)
                if (currentlyAtWaterSurface && !previouslyAtWaterSurface) {
                    // Check velocity threshold - only splash if moving fast enough
                    Vec3d velocity = entity.getVelocity();
                    double velocityMagnitude = velocity.length();

                    if (velocityMagnitude < MIN_VELOCITY_THRESHOLD) {
                        wasAtWaterSurface.put(entity, true);
                        continue;
                    }

                    // Check cooldown
                    long currentTime = world.getTime();
                    long lastSpawn = lastSpawnTime.getOrDefault(entity, 0L);
                    if (currentTime - lastSpawn < SPAWN_COOLDOWN) {
                        wasAtWaterSurface.put(entity, true);
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

                    // Calculate height multiplier based on velocity
                    double heightMultiplier = 1.0 + (velocityMagnitude - MIN_VELOCITY_THRESHOLD) * HEIGHT_SCALING_FACTOR;
                    heightMultiplier = Math.min(heightMultiplier, MAX_HEIGHT_MULTIPLIER);

                    // Find water surface position
                    BlockPos pos = new BlockPos((int) Math.floor(entity.getX()),
                            (int) Math.floor(entity.getY()),
                            (int) Math.floor(entity.getZ()));
                    double waterSurfaceY = getWaterSurfaceHeight(world, pos);

                    if (waterSurfaceY != Double.MIN_VALUE) {
                        // Spawn single particles at the water surface
                        spawnSplashParticles(world, entity.getX(), waterSurfaceY + 0.01, entity.getZ(), sizeMultiplier, heightMultiplier);
                    }
                }

                // Update water surface state tracking
                wasAtWaterSurface.put(entity, currentlyAtWaterSurface);
            }

            // Clean up old entries for dead entities
            lastSpawnTime.entrySet().removeIf(entry -> !entry.getKey().isAlive());
            wasAtWaterSurface.entrySet().removeIf(entry -> !entry.getKey().isAlive());
        });
    }

    private static boolean isEntityAtWaterSurface(Entity entity) {
        ClientWorld world = (ClientWorld) entity.getWorld();
        // Get the entity's eye position (center of entity) and block position
        double eyeY = entity.getY() + entity.getEyeHeight(entity.getPose());
        BlockPos pos = new BlockPos((int) Math.floor(entity.getX()),
                (int) Math.floor(eyeY),
                (int) Math.floor(entity.getZ()));

        // Get water surface height
        double waterSurfaceY = getWaterSurfaceHeight(world, pos);
        if (waterSurfaceY == Double.MIN_VALUE) {
            return false;
        }

        // Check if the entity's eye or feet are near the water surface (within 0.5 blocks)
        double feetY = entity.getY();
        return Math.abs(waterSurfaceY - eyeY) <= 0.5 || Math.abs(waterSurfaceY - feetY) <= 0.5;
    }

    private static double getWaterSurfaceHeight(ClientWorld world, BlockPos pos) {
        // Start at the heightmap's top position
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos checkPos;

        // Search downward from topY to find the water surface
        for (int y = topY; y >= world.getBottomY(); y--) {
            checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            FluidState fluidState = world.getFluidState(checkPos);
            if (fluidState.isIn(FluidTags.WATER)) {
                // Found water: check if the block above is not water to confirm surface
                BlockPos abovePos = checkPos.up();
                FluidState aboveFluid = world.getFluidState(abovePos);
                if (!aboveFluid.isIn(FluidTags.WATER)) {
                    return checkPos.getY() + fluidState.getHeight(world, checkPos);
                }
            }
        }

        return Double.MIN_VALUE; // No water surface found
    }

    private static void spawnSplashParticles(ClientWorld world, double x, double y, double z, double sizeMultiplier, double heightMultiplier) {
        // Spawn exactly one of each particle type at the center position
        world.addParticleClient(WaterParticleTypes.WATERSPLASH,
                x, y, z,
                sizeMultiplier, heightMultiplier, 0.0);

        world.addParticleClient(WaterParticleTypes.WATERSPLASHFOAM,
                x, y, z,
                sizeMultiplier, heightMultiplier, 0.0);

        world.addParticleClient(WaterParticleTypes.WATERSPLASHRING,
                x, y, z,
                sizeMultiplier, 0.0, 0.0); // Ring doesn't use height multiplier
    }
}