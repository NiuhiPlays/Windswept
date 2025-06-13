package com.niuhi;

import com.niuhi.debug.DebugCommands;
import com.niuhi.particle.player.*;
import com.niuhi.particle.water.*;
import com.niuhi.particle.weather.wind.WindParticle;
import com.niuhi.particle.weather.wind.WindParticleTypes;
import com.niuhi.player.FootprintSystem;
import com.niuhi.player.RunningCloudsSystem;
import com.niuhi.sounds.AmbientSoundSystem;
import com.niuhi.sounds.SoundEvents;
import com.niuhi.water.*;
import com.niuhi.weather.wind.WindSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Windswept implements ClientModInitializer {
	public static final String MOD_ID = "windswept";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static WindSystem WIND_SYSTEM = null; // Initialize to null, set during registration

	@Override
	public void onInitializeClient() {
		LOGGER.info("Blowing in the Clouds and stuff!");

		// Wind Particles
		WindParticleTypes.registerParticles();
		ParticleFactoryRegistry.getInstance().register(WindParticleTypes.WIND, WindParticle.Factory::new);

		// Water Particles
		WaterParticleTypes.registerParticles();
		ParticleFactoryRegistry.getInstance().register(WaterParticleTypes.CASCADE, CascadeParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(WaterParticleTypes.BIGSPLASH, BigSplashParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(WaterParticleTypes.RIPPLE, RippleParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(WaterParticleTypes.SPLASH, SplashParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(WaterParticleTypes.WAVE, WaveParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(WaterParticleTypes.FOAM, FoamParticle.Factory::new);

		// Player Particles
		PlayerParticleTypes.registerParticles();
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_WET, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_SNOW, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_MUDDY, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_SAND, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_REDSAND, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.DUST_CLOUD, DustCloudParticle.Factory::new);

		// Water Systems
		CascadeSystem.register();
		RippleSystem.register();
		WaveSystem.register();
		SplashSystem.register();

		// Player Systems
		FootprintSystem.register();
		RunningCloudsSystem.register();

		// Weather Systems
		WindSystem.register(); // Initialize WIND_SYSTEM here if needed

		// Sound Events
		SoundEvents.initialize();
		AmbientSoundSystem.register();

		// Debug
		DebugCommands.register();
	}
}