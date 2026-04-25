package com.evandev.snipsandsnails;

import com.evandev.snipsandsnails.config.ConfigFileHandler;
import com.evandev.snipsandsnails.config.LoggerNamePatternSelector;
import com.evandev.snipsandsnails.config.Reconfigurator;
import com.evandev.snipsandsnails.mixin.minecraft.MapColorAccessor;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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
        CLASSLOADER = SnipsAndSnails.class.getClassLoader();

        modEventBus.addListener(this::setup);

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

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ((MapColorAccessor) MapColor.GRASS).snipsandsnails$setCol(0x8EB971);
            ((MapColorAccessor) MapColor.SAND).snipsandsnails$setCol(0xF7E9E3);
            ((MapColorAccessor) MapColor.WOOL).snipsandsnails$setCol(0xFFFFFF);
            ((MapColorAccessor) MapColor.FIRE).snipsandsnails$setCol(0xFF0000);
            ((MapColorAccessor) MapColor.ICE).snipsandsnails$setCol(0xA0A0FF);
            ((MapColorAccessor) MapColor.METAL).snipsandsnails$setCol(0xA7A7D7);
            ((MapColorAccessor) MapColor.PLANT).snipsandsnails$setCol(0x507736);
            ((MapColorAccessor) MapColor.SNOW).snipsandsnails$setCol(0xFFFFFF);
            ((MapColorAccessor) MapColor.CLAY).snipsandsnails$setCol(0xA4A8B8);
            ((MapColorAccessor) MapColor.DIRT).snipsandsnails$setCol(0xB6855B);
            ((MapColorAccessor) MapColor.STONE).snipsandsnails$setCol(0x707070);
            ((MapColorAccessor) MapColor.WATER).snipsandsnails$setCol(0x3F76E4);
            ((MapColorAccessor) MapColor.WOOD).snipsandsnails$setCol(0xC0A361);
            ((MapColorAccessor) MapColor.QUARTZ).snipsandsnails$setCol(0xFFFDF5);
            ((MapColorAccessor) MapColor.COLOR_ORANGE).snipsandsnails$setCol(0xD87F33);
            ((MapColorAccessor) MapColor.COLOR_MAGENTA).snipsandsnails$setCol(0xB24CD8);
            ((MapColorAccessor) MapColor.COLOR_LIGHT_BLUE).snipsandsnails$setCol(0x6699D8);
            ((MapColorAccessor) MapColor.COLOR_YELLOW).snipsandsnails$setCol(0xE5E533);
            ((MapColorAccessor) MapColor.COLOR_LIGHT_GREEN).snipsandsnails$setCol(0x7FCC19);
            ((MapColorAccessor) MapColor.COLOR_PINK).snipsandsnails$setCol(0xF27FA5);
            ((MapColorAccessor) MapColor.COLOR_GRAY).snipsandsnails$setCol(0x4C4C4C);
            ((MapColorAccessor) MapColor.COLOR_LIGHT_GRAY).snipsandsnails$setCol(0x999999);
            ((MapColorAccessor) MapColor.COLOR_CYAN).snipsandsnails$setCol(0x4C7F99);
            ((MapColorAccessor) MapColor.COLOR_PURPLE).snipsandsnails$setCol(0x7F3FB2);
            ((MapColorAccessor) MapColor.COLOR_BLUE).snipsandsnails$setCol(0x334CB2);
            ((MapColorAccessor) MapColor.COLOR_BROWN).snipsandsnails$setCol(0x664C33);
            ((MapColorAccessor) MapColor.COLOR_GREEN).snipsandsnails$setCol(0x667F33);
            ((MapColorAccessor) MapColor.COLOR_RED).snipsandsnails$setCol(0x993333);
            ((MapColorAccessor) MapColor.COLOR_BLACK).snipsandsnails$setCol(0x191919);
            ((MapColorAccessor) MapColor.GOLD).snipsandsnails$setCol(0xFAEE4D);
            ((MapColorAccessor) MapColor.DIAMOND).snipsandsnails$setCol(0x5CD8D5);
            ((MapColorAccessor) MapColor.LAPIS).snipsandsnails$setCol(0x4A80FF);
            ((MapColorAccessor) MapColor.EMERALD).snipsandsnails$setCol(0x00D93A);
            ((MapColorAccessor) MapColor.PODZOL).snipsandsnails$setCol(0x815631);
            ((MapColorAccessor) MapColor.NETHER).snipsandsnails$setCol(0x700200);
            ((MapColorAccessor) MapColor.TERRACOTTA_WHITE).snipsandsnails$setCol(0xD1B1A1);
            ((MapColorAccessor) MapColor.TERRACOTTA_ORANGE).snipsandsnails$setCol(0x9F5224);
            ((MapColorAccessor) MapColor.TERRACOTTA_MAGENTA).snipsandsnails$setCol(0x95576C);
            ((MapColorAccessor) MapColor.TERRACOTTA_LIGHT_BLUE).snipsandsnails$setCol(0x706C8A);
            ((MapColorAccessor) MapColor.TERRACOTTA_YELLOW).snipsandsnails$setCol(0xBA8524);
            ((MapColorAccessor) MapColor.TERRACOTTA_LIGHT_GREEN).snipsandsnails$setCol(0x677535);
            ((MapColorAccessor) MapColor.TERRACOTTA_PINK).snipsandsnails$setCol(0xA04D4E);
            ((MapColorAccessor) MapColor.TERRACOTTA_GRAY).snipsandsnails$setCol(0x392923);
            ((MapColorAccessor) MapColor.TERRACOTTA_LIGHT_GRAY).snipsandsnails$setCol(0x876A62);
            ((MapColorAccessor) MapColor.TERRACOTTA_CYAN).snipsandsnails$setCol(0x575C5C);
            ((MapColorAccessor) MapColor.TERRACOTTA_PURPLE).snipsandsnails$setCol(0x7A4958);
            ((MapColorAccessor) MapColor.TERRACOTTA_BLUE).snipsandsnails$setCol(0x4C3E7C);
            ((MapColorAccessor) MapColor.TERRACOTTA_BROWN).snipsandsnails$setCol(0x4C3223);
            ((MapColorAccessor) MapColor.TERRACOTTA_GREEN).snipsandsnails$setCol(0x4C522A);
            ((MapColorAccessor) MapColor.TERRACOTTA_RED).snipsandsnails$setCol(0x8E3C2E);
            ((MapColorAccessor) MapColor.TERRACOTTA_BLACK).snipsandsnails$setCol(0x251610);
            ((MapColorAccessor) MapColor.CRIMSON_NYLIUM).snipsandsnails$setCol(0xBD3031);
            ((MapColorAccessor) MapColor.CRIMSON_STEM).snipsandsnails$setCol(0x5C191D);
            ((MapColorAccessor) MapColor.CRIMSON_HYPHAE).snipsandsnails$setCol(0x5C191D);
            ((MapColorAccessor) MapColor.WARPED_NYLIUM).snipsandsnails$setCol(0x167E86);
            ((MapColorAccessor) MapColor.WARPED_STEM).snipsandsnails$setCol(0x3A8E8C);
            ((MapColorAccessor) MapColor.WARPED_HYPHAE).snipsandsnails$setCol(0x14B485);
            ((MapColorAccessor) MapColor.WARPED_WART_BLOCK).snipsandsnails$setCol(0x167E86);
            ((MapColorAccessor) MapColor.DEEPSLATE).snipsandsnails$setCol(0x646464);
            ((MapColorAccessor) MapColor.RAW_IRON).snipsandsnails$setCol(0xD8AF93);
            ((MapColorAccessor) MapColor.GLOW_LICHEN).snipsandsnails$setCol(0x7FA796);

            LOGGER.info("Successfully injected nicer map colors!");
        });
    }

    /**
     * Prompts Log4j to scan for our {@link LoggerNamePatternSelector} plugin.
     */
    public static void loadPlugin() {
        PluginRegistry.getInstance().loadFromBundle(BUNDLE_ID, CLASSLOADER);
    }
}