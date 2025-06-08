package com.niuhi.particle.player;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class DustCloudParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected DustCloudParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 25;
        this.scale = 0.8f;

        this.alpha = 0.7f;
        this.gravityStrength = 0.06f;
        this.animationTimer = 0.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 5));
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.8f;
        int frameIndex = ((int) this.animationTimer) % 8;
        this.setSprite(spriteProvider.getSprite(frameIndex, 6));

        // Quick fade
        this.alpha = 0.7f - ((float) this.age / this.maxAge) * 0.6f;

        // Rapid deceleration
        this.velocityX *= 0.92;
        this.velocityZ *= 0.92;

        // Shrink as it disperses
        this.scale *= 0.98f;
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
            return new com.niuhi.particle.player.DustCloudParticle(world, x, y, z, spriteProvider);
        }
    }
}