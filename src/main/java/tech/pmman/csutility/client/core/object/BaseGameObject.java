package tech.pmman.csutility.client.core.object;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface BaseGameObject {
    /**
     * 在controller初始化时执行
     */
    void init();

    /**
     * 客户端tick
     */
    void tick();

    /**
     * 处理服务端发来的数据包
     *
     * @param packet 数据包
     */
    void handlePacket(CustomPacketPayload packet);

    /**
     * 在这里做收尾工作
     */
    void afterRemoved();
}
