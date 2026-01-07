package tech.pmman.csutility.client.core.object.logic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.core.gameObject.GameObject;
import tech.pmman.csutility.core.gameObject.network.PacketWithGameObjectId;
import tech.pmman.csutility.network.packet.gameObject.GameObjectCreatePacket;

@OnlyIn(Dist.CLIENT)
public class ClientGameObjectManager {
    // 收到创建gameObject的网络包后创建客户端gameObject
    public static void onCreatePacket(final GameObjectCreatePacket packet) {
        GameObject gameObject = ClientGameObjectFactory.createGameObject(packet.getGameObjectType());
        // 如果获取不到则打印错误日志
        if (gameObject == null) {
            String msg = "can not create game object from type %s".formatted(packet.getGameObjectType());
            CSUtility.LOGGER.error(msg);
            return;
        }
        ClientGameObjectRuntime.add(packet.getGameObjectId(), gameObject);
        // 调用init
        gameObject.init();
    }

    public static void dispatch(final PacketWithGameObjectId packet) {
        ClientGameObjectRuntime.dispatchPacket(packet);
    }
}
