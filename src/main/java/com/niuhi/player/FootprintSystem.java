package com.niuhi.player;

import com.niuhi.particle.player.PlayerParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FootprintSystem {
    private static final Map<UUID, EntityStepTracker> stepTrackers = new HashMap<>();
    private static final double STEP_DISTANCE = 0.6; // Distance per step for footprint spawning
    private static final float FOOT_OFFSET = 0.2f; // Offset for left/right foot placement
    private static final int WET_DURATION_TICKS = 60; // 3 seconds at 20 ticks/second

    public static void register() {
        // Register client tick event to track entity movement
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && !client.isPaused()) {
                handleEntityMovement(client.world);
            }
        });
    }

    private static void handleEntityMovement(ClientWorld world) {
        // Iterate over all living entities in the world
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof LivingEntity livingEntity) || entity.isSpectator()) {
                continue; // Skip non-living entities and spectators
            }

            // Skip entities that are swimming
            if (livingEntity.isSwimming()) {
                continue;
            }

            // Update wet state
            UUID entityId = entity.getUuid();
            EntityStepTracker tracker = stepTrackers.computeIfAbsent(entityId, k -> new EntityStepTracker());
            if (livingEntity.isTouchingWaterOrRain()) {
                tracker.wetTicks = WET_DURATION_TICKS; // Reset wet timer
            } else if (tracker.wetTicks > 0) {
                tracker.wetTicks--; // Decrement wet timer
            }

            // Skip entities that are not moving on the ground or landing
            boolean isOnGround = livingEntity.isOnGround();
            if (!isOnGround && !tracker.wasInAir) {
                continue;
            }

            // Check for landing from a jump
            boolean justLanded = isOnGround && tracker.wasInAir;
            if (justLanded) {
                spawnFootprint(world, entity, tracker);
                tracker.isLeftFoot = !tracker.isLeftFoot; // Alternate feet
            }

            // Update air state
            tracker.wasInAir = !isOnGround;

            // Check for walking/running movement
            if (!isOnGround || entity.getVelocity().horizontalLengthSquared() < 0.01) {
                continue;
            }

            // Update distance traveled
            Vec3d pos = entity.getPos();
            double distance = tracker.lastPos == null ? 0 : pos.distanceTo(tracker.lastPos);
            tracker.distanceTraveled += distance;

            // Check if enough distance has been traveled to spawn a footprint
            if (tracker.distanceTraveled >= STEP_DISTANCE) {
                spawnFootprint(world, entity, tracker);
                tracker.distanceTraveled = 0; // Reset distance
                tracker.isLeftFoot = !tracker.isLeftFoot; // Alternate feet
            }

            tracker.lastPos = pos; // Update last position
        }
    }

    private static void spawnFootprint(ClientWorld world, Entity entity, EntityStepTracker tracker) {
        // Calculate footprint position (offset left or right based on foot)
        Vec3d pos = entity.getPos();
        float yaw = entity.getYaw();
        float offsetX = tracker.isLeftFoot ? -FOOT_OFFSET : FOOT_OFFSET;
        double x = pos.x + offsetX * Math.cos(Math.toRadians(yaw));
        double z = pos.z + offsetX * Math.sin(Math.toRadians(yaw));
        double y = pos.y;

        // Adjust y position for snow layers or mud
        BlockPos blockPos = new BlockPos((int) x, (int) (y + 0.0625), (int) z);
        if (world.getBlockState(blockPos).isOf(Blocks.SNOW) || world.getBlockState(blockPos.down()).isOf(Blocks.SNOW)) {
            int layers = world.getBlockState(blockPos).isOf(Blocks.SNOW) ?
                    world.getBlockState(blockPos).get(SnowBlock.LAYERS) :
                    world.getBlockState(blockPos.down()).get(SnowBlock.LAYERS);
            y += layers * 0.125; // Each snow layer is 1/8 block (0.125) tall
        } else if (world.getBlockState(blockPos).isOf(Blocks.MUD) || world.getBlockState(blockPos.down()).isOf(Blocks.MUD)) {
            y += 0.125; // Mud is 0.9 blocks tall, add offset to place on surface
        }

        // Determine the block-specific particle
        SimpleParticleType particleType = getFootprintParticle(world, x, y, z);

        // Spawn particles based on conditions
        if (entity instanceof LivingEntity livingEntity && (livingEntity.isTouchingWaterOrRain() || tracker.wetTicks > 0)) {
            // If wet (in water or recently wet), spawn only wet footprint
            world.addParticleClient(PlayerParticleTypes.FOOTPRINT_WET, x, y, z, 0, 0, 0);
        } else if (world.isRaining() && isExposedToRain(world, x, y + 1.5, z)) {
            // If raining and exposed, spawn both wet and block-specific footprint
            world.addParticleClient(PlayerParticleTypes.FOOTPRINT_WET, x, y, z, 0, 0, 0);
            world.addParticleClient(particleType, x, y, z, 0, 0, 0);
        } else {
            // Otherwise, spawn only block-specific footprint
            world.addParticleClient(particleType, x, y, z, 0, 0, 0);
        }
    }

    private static SimpleParticleType getFootprintParticle(ClientWorld world, double x, double y, double z) {
        // Check block state for specific blocks
        BlockPos pos = new BlockPos((int) x, (int) (y + 0.0625), (int) z); // Slightly above for thin blocks like snow
        BlockPos posBelow = pos.down();

        if (world.getBlockState(pos).isOf(Blocks.SNOW) || world.getBlockState(posBelow).isOf(Blocks.SNOW) ||
                world.getBlockState(pos).isOf(Blocks.SNOW_BLOCK) || world.getBlockState(posBelow).isOf(Blocks.SNOW_BLOCK) ||
                world.getBlockState(posBelow).isOf(Blocks.POWDER_SNOW)) {
            return PlayerParticleTypes.FOOTPRINT_SNOW;
        }
        if (world.getBlockState(pos).isOf(Blocks.SAND) || world.getBlockState(posBelow).isOf(Blocks.SAND) ||
                world.getBlockState(posBelow).isOf(Blocks.SUSPICIOUS_SAND)) {
            return PlayerParticleTypes.FOOTPRINT_SAND;
        }
        if (world.getBlockState(pos).isOf(Blocks.RED_SAND) || world.getBlockState(posBelow).isOf(Blocks.RED_SAND)) {
            return PlayerParticleTypes.FOOTPRINT_REDSAND;
        }
        if (world.getBlockState(pos).isOf(Blocks.MUD) || world.getBlockState(posBelow).isOf(Blocks.MUD) ||
                world.getBlockState(posBelow).isOf(Blocks.PACKED_MUD) ||
                world.getBlockState(pos).isOf(Blocks.DIRT) || world.getBlockState(posBelow).isOf(Blocks.DIRT) ||
                world.getBlockState(pos).isOf(Blocks.COARSE_DIRT) || world.getBlockState(posBelow).isOf(Blocks.COARSE_DIRT) ||
                world.getBlockState(pos).isOf(Blocks.ROOTED_DIRT) || world.getBlockState(posBelow).isOf(Blocks.ROOTED_DIRT)) {
            return PlayerParticleTypes.FOOTPRINT_MUDDY;
        }

        // Fallback to normal footprint
        return PlayerParticleTypes.FOOTPRINT;
    }

    private static boolean isExposedToRain(ClientWorld world, double x, double y, double z) {
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        // Check if the sky is visible above the entity (no solid blocks)
        return world.isSkyVisible(pos) && !world.getBlockState(pos.up()).isSolidBlock(world, pos.up());
    }

    // Helper class to track entity movement
    private static class EntityStepTracker {
        Vec3d lastPos = null;
        double distanceTraveled = 0;
        boolean isLeftFoot = true;
        boolean wasInAir = false; // Track if entity was in air for jump detection
        int wetTicks = 0; // Track wet state duration
    }
}