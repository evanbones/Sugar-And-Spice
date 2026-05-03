package com.evandev.spicedcider.mixin.perf;

import net.minecraft.client.multiplayer.TagCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagCollector.class)
public class TagCollectorMixin {
    @Redirect(method = "refreshBuiltInTagDependentData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;rebuildCache()V"))
    private static void rebuildBlockCache() {
        // Don't need to do anything - block shape properties that are rebuilt here don't depend on tags
    }
}