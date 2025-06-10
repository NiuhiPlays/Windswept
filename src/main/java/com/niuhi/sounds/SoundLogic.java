package com.niuhi.sounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class SoundLogic {
    private final CrossfadeHandler windHandler;
    private final CrossfadeHandler cascadeHandler;

    public SoundLogic(float windFadeDuration, float cascadeFadeDuration) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.windHandler = new CrossfadeHandler(windFadeDuration);
        this.cascadeHandler = new CrossfadeHandler(cascadeFadeDuration);
    }

    public void playWindSound(SoundEvent sound, float volume) {
        windHandler.update(0.15f, sound, SoundType.STATIC, volume, null);
    }

    public void playCascadeSound(SoundEvent sound, float volume, BlockPos position) {
        cascadeHandler.update(0.15f, sound, SoundType.DIRECTIONAL, volume, position);
    }

    public void stopAllSounds() {
        windHandler.stop();
        cascadeHandler.stop();
    }
}