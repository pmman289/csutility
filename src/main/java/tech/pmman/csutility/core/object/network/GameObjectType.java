package tech.pmman.csutility.core.object.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum GameObjectType {
    CS_SHOP;
    public static final StreamCodec<ByteBuf, GameObjectType> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> values()[id], GameObjectType::ordinal);
}
