package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class RippleSystem {
    private static final float RIPPLE_CHANCE = 0.25f; // Chance to spawn ripple per tick
    private static int tickCounter = 0;

    // Define settings for each entity group
    private record RippleSettings(float sizeMultiplier, float maxAge, float animationSpeed) {
        static final RippleSettings PLAYER =     new RippleSettings(1.5f, 40.0f, 0.5f); // Standard ripples
        static final RippleSettings ANIMAL =     new RippleSettings(1.5f, 40.0f, 0.5f); // Smaller, lighter ripples
        static final RippleSettings MONSTER =    new RippleSettings(1.2f, 40.0f, 0.5f); // Larger, darker ripples
        static final RippleSettings ITEM =       new RippleSettings(1.0f, 20.0f, 0.5f); // Small, quick ripples
        static final RippleSettings PROJECTILE = new RippleSettings(1.0f, 20.0f, 0.5f); // Tiny, fast-dissipating
        static final RippleSettings BOAT =       new RippleSettings(2.5f, 40.0f, 0.5f); // Slightly larger than player
        static final RippleSettings RAVAGER =    new RippleSettings(4.5f, 30.0f, 0.5f); // Large ripples for Ravager
    }

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

        // For players, living entities, items, projectiles, and boats
        if (entity instanceof LivingEntity ||
                entity instanceof ItemEntity || entity instanceof ProjectileEntity ||
                entity instanceof BoatEntity) {
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
            // Determine entity group and settings
            RippleSettings settings = getRippleSettings(entity);

            // Calculate entity size-based multiplier
            float entityWidth = entity.getDimensions(entity.getPose()).width();
            float entityHeight = entity.getDimensions(entity.getPose()).height();
            float entitySize = (entityWidth + entityHeight) / 2.0f;
            float sizeMultiplier = MathHelper.clamp(entitySize * settings.sizeMultiplier, 0.5f, 2.0f);

            double waterHeight = blockPos.getY() + world.getFluidState(blockPos).getHeight(world, blockPos);
            world.addParticleClient(WaterParticleTypes.RIPPLE, // Assumes RIPPLE is your registered particle
                    pos.x, waterHeight + 0.1, pos.z,
                    sizeMultiplier, settings.maxAge, settings.animationSpeed);
        }
    }

    private static RippleSettings getRippleSettings(Entity entity) {
        if (entity.getType().toString().contains("ravager")) {
            return RippleSettings.RAVAGER;
        } else if (entity instanceof PlayerEntity) {
            return RippleSettings.PLAYER;
        } else if (entity instanceof PassiveEntity) {
            return RippleSettings.ANIMAL;
        } else if (entity instanceof HostileEntity) {
            return RippleSettings.MONSTER;
        } else if (entity instanceof ItemEntity) {
            return RippleSettings.ITEM;
        } else if (entity instanceof ProjectileEntity) {
            return RippleSettings.PROJECTILE;
        } else if (entity instanceof BoatEntity) {
            return RippleSettings.BOAT;
        }
        // Default fallback (should rarely be used)
        return RippleSettings.PLAYER;
    }
}