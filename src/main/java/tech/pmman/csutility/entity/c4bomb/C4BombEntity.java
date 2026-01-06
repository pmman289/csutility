package tech.pmman.csutility.entity.c4bomb;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.entity.SyncDataEntity;
import tech.pmman.csutility.network.packet.c4bomb.C4BombEventPacket;
import tech.pmman.csutility.network.packet.c4bomb.C4BombEventType;
import tech.pmman.csutility.util.MinecraftTool;

import javax.annotation.Nullable;
import java.util.Objects;

public class C4BombEntity extends Entity implements SyncDataEntity {
    /* ---------------- Synced Data ---------------- */

    public static final EntityDataAccessor<Integer> BOMB_COUNTDOWN =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Integer> DEFUSE_COUNTDOWN =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<String> DEFUSING_PLAYER_UUID =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.STRING);

    // 炸弹第一次放置的时间
    public static final EntityDataAccessor<Long> BOMB_PLANTED_TICK_TIME =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.LONG);

    // 实体数据是否同步完毕
    public static final EntityDataAccessor<Boolean> IS_READY =
            SynchedEntityData.defineId(C4BombEntity.class, EntityDataSerializers.BOOLEAN);

    public Long getBombPlantedTickTime() {
        return entityData.get(BOMB_PLANTED_TICK_TIME);
    }

    private void setBombPlantedTickTime(long time) {
        entityData.set(BOMB_PLANTED_TICK_TIME, time);
    }

    public String getDefusingPlayerUUID() {
        return entityData.get(DEFUSING_PLAYER_UUID);
    }

    @Override
    public boolean isReady() {
        return entityData.get(IS_READY);
    }

    private void syncReady() {
        entityData.set(IS_READY, true);
    }
    /* ---------------- Client Data ----------------- */

    /* ---------------- Server State ---------------- */

    private ServerPlayer defusingPlayer;

    private int readyBoomCount = (int) (1.3 * 20);
    private boolean canDefuse = true;
    // 延时销毁标记
    private boolean willDestroy = false;
    // 延迟5ticks销毁
    private int delayDestroyTick = 5;

    private void setDefusingPlayer(@Nullable ServerPlayer defusingPlayer) {
        this.defusingPlayer = defusingPlayer;
        // 设置同步参数
        if (defusingPlayer != null) {
            entityData.set(DEFUSING_PLAYER_UUID, defusingPlayer.getStringUUID());
            // 通知该玩家正在拆除炸弹
            PacketDistributor.sendToPlayer(defusingPlayer, new C4BombEventPacket(getId(), C4BombEventType.BOMB_DEFUSING_BY_ME));
        } else {
            entityData.set(DEFUSING_PLAYER_UUID, "");
        }
    }

    public C4BombEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BOMB_COUNTDOWN, 40 * 20);
        builder.define(DEFUSE_COUNTDOWN, 10 * 20);
        builder.define(DEFUSING_PLAYER_UUID, "");
        builder.define(BOMB_PLANTED_TICK_TIME, -1L);
        builder.define(IS_READY, false);
    }

    /* ---------------- Interaction ---------------- */

    @Override
    // 允许实体被点击
    public boolean isPickable() {
        return !this.isRemoved(); // 只有存活的实体才能被点击
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (MinecraftTool.isServerSide(level()) && defusingPlayer == null && canDefuse) {
            setDefusingPlayer((ServerPlayer) player);
            broadcastDefuseStartSound();
        }
        return InteractionResult.CONSUME;
    }

    /* ---------------- Tick ---------------- */
    @Override
    public void tick() {
        super.tick();

        // 现在这里只有服务端逻辑
        if (level().isClientSide()) {
            return;
        }
        // 延迟销毁逻辑
        if (willDestroy) {
            if (delayDestroyTick-- < 0) {
                discard();
            }
            return;
        }

        tickDefuse();

        int fuse = entityData.get(BOMB_COUNTDOWN) - 1;
        entityData.set(BOMB_COUNTDOWN, fuse);

        if (fuse < 0) {
            canDefuse = false;

            // 只有第一次执行到这里时会播放
            if (readyBoomCount-- == (int) (1.3 * 20)) {
                broadcastReadyBoomSound();
            }

            if (readyBoomCount <= 0) {
                explode();
            }
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // 不执行客户端逻辑
        if (level().isClientSide()) {
            return;
        }
        // 服务端当时间未设置时设置时间
        if (getBombPlantedTickTime() == -1L) {
            setBombPlantedTickTime(level().getGameTime());
        }

        // 标记数据同步完成
        syncReady();
    }

    /* ---------------- Defuse Logic ---------------- */

    private void tickDefuse() {
        if (!canDefuse || defusingPlayer == null) return;

        int count = entityData.get(DEFUSE_COUNTDOWN);

        if (!isPlayerLookingAtMe(defusingPlayer) || Objects.requireNonNull(
                level().getServer()).getPlayerList().getPlayer(defusingPlayer.getUUID()) != defusingPlayer
        ) {
            resetDefuse();
            return;
        }

        if (count <= 0) {
            defuse();
            return;
        }

        entityData.set(DEFUSE_COUNTDOWN, count - 1);
    }

    private void defuse() {
        // 通知客户端炸弹已拆除
        PacketDistributor.sendToPlayersTrackingEntity(this, new C4BombEventPacket(getId(), C4BombEventType.BOMB_DEFUSED));
        destroy();
    }

    /* ---------------- Explosion ---------------- */

    private void explode() {
        level().explode(this, getX(), getY(), getZ(), 40.0F, Level.ExplosionInteraction.NONE);
        destroy();
    }

    /* ---------------- Cleanup ---------------- */

    private void destroy() {
        // 这里仅标记将要销毁，在tick里做延迟销毁
        willDestroy = true;
    }

    private void resetDefuse() {
        entityData.set(DEFUSE_COUNTDOWN, 10 * 20);
        setDefusingPlayer(null);
    }

    /* ---------------- Utilities ---------------- */

    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).scale(3.0);
        return ProjectileUtil.getEntityHitResult(
                level(), player, eye, eye.add(look),
                getBoundingBox().inflate(1.0),
                e -> e == this
        ) != null;
    }

    private void broadcastDefuseStartSound() {
        level().playSound(null, blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 1f, 1.2f);
    }

    private void broadcastReadyBoomSound() {
        level().playSound(null, blockPosition(), ModSounds.C4BOMB_READYBOOM.get(), SoundSource.BLOCKS, 2f, 1f);
    }

    /* ---------------- Save ---------------- */

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("fuse", entityData.get(BOMB_COUNTDOWN));
        tag.putLong("bombPlantedTickTime", entityData.get(BOMB_PLANTED_TICK_TIME));
        tag.putBoolean("willDestroy", willDestroy);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("fuse")) {
            entityData.set(BOMB_COUNTDOWN, tag.getInt("fuse"));
        }
        if (tag.contains("bombPlantedTickTime")) {
            entityData.set(BOMB_PLANTED_TICK_TIME, tag.getLong("bombPlantedTickTime"));
        }
        if (tag.contains("willDestroy")) {
            willDestroy = tag.getBoolean("willDestroy");
        }
    }
}
