package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class CascadeParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected CascadeParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 100 + random.nextInt(40); // 60-80 ticks
        this.scale = 0.4f + random.nextFloat() * 0.3f; // Variable size
        this.alpha = 0.8f;
        this.animationTimer = 0.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 10));
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.5f;
        int frameIndex = ((int) this.animationTimer) % 11;
        this.setSprite(spriteProvider.getSprite(frameIndex, 10));

    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new CascadeParticle(world, x, y, z, spriteProvider);
        }
    }
}