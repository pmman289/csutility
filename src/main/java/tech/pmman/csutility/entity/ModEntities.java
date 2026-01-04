package tech.pmman.csutility.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CSUtility.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<C4BombEntity>> C4BOMB_ENTITY =
            ENTITIES.register("c4bomb_entity", () -> EntityType.Builder.of(C4BombEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f) // 定义碰撞箱大小
                    .build("c4bomb_entity"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
