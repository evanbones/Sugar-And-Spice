package com.evandev.spicedcider.client.renderer;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.config.SpicedCiderConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.*;

public class SpicedCiderCloudRenderer {

    static final Random RANDOM = new Random(0);
    static final short EMPTY_CLOUD = (short) 0xF000;
    private static final int RADIUS_CHUNKS = 9;

    private static final Map<Long, CloudChunk> chunks = new HashMap<>();
    private static final ResourceLocation CLOUD_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpicedCider.MOD_ID, "textures/environment/cloud.png");
    private static final List<ChunkPos> CHUNK_OFFSETS = new ArrayList<>();
    private static int lastCenterCX = Integer.MAX_VALUE;
    private static int lastCenterCZ = Integer.MAX_VALUE;

    static {
        for (int dx = -RADIUS_CHUNKS; dx <= RADIUS_CHUNKS; dx++) {
            for (int dz = -RADIUS_CHUNKS; dz <= RADIUS_CHUNKS; dz++) {
                CHUNK_OFFSETS.add(new ChunkPos(dx, dz));
            }
        }

        CHUNK_OFFSETS.sort((p1, p2) -> {
            int dist1 = p1.x * p1.x + p1.z * p1.z;
            int dist2 = p2.x * p2.x + p2.z * p2.z;
            return Integer.compare(dist2, dist1);
        });
    }

    public static void render(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double cloudSpeed = SpicedCiderConfig.COMMON.cloudsSpeed.get();
        double timeOffset = ((double) level.getGameTime() + partialTick) * cloudSpeed * 32.0D;

        int centerCX = Mth.floor(camX / 32.0);
        int centerCZ = Mth.floor((camZ - timeOffset) / 32.0);

        if (centerCX != lastCenterCX || centerCZ != lastCenterCZ) {
            lastCenterCX = centerCX;
            lastCenterCZ = centerCZ;
            chunks.entrySet().removeIf(entry -> {
                CloudChunk chunk = entry.getValue();
                if (Math.abs(chunk.cx - centerCX) > RADIUS_CHUNKS || Math.abs(chunk.cz - centerCZ) > RADIUS_CHUNKS) {
                    chunk.close();
                    return true;
                }
                return false;
            });
        }

        Frustum frustum = new Frustum(modelViewMatrix, projectionMatrix);
        frustum.prepare(camX, camY, camZ);

        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        RenderSystem.setShader(GameRenderer::getRendertypeCloudsShader);
        RenderSystem.setShaderTexture(0, CLOUD_TEXTURE);

        Vec3 cloudColor = level.getCloudColor(partialTick);
        RenderSystem.setShaderColor((float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, 1.0f);

        poseStack.pushPose();
        poseStack.last().pose().mul(modelViewMatrix);

        float cloudHeight = 192.0F;
        int updatesThisFrame = 0;

        for (ChunkPos offset : CHUNK_OFFSETS) {
            int cx = centerCX + offset.x;
            int cz = centerCZ + offset.z;
            long key = ChunkPos.asLong(cx, cz);

            double chunkMinX = cx * 32.0;
            double chunkMinZ = cz * 32.0 + timeOffset;

            AABB chunkBox = new AABB(chunkMinX, cloudHeight, chunkMinZ, chunkMinX + 32.0, cloudHeight + 64.0, chunkMinZ + 32.0);

            if (!frustum.isVisible(chunkBox)) {
                continue;
            }

            CloudChunk chunk = chunks.computeIfAbsent(key, k -> new CloudChunk(cx, cz));

            if (chunk.needsUpdate && updatesThisFrame < 2) {
                chunk.rebuild(level);
                updatesThisFrame++;
            }

            if (!chunk.isEmpty && chunk.buffer != null) {
                poseStack.pushPose();
                double shiftX = chunkMinX - camX;
                double shiftZ = chunkMinZ - camZ;
                poseStack.translate(shiftX, cloudHeight - camY, shiftZ);
                poseStack.scale(2.0f, 2.0f, 2.0f);

                chunk.buffer.bind();
                chunk.buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getRendertypeCloudsShader());
                poseStack.popPose();
            }
        }

        VertexBuffer.unbind();
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    static void buildFluffyCloudBlock(VertexConsumer builder, Matrix4f matrix, int localX, int localY, int localZ, float r, float g, float b, float a) {
        float px = localX + RANDOM.nextFloat() * 0.1F - 0.05F;
        float py = localY + RANDOM.nextFloat() * 0.1F - 0.05F;
        float pz = localZ + RANDOM.nextFloat() * 0.1F - 0.05F;

        int rI = Mth.clamp((int) (r * 255), 0, 255);
        int gI = Mth.clamp((int) (g * 255), 0, 255);
        int bI = Mth.clamp((int) (b * 255), 0, 255);
        int aI = Mth.clamp((int) (a * 255), 0, 255);

        // Top/Bottom
        builder.addVertex(matrix, px - 0.207107F, py + 1.207107F, pz + 0.5F).setColor(rI, gI, bI, aI).setUv(0.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.207107F, py - 0.207107F, pz + 0.5F).setColor(rI, gI, bI, aI).setUv(1.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.207107F, py - 0.207107F, pz - 1.5F).setColor(rI, gI, bI, aI).setUv(1.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py + 1.207107F, pz - 1.5F).setColor(rI, gI, bI, aI).setUv(0.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);

        builder.addVertex(matrix, px + 1.207107F, py + 1.207107F, pz + 0.5F).setColor(rI, gI, bI, aI).setUv(0.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py - 0.207107F, pz + 0.5F).setColor(rI, gI, bI, aI).setUv(1.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py - 0.207107F, pz - 1.5F).setColor(rI, gI, bI, aI).setUv(1.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.207107F, py + 1.207107F, pz - 1.5F).setColor(rI, gI, bI, aI).setUv(0.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);

        // Sides
        builder.addVertex(matrix, px + 1.5F, py + 1.207107F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.5F, py - 0.207107F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.5F, py - 0.207107F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.5F, py + 1.207107F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);

        builder.addVertex(matrix, px + 1.5F, py + 1.207107F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.5F, py - 0.207107F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.5F, py - 0.207107F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.5F, py + 1.207107F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);

        builder.addVertex(matrix, px + 1.207107F, py - 0.5F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py - 0.5F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py + 1.5F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.207107F, py + 1.5F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);

        builder.addVertex(matrix, px + 1.207107F, py - 0.5F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py - 0.5F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 0.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px - 0.207107F, py + 1.5F, pz + 0.207107F).setColor(rI, gI, bI, aI).setUv(1.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix, px + 1.207107F, py + 1.5F, pz - 1.207107F).setColor(rI, gI, bI, aI).setUv(0.0F, 1.0F).setNormal(0.0F, 1.0F, 0.0F);
    }
}