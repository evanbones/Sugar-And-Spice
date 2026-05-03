package com.evandev.spicedcider.mixin.perf;

import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Redirect(method = "updateRegistryTags()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;rebuildCache()V"))
    private void rebuildBlockCache() {
        // Don't need to do anything - block shape properties that are rebuilt here don't depend on tags
    }
}