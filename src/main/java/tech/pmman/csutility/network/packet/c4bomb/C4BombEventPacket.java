package tech.pmman.csutility.network.packet.c4bomb;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tech.pmman.csutility.CSUtility;
import tech.pmman.csutility.network.core.PacketWithEntityId;

@Data
public class C4BombEventPacket implements CustomPacketPayload, PacketWithEntityId {
    public int entityId;
    public C4BombEventType eventType;

    public static final CustomPacketPayload.Type<C4BombEventPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    CSUtility.MODID, "c4bomb_event_packet"
            ));
    public static final StreamCodec<ByteBuf, C4BombEventPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C4BombEventPacket::getEntityId,
            C4BombEventType.STREAM_CODEC, C4BombEventPacket::getEventType,
            C4BombEventPacket::new
    );

    public C4BombEventPacket() {
    }

    public C4BombEventPacket(int entityId, C4BombEventType eventType) {
        this.entityId = entityId;
        this.eventType = eventType;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
