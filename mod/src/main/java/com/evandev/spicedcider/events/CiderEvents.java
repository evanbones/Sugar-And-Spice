package com.evandev.spicedcider.events;

import com.evandev.spicedcider.SpicedCider;
import dev.emi.emi.config.EmiConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = SpicedCider.MODID, value = Dist.CLIENT)
public class CiderEvents {

    @SubscribeEvent
    public static void disableEmi(ScreenEvent.Opening event) {
        EmiConfig.enabled = false;
    }
}

