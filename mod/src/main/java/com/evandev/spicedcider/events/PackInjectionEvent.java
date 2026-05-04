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
import java.util.stream.Stream;

@EventBusSubscriber(modid = SpicedCider.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PackInjectionEvent {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {

            Path gameDir = FMLPaths.GAMEDIR.get();
            Path cacheDir = gameDir.resolve(".spicedcider_cache");
            Path manifestPath = FMLPaths.CONFIGDIR.get().resolve("spicedcider/spicedcider_manifest.json");
            Path resourcePacksDir = gameDir.resolve("resourcepacks");

            try {
                Files.createDirectories(cacheDir);

                boolean cacheIsEmpty;
                try (Stream<Path> s = Files.list(cacheDir)) {
                    cacheIsEmpty = s.findAny().isEmpty();
                }

                if (Files.exists(manifestPath) && cacheIsEmpty) {
                    ResourceBaker.bakeFromManifest(cacheDir, manifestPath, resourcePacksDir);
                }

                try (Stream<Path> paths = Files.list(cacheDir)) {
                    paths.filter(p -> p.toString().endsWith("_jit.zip")).forEach(cacheZip -> {
                        String packId = "spicedcider_" + cacheZip.getFileName().toString().replace(".zip", "");

                        PackLocationInfo info = new PackLocationInfo(
                                packId,
                                Component.literal("Spiced Cider JIT: " + cacheZip.getFileName()),
                                PackSource.BUILT_IN,
                                Optional.empty()
                        );

                        PackSelectionConfig selectionConfig = new PackSelectionConfig(true, Pack.Position.TOP, false);

                        Pack pack = Pack.readMetaAndCreate(
                                info,
                                new FilePackResources.FileResourcesSupplier(cacheZip),
                                PackType.CLIENT_RESOURCES,
                                selectionConfig
                        );

                        if (pack != null) {
                            event.addRepositorySource(consumer -> consumer.accept(pack));
                            SpicedCider.LOGGER.info("Injected cache pack: {}", packId);
                        }
                    });
                }
            } catch (Exception e) {
                SpicedCider.LOGGER.error("Fatal error setting up Spiced Cider JIT resource compiler", e);
            }
        }
    }
}