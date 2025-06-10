package com.niuhi.sounds;

import net.minecraft.sound.SoundEvent;
import java.util.HashMap;
import java.util.Map;

public class SoundSettings {
    public record SoundConfig(SoundType type, float maxVolume, float pitch, float fadeDuration) {}

    private static final Map<SoundEvent, SoundConfig> SETTINGS = new HashMap<>();

    static {
        // Wind sounds (Static)
        SETTINGS.put(SoundEvents.SOFT_WIND, new SoundConfig(SoundType.STATIC, 0.9f, 1.0f, 0.3f));
        SETTINGS.put(SoundEvents.NORMAL_WIND, new SoundConfig(SoundType.STATIC, 0.9f, 1.0f, 0.3f));
        SETTINGS.put(SoundEvents.HEAVY_WIND, new SoundConfig(SoundType.STATIC, 0.9f, 1.0f, 0.3f));
        SETTINGS.put(SoundEvents.STORM_WIND, new SoundConfig(SoundType.STATIC, 0.9f, 1.0f, 0.3f));
        // Cascade sound (Directional)
        SETTINGS.put(SoundEvents.CASCADE, new SoundConfig(SoundType.DIRECTIONAL, 1.0f, 1.0f, 1.5f)); // Increased max volume further
    }

    public static SoundConfig getSettings(SoundEvent sound) {
        return SETTINGS.getOrDefault(sound, new SoundConfig(SoundType.STATIC, 1.0f, 1.0f, 0.3f));
    }
}