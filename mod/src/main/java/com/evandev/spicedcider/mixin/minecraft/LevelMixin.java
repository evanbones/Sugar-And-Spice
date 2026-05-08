package com.evandev.spicedcider.mixin.minecraft;

import com.evandev.spicedcider.api.WeatherAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void spicedcider$isRaining(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void spicedcider$isThundering(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isRainingAt", at = @At("HEAD"), cancellable = true)
    private void spicedcider$isRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WeatherAPI.isRaining((Level) (Object) this, pos.getX(), pos.getY(), pos.getZ()));
    }
}