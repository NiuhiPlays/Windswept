package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;

public class WaveParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected WaveParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 25;
        this.scale = 1.0f;
        this.alpha = 1.0f;

        // Set biome-based color
        int waterColor = world.getBiome(new BlockPos((int)x, (int)y, (int)z)).value().getWaterColor();
        this.red = ((waterColor >> 16) & 0xFF) / 255.0f;
        this.green = ((waterColor >> 8) & 0xFF) / 255.0f;
        this.blue = (waterColor & 0xFF) / 255.0f;

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 19));
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.8f;
        int frameIndex = ((int) this.animationTimer) % 20;
        this.setSprite(spriteProvider.getSprite(frameIndex, 19));

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
            return new WaveParticle(world, x, y, z, spriteProvider);
        }
    }
}