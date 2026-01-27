package com.github.charlyb01.timm;

import com.github.charlyb01.timm.command.CommandRegistry;
import com.github.charlyb01.timm.config.Config;
import com.github.charlyb01.timm.config.ModConfigScreen;
import com.github.charlyb01.timm.music.BiomePlaylist;
import com.github.charlyb01.timm.music.Songs;
import com.github.charlyb01.timm.music.StructurePlaylist;
import com.github.charlyb01.timm.registry.SoundEventRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Timm.MOD_ID)
public class Timm {
    public static final String MOD_ID = "timm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Timm(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, screen) -> ModConfigScreen.create(screen));
        }

        SoundEventRegistry.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::onClientCommands);
    }

    public static Identifier id(final String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void debugLog(String debugString) {
        if (Config.DEBUG_LOG.get()) {
            LOGGER.info(debugString);
        }
    }

    private void onClientCommands(RegisterClientCommandsEvent event) {
        CommandRegistry.init(event);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BiomePlaylist.init();
                Songs.init();
                StructurePlaylist.init();
            });
        }
    }
}