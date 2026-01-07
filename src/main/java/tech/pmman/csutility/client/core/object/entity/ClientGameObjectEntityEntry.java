package tech.pmman.csutility.client.core.object.entity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.core.gameObject.GameObjectWithEntity;
import tech.pmman.csutility.util.MinecraftTool;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CSUtility.MODID, value = Dist.CLIENT)
public class ClientGameObjectEntityEntry {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册映射关系
        ClientGameObjectEntityFactory.register();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (MinecraftTool.isServerSide(event.getLevel())) return;
        // 注册到实体runtime
        GameObjectWithEntity controller = ClientGameObjectEntityFactory.createController(event.getEntity());
        if (controller != null) {
            ClientGameObjectEntityRuntime.add(event.getEntity().getId(), controller);
        }
    }

    @SubscribeEvent
    public static void onClientLevelTick(ClientTickEvent.Post event) {
        ClientGameObjectEntityRuntime.tickAll();
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (MinecraftTool.isServerSide(event.getLevel())) return;
        ClientGameObjectEntityRuntime.remove(event.getEntity().getId());
    }

    @SubscribeEvent
    // 退出时清理现场
    public static void onUniversalUnload(LevelEvent.Unload event) {
        // 必须是客户端层面的卸载
        if (!event.getLevel().isClientSide()) return;
        ClientGameObjectEntityRuntime.clear();
    }
}
