package com.niuhi;

import com.niuhi.particle.wind.WindParticle;
import com.niuhi.particle.wind.WindParticleTypes;
import com.niuhi.wind.WindSystem;
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
		WindParticleTypes.registerParticles();
		ParticleFactoryRegistry.getInstance().register(WindParticleTypes.WIND, WindParticle.Factory::new);
	}
}