package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;

public record ClientboundEntityPositionSyncPacket(int id, PositionMoveRotation values, boolean onGround) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, ClientboundEntityPositionSyncPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundEntityPositionSyncPacket::id, PositionMoveRotation.STREAM_CODEC, ClientboundEntityPositionSyncPacket::values, ByteBufCodecs.BOOL, ClientboundEntityPositionSyncPacket::onGround, ClientboundEntityPositionSyncPacket::new);

    public static ClientboundEntityPositionSyncPacket of(Entity entity) {
        return new ClientboundEntityPositionSyncPacket(entity.getId(), new PositionMoveRotation(entity.trackingPosition(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()), entity.onGround());
    }

    @Override
    public PacketType<ClientboundEntityPositionSyncPacket> type() {
        return GamePacketTypes.CLIENTBOUND_ENTITY_POSITION_SYNC;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleEntityPositionSync(this);
    }
}
