package com.niuhi.sounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class CrossfadeHandler {
    private final MinecraftClient client;
    private SoundInstance currentSound;
    private SoundInstance fadingSound;
    private SoundEvent currentSoundEvent;
    private float currentVolume;
    private float targetVolume;
    private float fadeProgress;
    private final float fadeDuration; // In seconds
    private long lastSoundStartTime; // Track when the last sound started
    private static final float SOUND_OVERLAP_THRESHOLD = 0.1f; // Start new sound 0.1s before current ends

    public CrossfadeHandler(float fadeDuration) {
        this.client = MinecraftClient.getInstance();
        this.currentSound = null;
        this.fadingSound = null;
        this.currentSoundEvent = null;
        this.currentVolume = 0.0f;
        this.targetVolume = 0.0f;
        this.fadeProgress = 1.0f;
        this.fadeDuration = fadeDuration * 1.5f; // Increase fade duration by 50% for smoother overlap
        this.lastSoundStartTime = 0;
    }

    public void update(float deltaTime, SoundEvent sound, SoundType type, float targetVolume, BlockPos position) {
        this.targetVolume = targetVolume;

        // Update fade progress for fading sound
        if (fadeProgress < 1.0f && fadingSound != null) {
            fadeProgress = MathHelper.clamp(fadeProgress + deltaTime / fadeDuration, 0.0f, 1.0f);
            float fadeOutVolume = MathHelper.lerp(fadeProgress, currentVolume, 0.0f);
            client.getSoundManager().updateSoundVolume(SoundCategory.AMBIENT, fadeOutVolume);
            if (fadeProgress >= 1.0f) {
                client.getSoundManager().stop(fadingSound);
                fadingSound = null;
            }
        }

        // Update current volume
        currentVolume = MathHelper.lerp(0.25f, currentVolume, targetVolume); // Slightly faster lerp for responsiveness

        // Check if a new sound instance is needed
        boolean needsNewSound = sound != null && (
                sound != currentSoundEvent || // Different sound
                        currentSound == null || // No current sound
                        !client.getSoundManager().isPlaying(currentSound) || // Current sound stopped
                        shouldPrestartSound() // Prestart for seamless looping
        );

        if (needsNewSound) {
            if (currentSound != null && currentVolume > 0.01f) {
                fadingSound = currentSound;
                fadeProgress = 0.0f; // Start crossfade
            }
            currentSound = null;
            if (targetVolume > 0.05f) { // Higher threshold to avoid faint starts
                currentSound = createSoundInstance(sound, type, currentVolume, position);
                client.getSoundManager().play(currentSound);
                currentSoundEvent = sound;
                lastSoundStartTime = currentTick();
            }
        } else if (currentSound != null) {
            client.getSoundManager().updateSoundVolume(SoundCategory.AMBIENT, currentVolume);
        }
    }

    private boolean shouldPrestartSound() {
        // Placeholder: Assume sound duration is unknown; use a fixed overlap check every 5 seconds
        // Ideally, we'd check the sound's actual duration, but Minecraft doesn't expose this
        long currentTime = currentTick();
        float ticksSinceStart = (currentTime - lastSoundStartTime) / 80.0f; // Convert ticks to seconds
        return ticksSinceStart >= 5.0f - SOUND_OVERLAP_THRESHOLD; // Start new sound 0.1s before estimated end
    }

    private long currentTick() {
        return client.world != null ? client.world.getTime() : 0;
    }

    private SoundInstance createSoundInstance(SoundEvent sound, SoundType type, float volume, BlockPos position) {
        double x = position != null ? position.getX() + 0.5 : 0.0;
        double y = position != null ? position.getY() + 0.5 : 0.0;
        double z = position != null ? position.getZ() + 0.5 : 0.0;
        boolean relative = type == SoundType.STATIC;
        SoundInstance.AttenuationType attenuation = type == SoundType.STATIC ? SoundInstance.AttenuationType.NONE : SoundInstance.AttenuationType.LINEAR;

        assert client.world != null;
        return new PositionedSoundInstance(
                sound.id(),
                SoundCategory.AMBIENT,
                volume,
                1.0f,
                client.world.random,
                true, // Looping
                0, // No delay
                attenuation,
                x, y, z,
                relative
        );
    }

    public void stop() {
        if (currentSound != null) {
            client.getSoundManager().stop(currentSound);
            currentSound = null;
            currentSoundEvent = null;
        }
        if (fadingSound != null) {
            client.getSoundManager().stop(fadingSound);
            fadingSound = null;
        }
        currentVolume = 0.0f;
        targetVolume = 0.0f;
        fadeProgress = 1.0f;
        lastSoundStartTime = 0;
    }
}