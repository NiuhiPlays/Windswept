package com.niuhi.water.culling;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ParticleCulling {
    private static Frustum cachedFrustum;
    private static long lastUpdateTime = 0;
    private static final long FRUSTUM_CACHE_TIME = 50; // Cache frustum for 50ms (1 tick)

    /**
     * Check if a position is within the player's view frustum
     * @param pos The world position to check
     * @param radius The radius around the position to consider (for particle spread)
     * @return true if the position should be rendered, false if it can be culled
     */
    public static boolean isInView(Vec3d pos, double radius) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return false;
        }

        // Get current frustum (cached for performance)
        Frustum frustum = getCurrentFrustum();
        if (frustum == null) {
            return true; // If we can't get frustum, render everything to be safe
        }

        // Create a bounding box around the particle position
        Box particleBox = new Box(
                pos.x - radius, pos.y - radius, pos.z - radius,
                pos.x + radius, pos.y + radius, pos.z + radius
        );

        // Check if the box intersects with the view frustum
        return frustum.isVisible(particleBox);
    }

    /**
     * Check if a position is within the player's view frustum with default radius
     * @param pos The world position to check
     * @return true if the position should be rendered, false if it can be culled
     */
    public static boolean isInView(Vec3d pos) {
        return isInView(pos, 1.0); // Default 1 block radius for particle effects
    }

    /**
     * Additional distance-based culling for performance
     * @param pos The world position to check
     * @param maxDistance Maximum distance to render particles
     * @return true if within distance, false if too far
     */
    public static boolean isWithinDistance(Vec3d pos, double maxDistance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        Vec3d playerPos = client.player.getPos();
        double distanceSquared = playerPos.squaredDistanceTo(pos);
        return distanceSquared <= (maxDistance * maxDistance);
    }

    /**
     * Combined culling check: both frustum and distance
     * @param pos The world position to check
     * @param radius The radius around the position to consider
     * @param maxDistance Maximum distance to render
     * @return true if should render, false if should cull
     */
    public static boolean shouldRender(Vec3d pos, double radius, double maxDistance) {
        return isWithinDistance(pos, maxDistance) && isInView(pos, radius);
    }

    /**
     * Get current view frustum with caching for performance
     */
    private static Frustum getCurrentFrustum() {
        long currentTime = System.currentTimeMillis();

        // Use cached frustum if it's recent enough
        if (cachedFrustum != null && (currentTime - lastUpdateTime) < FRUSTUM_CACHE_TIME) {
            return cachedFrustum;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null || client.player == null) {
            return null;
        }

        try {
            // Get the current camera
            Camera camera = client.gameRenderer.getCamera();
            if (camera == null) {
                return cachedFrustum; // Return old cached version if available
            }

            // Use a simpler approach - create frustum from camera position and rotation
            // This avoids accessing private methods
            Entity cameraEntity = camera.getFocusedEntity();
            if (cameraEntity == null) {
                return cachedFrustum;
            }

            // Create a basic frustum using the camera's position and the standard projection
            // We'll use the worldRenderer's frustum if available, otherwise create a simple one
            if (client.worldRenderer != null) {
                // Try to access the world renderer's frustum
                // This is a more reliable approach than trying to create our own
                cachedFrustum = client.worldRenderer.getCapturedFrustum();
                if (cachedFrustum != null) {
                    lastUpdateTime = currentTime;
                    return cachedFrustum;
                }
            }

            // If we can't get the world renderer's frustum, fall back to null
            // This will cause the system to render all particles (safe fallback)
            return null;

        } catch (Exception e) {
            // If frustum creation fails, return null (will default to rendering everything)
            return null;
        }
    }

    /**
     * Check if player is looking approximately towards a position
     * This is a cheaper alternative to full frustum culling for simple cases
     * @param pos The position to check
     * @param fovAngle The field of view angle in degrees (e.g., 90 for 90-degree cone)
     * @return true if player is looking towards the position
     */
    public static boolean isLookingTowards(Vec3d pos, double fovAngle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }

        Vec3d playerPos = client.player.getEyePos();
        Vec3d lookDirection = client.player.getRotationVec(1.0f);
        Vec3d toPosition = pos.subtract(playerPos).normalize();

        // Calculate angle between look direction and direction to position
        double dot = lookDirection.dotProduct(toPosition);
        double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));

        return angle <= (fovAngle / 2.0);
    }
}