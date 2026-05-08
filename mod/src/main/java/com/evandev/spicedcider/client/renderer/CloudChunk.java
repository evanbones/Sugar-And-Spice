package com.evandev.spicedcider.client.renderer;

import com.evandev.spicedcider.api.WeatherAPI;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

class CloudChunk {
    int cx, cz;
    VertexBuffer buffer;
    boolean needsUpdate = true;
    boolean isEmpty = true;

    public CloudChunk(int cx, int cz) {
        this.cx = cx;
        this.cz = cz;
    }

    private boolean isCloudEmpty(ClientLevel level, int posX, int y, int posZ) {
        if (y < 0 || y > 31) return true;
        float rainFront = WeatherAPI.sampleFront(level, posX, posZ, 0.2);
        float density = WeatherAPI.getCloudDensity(posX << 1, y << 1, posZ << 1, rainFront);
        float coverage = WeatherAPI.getCoverage(rainFront);
        return density < coverage;
    }

    public void rebuild(ClientLevel level) {
        this.needsUpdate = false;
        if (this.buffer == null) {
            this.buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        }

        short[] data = new short[8192];
        boolean hasBlocks = false;

        int posX = cx << 4;
        int posZ = cz << 4;

        for (int index = 0; index < 8192; index++) {
            int x = (index & 15) | posX;
            int y = (index >> 4) & 31;
            int z = (index >> 9) | posZ;

            float rainFront = WeatherAPI.sampleFront(level, x, z, 0.2);
            float density = WeatherAPI.getCloudDensity(x << 1, y << 1, z << 1, rainFront);
            float coverage = WeatherAPI.getCoverage(rainFront);

            if (density < coverage) {
                data[index] = SpicedCiderCloudRenderer.EMPTY_CLOUD;
            } else {
                data[index] = (short) ((byte) (rainFront * 15) << 4);
                byte thunder = (byte) (WeatherAPI.sampleThunderstorm(level, x, z, 0.1) * rainFront * 15);
                data[index] |= (short) (thunder << 8);
                hasBlocks = true;
            }
        }

        this.isEmpty = !hasBlocks;
        if (this.isEmpty) return;

        for (int index = 0; index < 8192; index++) {
            if (data[index] == SpicedCiderCloudRenderer.EMPTY_CLOUD) continue;
            int y = (index >> 4) & 31;
            byte light = 15;
            for (byte i = 1; i < 15; i++) {
                if (y + i > 31) break;
                int index2 = index + (i << 4);
                if (data[index2] != SpicedCiderCloudRenderer.EMPTY_CLOUD) light--;
            }
            data[index] |= light;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f matrix = new Matrix4f();

        for (int i = 0; i < 8192; i++) {
            if (data[i] == SpicedCiderCloudRenderer.EMPTY_CLOUD) continue;

            int x = i & 15;
            int y = (i >> 4) & 31;
            int z = i >> 9;

            int worldX = posX + x;
            int worldZ = posZ + z;

            boolean leftEmpty = (x == 0) ? isCloudEmpty(level, worldX - 1, y, worldZ) : (data[i - 1] == SpicedCiderCloudRenderer.EMPTY_CLOUD);
            boolean rightEmpty = (x == 15) ? isCloudEmpty(level, worldX + 1, y, worldZ) : (data[i + 1] == SpicedCiderCloudRenderer.EMPTY_CLOUD);
            boolean bottomEmpty = y == 0 || (data[i - 16] == SpicedCiderCloudRenderer.EMPTY_CLOUD);
            boolean topEmpty = y == 31 || (data[i + 16] == SpicedCiderCloudRenderer.EMPTY_CLOUD);
            boolean frontEmpty = (z == 0) ? isCloudEmpty(level, worldX, y, worldZ - 1) : (data[i - 512] == SpicedCiderCloudRenderer.EMPTY_CLOUD);
            boolean backEmpty = (z == 15) ? isCloudEmpty(level, worldX, y, worldZ + 1) : (data[i + 512] == SpicedCiderCloudRenderer.EMPTY_CLOUD);

            if (!(leftEmpty || rightEmpty || bottomEmpty || topEmpty || frontEmpty || backEmpty)) {
                continue;
            }

            SpicedCiderCloudRenderer.RANDOM.setSeed(Mth.getSeed(x | posX, y, z | posZ));

            float deltaBrightness = ((data[i] & 15) + SpicedCiderCloudRenderer.RANDOM.nextFloat()) / 15F;
            float deltaWetness = (((data[i] >> 4) & 15) + SpicedCiderCloudRenderer.RANDOM.nextFloat()) / 15F;
            float deltaThunder = ((data[i] >> 8) & 15) / 15F;

            deltaBrightness *= (1 - deltaWetness) * 0.5F + 0.5F;
            deltaThunder = Mth.lerp(deltaThunder, 1.0f, 0.5f);

            float r = Mth.lerp(deltaWetness, 66F / 255F, 150F / 255F);
            float g = Mth.lerp(deltaWetness, 74F / 255F, 176F / 255F);
            float b = Mth.lerp(deltaWetness, 74F / 255F, 211F / 255F);

            r = Mth.lerp(deltaBrightness, r, 1.0F) * deltaThunder;
            g = Mth.lerp(deltaBrightness, g, 1.0F) * deltaThunder;
            b = Mth.lerp(deltaBrightness, b, 1.0F) * deltaThunder;

            SpicedCiderCloudRenderer.buildFluffyCloudBlock(builder, matrix, x, y, z, r, g, b, 1.0f);
        }

        MeshData meshData = builder.build();
        if (meshData != null) {
            this.buffer.bind();
            this.buffer.upload(meshData);
            VertexBuffer.unbind();
        }
    }

    public void close() {
        if (this.buffer != null) {
            this.buffer.close();
        }
    }
}
