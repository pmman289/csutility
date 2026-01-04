package tech.pmman.csutility.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

@OnlyIn(Dist.CLIENT)
public class C4DefusedRegisterClientSound extends AbstractC4BombClientSound{
    private boolean isPlayed = false;

    public C4DefusedRegisterClientSound(C4BombEntity c4) {
        // 先占位不播放
        super(c4, SoundEvents.ALLAY_THROW);
        // 一直播放占位
        this.looping = true;
    }

    @Override
    float getMaxDist() {
        return 32f;
    }

    @Override
    public boolean canPlaySound() {
        // 不检查c4是否销毁，因为下面要在移除后发出声音
        return player != null;
    }

    @Override
    public void tick() {
        // 正常拆除才播放
        if (c4.isRemoved() && c4.isDefused()){
            // 直到实体被移除后再开始播放
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;

            if (level != null && !isPlayed) {
                level.playLocalSound(
                        x, y, z,                               // 声音坐标
                        ModSounds.C4BOMB_DEFUSED_AND_CTWIN.get(),              // SoundEvent
                        SoundSource.BLOCKS,                   // 声音类别
                        1.0F,                                 // 音量
                        1.0F,                                 // 音调
                        false                                 // 是否延迟
                );
                // 防止重复播放
                isPlayed = true;
            }
            this.stop();
        }else {
            // 同步 C4 世界坐标
            this.x = c4.getX();
            this.y = c4.getY();
            this.z = c4.getZ();
        }
    }
}
