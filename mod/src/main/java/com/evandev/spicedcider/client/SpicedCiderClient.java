package com.evandev.spicedcider.client;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.api.WeatherAPI;
import com.evandev.spicedcider.client.renderer.SpicedCiderLightningRenderer;
import com.evandev.spicedcider.client.renderer.SpicedCiderRainRenderer;
import com.evandev.spicedcider.client.renderer.SpicedCiderWeatherEffects;
import com.evandev.spicedcider.client.renderer.WorkstoneRenderer;
import com.evandev.spicedcider.client.sound.WeatherSoundManager;
import com.evandev.spicedcider.registry.ModBlockEntities;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = SpicedCider.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SpicedCiderClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WORKSTONE.get(), WorkstoneRenderer::new);
        event.registerEntityRenderer(EntityType.LIGHTNING_BOLT, SpicedCiderLightningRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(SpicedCider.RANDOM_NAME_GENERATOR);
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.withDefaultNamespace("overworld"),
                new SpicedCiderWeatherEffects()
        );

        SpicedCider.LOGGER.info("Successfully registered localized weather & cloud renderer!");
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Vec3 camPos = event.getCamera().getPosition();

                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

                SpicedCiderRainRenderer.render(
                        mc.level,
                        event.getRenderTick(),
                        partialTick,
                        event.getPoseStack(),
                        camPos.x, camPos.y, camPos.z
                );
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        WeatherSoundManager.clientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Vec3 pos = camera.getPosition();

        float rainDensity = WeatherAPI.getRainDensity(level, pos.x, pos.y, pos.z, true);
        if (rainDensity > 0) {
            float darken = 1.0F - (rainDensity * 0.5F);
            event.setRed(event.getRed() * darken);
            event.setGreen(event.getGreen() * darken);
            event.setBlue(event.getBlue() * darken);
        }

        float inCloud = WeatherAPI.inCloud(level, pos.x, pos.y, pos.z);
        if (inCloud > 0) {
            event.setRed(Mth.lerp(inCloud, event.getRed(), 0.8f));
            event.setGreen(Mth.lerp(inCloud, event.getGreen(), 0.8f));
            event.setBlue(Mth.lerp(inCloud, event.getBlue(), 0.8f));
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Vec3 pos = camera.getPosition();
        float inCloud = WeatherAPI.inCloud(level, pos.x, pos.y, pos.z);

        if (inCloud > 0) {
            event.setNearPlaneDistance(Mth.lerp(inCloud, event.getNearPlaneDistance(), 2.0f));
            event.setFarPlaneDistance(Mth.lerp(inCloud, event.getFarPlaneDistance(), 16.0f));
            event.setCanceled(true);
        }
    }
}