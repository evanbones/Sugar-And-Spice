package com.evandev.snipsandsnails.mixin.particlerain;

import com.evandev.snipsandsnails.SnipsAndSnails;
import net.minecraft.client.renderer.texture.SpriteLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.particlerain.config.ConfigManager;

@Mixin(SpriteLoader.class)
public class ParticleRainConfigFixMixin {

    /**
     * Injects at the very beginning of the stitch method.
     * This guarantees it runs before ParticleRain's @ModifyExpressionValue
     */
    @Inject(method = "stitch", at = @At("HEAD"))
    private void particlerainfix$forceLoadConfigEarly(CallbackInfoReturnable<?> cir) {
        if (ConfigManager.config == null) {
            SnipsAndSnails.LOGGER.info("[ParticleRain Fix] Forcing early config load during SpriteLoader stitch.");
            ConfigManager.load();
        }
    }
}