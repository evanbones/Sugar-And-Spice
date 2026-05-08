package com.evandev.spicedcider.client.gui;

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

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Clouds Speed"), SpicedCiderConfig.COMMON.cloudsSpeed.get())
                .setDefaultValue(0.001D)
                .setMin(0.0D).setMax(1.0D)
                .setSaveConsumer(SpicedCiderConfig.COMMON.cloudsSpeed::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Use Vanilla Clouds"), SpicedCiderConfig.COMMON.useVanillaClouds.get())
                .setDefaultValue(false)
                .setSaveConsumer(SpicedCiderConfig.COMMON.useVanillaClouds::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Eternal Rain"), SpicedCiderConfig.COMMON.eternalRain.get())
                .setDefaultValue(false)
                .setSaveConsumer(SpicedCiderConfig.COMMON.eternalRain::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Eternal Thunder"), SpicedCiderConfig.COMMON.eternalThunder.get())
                .setDefaultValue(false)
                .setSaveConsumer(SpicedCiderConfig.COMMON.eternalThunder::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Frequent Rain"), SpicedCiderConfig.COMMON.frequentRain.get())
                .setDefaultValue(false)
                .setSaveConsumer(SpicedCiderConfig.COMMON.frequentRain::set)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.literal("Lightning Chance"), SpicedCiderConfig.COMMON.lightningChance.get())
                .setDefaultValue(300)
                .setMin(1).setMax(Short.MAX_VALUE)
                .setSaveConsumer(SpicedCiderConfig.COMMON.lightningChance::set)
                .build());

        return builder.build();
    }
}