package com.evandev.spicedcider.client.renderer;

import com.evandev.spicedcider.SpicedCider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LightningBolt;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Random;

public class SpicedCiderLightningRenderer extends EntityRenderer<LightningBolt> {

    private static final ResourceLocation LIGHTNING_TEXTURE = ResourceLocation.fromNamespaceAndPath(SpicedCider.MOD_ID, "textures/environment/lightning.png");
    private static final Random RANDOM = new Random(0);

    public SpicedCiderLightningRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LightningBolt entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float cloudTop = 192.0f + 8.5f;
        float startY = cloudTop - (float) entity.getY();

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(LIGHTNING_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        float dx = 0.5f;
        float dz = 0.5f;

        float x1 = 0.5F + dx;
        float x2 = 0.5F - dx;
        float z1 = 0.5F + dz;
        float z2 = 0.5F - dz;
        float x1_2 = 0.5F + dx * 0.5F;
        float x2_2 = 0.5F - dx * 0.5F;
        float z1_2 = 0.5F + dz * 0.5F;
        float z2_2 = 0.5F - dz * 0.5F;

        int sectionCount = Mth.floor(startY / 8F + 1);
        float secDelta = startY / sectionCount;

        float currentY = 0;
        float dx1 = 0;
        float dz1 = 0;
        float dx2 = 0;
        float dz2 = 0;

        RANDOM.setSeed(entity.getId());

        for (int i = 0; i < sectionCount; i++) {
            float nextY = currentY + secDelta;

            vertexConsumer.addVertex(matrix, x1 + dx1, currentY, z1 + dz1).setColor(255, 255, 255, 255).setUv(0F, 0F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix, x1 + dx2, nextY, z1 + dz2).setColor(255, 255, 255, 255).setUv(0F, 1F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix, x2 + dx2, nextY, z2 + dz2).setColor(255, 255, 255, 255).setUv(1F, 1F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix, x2 + dx1, currentY, z2 + dz1).setColor(255, 255, 255, 255).setUv(1F, 0F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);

            if (i > 0 && RANDOM.nextInt(3) == 0) {
                float dist = RANDOM.nextFloat() * 15;
                float dx3 = dx * dist;
                float dz3 = dz * dist;

                vertexConsumer.addVertex(matrix, x1_2 + dx3, currentY, z1_2 + dz3).setColor(255, 255, 255, 255).setUv(0F, 0F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
                vertexConsumer.addVertex(matrix, x1_2 + dx2, nextY, z1_2 + dz2).setColor(255, 255, 255, 255).setUv(0F, 1F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
                vertexConsumer.addVertex(matrix, x2_2 + dx2, nextY, z2_2 + dz2).setColor(255, 255, 255, 255).setUv(1F, 1F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
                vertexConsumer.addVertex(matrix, x2_2 + dx3, currentY, z2_2 + dz3).setColor(255, 255, 255, 255).setUv(1F, 0F).setOverlay(packedLight).setLight(packedLight).setNormal(0, 1, 0);
            }

            dx1 = dx2;
            dz1 = dz2;
            float dist = RANDOM.nextFloat() * 7;
            dx2 = dx * dist;
            dz2 = dz * dist;
            currentY = nextY;
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LightningBolt entity) {
        return LIGHTNING_TEXTURE;
    }
}