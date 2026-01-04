package tech.pmman.csutility.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

@OnlyIn(Dist.CLIENT)
public class ClientSoundPlayer {
    public static void registerDefusedSound(C4BombEntity entity){
        Minecraft.getInstance().getSoundManager()
                .play(new C4DefusedRegisterClientSound(entity));
    }

    public static void playBombBeepSoundOnClient(C4BombEntity entity){
        Minecraft.getInstance().getSoundManager()
                .play(new C4BeepClientSound(entity));
    }
}
