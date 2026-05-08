package com.evandev.spicedcider.mixin.structures;

import com.evandev.spicedcider.SpicedCider;
import com.evandev.spicedcider.command.SpicedCiderStructureCommand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureErrorMixin {

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithContext(Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onPlace(PlacedFeature instance, PlacementContext context, RandomSource randomSource, BlockPos pos, Operation<Boolean> original) {
        ResourceLocation key = context.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getKey(instance);

        try {
            long prev = System.nanoTime();
            boolean result = original.call(instance, context, randomSource, pos);
            long elapsedTime = (System.nanoTime() - prev) / 10L;

            if (key != null) {
                SpicedCiderStructureCommand.FEATURE_TIMINGS.merge(key, elapsedTime, Long::sum);
            }
            return result;
        } catch (Exception e) {
            SpicedCider.LOGGER.warn("Feature: {} errored during placement at {}", key, pos);
            return false;
        }
    }

    @WrapOperation(method = "placeWithBiomeCheck", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithContext(Lnet/minecraft/world/level/levelgen/placement/PlacementContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean onPlaceWithBiome(PlacedFeature instance, PlacementContext context, RandomSource randomSource, BlockPos pos, Operation<Boolean> original) {
        ResourceLocation key = context.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getKey(instance);

        try {
            long prev = System.nanoTime();
            boolean result = original.call(instance, context, randomSource, pos);
            long elapsedTime = (System.nanoTime() - prev) / 10L;

            if (key != null) {
                SpicedCiderStructureCommand.FEATURE_TIMINGS.merge(key, elapsedTime, Long::sum);
            }
            return result;
        } catch (Exception e) {
            SpicedCider.LOGGER.warn("Feature: {} errored during placement at {}", key, pos);
            return false;
        }
    }
}