package tech.pmman.csutility.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import tech.pmman.csutility.CSUtility;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CSUtility.MODID, value = Dist.CLIENT)
public class GuiEntry {
    @SubscribeEvent
    // 注册ui
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4bomb_defusing_progress"),
                (guiGraphics, partialTick) -> DefuseGuiRenderer.render(guiGraphics)
        );
    }
}
