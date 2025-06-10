package com.niuhi.sounds;

import com.niuhi.Windswept;
import com.niuhi.water.CascadeSystem;
import com.niuhi.wind.WindSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AmbientSoundSystem {
    private final MinecraftClient client;
    private SoundInstance currentWindSound;
    private SoundInstance currentCascadeSound;
    private float windSoundVolume;
    private float cascadeSoundVolume;
    private WindSystem.WindType lastWindType;
    private boolean nearWaterfall;
    private long lastUpdateTick;
    private BlockPos lastWaterfallPos;

    public enum SoundType {
        STATIC, // Non-directional, centered on player
        DIRECTIONAL // Positioned at a specific location with attenuation
    }

    public AmbientSoundSystem() {
        this.client = MinecraftClient.getInstance();
        this.currentWindSound = null;
        this.currentCascadeSound = null;
        this.windSoundVolume = 0.0f;
        this.cascadeSoundVolume = 0.0f;
        this.lastWindType = WindSystem.WindType.NONE;
        this.nearWaterfall = false;
        this.lastUpdateTick = 0;
        this.lastWaterfallPos = null;
    }

    public void tick() {
        if (client.world == null || client.player == null) {
            stopAllSounds();
            return;
        }

        long currentTick = client.world.getTime();
        if (currentTick - lastUpdateTick < 3) { // Update every 3 ticks (~0.15 seconds)
            return;
        }
        lastUpdateTick = currentTick;

        updateWindSound();
        updateCascadeSound();
    }

    private void updateWindSound() {
        if (Windswept.WIND_SYSTEM == null) return;

        WindSystem.WindType currentWindType = Windswept.WIND_SYSTEM.getWindType();
        SoundEvent targetSound = getWindSound(currentWindType);
        float targetVolume = currentWindType.getStrength() > 0 ? Math.min(currentWindType.getStrength() * 0.4f, 0.8f) : 0.0f;

        windSoundVolume = MathHelper.lerp(0.15f, windSoundVolume, targetVolume);

        if (currentWindType != lastWindType || currentWindSound == null || !client.getSoundManager().isPlaying(currentWindSound)) {
            if (currentWindSound != null) {
                client.getSoundManager().stop(currentWindSound);
                currentWindSound = null;
            }

            if (targetSound != null && windSoundVolume > 0.01f) {
                currentWindSound = createSoundInstance(
                        targetSound,
                        SoundType.STATIC,
                        windSoundVolume,
                        null // No specific position for static sounds
                );
                client.getSoundManager().play(currentWindSound);
            }
            lastWindType = currentWindType;
        } else if (currentWindSound != null) {
            client.getSoundManager().updateSoundVolume(SoundCategory.AMBIENT, windSoundVolume);
        }
    }

    private void updateCascadeSound() {
        assert client.player != null;
        BlockPos playerPos = client.player.getBlockPos();
        BlockPos nearestWaterfall = findNearestWaterfall(playerPos);
        boolean isNearWaterfall = nearestWaterfall != null;

        float targetVolume = isNearWaterfall ? calculateCascadeVolume(playerPos, nearestWaterfall) : 0.0f;
        cascadeSoundVolume = MathHelper.lerp(0.2f, cascadeSoundVolume, targetVolume);

        if (isNearWaterfall != nearWaterfall || !isSameWaterfall(nearestWaterfall) || currentCascadeSound == null || !client.getSoundManager().isPlaying(currentCascadeSound)) {
            if (currentCascadeSound != null) {
                client.getSoundManager().stop(currentCascadeSound);
                currentCascadeSound = null;
            }

            if (isNearWaterfall && cascadeSoundVolume > 0.01f) {
                currentCascadeSound = createSoundInstance(
                        SoundEvents.CASCADE,
                        SoundType.DIRECTIONAL,
                        cascadeSoundVolume,
                        nearestWaterfall
                );
                client.getSoundManager().play(currentCascadeSound);
            }
            nearWaterfall = isNearWaterfall;
            lastWaterfallPos = nearestWaterfall;
        } else if (currentCascadeSound != null) {
            client.getSoundManager().updateSoundVolume(SoundCategory.AMBIENT, cascadeSoundVolume);
        }
    }

    private SoundInstance createSoundInstance(SoundEvent sound, SoundType type, float volume, BlockPos position) {
        if (type == SoundType.STATIC) {
            assert client.world != null;
            return new PositionedSoundInstance(
                    sound.id(),
                    SoundCategory.AMBIENT,
                    volume,
                    (float) 1.0,
                    client.world.random,
                    true, // Looping
                    0, // No delay
                    SoundInstance.AttenuationType.NONE, // No attenuation for static
                    0.0, 0.0, 0.0, // Centered on player
                    true // Relative
            );
        } else { // DIRECTIONAL
            double x;
            if (position != null) {
                x = position.getX() + 0.5;
            } else {
                assert client.player != null;
                x = client.player.getX();
            }
            double y = position != null ? position.getY() + 0.5 : client.player.getY();
            double z = position != null ? position.getZ() + 0.5 : client.player.getZ();
            assert client.world != null;
            return new PositionedSoundInstance(
                    sound.id(),
                    SoundCategory.AMBIENT,
                    volume,
                    (float) 1.0,
                    client.world.random,
                    true, // Looping
                    0, // No delay
                    SoundInstance.AttenuationType.LINEAR, // Distance-based attenuation
                    x, y, z, // Position at waterfall
                    false // Not relative
            );
        }
    }

    private BlockPos findNearestWaterfall(BlockPos playerPos) {
        int radius = 12;
        BlockPos nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    assert client.world != null;
                    BlockPos impactPos = CascadeSystem.findWaterfallImpact(client.world, pos);
                    if (impactPos != null) {
                        double distance = Math.sqrt(playerPos.getSquaredDistance(impactPos));
                        if (distance <= 12.0 && distance < minDistance) {
                            minDistance = distance;
                            nearest = impactPos;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private float calculateCascadeVolume(BlockPos playerPos, BlockPos waterfallPos) {
        double distance = Math.sqrt(playerPos.getSquaredDistance(waterfallPos));
        // Linear volume falloff: full volume (1.2) at distance 0, 0 at distance 12
        float maxVolume = 1.2f;
        float maxDistance = 12.0f;
        return MathHelper.clamp(maxVolume * (1.0f - (float)(distance / maxDistance)), 0.0f, maxVolume);
    }

    private boolean isSameWaterfall(BlockPos newWaterfallPos) {
        if (lastWaterfallPos == null || newWaterfallPos == null) {
            return lastWaterfallPos == newWaterfallPos;
        }
        return lastWaterfallPos.equals(newWaterfallPos);
    }

    private SoundEvent getWindSound(WindSystem.WindType windType) {
        return switch (windType) {
            case SOFT -> SoundEvents.SOFT_WIND;
            case NORMAL -> SoundEvents.NORMAL_WIND;
            case HEAVY -> SoundEvents.HEAVY_WIND;
            case STORM -> SoundEvents.STORM_WIND;
            default -> null;
        };
    }

    private void stopAllSounds() {
        if (currentWindSound != null) {
            client.getSoundManager().stop(currentWindSound);
            currentWindSound = null;
            windSoundVolume = 0.0f;
        }
        if (currentCascadeSound != null) {
            client.getSoundManager().stop(currentCascadeSound);
            currentCascadeSound = null;
            cascadeSoundVolume = 0.0f;
        }
        lastWindType = WindSystem.WindType.NONE;
        nearWaterfall = false;
        lastWaterfallPos = null;
    }

    public static void register() {
        AmbientSoundSystem soundSystem = new AmbientSoundSystem();
        ClientTickEvents.END_CLIENT_TICK.register(client -> soundSystem.tick());
    }
}