package tech.pmman.csutility.client.core.object.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.client.core.object.BaseGameObject;
import tech.pmman.csutility.core.object.entity.ServerEntity;
import tech.pmman.csutility.core.object.network.PacketWithGameObjectId;

import java.util.*;


@OnlyIn(Dist.CLIENT)
public class ClientEntityRuntime {
    // 这里存放等待数据加载就绪的controller
    public static final Int2ObjectMap<ClientEntity> CLIENT_OBJECT_ENTITY_WAIT_READY_MAP =
            new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<ClientEntity> CLIENT_OBJECT_ENTITY_MAP =
            new Int2ObjectOpenHashMap<>();

    // 存放待处理的网络包
    public static final Int2ObjectMap<Deque<PacketWithGameObjectId>> PENDING_PACKET_QUEUE = new Int2ObjectOpenHashMap<>();
    // 网络包队列最大容量
    private static final int MAX_PENDING_PACKET = 32;

    /**
     * 注册控制器
     *
     * @param entityId             实体id
     * @param gameObjectWithEntity 包含实体的游戏对象
     */
    public static void add(int entityId, ClientEntity gameObjectWithEntity) {
        // 只添加到等待map，然后在tick里检测是否ready
        if (gameObjectWithEntity.getEntity() instanceof ServerEntity) {
            CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.put(gameObjectWithEntity.getEntity().getId(), gameObjectWithEntity);
        } else {
            CLIENT_OBJECT_ENTITY_MAP.put(entityId, gameObjectWithEntity);
        }
    }

    /**
     * 注销指定控制器
     *
     * @param entityId 实体id
     */
    public static void remove(int entityId) {
        ClientEntity gameObjectWithEntity = CLIENT_OBJECT_ENTITY_MAP.remove(entityId);
        if (gameObjectWithEntity != null) {
            gameObjectWithEntity.afterRemoved();
        }
    }

    private static void checkWaitMap() {
        // 检测待添加的是否数据同步完毕
        HashSet<Integer> preRemoveSet = new HashSet<>();
        CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.forEach((entityId, clientEntity) -> {
            if (clientEntity.getEntity().isReady()) {
                CLIENT_OBJECT_ENTITY_MAP.put(clientEntity.getEntity().getId(), clientEntity);
                clientEntity.init();
                // 补充处理所有pending的网络包
                flushPacketQueue(entityId, clientEntity);
                preRemoveSet.add(entityId);
            }
        });
        preRemoveSet.forEach(c -> CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.remove((int) c));
    }

    private static void checkEntityAlive() {
        HashSet<Integer> mainRemoveSet = new HashSet<>();
        CLIENT_OBJECT_ENTITY_MAP.values().forEach(gameObjectWithEntity -> {
            // 检测实体是否已被释放
            if (gameObjectWithEntity.getEntity().isRemoved()) {
                mainRemoveSet.add(gameObjectWithEntity.getEntity().getId());
                gameObjectWithEntity.afterRemoved();
            }
        });
        mainRemoveSet.forEach(c -> CLIENT_OBJECT_ENTITY_MAP.remove((int) c));
        // 这里重新遍历再调用tick是因为如果在上面tick可能触发ConcurrentModificationException
        CLIENT_OBJECT_ENTITY_MAP.values().forEach(BaseGameObject::tick);
    }

    /**
     * 执行所有控制器的tick方法
     */
    public static void tickAll() {
        // 检测等待同步完成的map，把同步完成的实体移动到就绪map
        checkWaitMap();
        // 检测实体是否还存活，未存活的实体进行移除
        checkEntityAlive();
    }

    private static void joinPendingPacket(PacketWithGameObjectId packet) {
        // 如果wait里找到了就把包加入pending队列，没有就放弃本次处理
        if (CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.containsKey(packet.getGameObjectId())) {
            Deque<PacketWithGameObjectId> packetQueue = PENDING_PACKET_QUEUE
                    .computeIfAbsent(packet.getGameObjectId(), k -> new ArrayDeque<>());
            // 如果当前队列大于最大容量，弹出队首对象再插入
            if (packetQueue.size() >= MAX_PENDING_PACKET) {
                packetQueue.pollFirst();
            }
            packetQueue.addLast(packet);
        }
    }

    public static void dispatchPacket(final PacketWithGameObjectId packet) {
        ClientEntity gameObjectWithEntity = CLIENT_OBJECT_ENTITY_MAP.get(packet.getGameObjectId());
        if (gameObjectWithEntity != null) {
            gameObjectWithEntity.handlePacket((CustomPacketPayload) packet);
        } else {
            // 将该包放入等待队列
            joinPendingPacket(packet);
        }
    }

    /**
     * 清理容器
     */
    public static void clear() {
        CLIENT_OBJECT_ENTITY_MAP.values().forEach(ClientEntity::afterRemoved);
        CLIENT_OBJECT_ENTITY_MAP.clear();
        CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.values().forEach(ClientEntity::afterRemoved);
        CLIENT_OBJECT_ENTITY_WAIT_READY_MAP.clear();
        PENDING_PACKET_QUEUE.clear();
    }

    private static void flushPacketQueue(int entityId, ClientEntity gameObjectWithEntity) {
        Deque<PacketWithGameObjectId> queue = PENDING_PACKET_QUEUE.remove(entityId);
        if (queue == null) return;

        while (!queue.isEmpty()) {
            PacketWithGameObjectId packet = queue.pollFirst();
            gameObjectWithEntity.handlePacket((CustomPacketPayload) packet);
        }
    }
}
