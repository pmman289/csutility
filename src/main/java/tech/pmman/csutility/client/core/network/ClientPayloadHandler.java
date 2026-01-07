package tech.pmman.csutility.client.core.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import tech.pmman.csutility.client.core.object.entity.ClientGameObjectEntityRuntime;
import tech.pmman.csutility.client.core.object.logic.ClientGameObjectManager;
import tech.pmman.csutility.client.core.object.logic.ClientGameObjectRuntime;
import tech.pmman.csutility.core.gameObject.network.PacketWithGameObjectId;
import tech.pmman.csutility.network.packet.gameObject.GameObjectCreatePacket;

public class ClientPayloadHandler {
    public static void c4BombHandler(final PacketWithGameObjectId packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientGameObjectEntityRuntime.dispatchPacket(packet));
    }

    /**
     * 这里是gameObject在客户端的创建入口
     *
     * @param packet  创建包
     * @param context 上下文
     */
    public static void gameObjectCreatePacketHandler(final GameObjectCreatePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientGameObjectManager.onCreatePacket(packet));
    }

    public static void gameObjectPacketHandler(final PacketWithGameObjectId packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientGameObjectManager.dispatch(packet));
    }
}
