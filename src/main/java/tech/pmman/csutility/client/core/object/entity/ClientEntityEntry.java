package tech.pmman.csutility.client.core.object.entity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.util.MinecraftTool;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CSUtility.MODID, value = Dist.CLIENT)
public class ClientEntityEntry {
    private static long LAST_TICK_TIME = 0;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册映射关系
        ClientEntityFactory.register();
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (MinecraftTool.isServerSide(event.getLevel())) return;
        // 注册到实体runtime
        ClientEntity controller = ClientEntityFactory.createController(event.getEntity());
        if (controller != null) {
            ClientEntityRuntime.add(event.getEntity().getId(), controller);
        }
    }

    @SubscribeEvent
    public static void onClientLevelTick(LevelTickEvent.Post event) {
        if (MinecraftTool.isServerSide(event.getLevel())) return;
        // 保证每tick只执行一次
        if (LAST_TICK_TIME == MinecraftTool.getLocalGameTime()) return;
        ClientEntityRuntime.tickAll();
        // 更新时间
        LAST_TICK_TIME = MinecraftTool.getLocalGameTime();
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (MinecraftTool.isServerSide(event.getLevel())) return;
        ClientEntityRuntime.remove(event.getEntity().getId());
    }

    @SubscribeEvent
    // 退出时清理现场
    public static void onUniversalUnload(LevelEvent.Unload event) {
        // 必须是客户端层面的卸载
        if (!event.getLevel().isClientSide()) return;
        ClientEntityRuntime.clear();
    }
}
