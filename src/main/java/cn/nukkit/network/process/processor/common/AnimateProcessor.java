package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.player.PlayerAnimationEvent;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.SwingSource;
import org.jetbrains.annotations.NotNull;

public class AnimateProcessor extends DataPacketProcessor<AnimatePacket> {

    public static final AnimateProcessor INSTANCE = new AnimateProcessor();

    private static final int NO_SHIELD_DELAY = 10;

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull AnimatePacket packet) {
        if (!handle.isSpawned() || !handle.isAlive()) {
            return;
        }

        if (packet.action == null
                || packet.action == AnimatePacket.Action.WAKE_UP
                || packet.action == AnimatePacket.Action.CRITICAL_HIT
                || packet.action == AnimatePacket.Action.MAGIC_CRITICAL_HIT) {
            return;
        }

        PlayerAnimationEvent event = new PlayerAnimationEvent(handle.player, packet.action);
        if (!event.call()) return;

        AnimatePacket.Action action = event.getAnimationType();

        switch (action) {
            case ROW_RIGHT:
            case ROW_LEFT:
                if (handle.getRiding() instanceof EntityBoat boat) {
                    if (handle.getProtocol() >= ProtocolInfo.v1_21_130) {
                        boat.onPaddle(action, 1);
                    } else {
                        boat.onPaddle(action, packet.rowingTime);
                    }
                }
                break;
        }

        if (action == AnimatePacket.Action.SWING_ARM) {
            handle.player.setNoShieldTicks(NO_SHIELD_DELAY);
        }

        packet.eid = handle.getId();
        packet.action = action;
        packet.swingSource = SwingSource.EVENT;

        for (Player player : handle.player.getViewers().values()) {
            // Skip row actions from lower version players to higher version viewers (causes unknown error)
            if (player.protocol >= ProtocolInfo.v1_21_130 && handle.player.protocol < ProtocolInfo.v1_21_130
                    && (action == AnimatePacket.Action.ROW_RIGHT || action == AnimatePacket.Action.ROW_LEFT)) {
                continue;
            }
            player.dataPacket(packet);
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.ANIMATE_PACKET;
    }

    @Override
    public Class<? extends AnimatePacket> getPacketClass() {
        return AnimatePacket.class;
    }
}
