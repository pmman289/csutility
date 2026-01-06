package tech.pmman.csutility.client.entity;

import lombok.Getter;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.client.core.ClientController;
import tech.pmman.csutility.client.sound.ClientSoundPlayer;
import tech.pmman.csutility.client.sound.SyncedSoundInstance;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;
import tech.pmman.csutility.network.packet.c4bomb.C4BombEventPacket;
import tech.pmman.csutility.util.MinecraftTool;

public final class ClientC4BombController implements ClientController {
    @Getter
    // 标记正在被我拆除的炸弹
    private static C4BombEntity bombDefusingByMe;

    private C4BombEntity entity;
    private SyncedSoundInstance bombPlantedSoundInstance;

    public ClientC4BombController(C4BombEntity entity) {
        this.entity = entity;
    }

    @Override
    public void init() {
        playBombPlantedSound();
    }

    @Override
    public void tick() {
        String stringUUID = MinecraftTool.getLocalPlayerStringUUID();
        // 检测是否是本地玩家在拆除炸弹，否则移除
        if (bombDefusingByMe != null) {
            if (stringUUID != null && !stringUUID.equals(entity.getDefusingPlayerUUID())) {
                bombDefusingByMe = null;
            }
        }
    }

    @Override
    public void handlePacket(CustomPacketPayload packet) {
        if (packet instanceof C4BombEventPacket eventPacket) {
            switch (eventPacket.getEventType()) {
                case BOMB_DEFUSING_BY_ME -> bombDefusingByMe = entity;
                case BOMB_DEFUSED -> {
                    // 先停止播放beep音效
                    stopPlayBombPlantedSound();
                    playBombDefusedSound();
                }
            }
        }
    }

    @Override
    public void afterRemoved() {
        if (bombPlantedSoundInstance != null) bombPlantedSoundInstance.stopPlay();
        entity = null;
        bombDefusingByMe = null;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    private void playBombPlantedSound() {
        bombPlantedSoundInstance = ClientSoundPlayer.playSyncedSound(ModSounds.C4BOMB_PLANTED_TO_READY_BOOM.get(),
                entity.position(), entity.getBombPlantedTickTime(), 1f, 60f, false);
    }

    private void playBombDefusedSound() {
        ClientSoundPlayer.playNormalSound(ModSounds.C4BOMB_DEFUSED_AND_CTWIN.get(), entity.position(),
                SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void stopPlayBombPlantedSound() {
        bombPlantedSoundInstance.stopPlay();
    }
}

