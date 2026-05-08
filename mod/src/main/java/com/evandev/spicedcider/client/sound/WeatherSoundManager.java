package com.evandev.spicedcider.client.sound;

import com.evandev.spicedcider.api.WeatherAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class WeatherSoundManager {
    private static AmbientRainSound currentRainSound;
    private static float currentVolume = 0.0F;

    public static void clientTick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            stop();
            return;
        }

        float density = WeatherAPI.getRainDensity(mc.level, mc.player.getX(), mc.player.getEyeY(), mc.player.getZ(), false);
        float targetVolume = density * 0.5F;

        currentVolume = Mth.lerp(0.1F, currentVolume, targetVolume);

        if (currentVolume <= 0.01F) {
            stop();
            currentVolume = 0.0F;
            return;
        }

        if (currentRainSound == null || currentRainSound.isStopped()) {
            currentRainSound = new AmbientRainSound(mc.player, currentVolume);
            mc.getSoundManager().play(currentRainSound);
        }

        currentRainSound.setVolume(currentVolume);

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