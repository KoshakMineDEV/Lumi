package cn.nukkit.network.process.processor.common;

import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.player.PlayerAnimationEvent;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.SwingSource;
import org.jetbrains.annotations.NotNull;

/**
 * @author SocialMoods
 */
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

        PlayerAnimationEvent animationEvent = new PlayerAnimationEvent(handle.player, packet.action);
        handle.callEvent(animationEvent);
        if (animationEvent.isCancelled()) return;

        AnimatePacket.Action animation = animationEvent.getAnimationType();

        switch (animation) {
            case ROW_RIGHT:
            case ROW_LEFT:
                if (handle.getRiding() instanceof EntityBoat boat) {
                    if (handle.getProtocol() >= 407 /*v1_21_130*/) {
                        boat.onPaddle(animation, 1);
                    } else {
                        boat.onPaddle(animation, packet.rowingTime);
                    }
                }
                break;
        }

        if (animation == AnimatePacket.Action.SWING_ARM) {
            handle.player.setNoShieldTicks(NO_SHIELD_DELAY);
        }

        packet.eid = handle.getId();
        packet.action = animation;
        packet.swingSource = SwingSource.EVENT;

        Server.broadcastPacket(handle.player.getViewers().values(), packet);
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
