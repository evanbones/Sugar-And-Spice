package com.evandev.spicedcider.client.renderer;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.api.WeatherAPI;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
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

    public static void render(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, PoseStack poseStack, double camX, double camY, double camZ) {
        Minecraft mc = Minecraft.getInstance();
        int radius = mc.options.graphicsMode().get().getId() > 0 ? 32 : 16;

        int ix = Mth.floor(camX);
        int iy = Mth.floor(camY);
        int iz = Mth.floor(camZ);

        int rainTop = 192 + 8;
        if (iy - rainTop > 40) return;

        float timeDelta = ((float) ticks + partialTick);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getParticleShader);
        lightTexture.turnOnLightLayer();

        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.setShaderTexture(0, RAIN_LOCATION);
        BufferBuilder rainBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        float rainVOffset = -(timeDelta * 0.05f) % 1.0f;

        buildWeather(level, rainBuilder, matrix, ix, iy, iz, radius, rainTop, camX, camY, camZ, rainVOffset, false);

        MeshData rainData = rainBuilder.build();
        if (rainData != null) BufferUploader.drawWithShader(rainData);

        int circleRadius = 16;
        RenderSystem.setShaderTexture(0, WATER_CIRCLES_LOCATION);
        BufferBuilder waterBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        float circleVOffset = (timeDelta * 0.07f) % 1.0f;

        buildWaterCircles(level, waterBuilder, matrix, ix, iy, iz, circleRadius, camX, camY, camZ, circleVOffset);

        MeshData waterData = waterBuilder.build();
        if (waterData != null) BufferUploader.drawWithShader(waterData);

        RenderSystem.setShaderTexture(0, SNOW_LOCATION);
        BufferBuilder snowBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        float snowVOffset = -(timeDelta * 0.002f) % 1.0f;

        buildWeather(level, snowBuilder, matrix, ix, iy, iz, radius, rainTop, camX, camY, camZ, snowVOffset, true);

        MeshData snowData = snowBuilder.build();
        if (snowData != null) BufferUploader.drawWithShader(snowData);

        lightTexture.turnOffLightLayer();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private static void buildWeather(ClientLevel level, VertexConsumer builder, Matrix4f matrix, int ix, int iy, int iz, int radius, int rainTop, double camX, double camY, double camZ, float vOffset, boolean drawingSnow) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int wx = ix + dx;
                int wz = iz + dz;

                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);
                int maxDist = Math.max(absX, absZ);

                int lodLevel = 1;
                if (maxDist > 64) lodLevel = 8;
                else if (maxDist > 32) lodLevel = 4;
                else if (maxDist > 16) lodLevel = 2;

                if (lodLevel > 1 && ((wx & (lodLevel - 1)) != 0 || (wz & (lodLevel - 1)) != 0)) continue;

                int terrain = WeatherAPI.getRainHeight(level, wx, wz);

                int renderBottom = Math.max(terrain, iy - 32);
                int renderTop = Math.min(rainTop, iy + 32);
                if (renderBottom >= renderTop) continue;

                pos.set(wx, terrain, wz);

                if (!level.getBiome(pos).value().hasPrecipitation()) continue;
                if (!WeatherAPI.isRaining(level, wx, terrain, wz)) continue;

                boolean isSnowy = level.getBiome(pos).value().coldEnoughToSnow(pos);
                if (isSnowy != drawingSnow) continue;

                float alpha = WeatherAPI.sampleFront(level, wx, wz, 0.1F);
                alpha = Mth.clamp((alpha - 0.2F) * 2, 0.5F, 1.0F);

                if (lodLevel > 1) {
                    alpha *= (1.0f - (maxDist / (float) radius) * 0.5f);
                }
                int aI = (int) (alpha * 255);

                pos.set(wx, Math.max(terrain, iy), wz);
                int packedLight = LevelRenderer.getLightColor(level, pos);

                float cX = (float) (camX - (wx + 0.5));
                float cZ = (float) (camZ - (wz + 0.5));
                float length = Mth.sqrt(cX * cX + cZ * cZ);

                if (length > 0) {
                    length /= 0.5f;
                    cX /= length;
                    cZ /= length;

                    float temp = cX;
                    cX = -cZ;
                    cZ = temp;
                } else {
                    cX = 0.5f;
                    cZ = 0.0f;
                }

                float quadSize = (float) lodLevel;
                cX *= quadSize;
                cZ *= quadSize;

                float rx1 = (float) (wx + 0.5f - cX - camX);
                float rx2 = (float) (wx + 0.5f + cX - camX);
                float rz1 = (float) (wz + 0.5f + cZ - camZ);
                float rz2 = (float) (wz + 0.5f - cZ - camZ);

                float ryTerrain = (float) (renderBottom - camY);
                float ryTop = (float) (renderTop - camY);

                float u1 = ((wx + wz) & 3) * 0.25F;
                float u2 = u1 + (0.25F * quadSize);

                float baseV = randomOffset[(wx & 15) << 4 | (wz & 15)] + vOffset;
                float vBottom = ((rainTop - renderBottom) * 0.0625F + baseV);
                float vTop = ((rainTop - renderTop) * 0.0625F + baseV);

                builder.addVertex(matrix, rx1, ryTerrain, rz1).setColor(255, 255, 255, aI).setUv(u1, vBottom).setLight(packedLight);
                builder.addVertex(matrix, rx1, ryTop, rz1).setColor(255, 255, 255, aI).setUv(u1, vTop).setLight(packedLight);
                builder.addVertex(matrix, rx2, ryTop, rz2).setColor(255, 255, 255, aI).setUv(u2, vTop).setLight(packedLight);
                builder.addVertex(matrix, rx2, ryTerrain, rz2).setColor(255, 255, 255, aI).setUv(u2, vBottom).setLight(packedLight);
            }
        }
    }

    private static void buildWaterCircles(ClientLevel level, VertexConsumer builder, Matrix4f matrix, int ix, int iy, int iz, int radius, double camX, double camY, double camZ, float vOffset) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int wx = ix + dx;
                int wz = iz + dz;

                int terrain = WeatherAPI.getRainHeight(level, wx, wz);
                if (Math.abs(terrain - iy) > 40) continue;

                pos.set(wx, terrain - 1, wz);

                if (!level.getFluidState(pos).is(FluidTags.WATER)) continue;
                if (!level.getBiome(pos).value().hasPrecipitation()) continue;
                if (!WeatherAPI.isRaining(level, wx, terrain, wz)) continue;
                if (level.getBiome(pos).value().coldEnoughToSnow(pos)) continue;

                float distanceSq = dx * dx + (iy - terrain) * (iy - terrain) + dz * dz;
                float alpha = 1.0F - (Mth.sqrt(distanceSq) / radius);
                alpha = alpha * 4.0F;
                if (alpha <= 0.01F) continue;
                if (alpha > 1.0F) alpha = 1.0F;

                int aI = (int) (alpha * 255);

                int packedLight = LevelRenderer.getLightColor(level, pos.above());

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

                float rx = (float) (wx - camX);
                float rz = (float) (wz - camZ);
                float ry = (float) (terrain + 0.02f - camY);

                builder.addVertex(matrix, rx, ry, rz).setColor(255, 255, 255, aI).setUv(u1, v1).setLight(packedLight);
                builder.addVertex(matrix, rx, ry, rz + 1).setColor(255, 255, 255, aI).setUv(u1, v2).setLight(packedLight);
                builder.addVertex(matrix, rx + 1, ry, rz + 1).setColor(255, 255, 255, aI).setUv(u2, v2).setLight(packedLight);
                builder.addVertex(matrix, rx + 1, ry, rz).setColor(255, 255, 255, aI).setUv(u2, v1).setLight(packedLight);
            }
        }
    }
}