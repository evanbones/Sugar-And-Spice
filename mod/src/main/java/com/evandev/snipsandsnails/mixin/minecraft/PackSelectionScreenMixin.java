package com.evandev.snipsandsnails.mixin.minecraft;

import com.evandev.snipsandsnails.SnipsAndSnails;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Collectors;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin {

    /*@Inject(method = "init", at = @At("TAIL"))
    private void snipsandsnails$dumpResourcePacks(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        PackRepository repository = minecraft.getResourcePackRepository();

        String available = repository.getAvailablePacks().stream()
                .map(Pack::getId)
                .collect(Collectors.joining(", "));

        String selected = repository.getSelectedPacks().stream()
                .map(Pack::getId)
                .collect(Collectors.joining(", "));

        SnipsAndSnails.LOGGER.info("=== Resource Packs Dump ===");
        SnipsAndSnails.LOGGER.info("Available Packs: [{}]", available);
        SnipsAndSnails.LOGGER.info("Selected Packs: [{}]", selected);
        SnipsAndSnails.LOGGER.info("===========================");
    }*/
}