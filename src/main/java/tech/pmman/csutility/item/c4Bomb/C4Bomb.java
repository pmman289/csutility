package tech.pmman.csutility.item.c4Bomb;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.entity.ModEntities;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;
import tech.pmman.csutility.util.MinecraftTool;

public class C4Bomb extends Item {
    private static final ResourceLocation C4_FREEZE_ID =
            ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4_freeze");

    public C4Bomb(Properties properties) {
        super(properties);
    }

    private void removeFreeze(LivingEntity entity) {
        var speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(C4_FREEZE_ID);
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        // 3秒安放时间
        return 60;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    private void playBombPlantingSound(Player player){
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.C4BOMB_PLANTING.get(),
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player,
                                                           @NotNull InteractionHand usedHand) {
        player.startUsingItem(usedHand);
        // 播放c4安装中声音
        playBombPlantingSound(player);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (livingEntity instanceof Player player) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && !speed.hasModifier(C4_FREEZE_ID)) {
                // 创建一个减速 100% 的修改器
                AttributeModifier freeze = new AttributeModifier(
                        C4_FREEZE_ID,
                        -1.0, // 减少 100%
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                );
                speed.addTransientModifier(freeze);
            }
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level,
                             @NotNull LivingEntity livingEntity, int timeCharged) {
        if (MinecraftTool.isServerSide(level)){
            removeFreeze(livingEntity);
            // 广播停止播放音频包
            // 1. 获取声音的 ResourceLocation
            ResourceLocation soundLocation = ModSounds.C4BOMB_PLANTING.getId();

            // 2. 创建停止声音的数据包
            // 参数：声音 ID, 声音分类 (必须和播放时一致，比如 SoundSource.PLAYERS 或 BLOCKS)
            ClientboundStopSoundPacket stopPacket = new ClientboundStopSoundPacket(soundLocation, SoundSource.BLOCKS);

            // 3. 广播给附近的所有玩家
            // 找到实体坐标附近的所有玩家并发送包
            double radius = 16.0; // 停止半径，建议与声音传播范围一致
            level.getEntitiesOfClass(ServerPlayer.class, livingEntity.getBoundingBox().inflate(radius)).forEach(
                    player -> player.connection.send(stopPacket));
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level,
                                              @NotNull LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            // 1. 获取玩家脚下的精确坐标
            // player.getX(), player.getY(), player.getZ()
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            // 2. 实例化你的 C4 实体
            // 这里的 ModEntities.C4_BOMB.get() 是你之前在 Registry 里注册的 EntityType
            C4BombEntity c4 = new C4BombEntity(ModEntities.C4BOMB_ENTITY.get(), level);

            // 3. 设置实体位置
            c4.setPos(x, y, z);

            // 4. 将实体安放到世界中
            // 这相当于把对象交给 Level 的管理引擎，随后它的 tick() 方法就会开始跑
            level.addFreshEntity(c4);

            // 消耗物品
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // 移除定身效果
            removeFreeze(player);
        }
        return stack;
    }
}
