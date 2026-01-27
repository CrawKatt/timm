package com.github.charlyb01.timm.registry;

import com.github.charlyb01.timm.Timm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;

public class SoundEventRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Timm.MOD_ID);
    public static final HashMap<Identifier, Holder<SoundEvent>> SOUNDEVENT_BY_ID = new HashMap<>();

    static {
        register("menu");
        register("badlands");
        register("bamboo_jungle");
        register("beach");
        register("birch_forest");
        register("cherry_grove");
        register("cold_ocean");
        register("dark_forest");
        register("deep_dark");
        register("desert");
        register("dripstone_caves");
        register("flower_forest");
        register("forest");
        register("ice_spikes");
        register("jungle");
        register("lush_caves");
        register("meadow");
        register("mountains");
        register("mushroom_fields");
        register("ocean");
        register("plains");
        register("river");
        register("savanna");
        register("snow_plains");
        register("swamp");
        register("taiga");
        register("warm_ocean");
        register("windy_hills");
        register("basalt_deltas");
        register("crimson_forest");
        register("nether_wastes");
        register("soul_sand_valley");
        register("end");
    }

    private static void register(final String name) {
        Identifier id = Timm.id(name);
        Holder<SoundEvent> holder = SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
        SOUNDEVENT_BY_ID.put(id, holder);
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}