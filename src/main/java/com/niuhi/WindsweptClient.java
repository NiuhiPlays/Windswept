package com.niuhi;

import com.niuhi.debug.DebugCommands;
import com.niuhi.player.FootprintSystem;
import com.niuhi.player.RunningCloudsSystem;
import com.niuhi.sounds.AmbientSoundSystem;
import com.niuhi.sounds.SoundEvents;
import com.niuhi.water.CascadeSystem;
import com.niuhi.water.RippleSystem;
import com.niuhi.wind.WindSystem;
import net.fabricmc.api.ClientModInitializer;

public class WindsweptClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Water systems
        CascadeSystem.register();
        RippleSystem.register();

        // Player systems
        FootprintSystem.register();
        RunningCloudsSystem.register();

        // Weather systems
        WindSystem.register();

        // Sound Events
        SoundEvents.initialize();
        AmbientSoundSystem.register();

        // Debug
        DebugCommands.register();
    }
}