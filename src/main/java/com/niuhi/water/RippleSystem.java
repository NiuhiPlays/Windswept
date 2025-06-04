package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class RippleSystem {
    private static final float RIPPLE_CHANCE = 0.15f; // Chance to spawn ripple per tick
    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter % 5 == 0) { // Run every 3 ticks to reduce performance impact
                spawnRipplesForEntities(client);
            }
        });
    }

    private static void spawnRipplesForEntities(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return;

        for (Entity entity : world.getEntities()) {
            if (shouldSpawnRipple(entity)) {
                spawnRippleForEntity(world, entity);
            }
        }
    }

    private static boolean shouldSpawnRipple(Entity entity) {
        // Check if entity is in water
        BlockPos checkPos = entity.getBlockPos();
        if (!entity.isTouchingWater() || !entity.getWorld().getFluidState(checkPos).isOf(Fluids.WATER)) {
            return false;
        }

        // For players, mobs, items, and projectiles
        if (entity instanceof PlayerEntity || entity instanceof ItemEntity || entity instanceof ProjectileEntity || entity.isLiving()) {
            // Check movement or submersion
            Vec3d velocity = entity.getVelocity();
            double speed = velocity.horizontalLength();
            return speed > 0.01 || entity.isSubmergedInWater() || entity.getY() < entity.getWorld().getSeaLevel();
        }

        return false;
    }

    private static void spawnRippleForEntity(ClientWorld world, Entity entity) {
        if (world.random.nextFloat() > RIPPLE_CHANCE) return; // Randomize to avoid excessive particles

        Vec3d pos = entity.getPos();
        BlockPos blockPos = entity.getBlockPos();
        if (world.getFluidState(blockPos).isOf(Fluids.WATER)) {
            // Calculate entity size based on bounding box
            float entityWidth = entity.getDimensions(entity.getPose()).width();
            float entityHeight = entity.getDimensions(entity.getPose()).height();
            float entitySize = (entityWidth + entityHeight) / 1.5f; // Average of width and height
            float sizeMultiplier = MathHelper.clamp(entitySize, 1.0f, 3.0f); // Normalize to 0.5-2.0

            double waterHeight = blockPos.getY() + world.getFluidState(blockPos).getHeight(world, blockPos);
            world.addParticleClient(WaterParticleTypes.RIPPLE, // Assumes RIPPLE is your registered particle
                    pos.x, waterHeight + 0.1, pos.z,
                    sizeMultiplier, 0.0, 0.0); // Pass sizeMultiplier via velocityX
        }
    }
}