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
        this.maxAge = 100 + world.random.nextInt(80); // 5-9 seconds
        this.scale = 0.6f + world.random.nextFloat() * 0.4f; // Smaller, subtler size
        this.alpha = 0.5f + world.random.nextFloat() * 0.3f; // Slightly less opaque

        this.windDirection = new Vec3d(1, 0.1, 0);
        this.windStrength = 0.02f + world.random.nextFloat() * 0.02f; // Slower movement

        this.setSprite(spriteProvider.getSprite(0, 30));
        this.gravityStrength = -0.005f; // Gentle upward drift
        this.collidesWithWorld = false;
    }

    @Override
    public void tick() {
        this.animationTimer += 0.4f; // Slower animation to reduce flickering
        int frameIndex = ((int) this.animationTimer) % 31; // Cycle through 31 textures
        this.setSprite(spriteProvider.getSprite(frameIndex, 30));

        // Slower movement with less randomness
        this.velocityX = windDirection.x * windStrength + (random.nextGaussian() * 0.005);
        this.velocityY = windDirection.y * windStrength + (random.nextGaussian() * 0.003);
        this.velocityZ = windDirection.z * windStrength + (random.nextGaussian() * 0.005);

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        if (this.age++ >= this.maxAge) {
            this.markDead();
        }

        // Gradual fade-out
        float ageRatio = (float) this.age / (float) this.maxAge;
        this.alpha = Math.max(0.2f, 0.8f - (ageRatio * 0.6f));

        // Subtle scale changes
        this.scale += (float) (random.nextGaussian() * 0.001);
        this.scale = Math.max(0.4f, Math.min(0.8f, this.scale));
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getBrightness(float tint) {
        return 240; // Full brightness
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
            return new WindParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
        }
    }
}