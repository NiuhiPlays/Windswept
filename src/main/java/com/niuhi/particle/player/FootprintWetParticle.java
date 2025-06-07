package com.niuhi.particle.player;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.joml.Vector3f;

public class FootprintWetParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected FootprintWetParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 200 ;
        this.scale = 0.3f;


        // Set initial sprite frame
        this.setSprite(spriteProvider.getSprite(0, 1));
    }

    @Override
    public void tick() {
        super.tick();

        // Animate sprite
        this.animationTimer += 0.5f;
        int frameIndex = ((int) this.animationTimer) % 8;
        this.setSprite(spriteProvider.getSprite(frameIndex, 1));

        // Quick fade
        this.alpha = 0.8f - ((float) this.age / this.maxAge) * 0.8f;

    }
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // Get particle position relative to camera
        double x = this.x - camera.getPos().x;
        double y = (this.y + 0.01f) - camera.getPos().y;
        double z = this.z - camera.getPos().z;

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
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .texture(minU, minV).color(this.red, this.green, this.blue, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .texture(maxU, minV).color(this.red, this.green, this.blue, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .texture(maxU, maxV).color(this.red, this.green, this.blue, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
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
            return new FootprintWetParticle(world, x, y, z, spriteProvider);
        }
    }
}