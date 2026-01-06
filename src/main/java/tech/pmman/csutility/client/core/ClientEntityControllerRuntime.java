package tech.pmman.csutility.client.core;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import tech.pmman.csutility.entity.SyncDataEntity;
import tech.pmman.csutility.network.core.PacketWithEntityId;

import java.util.*;


@OnlyIn(Dist.CLIENT)
public class ClientEntityControllerRuntime {
    // 这里存放等待数据加载就绪的controller
    private static final Int2ObjectMap<ClientController> CLIENT_CONTROLLER_WAIT_READY_MAP =
            new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<ClientController> CLIENT_CONTROLLER_MAP =
            new Int2ObjectOpenHashMap<>();

    // 存放待处理的网络包
    private static final Int2ObjectMap<Deque<PacketWithEntityId>> PENDING_PACKET_QUEUE = new Int2ObjectOpenHashMap<>();

    /**
     * 注册控制器
     *
     * @param entityId   实体id
     * @param controller 控制器
     */
    public static void add(int entityId, ClientController controller) {
        // 只添加到等待map，然后在tick里检测是否ready
        if (controller.getEntity() instanceof SyncDataEntity) {
            CLIENT_CONTROLLER_WAIT_READY_MAP.put(controller.getEntity().getId(), controller);
        } else {
            CLIENT_CONTROLLER_MAP.put(entityId, controller);
        }
    }

    /**
     * 注销指定控制器
     *
     * @param entityId 实体id
     */
    public static void remove(int entityId) {
        ClientController controller = CLIENT_CONTROLLER_MAP.remove(entityId);
        if (controller != null) {
            controller.afterRemoved();
        }
    }

    /**
     * 执行所有控制器的tick方法
     */
    public static void tickAll() {
        // 检测待添加的是否数据同步完毕
        HashSet<Integer> preRemoveSet = new HashSet<>();
        CLIENT_CONTROLLER_WAIT_READY_MAP.forEach((entityId, controller) -> {
            if (((SyncDataEntity) controller.getEntity()).isReady()) {
                CLIENT_CONTROLLER_MAP.put(controller.getEntity().getId(), controller);
                controller.init();
                // 补充处理所有pending网络包
                flushPacketQueue(entityId, controller);
                preRemoveSet.add(entityId);
            }
        });
        preRemoveSet.forEach(c -> CLIENT_CONTROLLER_WAIT_READY_MAP.remove((int) c));

        HashSet<Integer> mainRemoveSet = new HashSet<>();
        CLIENT_CONTROLLER_MAP.values().forEach(controller -> {
            // 检测实体是否已被释放
            if (controller.getEntity().isRemoved()) {
                mainRemoveSet.add(controller.getEntity().getId());
                controller.afterRemoved();
                return;
            }
            controller.tick();
        });
        mainRemoveSet.forEach(c -> CLIENT_CONTROLLER_MAP.remove((int) c));
    }

    public static void dispatchPacket(final PacketWithEntityId packet) {
        ClientController targetController = CLIENT_CONTROLLER_MAP.get(packet.getEntityId());
        // 如果找不到再去wait里找
        if (targetController != null) {
            targetController.handlePacket((CustomPacketPayload) packet);
        } else {
            // 如果wait里找到了就把包加入pending队列，没有就放弃本次处理
            if (CLIENT_CONTROLLER_WAIT_READY_MAP.containsKey(packet.getEntityId())) {
                PENDING_PACKET_QUEUE.computeIfAbsent(packet.getEntityId(), k -> new ArrayDeque<>()).addLast(packet);
            }
        }
    }

    /**
     * 清理容器
     */
    public static void clear() {
        CLIENT_CONTROLLER_MAP.values().forEach(ClientController::afterRemoved);
        CLIENT_CONTROLLER_MAP.clear();
        CLIENT_CONTROLLER_WAIT_READY_MAP.values().forEach(ClientController::afterRemoved);
        CLIENT_CONTROLLER_WAIT_READY_MAP.clear();
        PENDING_PACKET_QUEUE.clear();
    }

    private static void flushPacketQueue(int entityId, ClientController controller) {
        Deque<PacketWithEntityId> queue = PENDING_PACKET_QUEUE.remove(entityId);
        if (queue == null) return;

        while (!queue.isEmpty()) {
            PacketWithEntityId packet = queue.pollFirst();
            controller.handlePacket((CustomPacketPayload) packet);
        }
    }
}
