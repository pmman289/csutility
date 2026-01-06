package tech.pmman.csutility.network.packet.c4bomb;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum C4BombEventType {
    BOMB_DEFUSING_BY_ME,
    BOMB_DEFUSED;

    public static final StreamCodec<ByteBuf, C4BombEventType> STREAM_CODEC = ByteBufCodecs.idMapper(id -> values()[id], C4BombEventType::ordinal);
}
