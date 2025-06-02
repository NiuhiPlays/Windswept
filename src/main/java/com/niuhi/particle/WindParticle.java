package com.niuhi.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class WindParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;
    private final Vec3d windDirection;

    protected WindParticle(ClientWorld world, double x, double y, double z,
                           SpriteProvider spriteProvider) {
        super(world, x, y, z, 0, 0, 0); // Zero velocity
        this.spriteProvider = spriteProvider;
        this.maxAge = 100 + world.random.nextInt(80); // 5-9 seconds
        this.scale = 0.6f + world.random.nextFloat() * 0.4f; // 0.6-1.0 scale
        this.alpha = 0.7f; // Constant alpha, textures handle opacity
        this.windDirection = new Vec3d(1, 0.1, 0).normalize(); // Fixed wind direction
        this.setSprite(spriteProvider.getSprite(0, 30));
        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f; // No gravity to prevent Y-axis jitter
    }

    @Override
    public void tick() {
        // Update animation
        this.animationTimer += 0.3f; // Slightly faster animation
        int frameIndex = ((int) this.animationTimer) % 31; // Cycle through 31 textures
        this.setSprite(spriteProvider.getSprite(frameIndex, 30));

        // Age the particle
        if (this.age++ >= this.maxAge) {
            this.markDead();
        }

        // Subtle scale changes
        this.scale += (float) (random.nextGaussian() * 0.001);
        this.scale = Math.max(0.4f, Math.min(0.8f, this.scale));
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // Get camera and particle position
        Vec3d camPos = camera.getPos();
        float x = (float) (this.x - camPos.x);
        float y = (float) (this.y - camPos.y);
        float z = (float) (this.z - camPos.z);

        // Calculate rotation to align with wind direction (XZ plane)
        float yaw = (float) MathHelper.atan2(windDirection.z, windDirection.x);
        float cosYaw = MathHelper.cos(-yaw - (float) Math.PI / 2);
        float sinYaw = MathHelper.sin(-yaw - (float) Math.PI / 2);

        // Define quad vertices (size based on scale)
        float size = this.getSize(partialTicks);
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-size * cosYaw - size * sinYaw, 0, -size * sinYaw + size * cosYaw),
                new Vector3f(-size * cosYaw + size * sinYaw, 0, -size * sinYaw - size * cosYaw),
                new Vector3f(size * cosYaw + size * sinYaw, 0, size * sinYaw - size * cosYaw),
                new Vector3f(size * cosYaw - size * sinYaw, 0, size * sinYaw + size * cosYaw)
        };

        // Translate vertices to particle position
        for (Vector3f vertex : vertices) {
            vertex.add(x, y, z);
        }

        // Get sprite UV coordinates
        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();

        // Draw quad with full brightness
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .texture(minU, maxV).color(1.0f, 1.0f, 1.0f, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .texture(minU, minV).color(1.0f, 1.0f, 1.0f, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .texture(maxU, minV).color(1.0f, 1.0f, 1.0f, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .texture(maxU, maxV).color(1.0f, 1.0f, 1.0f, this.alpha)
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
            return new WindParticle(world, x, y, z, spriteProvider);
        }
    }
}