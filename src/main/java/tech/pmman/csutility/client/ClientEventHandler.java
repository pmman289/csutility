package tech.pmman.csutility.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.client.entity.ClientC4BombController;
import tech.pmman.csutility.client.gui.DefuseGuiRenderer;

@EventBusSubscriber(modid = CSUtility.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onLevelTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) {
            return;
        }
        ClientC4BombController.onClientTick(level);
    }

    @SubscribeEvent
    // 注册ui
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4bomb_defusing_progress"),
                (guiGraphics, partialTick) -> DefuseGuiRenderer.render(guiGraphics)
        );
    }

    @SubscribeEvent
    // 退出时清理现场
    public static void onUniversalUnload(LevelEvent.Unload event) {
        // 关必须是客户端层面的卸载
        if (event.getLevel().isClientSide()) {
            ClientC4BombController.clear();
        }
    }
}
