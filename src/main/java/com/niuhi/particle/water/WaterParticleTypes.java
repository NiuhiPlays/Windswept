package com.niuhi.particle.water;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WaterParticleTypes {
    public static final SimpleParticleType CASCADE = FabricParticleTypes.simple();
    public static final SimpleParticleType TIDE_SPLASH = FabricParticleTypes.simple();
    public static final SimpleParticleType RIPPLE = FabricParticleTypes.simple();
    public static final SimpleParticleType SPLASH = FabricParticleTypes.simple();

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "cascade"), CASCADE);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "tide_splash"), TIDE_SPLASH);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "ripple"), RIPPLE);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "splash"), SPLASH);
    }
}