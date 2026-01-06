package tech.pmman.csutility.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.client.network.ClientPayloadHandler;
import tech.pmman.csutility.network.packet.c4bomb.C4BombEventPacket;

@EventBusSubscriber(modid = CSUtility.MODID)
public class NetworkRegistry {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                C4BombEventPacket.TYPE,
                C4BombEventPacket.STREAM_CODEC,
                ClientPayloadHandler::c4BombHandler
        );
    }
}
