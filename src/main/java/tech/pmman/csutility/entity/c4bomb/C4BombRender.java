package tech.pmman.csutility.entity.c4bomb;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class C4BombRender extends EntityRenderer<C4BombEntity> {
    // 临时方案：直接引用原版 TNT 的模型进行缩放
    private final BlockRenderDispatcher blockRenderer;

    public C4BombRender(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@NotNull C4BombEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 将模型缩小，看起来像个 C4 块
        poseStack.scale(0.5f, 0.2f, 0.3f);
        poseStack.translate(-0.5, 0, -0.5); // 居中

        // 渲染一个原版 TNT 方块作为占位符
        this.blockRenderer.renderSingleBlock(Blocks.TNT.defaultBlockState(), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull C4BombEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
