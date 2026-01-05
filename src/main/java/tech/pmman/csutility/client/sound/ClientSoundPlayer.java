package tech.pmman.csutility.client.sound;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.client.C4DefusedRegisterClientSound;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

@OnlyIn(Dist.CLIENT)
public class ClientSoundPlayer {
    public static void registerDefusedSound(C4BombEntity entity){
        Minecraft.getInstance().getSoundManager()
                .play(new C4DefusedRegisterClientSound(entity));
    }

    public static void playBombBeepSoundOnClient(C4BombEntity entity){
        SyncedSoundInstance syncedBombBeepSound = new SyncedSoundInstance(ModSounds.C4BOMB_BEEP.get(),
                entity.position(),
                entity.getBombPlantedTickTime(),
                1.0f,
                60f,
                false
                );
        Minecraft.getInstance().getSoundManager()
                .play(syncedBombBeepSound);
    }
}
