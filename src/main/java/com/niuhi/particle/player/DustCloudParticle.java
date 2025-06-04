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
        this.maxAge = 25 + random.nextInt(10); // 25-35 ticks - shorter than tide splash
        this.scale = 0.2f + random.nextFloat() * 0.3f; // Smaller than tide splash

        // Quick, small splash velocities
        double angle = random.nextDouble() * Math.PI * 2;
        double speed = 0.05 + random.nextDouble() * 0.08;
        this.velocityX = Math.cos(angle) * speed;
        this.velocityZ = Math.sin(angle) * speed;
        this.velocityY = 0.02 + random.nextDouble() * 0.06; // Small upward motion

        this.alpha = 0.7f;
        this.gravityStrength = 0.06f; // More affected by gravity than tide splash
        this.animationTimer = 0.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 7)); // Assuming 8 frames (0-7)
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.8f;
        int frameIndex = ((int) this.animationTimer) % 8;
        this.setSprite(spriteProvider.getSprite(frameIndex, 7));

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