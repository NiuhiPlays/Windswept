package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public class WaveParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;
    private final float directionX; // Normal X from shoreline (points into water)
    private final float directionZ; // Normal Z from shoreline (points into water)

    protected WaveParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider,
                           double directionX, double directionZ) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.maxAge = 25;
        this.scale = 1.0f;
        this.alpha = 1.0f;
        this.velocityX = 0.0; // Lock position
        this.velocityY = 0.0;
        this.velocityZ = 0.0;
        // Normalize direction or use fallback
        float length = (float) Math.sqrt(directionX * directionX + directionZ * directionZ);
        this.directionX = length > 0 ? (float) (directionX / length) : 1.0f;
        this.directionZ = length > 0 ? (float) (directionZ / length) : 0.0f;

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

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // Get particle position relative to camera
        double x = this.x - camera.getPos().x;
        double y = this.y - camera.getPos().y;
        double z = this.z - camera.getPos().z;

        // Define quad size
        float size = this.getSize(partialTicks) * 0.5f;

        // Calculate orientation vectors
        Vector3f along = new Vector3f(-directionZ, 0, directionX).normalize().mul(size); // Perpendicular to normal (along shoreline)
        Vector3f forward = new Vector3f(-directionX, 0, -directionZ).normalize().mul(size); // Opposite normal (toward shoreline)

        // Define quad vertices flat on XZ plane
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f((float)x - along.x() - forward.x(), (float)y, (float)z - along.z() - forward.z()), // Back-left
                new Vector3f((float)x - along.x() + forward.x(), (float)y, (float)z - along.z() + forward.z()), // Front-left
                new Vector3f((float)x + along.x() + forward.x(), (float)y, (float)z + along.z() + forward.z()), // Front-right
                new Vector3f((float)x + along.x() - forward.x(), (float)y, (float)z + along.z() - forward.z())  // Back-right
        };

        // Get UV coordinates
        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();

        // Render quad
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

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new WaveParticle(world, x, y, z, spriteProvider, velocityX, velocityZ);
        }
    }
}