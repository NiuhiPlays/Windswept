package com.niuhi.particle.player;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class PlayerParticleTypes {
    public static final SimpleParticleType FOOTPRINT = FabricParticleTypes.simple();
    public static final SimpleParticleType FOOTPRINT_WET = FabricParticleTypes.simple();
    public static final SimpleParticleType FOOTPRINT_SNOW = FabricParticleTypes.simple();
    public static final SimpleParticleType FOOTPRINT_MUDDY = FabricParticleTypes.simple();
    public static final SimpleParticleType FOOTPRINT_SAND = FabricParticleTypes.simple();
    public static final SimpleParticleType FOOTPRINT_REDSAND = FabricParticleTypes.simple();
    public static final SimpleParticleType DUST_CLOUD = FabricParticleTypes.simple();

    public static void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint"), FOOTPRINT);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint_wet"), FOOTPRINT_WET);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint_snow"), FOOTPRINT_SNOW);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint_muddy"), FOOTPRINT_MUDDY);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint_sand"), FOOTPRINT_SAND);
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "footprint_redsand"), FOOTPRINT_REDSAND);

        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of("windswept", "dustcloud"), DUST_CLOUD);
    }
}