package tech.pmman.csutility.client.object.entity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.object.entity.ModEntities;

@EventBusSubscriber(modid = CSUtility.MODID, value = Dist.CLIENT)
public class EntityRegisterListener {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 将 C4 实体类型与 C4 渲染器绑定
        event.registerEntityRenderer(ModEntities.C4BOMB_ENTITY.get(), C4BombRender::new);
    }
}
