package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.Server;
import org.jetbrains.annotations.NotNull;

public class MovePlayerProcessor extends DataPacketProcessor<MovePlayerPacket> {

    public static final MovePlayerProcessor INSTANCE = new MovePlayerProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull MovePlayerPacket packet) {
        Player player = handle.player;
        Server server = player.getServer();

        int protocol = handle.getProtocol();
        if (protocol > ProtocolInfo.v1_21_90) {
            handle.close("", "Client sent invalid packet");
            return;
        }

        if (handle.getTeleportPosition() != null
                || !handle.isSpawned()
                || handle.isMovementServerAuthoritative()
                || handle.isLockMovementInput()) {
            return;
        }

        Vector3 newPos = new Vector3(
                packet.x,
                packet.y - handle.getBaseOffset(),
                packet.z
        );

        double dis = newPos.distanceSquared(player);

        if (dis == 0
                && packet.yaw % 360 == player.yaw
                && packet.pitch % 360 == player.pitch) {
            return;
        }

        if (handle.getLastTeleportTick() + 10 > server.getTick()
                && newPos.distance(
                        handle.getTemporalVector()
                                .setComponents(
                                        handle.getLastX(),
                                        handle.getLastY(),
                                        handle.getLastZ()
                                )
                ) < 5) {
            return;
        }

        if (dis > 100) {
            if (handle.getLastTeleportTick() + 30 < server.getTick()) {
                handle.sendPosition(
                        player,
                        packet.yaw,
                        packet.pitch,
                        MovePlayerPacket.MODE_RESET
                );
            }
            return;
        }

        boolean revert = false;
        if (!handle.isAlive() || !handle.isSpawned()) {
            revert = true;
            handle.setForceMovement(player);
        }

        if (handle.getForceMovement() != null
                && (newPos.distanceSquared(handle.getForceMovement()) > 0.1 || revert)) {

            handle.sendPosition(
                    handle.getForceMovement(),
                    packet.yaw,
                    packet.pitch,
                    MovePlayerPacket.MODE_RESET
            );
            return;
        }

        // normalize rotations
        packet.yaw %= 360;
        packet.headYaw %= 360;
        packet.pitch %= 360;

        if (packet.yaw < 0) packet.yaw += 360;
        if (packet.headYaw < 0) packet.headYaw += 360;

        handle.setRotation(packet.yaw, packet.pitch, packet.headYaw);
        handle.setNewPosition(newPos);
        handle.setForceMovement(null);
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.MOVE_PLAYER_PACKET;
    }

    @Override
    public Class<MovePlayerPacket> getPacketClass() {
        return MovePlayerPacket.class;
    }

}
