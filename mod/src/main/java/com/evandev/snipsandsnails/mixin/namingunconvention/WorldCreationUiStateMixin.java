package com.evandev.snipsandsnails.mixin.namingunconvention;

import com.evandev.snipsandsnails.SnipsAndSnails;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {

    @Shadow private String name;
    @Shadow public abstract void setName(String name);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void snipsandsnails$applyRandomName(Path savesFolder, WorldCreationContext settings, Optional<ResourceKey<WorldPreset>> preset, OptionalLong seed, CallbackInfo ci) {
        String defaultName = Component.translatable("selectWorld.newWorld").getString();
        if (this.name == null || this.name.equals(defaultName) || this.name.trim().isEmpty()) {
            this.setName(SnipsAndSnails.RANDOM_NAME_GENERATOR.generateRandomName());
        }
    }
}