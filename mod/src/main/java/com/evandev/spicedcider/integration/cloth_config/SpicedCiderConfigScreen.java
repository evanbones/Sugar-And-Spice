package com.evandev.spicedcider.integration.cloth_config;

import com.evandev.spicedcider.config.SpicedCiderConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SpicedCiderConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.spicedcider.config"));

        builder.setSavingRunnable(SpicedCiderConfig.COMMON_SPEC::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.spicedcider.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();


        return builder.build();
    }
}