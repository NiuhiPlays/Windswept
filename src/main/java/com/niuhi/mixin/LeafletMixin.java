package com.niuhi.mixin;

import com.niuhi.Windswept;
import com.niuhi.wind.WindSystem;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.particle.LeavesParticle.class)
public abstract class LeafletMixin extends SpriteBillboardParticle {
	protected LeafletMixin(ClientWorld world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void disableCollisions(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, float gravity, float f, boolean bl, boolean bl2, float size, float initialYVelocity, CallbackInfo ci) {
		this.collidesWithWorld = false; // Prevent leaflets from getting stuck on blocks
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void applyWindEffect(CallbackInfo ci) {
		if (Windswept.WIND_SYSTEM != null && this.world != null) {
			WindSystem windSystem = Windswept.WIND_SYSTEM;
			Vec3d windDirection = windSystem.getWindDirection();
			float windStrength = windSystem.getWindStrength();
			float baseStrength = 0.003f; // Subtle effect for leaves
			this.velocityX += windDirection.x * windStrength * baseStrength;
			this.velocityY += windDirection.y * windStrength * baseStrength;
			this.velocityZ += -windDirection.z * windStrength * baseStrength; // Negate Z to match WindParticle render
		}
	}
}