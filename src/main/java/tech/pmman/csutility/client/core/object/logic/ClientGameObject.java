package tech.pmman.csutility.client.core.object.logic;

import tech.pmman.csutility.client.core.object.BaseGameObject;

public interface ClientGameObject extends BaseGameObject {
    /**
     * 返回游戏对象id
     *
     * @return 对象id
     */
    int getId();

    /**
     * 返回当前对象是否已被移除
     *
     * @return 是否已被移除
     */
    boolean isRemoved();
}
