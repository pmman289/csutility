package tech.pmman.csutility.client.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import tech.pmman.csutility.client.entity.ClientC4BombController;
import tech.pmman.csutility.entity.ModEntities;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClientEntityControllerFactory {
    private static final Map<EntityType<?>, Function<Entity, ClientController>> REGISTRY =
            new HashMap<>();

    /**
     * 在这个方法中注册控制器创建方法
     */
    public static void register() {
        REGISTRY.put(
                ModEntities.C4BOMB_ENTITY.get(),
                e -> new ClientC4BombController((C4BombEntity) e)
        );
    }

    /**
     * 在这个方法中根据类型新建返回对应的controller
     *
     * @param entity 实体实例
     * @return 对应的controller
     */
    public static ClientController createController(Entity entity) {
        Function<Entity, ClientController> controllerCreator = REGISTRY.get(entity.getType());
        if (controllerCreator != null) {
            return controllerCreator.apply(entity);
        }
        // 兜底返回
        return null;
    }
}
