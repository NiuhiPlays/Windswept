package com.niuhi;

import com.niuhi.particle.player.*;
import com.niuhi.particle.player.PlayerParticleTypes;
import com.niuhi.particle.water.*;
import com.niuhi.particle.water.WaterParticleTypes;
import com.niuhi.particle.weather.wind.WindParticle;
import com.niuhi.particle.weather.wind.WindParticleTypes;
import com.niuhi.weather.wind.WindSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Windswept implements ModInitializer {
	public static final String MOD_ID = "windswept";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static WindSystem WIND_SYSTEM = null; // Initialize to null, set in WindsweptClient

    @Override
	public void onInitialize() {
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

		//Player Particles
		PlayerParticleTypes.registerParticles();
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_WET, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_SNOW, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_MUDDY, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_SAND, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.FOOTPRINT_REDSAND, FootprintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(PlayerParticleTypes.DUST_CLOUD, DustCloudParticle.Factory::new);
	}
}