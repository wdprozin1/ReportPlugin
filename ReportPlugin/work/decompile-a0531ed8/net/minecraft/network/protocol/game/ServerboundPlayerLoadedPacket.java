package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundPlayerLoadedPacket() implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<ByteBuf, ServerboundPlayerLoadedPacket> STREAM_CODEC = StreamCodec.unit(new ServerboundPlayerLoadedPacket());

    @Override
    public PacketType<ServerboundPlayerLoadedPacket> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_LOADED;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleAcceptPlayerLoad(this);
    }
}
