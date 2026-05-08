package com.evandev.spicedcider.mixin.minecraft;

import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void spicedcider$forceNoVanillaRain(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void spicedcider$forceNoVanillaThunder(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}