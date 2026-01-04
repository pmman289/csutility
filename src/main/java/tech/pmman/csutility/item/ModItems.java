package tech.pmman.csutility.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tech.pmman.csutility.CSUtility;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CSUtility.MODID);

    public static final DeferredItem<C4Bomb> C4BOMB = ITEMS.register("c4bomb",
            () -> new C4Bomb(new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
