package com.evandev.snipsandsnails.namingunconvention;

import com.evandev.snipsandsnails.SnipsAndSnails;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;

public class RandomNameGenerator implements ResourceManagerReloadListener {
    private String[] adjectives = new String[]{"Chaotic"};
    private String[] nouns = new String[]{"World"};
    private String[] locations = new String[]{"Place"};
    private String[] compositions = new String[]{"The # @ of &"};

    private static String[] readFileLines(String filename, ResourceManager resourceManager) throws IOException {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("snipsandsnails", "naming_unconvention/" + filename);
        return resourceManager.getResourceOrThrow(loc).openAsReader().lines().toArray(String[]::new);
    }

    public String generateRandomName() {
        Random random = new Random();
        return compositions[random.nextInt(compositions.length)]
                .replace("#", adjectives[random.nextInt(adjectives.length)])
                .replace("@", locations[random.nextInt(locations.length)])
                .replace("&", nouns[random.nextInt(nouns.length)]);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        try {
            adjectives = readFileLines("adjectives.txt", resourceManager);
            nouns = readFileLines("nouns.txt", resourceManager);
            locations = readFileLines("locations.txt", resourceManager);
            compositions = readFileLines("compositions.txt", resourceManager);
        } catch (Exception e) {
            SnipsAndSnails.LOGGER.error("Failed to load Naming Unconvention dictionaries, falling back to defaults.", e);
        }
    }
}