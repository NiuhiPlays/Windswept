package com.niuhi.sounds;

import com.niuhi.Windswept;
import com.niuhi.water.CascadeSystem;
import com.niuhi.wind.WindSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AmbientSoundSystem {
    private final MinecraftClient client;
    private final SoundLogic soundLogic;
    private WindSystem.WindType lastWindType;
    private BlockPos lastWaterfallPos;
    private long lastUpdateTick;

    public AmbientSoundSystem() {
        this.client = MinecraftClient.getInstance();
        this.soundLogic = new SoundLogic(0.3f, 0.4f); // Fade durations from SoundSettings
        this.lastWindType = WindSystem.WindType.NONE;
        this.lastWaterfallPos = null;
        this.lastUpdateTick = 0;
    }

    public void tick() {
        if (client.world == null || client.player == null) {
            soundLogic.stopAllSounds();
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
        if (Windswept.WIND_SYSTEM == null) {
            soundLogic.playWindSound(null, 0.0f);
            lastWindType = WindSystem.WindType.NONE;
            return;
        }

        WindSystem.WindType currentWindType = Windswept.WIND_SYSTEM.getWindType();
        SoundEvent targetSound = getWindSound(currentWindType);
        SoundSettings.SoundConfig config = SoundSettings.getSettings(targetSound != null ? targetSound : SoundEvents.SOFT_WIND);
        float targetVolume = currentWindType.getStrength() > 0 ? Math.min(currentWindType.getStrength() * 0.4f, config.maxVolume()) : 0.0f;

        soundLogic.playWindSound(targetSound, targetVolume);
        lastWindType = currentWindType;
    }

    private void updateCascadeSound() {
        assert client.player != null;
        BlockPos playerPos = client.player.getBlockPos();
        BlockPos nearestWaterfall = findNearestWaterfall(playerPos);
        boolean isNearWaterfall = nearestWaterfall != null;

        SoundSettings.SoundConfig config = SoundSettings.getSettings(SoundEvents.CASCADE);
        float targetVolume = isNearWaterfall ? calculateCascadeVolume(playerPos, nearestWaterfall, config.maxVolume()) : 0.0f;

        soundLogic.playCascadeSound(isNearWaterfall ? SoundEvents.CASCADE : null, targetVolume, nearestWaterfall);
        lastWaterfallPos = nearestWaterfall;
    }

    private BlockPos findNearestWaterfall(BlockPos playerPos) {
        int radius = 24;
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
                        if (distance <= 24.0 && distance < minDistance) {
                            minDistance = distance;
                            nearest = impactPos;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private float calculateCascadeVolume(BlockPos playerPos, BlockPos waterfallPos, float maxVolume) {
        double distance = Math.sqrt(playerPos.getSquaredDistance(waterfallPos));
        float maxDistance = 24.0f;
        // Adjusted falloff to maintain higher volume closer to source
        return MathHelper.clamp(maxVolume * (1.0f - (float)(distance / (maxDistance * 1.5f))), 0.0f, maxVolume);
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

    public static void register() {
        AmbientSoundSystem soundSystem = new AmbientSoundSystem();
        ClientTickEvents.END_CLIENT_TICK.register(client -> soundSystem.tick());
    }
}