package tech.pmman.csutility.core.gameObject;

import net.minecraft.world.entity.Entity;

public interface GameObjectWithEntity extends BaseGameObject {
    /**
     * 在这里返回entity供runtime使用
     *
     * @return 实体
     */
    Entity getEntity();
}
