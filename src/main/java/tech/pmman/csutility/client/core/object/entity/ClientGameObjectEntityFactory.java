package tech.pmman.csutility.client.core.object.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.client.object.entity.ClientC4BombEntity;
import tech.pmman.csutility.object.entity.ModEntities;
import tech.pmman.csutility.object.entity.c4bomb.ServerC4BombEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ClientGameObjectEntityFactory {
    private static final Map<EntityType<?>, Function<Entity, ClientC4BombEntity>> REGISTRY =
            new HashMap<>();

    /**
     * 在这个方法中注册控制器创建方法
     */
    public static void register() {
        REGISTRY.put(
                ModEntities.C4BOMB_ENTITY.get(),
                e -> new ClientC4BombEntity((ServerC4BombEntity) e)
        );
    }

    /**
     * 在这个方法中根据类型新建返回对应的controller
     *
     * @param entity 实体实例
     * @return 对应的controller
     */
    public static ClientC4BombEntity createController(Entity entity) {
        Function<Entity, ClientC4BombEntity> controllerCreator = REGISTRY.get(entity.getType());
        if (controllerCreator != null) {
            return controllerCreator.apply(entity);
        }
        // 兜底返回
        return null;
    }
}
