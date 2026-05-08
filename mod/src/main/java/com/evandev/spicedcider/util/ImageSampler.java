package com.evandev.spicedcider.util;

import com.evandev.spicedcider.SpicedCider;
import net.minecraft.util.Mth;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ImageSampler {
    private final float[] data;
    private final int width;
    private final int height;
    private boolean smooth;

    public ImageSampler(String path) {
        BufferedImage image = null;
        try (InputStream stream = ImageSampler.class.getResourceAsStream(path)) {
            if (stream != null) {
                image = ImageIO.read(stream);
            } else {
                SpicedCider.LOGGER.error("Failed to find noise texture at: {}", path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (image == null) {
            image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        width = image.getWidth();
        height = image.getHeight();
        data = new float[width * height];

        int[] pixels = new int[data.length];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        for (int i = 0; i < data.length; i++) {
            data[i] = (pixels[i] & 255) / 255F;
        }
    }

    public float sample(double x, double z) {
        int x1 = Mth.floor(x);
        int z1 = Mth.floor(z);
        int x2 = wrap(x1 + 1, width);
        int z2 = wrap(z1 + 1, height);
        float dx = (float) (x - x1);
        float dz = (float) (z - z1);
        x1 = wrap(x1, width);
        z1 = wrap(z1, height);

        float a = data[getIndex(x1, z1)];
        float b = data[getIndex(x2, z1)];
        float c = data[getIndex(x1, z2)];
        float d = data[getIndex(x2, z2)];

        if (smooth) {
            dx = smoothStep(dx);
            dz = smoothStep(dz);
        }

        return (float) Mth.lerp2(dx, dz, a, b, c, d);
    }

    private int getIndex(int x, int z) {
        return z * width + x;
    }

    public ImageSampler setSmooth(boolean smooth) {
        this.smooth = smooth;
        return this;
    }

    private float smoothStep(float x) {
        return x * x * x * (x * (x * 6 - 15) + 10);
    }

    private int wrap(int value, int side) {
        if (Mth.isPowerOfTwo(side)) {
            return value & (side - 1);
        }
        int result = (value - value / side * side);
        return result < 0 ? result + side : result;
    }
}