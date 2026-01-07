package tech.pmman.csutility.client.object.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tech.pmman.csutility.item.ModItems;
import tech.pmman.csutility.object.entity.c4bomb.ServerC4BombEntity;

public class C4BombRender extends EntityRenderer<ServerC4BombEntity> {

    public C4BombRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull ServerC4BombEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. 调整位置（根据 JSON 模型的原点进行微调）
        poseStack.translate(0.0D, 0.25D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));

        // 2. 获取 ItemRenderer
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 3. 直接获取注册/加载的模型
        ItemStack stack = new ItemStack(ModItems.C4BOMB.get());
        BakedModel bakedModel = itemRenderer.getModel(stack, entity.level(), null, entity.getId());

        // 4. 执行渲染
        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedModel);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ServerC4BombEntity entity) {
        // 当使用 BakedModel 时，贴图通常由模型文件内部指定，这里返回默认即可
        return InventoryMenu.BLOCK_ATLAS;
    }
}
