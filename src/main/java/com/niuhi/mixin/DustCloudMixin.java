package com.niuhi.mixin;

import com.niuhi.Windswept;
import com.niuhi.particle.player.DustCloudParticle;
import com.niuhi.wind.WindSystem;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DustCloudParticle.class)
public abstract class DustCloudMixin extends SpriteBillboardParticle {
    protected DustCloudMixin(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyWindEffect(CallbackInfo ci) {
        if (Windswept.WIND_SYSTEM != null && this.world != null) {
            WindSystem windSystem = Windswept.WIND_SYSTEM;
            Vec3d windDirection = windSystem.getWindDirection();
            float windStrength = windSystem.getWindStrength();
            float baseStrength = 0.01f; // Moderate effect for dust
            this.velocityX += windDirection.x * windStrength * baseStrength;
            this.velocityY += windDirection.y * windStrength * baseStrength;
            this.velocityZ += -windDirection.z * windStrength * baseStrength; // Negate Z to match WindParticle render

            // Apply drag in strong winds
            if (windStrength > 1.0f) { // Affects NORMAL and HEAVY winds
                float dragFactor = 0.99f;
                this.velocityX *= dragFactor;
                this.velocityZ *= dragFactor;
            }
        }
    }
}