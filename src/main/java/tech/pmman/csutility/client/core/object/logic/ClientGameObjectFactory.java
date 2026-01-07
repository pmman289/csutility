package tech.pmman.csutility.client.core.object.logic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.core.gameObject.GameObject;
import tech.pmman.csutility.network.packet.gameObject.GameObjectType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ClientGameObjectFactory {
    private static final Map<GameObjectType, Function<GameObjectType, GameObject>> REGISTRY =
            new HashMap<>();
    private static boolean IS_REGISTERED = false;

    /**
     * 在这个方法中注册控制器创建方法
     */
    private static void register() {
        IS_REGISTERED = true;
    }

    /**
     * 在这个方法中根据类型新建返回对应的gameObject
     *
     * @param gameObjectType 实体实例
     * @return 对应的gameObject对象
     */
    public static GameObject createGameObject(GameObjectType gameObjectType) {
        // 如果还未注册，则注册
        if (!IS_REGISTERED) {
            register();
        }

        Function<GameObjectType, GameObject> controllerCreator = REGISTRY.get(gameObjectType);
        if (controllerCreator != null) {
            return controllerCreator.apply(gameObjectType);
        }
        // 兜底返回
        return null;
    }
}
