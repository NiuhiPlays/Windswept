package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class RippleParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected RippleParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 20; // Particle lifespan in ticks
        this.scale = 0.1f; // Small initial size
        this.velocityY = 0.0; // Ripples stay on water surface
        this.alpha = 0.8f;
        this.animationTimer = 0.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 9)); // Assuming 10 frames (0-9)
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.5f;
        int frameIndex = ((int) this.animationTimer) % 10;
        this.setSprite(spriteProvider.getSprite(frameIndex, 9));

        this.scale += 0.02f; // Gradually expand
        this.alpha = 0.8f - ((float) this.age / this.maxAge) * 0.8f; // Fade out
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
            return new RippleParticle(world, x, y, z, spriteProvider);
        }
    }
}