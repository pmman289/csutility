package tech.pmman.csutility.client.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class SyncedSoundInstance extends AbstractSoundInstance {
    private final long startTick;
    private final float maxDistance;

    private Channel channel;

    public SyncedSoundInstance(SoundEvent soundEvent,
                                  Vec3 pos,
                                  long startTick,
                                  float volume,
                                  float maxDistance,
                               boolean isLoop
                               ) {
        super(soundEvent, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.startTick = startTick;
        this.volume = volume;
        this.maxDistance = maxDistance;

        this.looping = isLoop;
        this.relative = false;

        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public long getStartTick() {
        return startTick;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void stopPlay(){
        channel.stop();
    }

    // 关闭 MC 默认的距离衰减
    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.NONE;
    }
}
