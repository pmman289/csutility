package tech.pmman.csutility.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

@OnlyIn(Dist.CLIENT)
public class C4BeepClientSound extends AbstractC4BombClientSound {


    public C4BeepClientSound(C4BombEntity c4) {
        super(c4, ModSounds.C4BOMB_BEEP.get());
    }

    @Override
    float getMaxDist() {
        return 60f;
    }
}

