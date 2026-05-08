package com.evandev.spicedcider.api;

import com.evandev.spicedcider.util.ImageSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class WeatherAPI {
    private static final ImageSampler MAIN_SHAPE_SAMPLER = new ImageSampler("/assets/spicedcider/textures/weather/main_shape.png");
    private static final ImageSampler LARGE_DETAILS_SAMPLER = new ImageSampler("/assets/spicedcider/textures/weather/large_details.png");
    private static final ImageSampler VARIATION_SAMPLER = new ImageSampler("/assets/spicedcider/textures/weather/variation.png");
    private static final ImageSampler FRONTS_SAMPLER = new ImageSampler("/assets/spicedcider/textures/weather/rain_fronts.png");
    private static final ImageSampler RAIN_DENSITY = new ImageSampler("/assets/spicedcider/textures/weather/rain_density.png");
    private static final ImageSampler VANILLA_CLOUDS = new ImageSampler("/assets/spicedcider/textures/weather/vanilla_clouds.png").setSmooth(true);
    private static final ImageSampler THUNDERSTORMS = new ImageSampler("/assets/spicedcider/textures/weather/thunderstorms.png");

    private static final float[] CLOUD_SHAPE = new float[64];
    private static final Vector2i[] OFFSETS;

    // TODO: Link these to actual NeoForge config setup
    private static final float CLOUDS_SPEED = 0.001f;
    private static final boolean USE_VANILLA_CLOUDS = false;
    private static final boolean ETERNAL_RAIN = false;
    private static final boolean FREQUENT_RAIN = false;
    private static final boolean ETERNAL_THUNDER = false;

    static {
        for (byte i = 0; i < 16; i++) {
            CLOUD_SHAPE[i] = (16 - i) / 16F;
            CLOUD_SHAPE[i] *= CLOUD_SHAPE[i];
        }
        for (byte i = 16; i < 64; i++) {
            CLOUD_SHAPE[i] = (i - 16) / 48F;
            CLOUD_SHAPE[i] *= CLOUD_SHAPE[i];
        }

        int radius = 6;
        int capacity = radius * 2 + 1;
        capacity *= capacity;

        List<Vector2i> offsets = new ArrayList<>(capacity);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    offsets.add(new Vector2i(x, z));
                }
            }
        }
        offsets.sort((v1, v2) -> {
            int d1 = v1.x * v1.x + v1.y * v1.y;
            int d2 = v2.x * v2.x + v2.y * v2.y;
            return Integer.compare(d1, d2);
        });
        OFFSETS = offsets.toArray(Vector2i[]::new);
    }

    public static boolean isRaining(Level level, int x, int y, int z) {
        if (level.dimensionType().ultraWarm()) return false;

        if (y > getCloudHeight(level) + 8) return false;
        if (y < getRainHeight(level, x, z)) return false;

        z = (int) (z - level.getGameTime() * CLOUDS_SPEED * 32);

        if (ETERNAL_RAIN) {
            return !USE_VANILLA_CLOUDS || getCloudDensity(x, 2, z, 1F) > 0.5F;
        }

        float rainFront = sampleFront(level, x, z, 0.1);
        if (rainFront < 0.2F) return false;

        float coverage = getCoverage(rainFront);
        int sampleHeight = USE_VANILLA_CLOUDS ? 2 : 7;
        return getCloudDensity(x, sampleHeight, z, rainFront) > coverage;
    }

    public static boolean isThundering(Level level, int x, int y, int z) {
        return isRaining(level, x, y, z) && sampleThunderstorm(level, x, z, 0.05) > 0.3F;
    }

    public static float inCloud(Level level, double x, double y, double z) {
        z -= level.getGameTime() * CLOUDS_SPEED * 32;
        int x1 = Mth.floor(x / 2.0) << 1;
        int y1 = Mth.floor(y / 2.0) << 1;
        int z1 = Mth.floor(z / 2.0) << 1;

        int x2 = x1 + 2;
        int y2 = y1 + 2;
        int z2 = z1 + 2;

        float dx = (float) (x - x1) / 2F;
        float dy = (float) (y - y1) / 2F;
        float dz = (float) (z - z1) / 2F;

        float a = isInCloud(level, x1, y1, z1) ? 1F : 0F;
        float b = isInCloud(level, x2, y1, z1) ? 1F : 0F;
        float c = isInCloud(level, x1, y2, z1) ? 1F : 0F;
        float d = isInCloud(level, x2, y2, z1) ? 1F : 0F;
        float e = isInCloud(level, x1, y1, z2) ? 1F : 0F;
        float f = isInCloud(level, x2, y1, z2) ? 1F : 0F;
        float g = isInCloud(level, x1, y2, z2) ? 1F : 0F;
        float h = isInCloud(level, x2, y2, z2) ? 1F : 0F;

        return (float) Mth.lerp3(dx, dy, dz, a, b, c, d, e, f, g, h);
    }

    private static boolean isInCloud(Level level, int x, int y, int z) {
        if (level.dimensionType().ultraWarm()) return false;
        int start = (int) getCloudHeight(level);
        if (y < start || y > start + 64) return false;
        float rainFront = sampleFront(level, x, z, 0.1);
        float coverage = getCoverage(rainFront);
        return getCloudDensity(x, y - start, z, rainFront) > coverage;
    }

    public static float getCloudDensity(int x, int y, int z, float rainFront) {
        if (USE_VANILLA_CLOUDS) {
            if (y > 6) return 0;
            float shape = y == 0 || y == 5 ? 1 : 0;
            return VANILLA_CLOUDS.sample(x / 16.0, z / 16.0) * 3 - shape;
        }

        float density = MAIN_SHAPE_SAMPLER.sample(x * 0.75F, z * 0.75F);
        density += LARGE_DETAILS_SAMPLER.sample(x * 2.5F, z * 2.5F);

        density -= VARIATION_SAMPLER.sample(y * 2.5F, x * 2.5F) * 0.05F;
        density -= VARIATION_SAMPLER.sample(z * 2.5F, y * 2.5F) * 0.05F;
        density -= VARIATION_SAMPLER.sample(z * 2.5F, x * 2.5F) * 0.05F;

        int value = (int) (Mth.getSeed(x, y, z) % 3);
        density -= value * 0.01F;

        float density1 = density - CLOUD_SHAPE[Mth.clamp(y << 1, 0, 63)];
        float density2 = density + MAIN_SHAPE_SAMPLER.sample(x * 1.5F, z * 1.5F) - CLOUD_SHAPE[Mth.clamp(y, 0, 63)] * 3F;

        return Mth.lerp(rainFront, density1, density2);
    }

    public static float sampleFront(Level level, int x, int z, double scale) {
        if (ETERNAL_RAIN) return 1F;

        float front = FRONTS_SAMPLER.sample(x * scale, z * scale);
        if (!FREQUENT_RAIN) {
            scale *= 0.7;
            front *= RAIN_DENSITY.sample(x * scale, z * scale);
        }
        return front;
    }

    public static float sampleThunderstorm(Level level, int x, int z, double scale) {
        if (ETERNAL_THUNDER) return 1F;
        return THUNDERSTORMS.sample(x * scale, z * scale);
    }

    public static float getCoverage(float rainFront) {
        return Mth.lerp(rainFront, 1.3F, 0.5F);
    }

    public static int getRainHeight(Level level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    public static float getRainDensity(Level level, double x, double y, double z, boolean includeSnow) {
        int x1 = Mth.floor(x);
        int y1 = Mth.floor(y);
        int z1 = Mth.floor(z);
        int x2 = x1 + 1;
        int z2 = z1 + 1;

        float dx = (float) (x - x1);
        float dz = (float) (z - z1);
        dz -= (float) ((level.getGameTime() * CLOUDS_SPEED * 32) % 1.0);

        float a = getRainDensity(level, x1, y1, z1, includeSnow);
        float b = getRainDensity(level, x2, y1, z1, includeSnow);
        float c = getRainDensity(level, x1, y1, z2, includeSnow);
        float d = getRainDensity(level, x2, y1, z2, includeSnow);

        float value = (float) Mth.lerp2(dx, dz, a, b, c, d);
        return Mth.clamp(value, 0F, 1F);
    }

    private static float getRainDensity(Level level, int x, int y, int z, boolean includeSnow) {
        if (level.dimensionType().ultraWarm()) return 0;

        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (Vector2i offset : OFFSETS) {
            pos.set(x + offset.x, y, z + offset.y);
            boolean snowCheck = includeSnow || !level.getBiome(pos).value().coldEnoughToSnow(pos);
            if (snowCheck && isRaining(level, pos.getX(), y, pos.getZ())) {
                count++;
                if (count >= 64) return 1F;
            }
        }

        return count / 64F;
    }

    private static float getCloudHeight(Level level) {
        return 192F;
    }
}