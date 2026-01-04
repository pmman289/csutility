package tech.pmman.csutility.entity.c4bomb;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class C4BombEntity extends Entity {
    private Player defusingPlayer;
    private ServerBossEvent defuseProgressBossBar;

    // 使用 SyncedData 确保服务端修改倒计时，客户端能同步看到闪烁等效果
    private static final EntityDataAccessor<Integer> BOMB_COUNTDOWN =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOMB_DEFUSE_COUNT =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.INT);

    public C4BombEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        // 禁用重力
        this.noPhysics = true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 5秒倒计时
        builder.define(BOMB_COUNTDOWN, 40 * 20);
        // 10秒拆除时间
        builder.define(BOMB_DEFUSE_COUNT, 10 * 20);
    }

    private void playTickSound(int currentFuse) {
        // 播放滴滴声
        int interval;
        if (currentFuse > 20 * 20) interval = 20; // 每秒1次
        else if (currentFuse > 10 * 20) interval = 10; // 每秒2次
        else if (currentFuse > 5 * 20) interval = 5; // 每秒2次
        else interval = 2; // 每秒10次
        if (currentFuse % interval == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS,
                    1.0f,
                    2.0f
            );
        }
    }

    private void initBossBar() {
        this.defuseProgressBossBar = new ServerBossEvent(
                Component.literal("正在拆除 C4..."), // 标题
                BossEvent.BossBarColor.YELLOW,      // 颜色（拆除中用黄色或蓝色）
                BossEvent.BossBarOverlay.PROGRESS   // 样式
        );
    }

    private void playDefuseSound() {
        // 1. 播放金属碰撞声 (模拟工具到位)
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);

        // 2. 叠加一个短促的电子音 (模拟系统接入)
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 0.8f, 2.0f);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (isServerSide() && defusingPlayer == null) {
            defusingPlayer = player;
            playDefuseSound();
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
        super.tick();
        if (isServerSide()) {
            tickDefuseProgress();

            int currentFuse = this.entityData.get(BOMB_COUNTDOWN) - 1;
            this.entityData.set(BOMB_COUNTDOWN, currentFuse);
            if (currentFuse >= 0) {
                playTickSound(currentFuse);
            } else {
                // 爆炸并销毁实体
                explore();
            }
        }
    }

    private void explore() {
        // 参数：来源, X, Y, Z, 威力, 是否破坏方块
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 20.0F,
                Level.ExplosionInteraction.NONE);
        destroy();
    }

    private void defuseBomb() {
        // 播放拆除音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 1.0f, 1.0f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 0.5f);

        destroy();
    }

    // 所有销毁都要调用此方法
    private void destroy(){
        if (defuseProgressBossBar != null) {
            defuseProgressBossBar.removeAllPlayers();
            defuseProgressBossBar = null;
        }
        discard();
    }

    private boolean isServerSide() {
        return !this.level().isClientSide();
    }

    private boolean isPlayerLookingAtMe(Player player) {
        double reachDistance = 3.0D; // 交互距离
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 endPosition = eyePosition.add(viewVector.scale(reachDistance));

        // 创建一个包含检测范围的 AABB
        AABB checkArea = player.getBoundingBox().expandTowards(viewVector.scale(reachDistance)).inflate(1.0D);

        // 调用 NeoForge/Minecraft 提供的实体检测工具
        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eyePosition,
                endPosition,
                checkArea,
                (target) -> target == this // 只要目标是我自己
        );

        return entityHit != null && entityHit.getEntity() == this;
    }

    private void tickDefuseProgress() {
        // 检查拆除情况
        if (defusingPlayer != null) {
            int currentDefuseCount = entityData.get(BOMB_DEFUSE_COUNT);
            // 展示拆除进度条
            if (defusingPlayer instanceof ServerPlayer serverPlayer) {
                if (defuseProgressBossBar == null) {
                    initBossBar();
                    defuseProgressBossBar.addPlayer(serverPlayer);
                }
                // 更新进度条
                float bossBarProgress = Math.min(currentDefuseCount / 200f, 1.0f);
                defuseProgressBossBar.setProgress(bossBarProgress);
            }
            // 检查玩家是否还在看向c4
            if (isPlayerLookingAtMe(defusingPlayer)) {
                // 减少拆除计数
                entityData.set(BOMB_DEFUSE_COUNT, currentDefuseCount - 1);
                // 检查c4是否可以拆除
                if (currentDefuseCount <= 0) {
                    // 销毁实体
                    defuseBomb();
                }
            } else {
                // 销毁进度条
                defuseProgressBossBar.removeAllPlayers();
                defuseProgressBossBar = null;
                resetDefuseProgress();
                defusingPlayer = null;
            }
        }
    }

    private void resetDefuseProgress() {
        entityData.set(BOMB_DEFUSE_COUNT, 10 * 20);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Fuse")) this.entityData.set(BOMB_COUNTDOWN, compound.getInt("Fuse"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Fuse", this.entityData.get(BOMB_COUNTDOWN));
    }
}
