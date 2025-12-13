package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.Server;
import org.jetbrains.annotations.NotNull;

/**
 * @author SocialMoods
 */
public class MovePlayerProcessor extends DataPacketProcessor<MovePlayerPacket> {

    public static final MovePlayerProcessor INSTANCE = new MovePlayerProcessor();

    @Override
    public void handle(@NotNull PlayerHandle h, @NotNull MovePlayerPacket pk) {
        Player p = h.player;
        Server server = p.getServer();

        int protocol = h.getProtocol();
        if (protocol > ProtocolInfo.v1_21_90) {
            h.close("", "Client sent invalid packet");
            return;
        }

        if (h.getTeleportPosition() != null
                || !h.isSpawned()
                || h.isMovementServerAuthoritative()
                || h.isLockMovementInput()) {
            return;
        }

        Vector3 newPos = new Vector3(
                pk.x,
                pk.y - h.getBaseOffset(),
                pk.z
        );

        double dis = newPos.distanceSquared(p);

        if (dis == 0
                && pk.yaw % 360 == p.yaw
                && pk.pitch % 360 == p.pitch) {
            return;
        }

        if (h.getLastTeleportTick() + 10 > server.getTick()
                && newPos.distance(
                        h.getTemporalVector()
                                .setComponents(
                                        h.getLastX(),
                                        h.getLastY(),
                                        h.getLastZ()
                                )
                ) < 5) {
            return;
        }

        if (dis > 100) {
            if (h.getLastTeleportTick() + 30 < server.getTick()) {
                h.sendPosition(
                        p,
                        pk.yaw,
                        pk.pitch,
                        MovePlayerPacket.MODE_RESET
                );
            }
            return;
        }

        boolean revert = false;
        if (!h.isAlive() || !h.isSpawned()) {
            revert = true;
            h.setForceMovement(p);
        }

        if (h.getForceMovement() != null
                && (newPos.distanceSquared(h.getForceMovement()) > 0.1 || revert)) {

            h.sendPosition(
                    h.getForceMovement(),
                    pk.yaw,
                    pk.pitch,
                    MovePlayerPacket.MODE_RESET
            );
            return;
        }

        // normalize rotations
        pk.yaw %= 360;
        pk.headYaw %= 360;
        pk.pitch %= 360;

        if (pk.yaw < 0) pk.yaw += 360;
        if (pk.headYaw < 0) pk.headYaw += 360;

        h.setRotation(pk.yaw, pk.pitch, pk.headYaw);
        h.setNewPosition(newPos);
        h.offerClientMovement(newPos);
        h.setForceMovement(null);
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
