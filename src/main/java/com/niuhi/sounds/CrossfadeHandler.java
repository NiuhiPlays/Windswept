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
    private SoundEvent currentSoundEvent;
    private float currentVolume;
    private float targetVolume;
    private float fade; // 0.0 (silent) to 1.0 (full volume)
    private final float fadeDuration; // In seconds
    private long lastSoundStartTick; // Track sound start for proactive restarts
    private static final float LOOP_OVERLAP = 2.5f; // Overlap for looping transitions
    private static final float MIN_FADE_THRESHOLD = 0.05f; // Minimum fade to keep sound alive

    public CrossfadeHandler(float fadeDuration) {
        this.client = MinecraftClient.getInstance();
        this.currentSound = null;
        this.currentSoundEvent = null;
        this.currentVolume = 0.0f;
        this.targetVolume = 0.0f;
        this.fade = 0.0f;
        this.fadeDuration = fadeDuration;
        this.lastSoundStartTick = 0;
    }

    public void update(float deltaTime, SoundEvent sound, SoundType type, float targetVolume, BlockPos position) {
        this.targetVolume = targetVolume;

        // Clean up any lingering sounds to prevent stacking
        cleanUpSounds();

        // Update fade for current sound
        boolean shouldPlay = sound != null && targetVolume > MIN_FADE_THRESHOLD;
        float fadeTarget = shouldPlay ? 1.0f : 0.0f;
        fade = MathHelper.lerp(deltaTime / fadeDuration, fade, fadeTarget);

        // Update current volume
        currentVolume = MathHelper.lerp(0.25f, currentVolume, targetVolume);

        // Check if a new sound instance is needed
        boolean needsNewSound = shouldPlay && (
                sound != currentSoundEvent || // Different sound
                        currentSound == null || // No current sound
                        !client.getSoundManager().isPlaying(currentSound) || // Current sound stopped
                        shouldRestartForLoop() // Proactive restart for looping
        );

        if (needsNewSound) {
            if (currentSound != null && fade > MIN_FADE_THRESHOLD) {
                // Stop old sound immediately to prevent stacking
                client.getSoundManager().stop(currentSound);
            }
            currentSound = null;
            currentSound = createSoundInstance(sound, type, currentVolume * fade, position);
            client.getSoundManager().play(currentSound);
            currentSoundEvent = sound;
            lastSoundStartTick = currentTick();
        } else if (currentSound != null) {
            client.getSoundManager().updateSoundVolume(SoundCategory.AMBIENT, currentVolume * fade);
        }
    }

    private void cleanUpSounds() {
        // Stop any unexpected sound instances to prevent stacking (e.g., during rapid wind changes)
        if (currentSound != null && !client.getSoundManager().isPlaying(currentSound)) {
            client.getSoundManager().stop(currentSound);
            currentSound = null;
            currentSoundEvent = null;
        }
    }

    private boolean shouldRestartForLoop() {
        if (currentSound == null || currentSoundEvent == null) return false;

        // Use a heuristic for loop restart: restart every 3 seconds to ensure no gaps
        // AmbientSounds avoids duration tracking, relying on fade and isPlaying
        long currentTick = currentTick();
        float elapsedSeconds = (currentTick - lastSoundStartTick) / 20.0f; // Ticks to seconds
        return elapsedSeconds >= 20.0f - LOOP_OVERLAP; // Restart 0.15s before estimated loop point
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
        currentVolume = 0.0f;
        targetVolume = 0.0f;
        fade = 0.0f;
        lastSoundStartTick = 0;
    }
}