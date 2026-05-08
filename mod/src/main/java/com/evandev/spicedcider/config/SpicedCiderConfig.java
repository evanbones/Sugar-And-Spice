package com.evandev.spicedcider.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SpicedCiderConfig {
    public static class Common {
        public final ModConfigSpec.DoubleValue cloudsSpeed;
        public final ModConfigSpec.BooleanValue useVanillaClouds;
        public final ModConfigSpec.BooleanValue eternalRain;
        public final ModConfigSpec.BooleanValue eternalThunder;
        public final ModConfigSpec.BooleanValue frequentRain;
        public final ModConfigSpec.IntValue lightningChance;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("weather");

            cloudsSpeed = builder.comment("Clouds speed in ticks per chunk, larger values will cause clouds move faster")
                    .defineInRange("cloudsSpeed", 0.001D, 0.0D, 1.0D);

            useVanillaClouds = builder.comment("Use vanilla clouds texture as a base map for clouds")
                    .define("useVanillaClouds", false);

            eternalRain = builder.comment("Makes weather in the whole world rain only")
                    .define("eternalRain", false);

            eternalThunder = builder.comment("Makes weather in the whole world thunderstorm")
                    .define("eternalThunder", false);

            frequentRain = builder.comment("Makes rain more frequent instead of vanilla behaviour")
                    .define("frequentRain", false);

            lightningChance = builder.comment("Chance that lighting will happen in this chunk (during thunderstorm). Actual chance is 1/lightningChance.")
                    .defineInRange("lightningChance", 300, 1, Short.MAX_VALUE);

            builder.pop();
        }
    }

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }
}