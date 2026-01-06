package tech.pmman.csutility.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class ClientSoundPlayer {
    /**
     * 播放需要同步的音频
     *
     * @param sound       音频
     * @param pos         位置
     * @param startTick   开始时间
     * @param volume      音量
     * @param maxDistance 最大距离
     * @param isLoop      是否循环
     * @return 音频对象，用于控制音频停止播放
     */
    public static SyncedSoundInstance playSyncedSound(SoundEvent sound, Vec3 pos, long startTick, float volume,
                                                      float maxDistance, boolean isLoop) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        SyncedSoundInstance soundInstance = new SyncedSoundInstance(sound, pos, startTick, volume, maxDistance, isLoop);
        soundManager.play(soundInstance);
        return soundInstance;
    }

    /**
     * 播放普通声音
     *
     * @param sound      音频
     * @param pos        位置
     * @param sourceType 声音来源类型
     * @param volume     音量
     * @param pitch      音调
     */
    public static void playNormalSound(SoundEvent sound, Vec3 pos, SoundSource sourceType, float volume, float pitch) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        level.playLocalSound(
                BlockPos.containing(pos),
                sound,
                sourceType,
                volume, pitch, false
        );
    }
}
