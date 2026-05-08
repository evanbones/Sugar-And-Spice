package com.evandev.spicedcider.registry;

import com.evandev.spicedcider.SpicedCider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, SpicedCider.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER1 = SOUNDS.register("block.workstone.hammer1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SpicedCider.MOD_ID, "block.workstone.hammer1")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER2 = SOUNDS.register("block.workstone.hammer2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SpicedCider.MOD_ID, "block.workstone.hammer2")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WEATHER_RAIN = SOUNDS.register("ambient.weather.rain",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SpicedCider.MOD_ID, "ambient.weather.rain")));
}