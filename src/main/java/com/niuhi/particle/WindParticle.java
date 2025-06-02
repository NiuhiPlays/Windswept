package com.niuhi.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;

public class WindParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private final Vec3d windDirection;
    private final float windStrength;
    private float animationTimer;

    protected WindParticle(ClientWorld world, double x, double y, double z,
                           double velocityX, double velocityY, double velocityZ,
                           SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        this.spriteProvider = spriteProvider;
        this.maxAge = 60 + world.random.nextInt(40); // 3-5 seconds at 20 TPS
        this.scale = 0.3f + world.random.nextFloat() * 0.4f; // Varied sizes
        this.alpha = 0.6f + world.random.nextFloat() * 0.3f;

        // Wind properties
        this.windDirection = new Vec3d(1, 0, 0); // Default: East direction
        this.windStrength = 0.02f + world.random.nextFloat() * 0.03f;
        this.animationTimer = 0;

        // Set initial sprite
        this.setSprite(spriteProvider.getSprite(0, 30)); // First frame

        // Gravity and physics
        this.gravityStrength = 0.0f; // Wind particles float
        this.collidesWithWorld = false;
    }

    @Override
    public void tick() {
        super.tick();

        // Update animation
        this.animationTimer += 1.0f;
        int frameIndex = (int) (this.animationTimer * 0.5f) % 31; // Cycle through 31 textures
        this.setSprite(spriteProvider.getSprite(frameIndex, 30));

        // Apply wind movement
        this.velocityX = windDirection.x * windStrength + (random.nextGaussian() * 0.005);
        this.velocityY = windDirection.y * windStrength + (random.nextGaussian() * 0.002);
        this.velocityZ = windDirection.z * windStrength + (random.nextGaussian() * 0.005);

        // Fade out over time
        this.alpha = Math.max(0, this.alpha - (1.0f / this.maxAge));

        // Scale changes for wind effect
        this.scale += (float) (random.nextGaussian() * 0.001);
        this.scale = Math.max(0.1f, Math.min(1.0f, this.scale));
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    // Factory class for creating wind particles
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new WindParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
        }
    }
}