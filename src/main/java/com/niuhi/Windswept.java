package com.niuhi;

import com.niuhi.particle.WindParticle;
import com.niuhi.particle.WindParticleTypes;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Windswept implements ModInitializer {
	public static final String MOD_ID = "windswept";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Startup Message
		LOGGER.info("Blowing in the Clouds and stuff!");

		// Particle shit
		WindParticleTypes.registerParticles();
		ParticleFactoryRegistry.getInstance().register(WindParticleTypes.WIND, WindParticle.Factory::new);
	}
}