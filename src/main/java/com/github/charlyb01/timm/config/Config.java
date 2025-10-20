package com.github.charlyb01.timm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MIN_DELAY;
    public static final ModConfigSpec.IntValue MAX_DELAY;
    public static final ModConfigSpec.IntValue FADE_DELAY;
    public static final ModConfigSpec.IntValue FADE_DURATION;
    public static final ModConfigSpec.BooleanValue RESET_DELAY_ON_BIOME_SWITCH;
    public static final ModConfigSpec.BooleanValue PRINT_ON_SKIP;
    public static final ModConfigSpec.BooleanValue DEBUG_LOG;
    public static final ModConfigSpec.BooleanValue ENABLE_STRUCTURE_MUSIC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");

        MIN_DELAY = builder
                .comment("Minimum delay (in ticks)")
                .defineInRange("minDelay", 300, 0, 600);

        MAX_DELAY = builder
                .comment("Maximum delay (in ticks)")
                .defineInRange("maxDelay", 300, 0, 600);

        FADE_DELAY = builder
                .comment("Duration of music fade out")
                .defineInRange("fadeDuration", 3, 3, 15);

        FADE_DURATION = builder
                .comment("Duration of music fade out")
                .defineInRange("fadeDuration", 5, 5, 10);

        RESET_DELAY_ON_BIOME_SWITCH = builder
                .comment("Reset delay on biome switch")
                .define("resetDelayOnBiomeSwitch", false);

        PRINT_ON_SKIP = builder
                .comment("Print message when skipping")
                .define("printOnSkip", true);

        DEBUG_LOG = builder
                .comment("Enable debug logging (no GUI entry)")
                .define("debugLog", false);

        ENABLE_STRUCTURE_MUSIC = builder
                .comment("Enable structure songs playing")
                .define("enableStructure", true);

        builder.pop();
        SPEC = builder.build();
    }
}