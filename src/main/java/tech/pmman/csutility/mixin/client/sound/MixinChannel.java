package tech.pmman.csutility.mixin.client.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.sounds.AudioStream;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.pmman.csutility.client.sound.IChannelExtension;

import javax.sound.sampled.AudioFormat;
import java.util.OptionalInt;
import java.util.function.IntConsumer;

@OnlyIn(Dist.CLIENT)
@Mixin(Channel.class)
public class MixinChannel implements IChannelExtension {
    @Final
    @Shadow
    private int source;

    /** seek 延迟缓存（buffer attach 后再执行） */
    @Unique
    private float syncedSound$seekSecond = 0f;

    /** SoundInstance 的基础音量 */
    @Unique private float syncedSound$baseVolume = 1.0f;

    /** 最大听距 */
    @Unique private float syncedSound$maxDistance = 16f;

    @Override
    public void syncedSound$seek(float second) {
        syncedSound$seekSecond = second;
    }

    @Override
    public void syncedSound$setBaseVolume(float volume) {
        syncedSound$baseVolume = volume;
    }

    @Override
    public void syncedSound$setupAttenuation(float maxDistance) {
        this.syncedSound$maxDistance = maxDistance;
    }

    // 覆写声音衰减
    @Inject(method = "linearAttenuation", at = @At("HEAD"), cancellable = true)
    public void syncedSound$linearAttenuation(CallbackInfo ci) {
        AL10.alSourcei(source, AL10.AL_DISTANCE_MODEL, AL10.AL_INVERSE_DISTANCE);
        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, syncedSound$maxDistance * syncedSound$baseVolume);
        AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 10f);
        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 10f * syncedSound$baseVolume);
        ci.cancel();
    }

    /* ================= Stream 音频 ================= */

    @Inject(method = "attachBufferStream", at = @At("HEAD"))
    private void synced$onAttachStream(AudioStream stream, CallbackInfo ci) {
        if (syncedSound$seekSecond <= 0) return;

        try {
            AudioFormat format = stream.getFormat();

            int bytesToSkip = (int) (
                    format.getSampleRate()
                            * format.getChannels()
                            * (format.getSampleSizeInBits() / 8f)
                            * syncedSound$seekSecond
            );

            stream.read(bytesToSkip);

            AL11.alSourcef(source, AL11.AL_SEC_OFFSET, syncedSound$seekSecond);
            System.out.println("skipped: " + syncedSound$seekSecond);
        } catch (Exception e) {
            throw new RuntimeException("Audio seek failed", e);
        }

        syncedSound$seekSecond = 0;
    }

    /* ================= Static 音频 ================= */

    @Redirect(
            method = "attachStaticBuffer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/OptionalInt;ifPresent(Ljava/util/function/IntConsumer;)V"
            )
    )
    private void synced$onAttachStatic(
            OptionalInt buffer,
            IntConsumer consumer
    ) {
        IntConsumer wrapped = id -> {
            AL10.alSourcei(source, AL10.AL_BUFFER, id);

            if (syncedSound$seekSecond > 0) {
                AL11.alSourcef(source, AL11.AL_SEC_OFFSET, syncedSound$seekSecond);
                System.out.println("skipped: " + syncedSound$seekSecond);
                syncedSound$seekSecond = 0;
            }
        };

        buffer.ifPresent(wrapped);
    }
}
