package com.niuhi.player;

import com.niuhi.particle.player.PlayerParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class RunningCloudsSystem {
    private final ClientWorld world;
    private final MinecraftClient client;
    private final Random random;
    private double lastPlayerX;
    private double lastPlayerZ;
    private double lastHorseX;
    private double lastHorseZ;
    private boolean wasSprinting;
    private boolean wasRidingHorse;
    private int horseGroundGraceTicks = 0; // Grace period for horse ground check
    private int playerGroundGraceTicks = 0; // Grace period for player ground check
    private static int tickCounter = 0;
    private static RunningCloudsSystem instance; // Static instance

    public RunningCloudsSystem(ClientWorld world) {
        this.world = world;
        this.client = MinecraftClient.getInstance();
        this.random = world != null ? world.random : Random.create();
        this.lastPlayerX = 0;
        this.lastPlayerZ = 0;
        this.lastHorseX = 0;
        this.lastHorseZ = 0;
        this.wasSprinting = false;
        this.wasRidingHorse = false;
        this.horseGroundGraceTicks = 0;
        this.playerGroundGraceTicks = 0;
    }

    public void tick() {
        if (world == null || client.player == null) return;

        PlayerEntity player = client.player;
        BlockPos pos = player.getBlockPos();
        boolean isSprinting = player.isSprinting();
        boolean isRidingHorse = player.hasVehicle() && player.getVehicle() instanceof AbstractHorseEntity;

        // Handle sprinting
        if (isSprinting && !player.isSubmergedInWater()) {
            // Update grace period for player ground check
            if (player.isOnGround()) {
                playerGroundGraceTicks = 0; // Reset grace period when on ground
            } else {
                playerGroundGraceTicks++; // Increment when not on ground
            }

            // Allow particles if on ground OR within grace period (5 ticks = 0.25 seconds)
            boolean canSpawnParticles = player.isOnGround() || playerGroundGraceTicks <= 10;

            if (canSpawnParticles) {
                tickCounter++;
                if (tickCounter % 3 == 0) { // Spawn every 3 ticks (~0.15 seconds)
                    spawnDustParticles(pos, 1);
                }
            }
        }
        // Handle horse riding
        else if (isRidingHorse) {
            AbstractHorseEntity horse = (AbstractHorseEntity) player.getVehicle();

            // Update grace period for ground check
            if (horse.isOnGround()) {
                horseGroundGraceTicks = 0; // Reset grace period when on ground
            } else {
                horseGroundGraceTicks++; // Increment when not on ground
            }

            // Allow particles if on ground OR within grace period (5 ticks = 0.25 seconds)
            boolean canSpawnParticles = horse.isOnGround() || horseGroundGraceTicks <= 10;

            if (canSpawnParticles && !horse.isSubmergedInWater()) {
                // Initialize horse position on first ride or when switching horses
                if (!wasRidingHorse) {
                    lastHorseX = horse.getX();
                    lastHorseZ = horse.getZ();
                    wasRidingHorse = true;
                    return; // Skip first tick to establish baseline position
                }

                // Check horse movement
                double deltaX = horse.getX() - lastHorseX;
                double deltaZ = horse.getZ() - lastHorseZ;
                double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20; // Speed in blocks/second

                // Update position after calculating speed
                lastHorseX = horse.getX();
                lastHorseZ = horse.getZ();

                if (speed > 1.5) { // Require movement (adjust threshold if needed)
                    pos = horse.getBlockPos(); // Use horse's position
                    tickCounter++;
                    if (tickCounter % 3 == 0) { // Spawn every 3 ticks
                        spawnDustParticles(pos, 2);
                    }
                }
            }
        } else {
            wasSprinting = false;
            wasRidingHorse = false;
            horseGroundGraceTicks = 0; // Reset grace ticks when not riding
            playerGroundGraceTicks = 0; // Reset grace ticks when not sprinting
            tickCounter = 0; // Reset counter when not sprinting or riding
        }

        // Update player position
        lastPlayerX = player.getX();
        lastPlayerZ = player.getZ();
    }

    private void spawnDustParticles(BlockPos pos, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            // Spawn particles around player's or horse's feet
            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;
            double x = pos.getX() + 0.5 + offsetX;
            double y = pos.getY() + 0.1; // Slightly above ground
            double z = pos.getZ() + 0.5 + offsetZ;

            // Check if the block below is solid (e.g., dirt, sand)
            BlockPos belowPos = pos.down();
            if (!world.getBlockState(belowPos).isSolidBlock(world, belowPos)) {
                continue; // Skip if not a solid surface
            }

            // Minimal initial motion; wind effect handled by mixin
            double motionX = (random.nextDouble() - 0.5) * 0.015;
            double motionY = 0.03 + random.nextDouble() * 0.03; // Slight upward motion
            double motionZ = (random.nextDouble() - 0.5) * 0.015;

            // Spawn dust particle
            world.addParticleClient(PlayerParticleTypes.DUST_CLOUD, x, y, z, motionX, motionY, motionZ);
        }
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientWorld world = client.world;
            if (world == null || client.player == null) return;

            // Create or reuse instance to maintain state between ticks
            if (instance == null || instance.world != world) {
                instance = new RunningCloudsSystem(world);
            }
            instance.tick();
        });
    }
}