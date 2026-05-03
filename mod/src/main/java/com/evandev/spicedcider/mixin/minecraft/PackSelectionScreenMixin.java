package com.evandev.spicedcider.mixin.minecraft;

import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PackSelectionScreen.class)
public class PackSelectionScreenMixin {

    /*@Inject(method = "init", at = @At("TAIL"))
    private void spicedcider$dumpResourcePacks(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        PackRepository repository = minecraft.getResourcePackRepository();

        String available = repository.getAvailablePacks().stream()
                .map(Pack::getId)
                .collect(Collectors.joining(", "));

        String selected = repository.getSelectedPacks().stream()
                .map(Pack::getId)
                .collect(Collectors.joining(", "));

        SpicedCider.LOGGER.info("=== Resource Packs Dump ===");
        SpicedCider.LOGGER.info("Available Packs: [{}]", available);
        SpicedCider.LOGGER.info("Selected Packs: [{}]", selected);
        SpicedCider.LOGGER.info("===========================");
    }*/
}