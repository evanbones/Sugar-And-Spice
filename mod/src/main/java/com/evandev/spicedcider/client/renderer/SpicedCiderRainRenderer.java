package com.evandev.spicedcider.client.renderer;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.api.WeatherAPI;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Random;

public class SpicedCiderRainRenderer {

    private static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");
    private static final ResourceLocation WATER_CIRCLES_LOCATION = ResourceLocation.fromNamespaceAndPath(SpicedCider.MOD_ID, "textures/environment/water_circles.png");

    private static final float[] randomOffset = new float[256];
    private static final byte[] randomIndex = new byte[256];

    static {
        Random random = new Random(0);
        for (short i = 0; i < 256; i++) {
            randomOffset[i] = random.nextFloat();
            randomIndex[i] = (byte) random.nextInt(4);
        }
    }

    public static void render(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        Minecraft mc = Minecraft.getInstance();
        int radius = mc.options.graphicsMode().get().getId() > 0 ? 10 : 5;

        int ix = Mth.floor(camX);
        int iy = Mth.floor(camY);
        int iz = Mth.floor(camZ);

        int rainTop = 192 + 8;
        if (iy - rainTop > 40) return;

        float timeDelta = ((float) ticks + partialTick);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        Tesselator tesselator = Tesselator.getInstance();

        RenderSystem.setShaderTexture(0, RAIN_LOCATION);
        BufferBuilder rainBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float rainVOffset = (timeDelta * 0.05f) % 1.0f;
        buildWeather(level, rainBuilder, ix, iy, iz, radius, rainTop, camX, camY, camZ, rainVOffset, false);
        MeshData rainData = rainBuilder.build();
        if (rainData != null) BufferUploader.drawWithShader(rainData);

        RenderSystem.setShaderTexture(0, WATER_CIRCLES_LOCATION);
        BufferBuilder waterBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float circleVOffset = (timeDelta * 0.07f) % 1.0f;
        buildWaterCircles(level, waterBuilder, ix, iy, iz, radius, camX, camY, camZ, circleVOffset);
        MeshData waterData = waterBuilder.build();
        if (waterData != null) BufferUploader.drawWithShader(waterData);

        RenderSystem.setShaderTexture(0, SNOW_LOCATION);
        BufferBuilder snowBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float snowVOffset = (timeDelta * 0.002f) % 1.0f;
        buildWeather(level, snowBuilder, ix, iy, iz, radius, rainTop, camX, camY, camZ, snowVOffset, true);
        MeshData snowData = snowBuilder.build();
        if (snowData != null) BufferUploader.drawWithShader(snowData);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void buildWeather(ClientLevel level, VertexConsumer builder, int ix, int iy, int iz, int radius, int rainTop, double camX, double camY, double camZ, float vOffset, boolean drawingSnow) {
        Matrix4f matrix = new Matrix4f();
        matrix.translate((float) -camX, (float) -camY, (float) -camZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int wx = ix + dx;
                int wz = iz + dz;

                int terrain = WeatherAPI.getRainHeight(level, wx, wz);
                if (terrain - iy > 40) continue;

                pos.set(wx, terrain, wz);

                if (!WeatherAPI.isRaining(level, wx, terrain, wz)) continue;

                boolean isSnowy = level.getBiome(pos).value().coldEnoughToSnow(pos);
                if (isSnowy != drawingSnow) continue;

                float v1 = randomOffset[(wx & 15) << 4 | (wz & 15)] + vOffset;
                float v2 = ((rainTop - terrain) * 0.0625F + v1);

                float alpha = WeatherAPI.sampleFront(level, wx, wz, 0.1F);
                alpha = Mth.clamp((alpha - 0.2F) * 2, 0.5F, 1.0F);
                int aI = (int) (alpha * 255);

                float u1 = ((wx + wz) & 3) * 0.25F;
                float u2 = u1 + 0.25F;

                float cX = (float) (camX - (wx + 0.5));
                float cZ = (float) (camZ - (wz + 0.5));
                float length = Mth.sqrt(cX * cX + cZ * cZ);
                if (length > 0) {
                    cX /= (length * 2.0f);
                    cZ /= (length * 2.0f);
                } else {
                    cX = 0.5f;
                    cZ = 0.0f;
                }

                float x1 = wx + 0.5f - cZ;
                float x2 = wx + 0.5f + cZ;
                float z1 = wz + 0.5f + cX;
                float z2 = wz + 0.5f - cX;

                builder.addVertex(matrix, x1, terrain, z1).setColor(255, 255, 255, aI).setUv(u1, v1);
                builder.addVertex(matrix, x2, terrain, z2).setColor(255, 255, 255, aI).setUv(u2, v1);
                builder.addVertex(matrix, x2, rainTop, z2).setColor(255, 255, 255, aI).setUv(u2, v2);
                builder.addVertex(matrix, x1, rainTop, z1).setColor(255, 255, 255, aI).setUv(u1, v2);
            }
        }
    }

    private static void buildWaterCircles(ClientLevel level, VertexConsumer builder, int ix, int iy, int iz, int radius, double camX, double camY, double camZ, float vOffset) {
        Matrix4f matrix = new Matrix4f();
        matrix.translate((float) -camX, (float) -camY, (float) -camZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int wx = ix + dx;
                int wz = iz + dz;

                int terrain = WeatherAPI.getRainHeight(level, wx, wz);
                if (Math.abs(terrain - iy) > 40) continue;

                pos.set(wx, terrain - 1, wz);

                if (!level.getFluidState(pos).is(FluidTags.WATER)) continue;
                if (!WeatherAPI.isRaining(level, wx, terrain, wz)) continue;
                if (level.getBiome(pos).value().coldEnoughToSnow(pos)) continue;

                float distanceSq = dx * dx + (iy - terrain) * (iy - terrain) + dz * dz;
                float alpha = 1.0F - (Mth.sqrt(distanceSq) / radius);
                alpha = alpha * 4.0F;
                if (alpha <= 0.01F) continue;
                if (alpha > 1.0F) alpha = 1.0F;

                int aI = (int) (alpha * 255);

                float u1 = 0.0F;
                float u2 = 1.0F;

                int noiseIndex = (wx & 15) << 4 | (wz & 15);
                float localVOffset = vOffset + randomOffset[noiseIndex];
                float v1 = Mth.floor(localVOffset * 6.0F) / 6.0F;
                float v2 = v1 + (1.0F / 6.0F);

                byte index = randomIndex[noiseIndex];
                if ((index & 1) == 0) {
                    u2 = 0.0F;
                    u1 = 1.0F;
                }
                if (index > 1) {
                    float temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                float drawY = terrain + 0.02f;

                builder.addVertex(matrix, wx, drawY, wz).setColor(255, 255, 255, aI).setUv(u1, v1);
                builder.addVertex(matrix, wx, drawY, wz + 1).setColor(255, 255, 255, aI).setUv(u1, v2);
                builder.addVertex(matrix, wx + 1, drawY, wz + 1).setColor(255, 255, 255, aI).setUv(u2, v2);
                builder.addVertex(matrix, wx + 1, drawY, wz).setColor(255, 255, 255, aI).setUv(u2, v1);
            }
        }
    }
}