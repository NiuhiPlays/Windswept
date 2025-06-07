package com.niuhi.player;

import com.niuhi.particle.player.PlayerParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
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

            // Skip entities that are not moving on the ground or landing
            boolean isOnGround = livingEntity.isOnGround();
            UUID entityId = entity.getUuid();
            EntityStepTracker tracker = stepTrackers.computeIfAbsent(entityId, k -> new EntityStepTracker());

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

        // Determine the appropriate particle based on block or weather
        SimpleParticleType particleType = getFootprintParticle(world, x, y, z, entity);

        // Spawn the particle
        world.addParticleClient(particleType, x, y, z, 0, 0, 0);
    }

    private static SimpleParticleType getFootprintParticle(ClientWorld world, double x, double y, double z, Entity entity) {
        // Check block state for specific blocks
        BlockPos pos = new BlockPos((int) x, (int) (y + 0.0625), (int) z); // Slightly above for thin blocks like snow
        BlockPos posBelow = pos.down();

        if (world.getBlockState(pos).isOf(Blocks.SNOW) || world.getBlockState(posBelow).isOf(Blocks.SNOW) ||
                world.getBlockState(pos).isOf(Blocks.SNOW_BLOCK) || world.getBlockState(posBelow).isOf(Blocks.SNOW_BLOCK)) {
            return PlayerParticleTypes.FOOTPRINT_SNOW;
        }
        if (world.getBlockState(pos).isOf(Blocks.SAND) || world.getBlockState(posBelow).isOf(Blocks.SAND)) {
            return PlayerParticleTypes.FOOTPRINT_SAND;
        }
        if (world.getBlockState(pos).isOf(Blocks.RED_SAND) || world.getBlockState(posBelow).isOf(Blocks.RED_SAND)) {
            return PlayerParticleTypes.FOOTPRINT_REDSAND;
        }
        if (world.getBlockState(pos).isOf(Blocks.MUD) || world.getBlockState(posBelow).isOf(Blocks.MUD)) {
            return PlayerParticleTypes.FOOTPRINT_MUDDY;
        }
        // Check for water or rain
        if (entity instanceof LivingEntity livingEntity && livingEntity.isTouchingWaterOrRain()) {
            return PlayerParticleTypes.FOOTPRINT_WET;
        }

        // Fallback to normal footprint
        return PlayerParticleTypes.FOOTPRINT;
    }

    // Helper class to track entity movement
    private static class EntityStepTracker {
        Vec3d lastPos = null;
        double distanceTraveled = 0;
        boolean isLeftFoot = true;
        boolean wasInAir = false; // Track if entity was in air for jump detection
    }
}