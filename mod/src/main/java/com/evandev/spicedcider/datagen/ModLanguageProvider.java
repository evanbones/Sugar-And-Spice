package com.evandev.spicedcider.datagen;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.registry.ModBlocks;
import com.evandev.spicedcider.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, SpicedCider.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(ModBlocks.WORKSTONE, "Workstone");

        addItem(ModItems.FLINT_HAMMER, "Flint Hammer");
        addItem(ModItems.IRON_HAMMER, "Iron Hammer");
        addItem(ModItems.GOLDEN_HAMMER, "Golden Hammer");
        addItem(ModItems.DIAMOND_HAMMER, "Diamond Hammer");
        addItem(ModItems.NETHERITE_HAMMER, "Netherite Hammer");

        add("key.category.naming_unconvention.naming_unconvention", "Naming Unconvention");
        add("key.naming_unconvention.reroll", "Reroll World Name");
        add("subtitles.spicedcider.block.workstone.hammer", "Workstone hammered");
        add("emi.category.spicedcider.workstone", "Workstone");
        add("title.spicedcider.config", "Spiced Cider Config");
        add("category.spicedcider.general", "General");
    }
}