package tech.pmman.csutility.core.object.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import tech.pmman.csutility.CSUtility;

public class GameObjectCreatePacket implements CustomPacketPayload, PacketWithGameObjectId {
    @Getter
    private int gameObjectId;
    @Getter
    private GameObjectType gameObjectType;

    public GameObjectCreatePacket(int gameObjectId, GameObjectType gameObjectType) {
        this.gameObjectId = gameObjectId;
        this.gameObjectType = gameObjectType;
    }

    public static final CustomPacketPayload.Type<GameObjectCreatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    CSUtility.MODID, "game_object_create_packet"
            ));
    public static final StreamCodec<ByteBuf, GameObjectCreatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, GameObjectCreatePacket::getGameObjectId,
            GameObjectType.STREAM_CODEC, GameObjectCreatePacket::getGameObjectType,
            GameObjectCreatePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
