package tech.pmman.csutility.client.vision;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.item.c4Bomb.C4Bomb;

@EventBusSubscriber(value = Dist.CLIENT, modid = CSUtility.MODID)
public class FovFixerListener {
    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        // 如果玩家正在安放c4，取消fov变化
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof C4Bomb) {
            event.setNewFovModifier(1.0f);
        }
    }
}
