package com.niuhi.particle.water;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class WaterSplashFoamParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private float animationTimer;

    protected WaterSplashFoamParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider,
                                      double sizeMultiplier) {
        super(world, x, y + 0.2, z); // Above surface
        this.spriteProvider = spriteProvider;
        float sizeMultiplier1 = (float) Math.max(0.5, Math.min(2.0, sizeMultiplier));
        this.maxAge = (int) (25 * sizeMultiplier1); // Adjusted lifespan
        this.scale = sizeMultiplier1 * 1.1f; // Just slightly bigger than bounding box
        this.alpha = 0.9f; // Slightly translucent

        // Static particle
        this.velocityY = 0.0;
        this.velocityX = 0.0;
        this.velocityZ = 0.0;
        this.gravityStrength = 0.0f;

        // White foam color
        this.red = 1.0f;
        this.green = 1.0f;
        this.blue = 1.0f;

        this.setSprite(spriteProvider.getSprite(0, 8));
    }

    @Override
    public void tick() {
        super.tick();

        // Play animation only once by clamping the frame index
        this.animationTimer += 0.8f;
        int frameIndex = Math.min(8, (int) this.animationTimer); // Clamp to max frame (8)
        this.setSprite(spriteProvider.getSprite(frameIndex, 8));

        // No fading - keep constant alpha
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3d cameraPos = camera.getPos();
        float centerX = (float)(this.x - cameraPos.x);
        float centerY = (float)(this.y - cameraPos.y);
        float centerZ = (float)(this.z - cameraPos.z);

        // Get light level
        BlockPos pos = new BlockPos((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
        int light = WorldRenderer.getLightmapCoordinates(world, pos);

        // Define quad size - foam should be slightly larger and taller than main splash
        float size = this.getSize(partialTicks) * 1.1f;
        float height = size * 1.8f; // Taller foam for dramatic effect
        float halfSize = size * 0.5f;

        // Get UV coordinates
        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();

        // Create 4 vertical foam walls that form a perfect square perimeter
        // Each wall spans exactly from corner to corner of the bounding box

        // North wall (spans full bounding box width at +Z edge)
        renderDoubleSidedVerticalWall(buffer, centerX, centerY, centerZ + halfSize,
                size, height, minU, maxU, minV, maxV, light, 0);

        // South wall (spans full bounding box width at -Z edge)
        renderDoubleSidedVerticalWall(buffer, centerX, centerY, centerZ - halfSize,
                size, height, minU, maxU, minV, maxV, light, 180);

        // East wall (spans full bounding box depth at +X edge)
        renderDoubleSidedVerticalWall(buffer, centerX + halfSize, centerY, centerZ,
                size, height, minU, maxU, minV, maxV, light, 90);

        // West wall (spans full bounding box depth at -X edge)
        renderDoubleSidedVerticalWall(buffer, centerX - halfSize, centerY, centerZ,
                size, height, minU, maxU, minV, maxV, light, 270);
    }

    private void renderDoubleSidedVerticalWall(VertexConsumer buffer, float centerX, float centerY, float centerZ,
                                               float width, float height, float minU, float maxU, float minV, float maxV,
                                               int light, float yRotation) {

        // Render front face
        renderVerticalWall(buffer, centerX, centerY, centerZ, width, height, minU, maxU, minV, maxV, light, yRotation, false);

        // Render back face (reversed winding order)
        renderVerticalWall(buffer, centerX, centerY, centerZ, width, height, minU, maxU, minV, maxV, light, yRotation, true);
    }

    private void renderVerticalWall(VertexConsumer buffer, float centerX, float centerY, float centerZ,
                                    float width, float height, float minU, float maxU, float minV, float maxV,
                                    int light, float yRotation, boolean reversed) {

        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        // Create vertices for a vertical quad
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-halfWidth, -halfHeight, 0), // Bottom-left
                new Vector3f(-halfWidth, halfHeight, 0),  // Top-left
                new Vector3f(halfWidth, halfHeight, 0),   // Top-right
                new Vector3f(halfWidth, -halfHeight, 0)   // Bottom-right
        };

        // Apply Y rotation to face the correct direction
        float radians = (float)Math.toRadians(yRotation);
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);

        for (Vector3f vertex : vertices) {
            float x = vertex.x();
            float z = vertex.z();
            vertex.set(
                    x * cos - z * sin + centerX,
                    vertex.y() + centerY,
                    x * sin + z * cos + centerZ
            );
        }

        // Render the vertical wall quad with slightly reduced alpha for layering effect
        float alpha = this.alpha * 0.8f; // Slightly more transparent for foam layering

        // Render with proper winding order
        if (!reversed) {
            // Front face - normal winding order
            buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                    .texture(minU, maxV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
            buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                    .texture(minU, minV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
            buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                    .texture(maxU, minV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
            buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                    .texture(maxU, maxV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
        } else {
            // Back face - reversed winding order
            buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                    .texture(maxU, maxV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
            buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                    .texture(maxU, minV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
            buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                    .texture(minU, minV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
            buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                    .texture(minU, maxV)
                    .color(this.red, this.green, this.blue, alpha)
                    .light(light);
        }
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
            return new WaterSplashFoamParticle(world, x, y, z, spriteProvider, velocityX);
        }
    }
}