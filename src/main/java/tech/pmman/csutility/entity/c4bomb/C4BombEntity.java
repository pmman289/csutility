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
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.client.sound.ClientSoundPlayer;
import tech.pmman.csutility.util.LevelTool;

public class C4BombEntity extends Entity {
    private Player defusingPlayer;
    private ServerBossEvent defuseProgressBossBar;

    private boolean isStartedBeep = false;

    // 等待爆炸的计数器，此时已经无法拆弹
    private int readyBoomCount = (int) (1.3 * 20);
    private boolean canDefuse = true;

    // 用来记录是否是正常拆除以判断是否播放拆除音效
    private boolean isDefused = false;

    // 使用 SyncedData 确保服务端修改倒计时，客户端能同步看到闪烁等效果
    private static final EntityDataAccessor<Integer> BOMB_COUNTDOWN =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOMB_DEFUSE_COUNT =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.INT);
    // 记录炸弹的安放时间
    private static final EntityDataAccessor<Long> BOMB_PLANTED_TICK_TIME =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.LONG);

    public C4BombEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        // 禁用重力
        this.noPhysics = true;
    }

    public long getBombPlantedTickTime() {
        return entityData.get(BOMB_PLANTED_TICK_TIME);
    }

    public boolean isDefused() {
        return isDefused;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 40秒倒计时
        builder.define(BOMB_COUNTDOWN, 40 * 20);
        // 10秒拆除时间
        builder.define(BOMB_DEFUSE_COUNT, 10 * 20);
        // 记录炸弹安放时间
        builder.define(BOMB_PLANTED_TICK_TIME, level().getGameTime());
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


    private void playReadyBoomSound(){
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.C4BOMB_READYBOOM.get(),
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (LevelTool.isServerSide(level()) && defusingPlayer == null) {
            defusingPlayer = player;
            playDefuseSound();
        }
        return InteractionResult.CONSUME;
    }

    /**
     * 判断是否即将被拆除
     * @return 结果
     */
    private boolean willDefused(){
        return entityData.get(BOMB_DEFUSE_COUNT) <= 0;
    }

    @Override
    public void tick() {
        super.tick();
        // 客户端播放音频逻辑
        if (level().isClientSide()){
            if (!isStartedBeep) {
                ClientSoundPlayer.playBombBeepSoundOnClient(this);
                // 注册拆除音效，等待实体销毁后播放
                ClientSoundPlayer.registerDefusedSound(this);
                isStartedBeep = true;
            }
        }
        if (LevelTool.isServerSide(level())) {
            tickDefuseProgress();

            int currentFuse = this.entityData.get(BOMB_COUNTDOWN) - 1;
            this.entityData.set(BOMB_COUNTDOWN, currentFuse);
            if (currentFuse < 0) {
                // 如果计数器归零，设置不允许拆弹
                if (canDefuse){
                    canDefuse = false;
                    // 播放最终音效
                    playReadyBoomSound();
                }
                // 开始计算即将爆炸计数器，此时只能等待爆炸
                readyBoomCount--;
                if (readyBoomCount < 0){
                    // 爆炸并销毁实体
                    explore();
                }
            }
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
    }

    private void explore() {
        // 参数：来源, X, Y, Z, 威力, 是否破坏方块
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 20.0F,
                Level.ExplosionInteraction.NONE);
        destroy();
    }

    private void defuseBomb() {
        isDefused = true;
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
        if (canDefuse && defusingPlayer != null) {
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
                // 检查c4是否可以拆除
                if (willDefused()) {
                    // 拆除炸弹
                    defuseBomb();
                }
                // 减少拆除计数
                entityData.set(BOMB_DEFUSE_COUNT, currentDefuseCount - 1);
            } else {
                // 销毁进度条
                if (defuseProgressBossBar != null) {
                    defuseProgressBossBar.removeAllPlayers();
                    defuseProgressBossBar = null;
                }
                resetDefuseProgress();
                defusingPlayer = null;
            }
        }else if (!canDefuse && defuseProgressBossBar != null){
            // 归零进度，销毁进度条
            defuseProgressBossBar.removeAllPlayers();
            defuseProgressBossBar = null;
            resetDefuseProgress();
            defusingPlayer = null;
        }
    }

    private void resetDefuseProgress() {
        entityData.set(BOMB_DEFUSE_COUNT, 10 * 20);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Fuse")) this.entityData.set(BOMB_COUNTDOWN, compound.getInt("Fuse"));
        if (compound.contains("bombPlantedTickTime")) this.entityData.set(BOMB_PLANTED_TICK_TIME,
                compound.getLong("bombPlantedTickTime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Fuse", this.entityData.get(BOMB_COUNTDOWN));
        compound.putLong("bombPlantedTickTime", entityData.get(BOMB_PLANTED_TICK_TIME));
    }
}
