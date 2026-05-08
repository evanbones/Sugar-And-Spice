package com.evandev.spicedcider.client.sound;

import com.evandev.spicedcider.api.WeatherAPI;
import net.minecraft.client.Minecraft;

public class WeatherSoundManager {
    private static AmbientRainSound currentRainSound;

    public static void clientTick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            stop();
            return;
        }

        float density = WeatherAPI.getRainDensity(mc.level, mc.player.getX(), mc.player.getEyeY(), mc.player.getZ(), false);
        float volume = density * 0.5F;

        if (volume <= 0) {
            stop();
            return;
        }

        if (currentRainSound == null || currentRainSound.isStopped()) {
            currentRainSound = new AmbientRainSound(mc.player, volume);
            mc.getSoundManager().play(currentRainSound);
        }

        currentRainSound.setVolume(volume);

        boolean underRoof = mc.player.getEyeY() < WeatherAPI.getRainHeight(mc.level, mc.player.getBlockX(), mc.player.getBlockZ());
        currentRainSound.setPitch(underRoof ? 0.25F : 1.0F);
    }

    public static void stop() {
        if (currentRainSound != null) {
            currentRainSound.stopSound();
            currentRainSound = null;
        }
    }
}