package com.evandev.spicedcider.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class AmbientRainSound extends AbstractTickableSoundInstance {
    private final Player player;

    public AmbientRainSound(Player player, float initialVolume) {
        super(SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = initialVolume;
        this.relative = false;
    }

    public void stopSound() {
        this.stop();
    }

    public void setVolume(float vol) {
        this.volume = vol;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved()) {
            this.stop();
        } else {
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
    }
}