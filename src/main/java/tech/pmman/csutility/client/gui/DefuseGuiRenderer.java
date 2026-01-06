package tech.pmman.csutility.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.client.entity.ClientC4BombController;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

@OnlyIn(Dist.CLIENT)
public class DefuseGuiRenderer {
    public static void render(GuiGraphics guiGraphics) {
        C4BombEntity c4 = ClientC4BombController.getBombDefusingByMe();
        if (c4 == null) return;

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // 10秒(200tick) 倒计时进度计算
        float countdown = c4.getEntityData().get(C4BombEntity.DEFUSE_COUNTDOWN);
        float progress = 1.0f - (countdown / 200.0f);

        int barWidth = 120;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight / 2 + 30;

        // 绘制背景
        guiGraphics.fill(x - 2, y - 2, x + barWidth + 2, y + 6, 0x99000000);
        // 绘制进度条 (亮黄色)
        guiGraphics.fill(x, y, x + (int)(barWidth * progress), y + 4, 0xFFFFFF00);

        // 绘制文字
        String label = "正在拆除中...";
        guiGraphics.drawString(Minecraft.getInstance().font, label,
                (screenWidth - Minecraft.getInstance().font.width(label)) / 2, y - 12, 0xFFFFFFFF);
    }
}
