package tech.pmman.csutility.core.object.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class ServerEntity extends Entity {
    // 实体数据是否同步完毕
    private static final EntityDataAccessor<Boolean> IS_READY =
            SynchedEntityData.defineId(ServerEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    // 重写时请先调用此方法
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_READY, false);
    }

    public ServerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public boolean isReady() {
        return entityData.get(IS_READY);
    }

    private void syncReady() {
        entityData.set(IS_READY, true);
    }

    @Override
    public final void onAddedToLevel() {
        // 这里只有服务端逻辑
        if (level().isClientSide()) return;
        super.onAddedToLevel();
        onAddedToServerLevel();
        // 标记同步完成
        syncReady();
    }

    /**
     * 当实体被加载到level时调用
     */
    protected abstract void onAddedToServerLevel();

    @Override
    public final void tick() {
        // 这里只有服务端逻辑
        if (level().isClientSide()) return;
        super.tick();
        serverTick();
    }

    protected abstract void serverTick();
}
