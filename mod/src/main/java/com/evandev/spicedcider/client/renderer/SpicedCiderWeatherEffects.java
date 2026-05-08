package com.evandev.spicedcider.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class SpicedCiderWeatherEffects extends DimensionSpecialEffects {

    public SpicedCiderWeatherEffects() {
        super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 color, float sunHeight) {
        return color.multiply(
                sunHeight * 0.94F + 0.06F,
                sunHeight * 0.94F + 0.06F,
                sunHeight * 0.91F + 0.09F
        );
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderClouds(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull PoseStack poseStack, double camX, double camY, double camZ, @NotNull Matrix4f modelViewMatrix, @NotNull Matrix4f projectionMatrix) {
        SpicedCiderCloudRenderer.render(level, ticks, partialTick, poseStack, camX, camY, camZ, modelViewMatrix, projectionMatrix);
        return true;
    }

    @Override
    public boolean renderSnowAndRain(@NotNull ClientLevel level, int ticks, float partialTick, @NotNull LightTexture lightTexture, double camX, double camY, double camZ) {
        SpicedCiderRainRenderer.render(level, ticks, partialTick, lightTexture, new PoseStack(), camX, camY, camZ);
        return true;
    }
}