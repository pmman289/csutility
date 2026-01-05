package tech.pmman.csutility.mixin.client.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tech.pmman.csutility.client.sound.IChannelExtension;
import tech.pmman.csutility.client.sound.SyncedSoundInstance;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {

    @Redirect(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"
            )
    )
    private void syncedSound$wrapPlay(
            ChannelAccess.ChannelHandle handle,
            Consumer<Channel> original,
            SoundInstance instance
    ) {
        Consumer<Channel> wrapped = channel -> {
            original.accept(channel);

            if (instance instanceof SyncedSoundInstance synced) {

                synced.setChannel(channel);

                IChannelExtension ext = (IChannelExtension) channel;

                // 设置基础音量
                ext.syncedSound$setBaseVolume(instance.getVolume());

                // 设置 3D 衰减模型
                ext.syncedSound$setupAttenuation(synced.getMaxDistance());

                // 计算时间偏移
                assert Minecraft.getInstance().level != null;
                long worldTick = Minecraft.getInstance().level.getGameTime();
                long diff = worldTick - synced.getStartTick();

                if (diff > 0) {
                    float offset = diff / 20.0f;
                    ext.syncedSound$seek(offset);
                }
            }
        };

        handle.execute(wrapped);
    }

    /**
     * 强制 SyncedSoundInstance 允许 stream
     * 但 static 仍然兜底
     */
    @Redirect(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            at = @At(
                    value = "INVOKE",
                    // 注意看这里的包名：增加了 .client.resources
                    target = "Lnet/minecraft/client/resources/sounds/Sound;shouldStream()Z"
            )
    )
    private boolean syncedSound$forceStream(Sound sound, SoundInstance instance) {
        return sound.shouldStream() || instance instanceof SyncedSoundInstance;
    }
}

