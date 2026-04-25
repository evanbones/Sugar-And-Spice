package com.evandev.snipsandsnails.mixin.namingunconvention;

import com.evandev.snipsandsnails.SnipsAndSnails;
import de.keksuccino.modernworldcreation.ModernWorldCreationGameTab;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModernWorldCreationGameTab.class)
public class ModernWorldCreationGameTabMixin {

    @Unique
    private static final WidgetSprites REROLL_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("snipsandsnails", "naming_unconvention/reroll"),
            ResourceLocation.fromNamespaceAndPath("snipsandsnails", "naming_unconvention/reroll_highlighted")
    );
    @Shadow
    protected EditBox nameEdit;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/CommonLayouts;labeledElement(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/layouts/LayoutElement;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/layouts/Layout;"
            )
    )
    private Layout snipsandsnails$wrapNameEditBox(Font font, LayoutElement element, Component label) {
        Layout originalLayout = CommonLayouts.labeledElement(font, element, label);

        if (element == this.nameEdit) {

            ImageButton rerollBtn = new ImageButton(
                    0, 0, 20, 20, REROLL_SPRITES,
                    btn -> {
                        this.nameEdit.setValue(SnipsAndSnails.RANDOM_NAME_GENERATOR.generateRandomName());
                    }
            );

            LinearLayout horizontalWrapper = LinearLayout.horizontal().spacing(5);
            horizontalWrapper.addChild(originalLayout);

            horizontalWrapper.addChild(rerollBtn, horizontalWrapper.newCellSettings().alignVerticallyBottom());

            return horizontalWrapper;
        }

        return originalLayout;
    }
}