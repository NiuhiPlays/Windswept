package com.niuhi.particle.wind;

import com.niuhi.Windswept;
import com.niuhi.wind.WindSystem;
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
    private final WindSystem windSystem;

    protected WindParticle(ClientWorld world, double x, double y, double z,
                           SpriteProvider spriteProvider, WindSystem windSystem) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;
        this.windSystem = windSystem;
        this.maxAge = 100 + world.random.nextInt(80);

        // Set scale based on wind type
        float baseScale = switch (windSystem.getWindType()) {
            case SOFT -> 0.5f;
            case NORMAL -> 1.0f;
            case HEAVY -> 2.0f;
            default -> 0.8f; // Fallback for NONE (though particles shouldn't spawn)
        };
        this.scale = baseScale + world.random.nextFloat() * 0.2f; // Slight variation

        this.alpha = 0.8f;
        this.setSprite(spriteProvider.getSprite(0, 30));
        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f;
    }

    @Override
    public void tick() {
        this.animationTimer += 0.3f;
        int frameIndex = ((int) this.animationTimer) % 31;
        this.setSprite(spriteProvider.getSprite(frameIndex, 30));

        if (this.age++ >= this.maxAge) {
            this.markDead();
        }

        this.scale += (float) (random.nextGaussian() * 0.001);
        this.scale = Math.max(0.4f, Math.min(1.4f, this.scale)); // Adjusted bounds for scaling
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3d camPos = camera.getPos();
        float x = (float) (this.x - camPos.x);
        float y = (float) (this.y - camPos.y);
        float z = (float) (this.z - camPos.z);

        Vec3d windDirection = windSystem.getWindDirection();
        float yaw = (float) MathHelper.atan2(windDirection.z, windDirection.x);

        float size = this.getSize(partialTicks);

        float cosYaw = MathHelper.cos(yaw);
        float sinYaw = MathHelper.sin(yaw);

        Vector3f right = new Vector3f(cosYaw, 0, -sinYaw).mul(size);
        Vector3f up = new Vector3f(0, size, 0);

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(x - right.x() - up.x(), y - right.y() - up.y(), z - right.z() - up.z()),
                new Vector3f(x - right.x() + up.x(), y - right.y() + up.y(), z - right.z() + up.z()),
                new Vector3f(x + right.x() + up.x(), y + right.y() + up.y(), z + right.z() + up.z()),
                new Vector3f(x + right.x() - up.x(), y + right.y() - up.y(), z + right.z() - up.z())
        };

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();

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

        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .texture(maxU, maxV).color(1.0f, 1.0f, 1.0f, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .texture(maxU, minV).color(1.0f, 1.0f, 1.0f, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .texture(minU, minV).color(1.0f, 1.0f, 1.0f, this.alpha)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .texture(minU, maxV).color(1.0f, 1.0f, 1.0f, this.alpha)
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
            return new WindParticle(world, x, y, z, spriteProvider, Windswept.WIND_SYSTEM);
        }
    }
}