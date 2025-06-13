package com.niuhi;

import com.niuhi.debug.DebugCommands;
import com.niuhi.particle.ParticleRegistry;
import com.niuhi.player.FootprintSystem;
import com.niuhi.player.RunningCloudsSystem;
import com.niuhi.sounds.AmbientSoundSystem;
import com.niuhi.sounds.SoundEvents;
import com.niuhi.water.*;
import com.niuhi.weather.wind.WindSystem;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Windswept implements ClientModInitializer {
	public static final String MOD_ID = "windswept";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static WindSystem WIND_SYSTEM = null; // Initialize to null, set during registration

	@Override
	public void onInitializeClient() {
		LOGGER.info("Blowing in the Clouds and stuff!");

		// Register all particles
		ParticleRegistry.registerParticles();

		// Water Systems
		CascadeSystem.register();
		RippleSystem.register();
		WaveSystem.register();
		SplashSystem.register();

		// Player Systems
		FootprintSystem.register();
		RunningCloudsSystem.register();

		// Weather Systems
		WindSystem.register();

		// Sound Events
		SoundEvents.initialize();
		AmbientSoundSystem.register();

		// Debug
		DebugCommands.register();
	}
}