package com.niuhi.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundEvents {
    public static final SoundEvent SOFT_WIND = register("soft_wind");
    public static final SoundEvent NORMAL_WIND = register("normal_wind");
    public static final SoundEvent HEAVY_WIND = register("heavy_wind");
    public static final SoundEvent STORM_WIND = register("storm_wind");
    public static final SoundEvent CASCADE = register("cascade");
    public static final SoundEvent RIPPLE = register("ripple");
    public static final SoundEvent DUSTCLOUD = register("dustcloud");

    private static SoundEvent register(String id) {
        Identifier identifier = Identifier.of("windswept", id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void initialize() {
        // Static initializer for sound events
    }
}