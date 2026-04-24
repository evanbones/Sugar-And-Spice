package com.evandev.snipsandsnails;

import com.evandev.snipsandsnails.config.ConfigFileHandler;
import com.evandev.snipsandsnails.config.LoggerNamePatternSelector;
import com.evandev.snipsandsnails.config.Reconfigurator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;

import java.io.IOException;
import java.net.URI;

@Mod(SnipsAndSnails.MODID)
public class SnipsAndSnails {
    public static final String MODID = "snipsandsnails";
    public static final Logger LOGGER = LogManager.getLogger("Snips and Snails");

    /**
     * An arbitrary unique identifier to be passed to Log4j when loading our
     * {@link LoggerNamePatternSelector} plugin
     */
    public static final long BUNDLE_ID = 54321;

    public static ClassLoader CLASSLOADER;

    public SnipsAndSnails(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Starting Log4j reconfiguration.");

        CLASSLOADER = SnipsAndSnails.class.getClassLoader();

        loadPlugin();
        URI newConfigUri = ConfigFileHandler.getOrCreateDefaultConfigFile();

        try {
            Reconfigurator.reconfigureWithUri(newConfigUri);
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Failed to reconfigure Log4j:", e);
            return;
        }

        LOGGER.info("Finished Log4j reconfiguration.");
    }

    /**
     * Prompts Log4j to scan for our {@link LoggerNamePatternSelector} plugin.
     */
    public static void loadPlugin() {
        PluginRegistry.getInstance().loadFromBundle(BUNDLE_ID, CLASSLOADER);
    }
}