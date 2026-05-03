package com.evandev.spicedcider;

import com.evandev.spicedcider.config.ConfigFileHandler;
import com.evandev.spicedcider.config.LoggerNamePatternSelector;
import com.evandev.spicedcider.config.Reconfigurator;
import com.evandev.spicedcider.mixin.minecraft.accessor.MapColorAccessor;
import com.evandev.spicedcider.namingunconvention.RandomNameGenerator;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;

import java.io.IOException;
import java.net.URI;

@Mod(SpicedCider.MODID)
public class SpicedCider {
    public static final String MODID = "spicedcider";
    public static final Logger LOGGER = LogManager.getLogger("Spiced Cider");
    public static final RandomNameGenerator RANDOM_NAME_GENERATOR = new RandomNameGenerator();

    /**
     * An arbitrary unique identifier to be passed to Log4j when loading our
     * {@link LoggerNamePatternSelector} plugin
     */
    public static final long BUNDLE_ID = 54321;

    public static ClassLoader CLASSLOADER;

    public SpicedCider(IEventBus modEventBus, ModContainer modContainer) {
        CLASSLOADER = SpicedCider.class.getClassLoader();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerClientReloadListeners);

        LOGGER.info("Starting Log4j reconfiguration.");
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

    private void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RANDOM_NAME_GENERATOR);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ((MapColorAccessor) MapColor.GRASS).spicedcider$setCol(0x8EB971);
            ((MapColorAccessor) MapColor.SAND).spicedcider$setCol(0xF7E9E3);
            ((MapColorAccessor) MapColor.WOOL).spicedcider$setCol(0xFFFFFF);
            ((MapColorAccessor) MapColor.FIRE).spicedcider$setCol(0xFF0000);
            ((MapColorAccessor) MapColor.ICE).spicedcider$setCol(0xA0A0FF);
            ((MapColorAccessor) MapColor.METAL).spicedcider$setCol(0xA7A7D7);
            ((MapColorAccessor) MapColor.PLANT).spicedcider$setCol(0x507736);
            ((MapColorAccessor) MapColor.SNOW).spicedcider$setCol(0xFFFFFF);
            ((MapColorAccessor) MapColor.CLAY).spicedcider$setCol(0xA4A8B8);
            ((MapColorAccessor) MapColor.DIRT).spicedcider$setCol(0xB6855B);
            ((MapColorAccessor) MapColor.STONE).spicedcider$setCol(0x707070);
            ((MapColorAccessor) MapColor.WATER).spicedcider$setCol(0x3F76E4);
            ((MapColorAccessor) MapColor.WOOD).spicedcider$setCol(0xC0A361);
            ((MapColorAccessor) MapColor.QUARTZ).spicedcider$setCol(0xFFFDF5);
            ((MapColorAccessor) MapColor.COLOR_ORANGE).spicedcider$setCol(0xD87F33);
            ((MapColorAccessor) MapColor.COLOR_MAGENTA).spicedcider$setCol(0xB24CD8);
            ((MapColorAccessor) MapColor.COLOR_LIGHT_BLUE).spicedcider$setCol(0x6699D8);
            ((MapColorAccessor) MapColor.COLOR_YELLOW).spicedcider$setCol(0xE5E533);
            ((MapColorAccessor) MapColor.COLOR_LIGHT_GREEN).spicedcider$setCol(0x7FCC19);
            ((MapColorAccessor) MapColor.COLOR_PINK).spicedcider$setCol(0xF27FA5);
            ((MapColorAccessor) MapColor.COLOR_GRAY).spicedcider$setCol(0x4C4C4C);
            ((MapColorAccessor) MapColor.COLOR_LIGHT_GRAY).spicedcider$setCol(0x999999);
            ((MapColorAccessor) MapColor.COLOR_CYAN).spicedcider$setCol(0x4C7F99);
            ((MapColorAccessor) MapColor.COLOR_PURPLE).spicedcider$setCol(0x7F3FB2);
            ((MapColorAccessor) MapColor.COLOR_BLUE).spicedcider$setCol(0x334CB2);
            ((MapColorAccessor) MapColor.COLOR_BROWN).spicedcider$setCol(0x664C33);
            ((MapColorAccessor) MapColor.COLOR_GREEN).spicedcider$setCol(0x667F33);
            ((MapColorAccessor) MapColor.COLOR_RED).spicedcider$setCol(0x993333);
            ((MapColorAccessor) MapColor.COLOR_BLACK).spicedcider$setCol(0x191919);
            ((MapColorAccessor) MapColor.GOLD).spicedcider$setCol(0xFAEE4D);
            ((MapColorAccessor) MapColor.DIAMOND).spicedcider$setCol(0x5CD8D5);
            ((MapColorAccessor) MapColor.LAPIS).spicedcider$setCol(0x4A80FF);
            ((MapColorAccessor) MapColor.EMERALD).spicedcider$setCol(0x00D93A);
            ((MapColorAccessor) MapColor.PODZOL).spicedcider$setCol(0x815631);
            ((MapColorAccessor) MapColor.NETHER).spicedcider$setCol(0x700200);
            ((MapColorAccessor) MapColor.TERRACOTTA_WHITE).spicedcider$setCol(0xD1B1A1);
            ((MapColorAccessor) MapColor.TERRACOTTA_ORANGE).spicedcider$setCol(0x9F5224);
            ((MapColorAccessor) MapColor.TERRACOTTA_MAGENTA).spicedcider$setCol(0x95576C);
            ((MapColorAccessor) MapColor.TERRACOTTA_LIGHT_BLUE).spicedcider$setCol(0x706C8A);
            ((MapColorAccessor) MapColor.TERRACOTTA_YELLOW).spicedcider$setCol(0xBA8524);
            ((MapColorAccessor) MapColor.TERRACOTTA_LIGHT_GREEN).spicedcider$setCol(0x677535);
            ((MapColorAccessor) MapColor.TERRACOTTA_PINK).spicedcider$setCol(0xA04D4E);
            ((MapColorAccessor) MapColor.TERRACOTTA_GRAY).spicedcider$setCol(0x392923);
            ((MapColorAccessor) MapColor.TERRACOTTA_LIGHT_GRAY).spicedcider$setCol(0x876A62);
            ((MapColorAccessor) MapColor.TERRACOTTA_CYAN).spicedcider$setCol(0x575C5C);
            ((MapColorAccessor) MapColor.TERRACOTTA_PURPLE).spicedcider$setCol(0x7A4958);
            ((MapColorAccessor) MapColor.TERRACOTTA_BLUE).spicedcider$setCol(0x4C3E7C);
            ((MapColorAccessor) MapColor.TERRACOTTA_BROWN).spicedcider$setCol(0x4C3223);
            ((MapColorAccessor) MapColor.TERRACOTTA_GREEN).spicedcider$setCol(0x4C522A);
            ((MapColorAccessor) MapColor.TERRACOTTA_RED).spicedcider$setCol(0x8E3C2E);
            ((MapColorAccessor) MapColor.TERRACOTTA_BLACK).spicedcider$setCol(0x251610);
            ((MapColorAccessor) MapColor.CRIMSON_NYLIUM).spicedcider$setCol(0xBD3031);
            ((MapColorAccessor) MapColor.CRIMSON_STEM).spicedcider$setCol(0x5C191D);
            ((MapColorAccessor) MapColor.CRIMSON_HYPHAE).spicedcider$setCol(0x5C191D);
            ((MapColorAccessor) MapColor.WARPED_NYLIUM).spicedcider$setCol(0x167E86);
            ((MapColorAccessor) MapColor.WARPED_STEM).spicedcider$setCol(0x3A8E8C);
            ((MapColorAccessor) MapColor.WARPED_HYPHAE).spicedcider$setCol(0x14B485);
            ((MapColorAccessor) MapColor.WARPED_WART_BLOCK).spicedcider$setCol(0x167E86);
            ((MapColorAccessor) MapColor.DEEPSLATE).spicedcider$setCol(0x646464);
            ((MapColorAccessor) MapColor.RAW_IRON).spicedcider$setCol(0xD8AF93);
            ((MapColorAccessor) MapColor.GLOW_LICHEN).spicedcider$setCol(0x7FA796);

            LOGGER.info("Successfully injected nicer map colors!");
        });
    }
}