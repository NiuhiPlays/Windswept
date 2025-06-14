package com.niuhi.particle.water;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WaterParticleTypes {
    public static final SimpleParticleType CASCADE = FabricParticleTypes.simple();
    public static final SimpleParticleType BIGSPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType RIPPLE = FabricParticleTypes.simple();
    public static final SimpleParticleType SPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType WAVE = FabricParticleTypes.simple();
    public static final SimpleParticleType FOAM = FabricParticleTypes.simple();
    public static final SimpleParticleType WATERSPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType WATERSPLASHFOAM = FabricParticleTypes.simple();
    public static final SimpleParticleType WATERSPLASHRING = FabricParticleTypes.simple();

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "cascade"), CASCADE);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "bigsplash"), BIGSPLASH);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "ripple"), RIPPLE);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "splash"), SPLASH);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "wave"), WAVE);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "foam"), FOAM);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "watersplash"), WATERSPLASH);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "watersplashfoam"), WATERSPLASHFOAM);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "watersplashring"), WATERSPLASHRING);
    }
}