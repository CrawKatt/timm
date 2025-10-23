package com.github.charlyb01.timm.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Timm Configuration"))
                .setSavingRunnable(Config.SPEC::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
                .startIntField(Component.literal("Minimum Delay"), Config.MIN_DELAY.get())
                .setDefaultValue(300)
                .setMin(0)
                .setMax(600)
                .setSaveConsumer(Config.MIN_DELAY::set)
                .build());

        general.addEntry(entryBuilder
                .startIntField(Component.literal("Maximum Delay"), Config.MAX_DELAY.get())
                .setDefaultValue(300)
                .setMin(0)
                .setMax(600)
                .setSaveConsumer(Config.MAX_DELAY::set)
                .build());

        general.addEntry(entryBuilder
                .startIntField(Component.literal("Fade Delay"), Config.FADE_DELAY.get())
                .setDefaultValue(3)
                .setMin(3)
                .setMax(15)
                .setSaveConsumer(Config.FADE_DELAY::set)
                .build());

        general.addEntry(entryBuilder
                .startIntField(Component.literal("Fade Duration"), Config.FADE_DURATION.get())
                .setDefaultValue(5)
                .setMin(5)
                .setMax(10)
                .setSaveConsumer(Config.FADE_DURATION::set)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Reset Delay"), Config.RESET_DELAY_ON_BIOME_SWITCH.get())
                .setDefaultValue(true)
                .setSaveConsumer(Config.RESET_DELAY_ON_BIOME_SWITCH::set)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Print on Skip"), Config.PRINT_ON_SKIP.get())
                .setDefaultValue(true)
                .setSaveConsumer(Config.PRINT_ON_SKIP::set)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.literal("Enable structure songs playing"), Config.ENABLE_STRUCTURE_MUSIC.get())
                .setDefaultValue(true)
                .setSaveConsumer(Config.ENABLE_STRUCTURE_MUSIC::set)
                .build());

        general.addEntry(entryBuilder
                .startEnumSelector(
                        Component.translatable("text.autoconfig.timm.option.general.structureFadeOut"),
                        StructureFadeOut.class,
                        Config.STRUCTURE_FADE_OUT.get()
                )
                .setDefaultValue(StructureFadeOut.NEVER)
                .setSaveConsumer(Config.STRUCTURE_FADE_OUT::set)
                .build());

        return builder.build();
    }
}