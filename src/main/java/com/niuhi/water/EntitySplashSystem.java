package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.Map;

public class EntitySplashSystem {
    private static final long SPAWN_COOLDOWN = 10; // 10-tick cooldown for non-item entities
    private static final Map<Entity, Long> lastSpawnTime = new HashMap<>();
    private static final Map<Entity, Boolean> wasAtWaterSurface = new HashMap<>(); // Track if entity was at water surface
    private static final double MIN_VELOCITY_THRESHOLD = 0.25; // Minimum velocity to create splash
    private static final double MAX_HEIGHT_MULTIPLIER = 3.0; // Max height multiplier for high velocity
    private static final double HEIGHT_SCALING_FACTOR = 1.5; // Scales velocity to height multiplier
    private static final double ITEM_SURFACE_THRESHOLD = 0.1; // Threshold for items
    private static final double ENTITY_SURFACE_THRESHOLD = 0.1; // Tighter threshold for non-item entities

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientWorld world = client.world;
            if (world == null || client.player == null) {
                return;
            }

            // Check all entities in the world every tick
            for (Entity entity : world.getEntities()) {
                // Skip if it's the player in spectator mode
                if (entity == client.player && client.player.isSpectator()) {
                    continue;
                }

                boolean currentlyAtWaterSurface = isEntityAtWaterSurface(entity);
                boolean previouslyAtWaterSurface = wasAtWaterSurface.getOrDefault(entity, false);

                // Only spawn particles when entity ENTERS water surface
                if (currentlyAtWaterSurface && !previouslyAtWaterSurface) {
                    // Check velocity threshold - only splash if moving fast enough
                    Vec3d velocity = entity.getVelocity();
                    double velocityMagnitude = velocity.length();

                    if (velocityMagnitude < MIN_VELOCITY_THRESHOLD) {
                        wasAtWaterSurface.put(entity, true);
                        continue;
                    }

                    // Apply cooldown only for non-item entities
                    if (!(entity instanceof ItemEntity)) {
                        long currentTime = world.getTime();
                        long lastSpawn = lastSpawnTime.getOrDefault(entity, 0L);
                        if (currentTime - lastSpawn < SPAWN_COOLDOWN) {
                            wasAtWaterSurface.put(entity, true);
                            continue;
                        }
                        lastSpawnTime.put(entity, currentTime);
                    }

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
        // Get the entity's block position
        BlockPos pos = new BlockPos((int) Math.floor(entity.getX()),
                (int) Math.floor(entity.getY()),
                (int) Math.floor(entity.getZ()));

        // Get water surface height
        double waterSurfaceY = getWaterSurfaceHeight(world, pos);
        if (waterSurfaceY == Double.MIN_VALUE) {
            return false;
        }

        // Get the bottom of the entity's bounding box
        double bottomY = entity.getY();

        if (entity instanceof ItemEntity) {
            // For items, require the bottom of the bounding box to be at or below the water surface
            return bottomY <= waterSurfaceY + ITEM_SURFACE_THRESHOLD;
        } else {
            // For non-item entities, require the bottom of the bounding box to be at or below the water surface
            // and confirm the block at the feet position contains water
            BlockPos feetPos = new BlockPos((int) Math.floor(entity.getX()),
                    (int) Math.floor(bottomY),
                    (int) Math.floor(entity.getZ()));
            FluidState fluidState = world.getFluidState(feetPos);
            return bottomY <= waterSurfaceY + ENTITY_SURFACE_THRESHOLD && fluidState.isIn(FluidTags.WATER);
        }
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