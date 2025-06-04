package com.niuhi.player;

import com.niuhi.particle.player.PlayerParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
            if (!(entity instanceof LivingEntity) || entity.isSpectator()) {
                continue; // Skip non-living entities and spectators
            }

            // Skip entities that are not moving on the ground
            if (!entity.isOnGround() || entity.getVelocity().horizontalLengthSquared() < 0.01) {
                continue;
            }

            UUID entityId = entity.getUuid();
            EntityStepTracker tracker = stepTrackers.computeIfAbsent(entityId, k -> new EntityStepTracker());

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

        // Spawn the particle using the registered FOOTPRINT_PARTICLE
        world.addParticleClient(PlayerParticleTypes.FOOTPRINT, x, y, z, 0, 0, 0);
    }

    // Helper class to track entity movement
    private static class EntityStepTracker {
        Vec3d lastPos = null;
        double distanceTraveled = 0;
        boolean isLeftFoot = true;
    }
}