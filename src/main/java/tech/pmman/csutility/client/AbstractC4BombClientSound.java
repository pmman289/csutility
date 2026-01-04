package tech.pmman.csutility.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

public abstract class AbstractC4BombClientSound extends AbstractTickableSoundInstance {
    protected final C4BombEntity c4;
    protected final LocalPlayer player;

    public AbstractC4BombClientSound(C4BombEntity c4, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.c4 = c4;
        this.player = Minecraft.getInstance().player;

        this.looping = false;
        this.delay = 0;
        this.volume = 0.001f;     // 初始为 0.001靠 tick 算
        this.pitch = 1.0f;
        this.attenuation = SoundInstance.Attenuation.NONE; // 自己算距离
    }

    /**
     * 返回声音的最大距离
     * @return 声音传播的最大距离
     */
    abstract float getMaxDist();

    @Override
    public boolean canPlaySound() {
        // 确保实体还在，且没有被标记为移除
        return c4 != null && !c4.isRemoved();
    }

    @Override
    public void tick() {
        if (!canPlaySound()) {
            this.stop();
            return;
        }

        // 同步 C4 世界坐标
        this.x = c4.getX();
        this.y = c4.getY();
        this.z = c4.getZ();

        // 计算距离
        double dist = player.distanceTo(c4);

        if (dist > getMaxDist()) {
            this.volume = 0f;
        } else {
            float v = 1.0f - (float)(dist / getMaxDist());
            this.volume = Mth.clamp(v, 0f, 1f);
        }
    }
}
