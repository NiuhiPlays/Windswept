package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public class WaterSplashParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;
    private final float sizeMultiplier;

    protected WaterSplashParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider,
                                  double sizeMultiplier, double maxAge, double animationSpeed) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.sizeMultiplier = (float) Math.max(0.5, Math.min(2.0, sizeMultiplier)); // Clamp between 0.5x and 2x
        this.maxAge = (int) (20 * this.sizeMultiplier); // Scale lifespan (10-40 ticks)
        this.scale = 0.8f * this.sizeMultiplier; // Scale initial size
        this.alpha = 0.8f;


        // Set color based on biome's water color
        int waterColor = world.getBiome(new BlockPos((int)x, (int)y, (int)z)).value().getWaterColor();
        this.red = ((waterColor >> 16) & 0xFF) / 255.0f; // Extract red component
        this.green = ((waterColor >> 8) & 0xFF) / 255.0f; // Extract green component
        this.blue = (waterColor & 0xFF) / 255.0f; // Extract blue component

        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 8));
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.8f;
        int frameIndex = ((int) this.animationTimer) % 9;
        this.setSprite(spriteProvider.getSprite(frameIndex, 8));

        this.scale += 0.02f * this.sizeMultiplier; // Faster expansion for larger ripples
        this.alpha = 0.8f - ((float) this.age / this.maxAge) * 0.8f; // Fade out
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // Get particle position relative to camera
        double x = this.x - camera.getPos().x;
        double y = this.y - camera.getPos().y;
        double z = this.z - camera.getPos().z;

        // Get combined light level using WorldRenderer
        BlockPos pos = new BlockPos((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
        int light = WorldRenderer.getLightmapCoordinates(world, pos);

        // Define quad size (half the scale for each side)
        float size = this.getSize(partialTicks) * 0.5f;

        // Define the four corners of the particle quad (flat on XZ plane)
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f((float)x - size, (float)y, (float)z - size), // Bottom-left
                new Vector3f((float)x - size, (float)y, (float)z + size), // Top-left
                new Vector3f((float)x + size, (float)y, (float)z + size), // Top-right
                new Vector3f((float)x + size, (float)y, (float)z - size)  // Bottom-right
        };

        // Get UV coordinates for the current sprite
        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();

        // Render a single quad (flat, facing upward)
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .texture(minU, maxV).color(this.red, this.green, this.blue, this.alpha)
                .light(light);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .texture(minU, minV).color(this.red, this.green, this.blue, this.alpha)
                .light(light);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .texture(maxU, minV).color(this.red, this.green, this.blue, this.alpha)
                .light(light);
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .texture(maxU, maxV).color(this.red, this.green, this.blue, this.alpha)
                .light(light);
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
            return new WaterSplashParticle(world, x, y, z, spriteProvider, velocityX, velocityY, velocityZ); // Pass velocityX as sizeMultiplier
        }
    }
}