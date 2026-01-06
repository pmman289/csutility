package tech.pmman.csutility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    // 创建注册表
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CSUtility.MODID);

    // 注册声音事件
    public static final DeferredHolder<SoundEvent, SoundEvent> C4BOMB_PLANTED_TO_READY_BOOM =
            SOUNDS.register("c4bomb_beep", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4bomb_beep")
            ));
    public static final DeferredHolder<SoundEvent, SoundEvent> C4BOMB_DEFUSED_AND_CTWIN =
            SOUNDS.register("c4bomb_defused_and_ctwin", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4bomb_defused_and_ctwin")
            ));
    public static final DeferredHolder<SoundEvent, SoundEvent> C4BOMB_PLANTING =
            SOUNDS.register("c4bomb_planting", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4bomb_planting")
            ));
    public static final DeferredHolder<SoundEvent, SoundEvent> C4BOMB_READYBOOM =
            SOUNDS.register("c4bomb_readyboom", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CSUtility.MODID, "c4bomb_readyboom")
            ));

    public static void register(IEventBus eventBus){
        SOUNDS.register(eventBus);
    }
}
