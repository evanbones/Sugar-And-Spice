package com.evandev.spicedcider.mixin.minecraft;

import com.evandev.spicedcider.api.WeatherAPI;
import com.evandev.spicedcider.config.SpicedCiderConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void spicedcider$tickWeatherChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;

        int chance = SpicedCiderConfig.COMMON.lightningChance.get();
        if (chance > 1 && level.random.nextInt(chance) > 0) return;

        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;

        int lx = (cx << 4) | level.random.nextInt(16);
        int lz = (cz << 4) | level.random.nextInt(16);
        int ly = WeatherAPI.getRainHeight(level, lx, lz);

        if (WeatherAPI.isThundering(level, lx, ly, lz)) {
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningbolt != null) {
                lightningbolt.moveTo(lx, ly, lz);
                level.addFreshEntity(lightningbolt);
            }
        }
    }
}