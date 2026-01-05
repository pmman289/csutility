package tech.pmman.csutility.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import tech.pmman.csutility.ModSounds;
import tech.pmman.csutility.client.sound.SyncedSoundInstance;
import tech.pmman.csutility.entity.c4bomb.C4BombEntity;

import java.lang.ref.WeakReference;
import java.util.*;

public final class ClientC4BombController {
    // 存放实体弱引用集合
    private static final Set<C4BombEntity> ENTITY_SET = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<C4BombEntity, SyncedSoundInstance> BEEP_SOUND_MAP = new WeakHashMap<>();
    private static WeakReference<C4BombEntity> currentDefusingBombByMe = new WeakReference<>(null);

    public static C4BombEntity getCurrentDefusingBombByMe() {
        return currentDefusingBombByMe.get();
    }

    /**
     * 实体应当主动调用此方法将自己注册进来
     *
     * @param entity 实体
     */
    public static void register(C4BombEntity entity) {
        if (ENTITY_SET.stream()
                .filter(e -> e.getStringUUID().equals(entity.getStringUUID()))
                .findFirst()
                .isEmpty()
        ) {
            ENTITY_SET.add(entity);
        }
    }

    public static void onClientTick(Level level) {
        if (Minecraft.getInstance().player == null) {
            // 当本地玩家对象为null时，结束tick
            return;
        }
        HashSet<C4BombEntity> removeSet = new HashSet<>();
        // 开始时先清空拆除中的炸弹对象
        currentDefusingBombByMe = new WeakReference<>(null);
        for (C4BombEntity entity : ENTITY_SET) {
            // -----拆弹进度条处理-----
            if (entity.getDefusingPlayerUUID().equals(Minecraft.getInstance().player.getStringUUID())) {
                currentDefusingBombByMe = new WeakReference<>(entity);
            }

            // -----声音处理-----
            // 被拆除的炸弹停止播放beep，播放拆除音效
            if (entity.isDefused()) {
                stopPlayBeep(entity);
                playDefusedSound(entity);
                // 标记移除实体
                removeSet.add(entity);
            } else if (!BEEP_SOUND_MAP.containsKey(entity) && entity.getBombPlantedTickTime() != -1) {
                // 未播放beep声的开始播放
                startPlayBeep(entity);
            } else if (entity.isRemoved()) {
                // 如果实体被移除则也停止播放
                stopPlayBeep(entity);
                // 标记移除实体
                removeSet.add(entity);
            }
        }
        // 移除实体
        ENTITY_SET.removeAll(removeSet);
    }

    public static void startPlayBeep(C4BombEntity entity) {
        SyncedSoundInstance syncedBombBeepSound = new SyncedSoundInstance(ModSounds.C4BOMB_BEEP.get(),
                entity.position(),
                entity.getBombPlantedTickTime(),
                1.0f,
                60f,
                false
        );
        Minecraft.getInstance().getSoundManager()
                .play(syncedBombBeepSound);
        // 加入map
        BEEP_SOUND_MAP.put(entity, syncedBombBeepSound);
    }

    public static void stopPlayBeep(C4BombEntity entity) {
        SyncedSoundInstance s = BEEP_SOUND_MAP.remove(entity);
        if (s != null) s.stopPlay();
    }

    private static void playDefusedSound(C4BombEntity bomb) {
        assert Minecraft.getInstance().level != null;
        Minecraft.getInstance().level.playLocalSound(
                bomb.getX(), bomb.getY(), bomb.getZ(),
                ModSounds.C4BOMB_DEFUSED_AND_CTWIN.get(),
                SoundSource.BLOCKS,
                1f, 1f, false
        );
    }

    public static void clear(){
        ENTITY_SET.clear();
        BEEP_SOUND_MAP.clear();
        currentDefusingBombByMe.clear();
    }
}

