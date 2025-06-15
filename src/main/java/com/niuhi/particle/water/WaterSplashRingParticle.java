package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class WaterSplashRingParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;
    private final float sizeMultiplier;

    protected WaterSplashRingParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider,
                                      double sizeMultiplier) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.sizeMultiplier = (float) Math.max(0.8, Math.min(3.0, sizeMultiplier));
        this.maxAge = (int) (20 * this.sizeMultiplier); // Matched lifespan
        this.scale = 0.8f * this.sizeMultiplier; // Larger size for base
        this.alpha = 0.9f; // Slightly translucent

        // Static particle
        this.velocityY = 0.0;
        this.velocityX = 0.0;
        this.velocityZ = 0.0;
        this.gravityStrength = 0.0f;

        // Set color based on biome's water color
        int waterColor = world.getBiome(new BlockPos((int)x, (int)y, (int)z)).value().getWaterColor();
        this.red = ((waterColor >> 16) & 0xFF) / 255.0f;
        this.green = ((waterColor >> 8) & 0xFF) / 255.0f;
        this.blue = (waterColor & 0xFF) / 255.0f;

        this.setSprite(spriteProvider.getSprite(0, 8));
    }

    @Override
    public void tick() {
        super.tick();
        this.animationTimer += 0.8f;
        int frameIndex = ((int) this.animationTimer) % 9;
        this.setSprite(spriteProvider.getSprite(frameIndex, 8));
        this.scale += 0.02f * this.sizeMultiplier; // Expansion for animation
        this.alpha = MathHelper.clamp(0.9f - ((float) this.age / this.maxAge) * 0.7f, 0.0f, 0.9f); // Adjusted fade-out
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3d cameraPos = camera.getPos();
        float x = (float)(this.x - cameraPos.x);
        float y = (float)(this.y - cameraPos.y);
        float z = (float)(this.z - cameraPos.z);

        // Get combined light level
        BlockPos pos = new BlockPos((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
        int light = WorldRenderer.getLightmapCoordinates(world, pos);

        // Define quad size
        float size = this.getSize(partialTicks) * 0.7f; // Larger quad size

        // Define the four corners of the particle quad (flat on XZ plane)
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(x - size, y, z - size), // Bottom-left
                new Vector3f(x - size, y, z + size), // Top-left
                new Vector3f(x + size, y, z + size), // Top-right
                new Vector3f(x + size, y, z - size)  // Bottom-right
        };

        // Get UV coordinates
        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();

        // Render a single quad (flat, facing upward)
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).texture(minU, maxV)
                .color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).texture(minU, minV)
                .color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).texture(maxU, minV)
                .color(this.red, this.green, this.blue, this.alpha).light(light);
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).texture(maxU, maxV)
                .color(this.red, this.green, this.blue, this.alpha).light(light);
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
            return new WaterSplashRingParticle(world, x, y, z, spriteProvider, velocityX);
        }
    }
}