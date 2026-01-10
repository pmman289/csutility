package tech.pmman.csutility.client.core.object.entity;

import tech.pmman.csutility.client.core.object.BaseGameObject;
import tech.pmman.csutility.core.object.entity.ServerEntity;

public interface ClientEntity extends BaseGameObject {
    /**
     * 在这里返回entity供runtime使用
     *
     * @return 实体
     */
    ServerEntity getEntity();
}
