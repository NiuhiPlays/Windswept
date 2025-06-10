package com.niuhi.water;

import com.niuhi.particle.water.WaterParticleTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;

public class SplashSystem {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(SplashSystem::tick);
    }

    private static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            return;
        }

        BlockPos playerPos = client.player.getBlockPos();
        Random random = world.random;
        int radius = 16;

        Box box = new Box(playerPos).expand(radius);
        for (Entity entity : world.getEntitiesByClass(Entity.class, box, e -> e instanceof LivingEntity || e instanceof ItemEntity)) {
            if (entity.isTouchingWater()) {
                double velocityY = entity.getVelocity().y;
                if (velocityY < -0.2) {
                    BlockPos pos = entity.getBlockPos();
                    double px = pos.getX() + random.nextDouble();
                    double py = pos.getY() + 1.0;
                    double pz = pos.getZ() + random.nextDouble();

                    int particleCount = Math.min(5, (int) (Math.abs(velocityY) * 2));

                    if (velocityY < -0.5 && random.nextFloat() < 0.4f) {
                        for (int i = 0; i < particleCount; i++) {
                            world.addParticleClient(WaterParticleTypes.BIGSPLASH, px + random.nextGaussian() * 0.2,
                                    py, pz + random.nextGaussian() * 0.2, 0, 0, 0);
                        }
                    } else {
                        for (int i = 0; i < particleCount; i++) {
                            world.addParticleClient(WaterParticleTypes.SPLASH, px + random.nextGaussian() * 0.2,
                                    py, pz + random.nextGaussian() * 0.2, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }
}