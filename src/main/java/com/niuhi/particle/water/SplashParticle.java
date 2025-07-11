package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class SplashParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected SplashParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 25;
        this.scale = 1.0f;
        this.alpha = 1.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 3));
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.8f;
        int frameIndex = ((int) this.animationTimer) % 4;
        this.setSprite(spriteProvider.getSprite(frameIndex, 3));

        // Quick fade
        this.alpha = 0.7f - ((float) this.age / this.maxAge) * 0.6f;
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
            return new SplashParticle(world, x, y, z, spriteProvider);
        }
    }
}