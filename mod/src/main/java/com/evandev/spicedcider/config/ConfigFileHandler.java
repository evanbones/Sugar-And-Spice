package com.evandev.spicedcider.config;

import com.evandev.spicedcider.SpicedCider;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigFileHandler {
    public static final String CONFIG_FILENAME = "better_log4j_config.xml";
    public static final String FALLBACK_CONFIG_RESOURCE_PATH = "data/fallback_log4j_config.xml";

    public static Path getDefaultConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILENAME);
    }

    public static URI getOrCreateDefaultConfigFile() {
        Path configPath = getDefaultConfigPath();
        if (!Files.exists(configPath)) {
            SpicedCider.LOGGER.warn(
                    "Expected to find config file in default location '{}', but it does not exist! The fallback config will be written to this location to fix this.",
                    configPath);
            try {
                writeFallbackConfig(configPath);
            } catch (IOException e) {
                SpicedCider.LOGGER.error(
                        "Could not write fallback config to the aforementioned location! The fallback config will be used directly for this session instead, but this error may happen again if the issue is not fixed:",
                        e);
            }
            return getFallbackConfigUri();
        }
        return configPath.toUri();
    }

    public static URI getFallbackConfigUri() {
        try {
            return SpicedCider.CLASSLOADER.getResource(FALLBACK_CONFIG_RESOURCE_PATH).toURI();
        } catch (URISyntaxException e) {
            SpicedCider.LOGGER.error("Class loader returned an invalid URI! This should never happen:", e);
            throw new RuntimeException(e);
        }
    }

    public static InputStream getFallbackConfigBytes() {
        return SpicedCider.CLASSLOADER.getResourceAsStream(FALLBACK_CONFIG_RESOURCE_PATH);
    }

    public static void writeFallbackConfig(Path configPath) throws IOException {
        try (InputStream input = getFallbackConfigBytes()) {
            if (input != null) {
                Files.copy(input, configPath);
            }
        }
    }
}