package com.niuhi;

import com.niuhi.player.FootprintSystem;
import com.niuhi.player.RunningCloudsSystem;
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
    }
}