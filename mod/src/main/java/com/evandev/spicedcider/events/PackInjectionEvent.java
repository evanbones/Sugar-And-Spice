package com.evandev.spicedcider.events;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.resource.ResourceBaker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(modid = SpicedCider.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PackInjectionEvent {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        Path gameDir = FMLPaths.GAMEDIR.get();
        Path cacheDir = gameDir.resolve(".spicedcider_cache");
        Path manifestPath = FMLPaths.CONFIGDIR.get().resolve("spicedcider/spicedcider_manifest.json");
        Path resourcePacksDir = gameDir.resolve("resourcepacks");

        ResourceBaker.bakeFromManifest(cacheDir, manifestPath, resourcePacksDir);

        Path globalJitPath = cacheDir.resolve("spicedcider_global_jit.zip");
        if (!Files.exists(globalJitPath)) return;

        try {
            String packId = "spicedcider_global_jit";

            PackLocationInfo info = new PackLocationInfo(
                    packId,
                    Component.literal("Spiced Cider Global JIT"),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );

            PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);

            Pack pack = Pack.readMetaAndCreate(
                    info,
                    new FilePackResources.FileResourcesSupplier(globalJitPath),
                    PackType.CLIENT_RESOURCES,
                    selectionConfig
            );

            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        } catch (Exception e) {
            SpicedCider.LOGGER.error("Failed to inject Spiced Cider Global JIT pack", e);
        }
    }
}