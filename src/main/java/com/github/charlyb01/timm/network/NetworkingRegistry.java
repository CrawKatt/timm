package com.github.charlyb01.timm.network;

import com.github.charlyb01.timm.Timm;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Timm.MOD_ID)
public class NetworkingRegistry {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                PlayPayload.ID,
                PlayPayload.CODEC,
                ClientPayloadHandler::handleData
        );
    }
}