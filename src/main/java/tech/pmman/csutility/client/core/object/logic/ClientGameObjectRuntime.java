package tech.pmman.csutility.client.core.object.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.core.gameObject.GameObject;
import tech.pmman.csutility.core.gameObject.network.PacketWithGameObjectId;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

@OnlyIn(Dist.CLIENT)
public class ClientGameObjectRuntime {
    private static final Int2ObjectMap<GameObject> CLIENT_GAME_OBJECT_MAP =
            new Int2ObjectOpenHashMap<>();

    // 存放待处理的网络包
    public static final Int2ObjectMap<Deque<PacketWithGameObjectId>> PENDING_PACKET_QUEUE = new Int2ObjectOpenHashMap<>();
    // 网络包队列最大容量
    private static final int MAX_PENDING_PACKET = 32;

    /**
     * 注册控制器
     *
     * @param gameObjectId 实体id
     * @param gameObject   游戏对象
     */
    public static void add(int gameObjectId, GameObject gameObject) {
        // 添加到列表
        CLIENT_GAME_OBJECT_MAP.put(gameObjectId, gameObject);
        // 尝试处理未处理的包
        flushPacketQueue(gameObjectId, gameObject);
    }

    /**
     * 注销指定控制器
     *
     * @param entityId 实体id
     */
    public static void remove(int entityId) {
        GameObject gameObject = CLIENT_GAME_OBJECT_MAP.remove(entityId);
        if (gameObject != null) {
            gameObject.afterRemoved();
        }
    }

    private static void checkEntityAlive() {
        HashSet<Integer> mainRemoveSet = new HashSet<>();
        CLIENT_GAME_OBJECT_MAP.values().forEach(gameObject -> {
            // 检测实体是否已被释放
            if (gameObject.isRemoved()) {
                mainRemoveSet.add(gameObject.getId());
                gameObject.afterRemoved();
                return;
            }
            gameObject.tick();
        });
        mainRemoveSet.forEach(c -> CLIENT_GAME_OBJECT_MAP.remove((int) c));
    }

    /**
     * 执行所有控制器的tick方法
     */
    public static void tickAll() {
        checkEntityAlive();
    }

    private static void joinPendingPacket(PacketWithGameObjectId packet) {
        Deque<PacketWithGameObjectId> packetQueue = PENDING_PACKET_QUEUE
                .computeIfAbsent(packet.getGameObjectId(), k -> new ArrayDeque<>());
        // 如果当前队列大于最大容量，弹出队首对象再插入
        if (packetQueue.size() >= MAX_PENDING_PACKET) {
            packetQueue.pollFirst();
        }
        packetQueue.addLast(packet);
    }

    public static void dispatchPacket(final PacketWithGameObjectId packet) {
        GameObject gameObject = CLIENT_GAME_OBJECT_MAP.get(packet.getGameObjectId());
        if (gameObject != null) {
            gameObject.handlePacket((CustomPacketPayload) packet);
        } else {
            // 将该包放入等待队列
            joinPendingPacket(packet);
        }
    }

    /**
     * 清理容器
     */
    public static void clear() {
        CLIENT_GAME_OBJECT_MAP.values().forEach(GameObject::afterRemoved);
        CLIENT_GAME_OBJECT_MAP.clear();
        PENDING_PACKET_QUEUE.clear();
    }

    private static void flushPacketQueue(int gameObjectId, GameObject gameObject) {
        Deque<PacketWithGameObjectId> queue = PENDING_PACKET_QUEUE.remove(gameObjectId);
        if (queue == null) return;

        while (!queue.isEmpty()) {
            PacketWithGameObjectId packet = queue.pollFirst();
            gameObject.handlePacket((CustomPacketPayload) packet);
        }
    }
}
