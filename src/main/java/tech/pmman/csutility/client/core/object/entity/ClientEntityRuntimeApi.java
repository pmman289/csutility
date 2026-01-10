package tech.pmman.csutility.client.core.object.entity;

import static tech.pmman.csutility.client.core.object.entity.ClientEntityRuntime.*;

public class ClientEntityRuntimeApi {
    /**
     * 调用这个方法来将自己标记为失效
     *
     * @param gameObject 传入this
     */
    public static void unregister(ClientEntity gameObject) {
        // 尝试在两个map中移除之并调用其收尾方法
        int entityId = gameObject.getEntity().getId();
        CLIENT_OBJECT_ENTITY_MAP.remove(entityId);
        CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.remove(entityId);
        PENDING_PACKET_QUEUE.remove(entityId);
        gameObject.afterRemoved();
    }
}
