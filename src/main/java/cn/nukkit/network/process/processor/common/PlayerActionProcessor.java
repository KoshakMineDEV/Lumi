package cn.nukkit.network.process.processor.common;

import cn.nukkit.AdventureSettings;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemSpear;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.jetbrains.annotations.NotNull;

public class PlayerActionProcessor extends DataPacketProcessor<PlayerActionPacket> {

    public static PlayerActionProcessor INSTANCE = new PlayerActionProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull PlayerActionPacket packet) {
        if (!handle.isSpawned() || (!handle.isAlive() && packet.action != PlayerActionPacket.ACTION_RESPAWN)) {
            return;
        }

        Vector3 pos = new Vector3(packet.x, packet.y, packet.z);
        BlockFace face = BlockFace.fromIndex(packet.face);

        switch (packet.action) {
            case PlayerActionPacket.ACTION_START_BREAK -> {
                if (!handle.isServerAuthoritativeBlockBreaking()) {
                    handle.onBlockBreakStart(pos, face);
                }
            }

            case PlayerActionPacket.ACTION_ABORT_BREAK, PlayerActionPacket.ACTION_STOP_BREAK -> {
                if (!handle.isServerAuthoritativeBlockBreaking()) {
                    handle.onBlockBreakAbort(pos, face);
                }
            }

            case PlayerActionPacket.ACTION_STOP_SLEEPING -> {
                handle.stopSleep();
            }

            case PlayerActionPacket.ACTION_RESPAWN -> {
                if (!handle.isSpawned() || handle.isAlive() || !handle.isOnline()) {
                    return;
                }
                handle.respawn();
            }

            case PlayerActionPacket.ACTION_JUMP -> {
                if (handle.isMovementServerAuthoritative() || handle.isLockMovementInput()) {
                    return;
                }

                if (handle.getInAirTicks() > 40
                        && handle.isCheckMovement()
                        && !handle.checkFlightAllowed()
                        && !handle.isCreative()
                        && !handle.isSwimming()
                        && !handle.isGliding()) {

                    handle.kick(
                            PlayerKickEvent.Reason.FLYING_DISABLED,
                            "Flying is not enabled on this server",
                            true,
                            "type=ACTION_JUMP, inAirTicks=" + handle.getInAirTicks()
                    );
                    return;
                }
                new PlayerJumpEvent(handle.player).call();
            }

            case PlayerActionPacket.ACTION_START_SPRINT -> {
                toggleSprint(handle, true);
            }

            case PlayerActionPacket.ACTION_STOP_SPRINT -> {
                toggleSprint(handle, false);
            }

            case PlayerActionPacket.ACTION_START_SNEAK -> {
                toggleSneak(handle, true);
            }

            case PlayerActionPacket.ACTION_STOP_SNEAK -> {
                toggleSneak(handle, false);
            }

            case PlayerActionPacket.ACTION_CONTINUE_BREAK -> {
                if (!handle.isServerAuthoritativeBlockBreaking()) {
                    handle.onBlockBreakContinue(pos, face);
                }
            }

            case PlayerActionPacket.ACTION_START_SWIMMING -> {
                toggleSwim(handle, true);
            }

            case PlayerActionPacket.ACTION_STOP_SWIMMING -> {
                toggleSwim(handle, false);
            }

            case PlayerActionPacket.ACTION_START_GLIDE -> {
                toggleGlide(handle, true);
            }

            case PlayerActionPacket.ACTION_STOP_GLIDE -> {
                toggleGlide(handle, false);
            }

            case PlayerActionPacket.ACTION_START_CRAWLING -> {
                toggleCrawl(handle, true);
            }

            case PlayerActionPacket.ACTION_STOP_CRAWLING -> {
                toggleCrawl(handle, false);
            }

            case PlayerActionPacket.ACTION_START_FLYING -> {
                toggleFlight(handle, true);
            }

            case PlayerActionPacket.ACTION_STOP_FLYING -> {
                toggleFlight(handle, false);
            }

            case PlayerActionPacket.ACTION_MISSED_SWING -> {
                if (handle.isMovementServerAuthoritative() || handle.getProtocol() < ProtocolInfo.v1_20_10_21) {
                    return;
                }

                PlayerMissedSwingEvent evt = new PlayerMissedSwingEvent(handle.player);
                if (evt.call()) {
                    handle.player.level.addSound(handle.player, Sound.GAME_PLAYER_ATTACK_NODAMAGE);
                }
            }

            case PlayerActionPacket.ACTION_START_USING_ITEM -> {
                if (handle.player.getInventory().getItemInHand() instanceof ItemSpear) {
                    handle.setUsingItem(true);
                    return;
                }
            }
        }

        handle.setUsingItem(false);
    }

    private void toggleSprint(PlayerHandle handle, boolean value) {
        PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(handle.player, value);
        if (!event.call()) {
            handle.setNeedSendData(true);
        } else {
            handle.setSprinting(value);
        }
    }

    private void toggleSneak(PlayerHandle handle, boolean value) {
        PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(handle.player, value);
        if (event.call()) {
            handle.setNeedSendData(true);
        } else {
            handle.setSneaking(value);
        }
    }

    private void toggleSwim(PlayerHandle handle, boolean value) {
        PlayerToggleSwimEvent event = new PlayerToggleSwimEvent(handle.player, value);

        if (value && !handle.isInsideOfWater()) {
            event.setCancelled(true);
        }

        if (!value && handle.hasRidingOrSleeping()) {
            event.setCancelled(true);
        }

        if (!event.call()) {
            handle.setNeedSendData(true);
        } else {
            handle.setSwimming(value);
        }
    }

    private void toggleGlide(PlayerHandle handle, boolean value) {
        PlayerToggleGlideEvent event = new PlayerToggleGlideEvent(handle.player, value);
        Item chestplate = handle.getChestplate();

        if (handle.hasRidingOrSleeping() || (value && (chestplate == null || chestplate.getId() != 0xEL))) {
            event.setCancelled(true);
        }

        if (!event.call()) {
            handle.setNeedSendData(true);
        } else {
            handle.setGliding(value);
        }
    }

    private void toggleCrawl(PlayerHandle handle, boolean value) {
        PlayerToggleCrawlEvent event = new PlayerToggleCrawlEvent(handle.player, value);

        if (!event.call()) {
            handle.setNeedSendData(true);
        } else {
            handle.setCrawling(value);
        }
    }

    private void toggleFlight(PlayerHandle handle, boolean value) {
        PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(handle.player, value);
        if (!event.call()) {
            handle.setNeedSendAdventureSettings(true);
        } else {
            handle.player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, value);
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.PLAYER_ACTION_PACKET;
    }

    @Override
    public Class<? extends PlayerActionPacket> getPacketClass() {
        return PlayerActionPacket.class;
    }
}
