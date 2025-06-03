package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class TideSplashParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected TideSplashParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 30 + random.nextInt(15); // 30-45 ticks
        this.scale = 0.5f + random.nextFloat() * 0.4f; // Larger splash particles

        // Dynamic splash velocities
        double angle = random.nextDouble() * Math.PI * 2;
        double speed = 0.1 + random.nextDouble() * 0.15;
        this.velocityX = Math.cos(angle) * speed;
        this.velocityZ = Math.sin(angle) * speed;
        this.velocityY = 0.05 + random.nextDouble() * 0.1; // Upward splash

        this.alpha = 0.9f;
        this.gravityStrength = 0.04f; // Affected by gravity
        this.animationTimer = 0.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 11)); // Assuming 12 frames (0-11)
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.6f;
        int frameIndex = ((int) this.animationTimer) % 12;
        this.setSprite(spriteProvider.getSprite(frameIndex, 11));

        // Quick fade and fall
        this.alpha = 0.9f - ((float) this.age / this.maxAge) * 0.7f;

        // Slow down horizontal movement due to water resistance
        this.velocityX *= 0.95;
        this.velocityZ *= 0.95;

        // Slight size reduction as water droplets evaporate
        if (this.age > this.maxAge / 2) {
            this.scale *= 0.99f;
        }
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
            return new TideSplashParticle(world, x, y, z, spriteProvider);
        }
    }
}