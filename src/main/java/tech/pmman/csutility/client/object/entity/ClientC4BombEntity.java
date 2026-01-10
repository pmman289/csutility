package tech.pmman.csutility.client.object.entity;

import lombok.Getter;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.client.core.object.entity.ClientEntityRuntimeApi;
import tech.pmman.csutility.client.sound.ClientSoundPlayer;
import tech.pmman.csutility.client.sound.SyncedSoundInstance;
import tech.pmman.csutility.client.core.object.entity.ClientEntity;
import tech.pmman.csutility.core.object.entity.ServerEntity;
import tech.pmman.csutility.object.entity.c4bomb.ServerC4BombEntity;
import tech.pmman.csutility.network.packet.c4bomb.C4BombEventPacket;
import tech.pmman.csutility.util.MinecraftTool;

@OnlyIn(Dist.CLIENT)
public final class ClientC4BombEntity implements ClientEntity {
    @Getter
    // 标记正在被我拆除的炸弹
    private static ServerC4BombEntity bombDefusingByMe;

    private ServerC4BombEntity entity;
    private SyncedSoundInstance bombPlantedSoundInstance;

    public ClientC4BombEntity(ServerC4BombEntity entity) {
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
        if (bombDefusingByMe != null && entity == bombDefusingByMe) {
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
                    // 这里可以标记自己为废弃了
                    ClientEntityRuntimeApi.unregister(this);
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
    public ServerEntity getEntity() {
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

