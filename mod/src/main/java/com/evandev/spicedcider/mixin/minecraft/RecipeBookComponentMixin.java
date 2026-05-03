package com.evandev.spicedcider.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {

    @ModifyReturnValue(method = "updateScreenPosition", at = @At("RETURN"))
    private int spicedcider$cancelRecipeBookShift(int original, int width, int imageWidth) {
        return (width - imageWidth) / 2;
    }
}