package com.niuhi.mixin;

import com.niuhi.Windswept;
import com.niuhi.particle.water.CascadeParticle;
import com.niuhi.wind.WindSystem;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CascadeParticle.class)
public abstract class CascadeMixin extends SpriteBillboardParticle {
    protected CascadeMixin(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyWindEffect(CallbackInfo ci) {
        if (Windswept.WIND_SYSTEM != null && this.world != null) {
            WindSystem windSystem = Windswept.WIND_SYSTEM;
            Vec3d windDirection = windSystem.getWindDirection();
            float windStrength = windSystem.getWindStrength();

            // Stronger effect for cascade particles since they're water droplets
            float baseStrength = 0.0005f; // More responsive to wind than leaves

            // Apply wind with realistic water droplet physics
            this.velocityX += windDirection.x * windStrength * baseStrength;
            this.velocityY += windDirection.y * windStrength * baseStrength;
            this.velocityZ += -windDirection.z * windStrength * baseStrength; // Negate Z to match your system

            // Optional: Add some drag effect in strong winds
            if (windStrength > 0.5f) {
                float dragFactor = 0.98f; // Slight drag in strong winds
                this.velocityX *= dragFactor;
                this.velocityZ *= dragFactor;
            }
        }
    }
}