package tech.pmman.csutility.client.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import tech.pmman.csutility.client.core.ClientEntityControllerRuntime;
import tech.pmman.csutility.network.core.PacketWithEntityId;

public class ClientPayloadHandler {
    public static void c4BombHandler(final PacketWithEntityId packet, final IPayloadContext context){
        context.enqueueWork(() -> ClientEntityControllerRuntime.dispatchPacket(packet));
    }
}
