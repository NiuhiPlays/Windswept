package com.niuhi.particle.player;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class PlayerParticleTypes {
    public static final SimpleParticleType FOOTPRINT = FabricParticleTypes.simple();
    public static final SimpleParticleType DUST_CLOUD = FabricParticleTypes.simple();

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint"), FOOTPRINT);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "dustcloud"), DUST_CLOUD);
    }
}