package cn.nukkit.network.process.processor.common;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockWater;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityMinecartAbstract;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.*;
import org.jetbrains.annotations.NotNull;

public class PlayerAuthInputProcessor extends DataPacketProcessor<PlayerAuthInputPacket> {

    public static final PlayerAuthInputProcessor INSTANCE = new PlayerAuthInputProcessor();

    @Override
    public void handle(@NotNull PlayerHandle handle, @NotNull PlayerAuthInputPacket packet) {
        Player player = handle.player;
        Server server = player.getServer();
        Level level = player.getLevel();
        int protocol = handle.getProtocol();

        if (!handle.isMovementServerAuthoritative()) {
            return;
        }

        if (!packet.getBlockActionData().isEmpty()) {
            for (PlayerBlockActionData action : packet.getBlockActionData().values()) {
                BlockVector3 blockPos = action.getPosition();
                BlockFace blockFace = BlockFace.fromIndex(action.getFacing());

                if (handle.getLastBlockAction() != null
                        && handle.getLastBlockAction().getAction() == PlayerActionType.PREDICT_DESTROY_BLOCK
                        && action.getAction() == PlayerActionType.CONTINUE_DESTROY_BLOCK) {
                    handle.onBlockBreakStart(blockPos.asVector3(), blockFace);
                }

                BlockVector3 lastBreakPos = handle.getLastBlockAction() == null
                        ? null
                        : handle.getLastBlockAction().getPosition();

                if (lastBreakPos != null
                        && (lastBreakPos.getX() != blockPos.getX()
                        || lastBreakPos.getY() != blockPos.getY()
                        || lastBreakPos.getZ() != blockPos.getZ())) {

                    handle.onBlockBreakAbort(lastBreakPos.asVector3(), BlockFace.DOWN);
                    handle.onBlockBreakStart(blockPos.asVector3(), blockFace);
                }

                switch (action.getAction()) {
                    case START_DESTROY_BLOCK ->
                            handle.onBlockBreakStart(blockPos.asVector3(), blockFace);
                    case ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK ->
                            handle.onBlockBreakAbort(blockPos.asVector3(), blockFace);
                    case CONTINUE_DESTROY_BLOCK ->
                            handle.onBlockBreakContinue(blockPos.asVector3(), blockFace);
                    case PREDICT_DESTROY_BLOCK -> {
                        handle.onBlockBreakAbort(blockPos.asVector3(), blockFace);
                        handle.onBlockBreakComplete(blockPos, blockFace);
                    }
                }

                handle.setLastBlockAction(action);
            }
        }

        if (protocol >= ProtocolInfo.v1_20_10_21
                && packet.getInputData().contains(AuthInputAction.MISSED_SWING)) {

            PlayerMissedSwingEvent pmse = new PlayerMissedSwingEvent(player);
            if (handle.player.isSpectator()) {
                pmse.setCancelled();
            }

            if (pmse.call()) {
                level.addLevelSoundEvent(
                        player,
                        LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE,
                        -1,
                        "minecraft:player",
                        false,
                        false
                );
            }
        }

        if (handle.isLockMovementInput()) {
            return;
        }

        if (handle.getTeleportPosition() != null) {
            return;
        }

        boolean ignoreCoordinateMove = false;

        if (handle.getRiding() instanceof EntityMinecartAbstract minecart) {
            double inputY = packet.getMotion().getY();
            if (inputY >= -1.001 && inputY <= 1.001) {
                minecart.setCurrentSpeed(inputY);
            }
        } else if (handle.getRiding() instanceof EntityBoat boat) {
            if (protocol >= ProtocolInfo.v1_21_130) {
                double moveVecX = packet.getMotion().getX();
                double moveVecY = packet.getMotion().getY();
                moveVecX = NukkitMath.clamp(moveVecX, -1, 1);
                moveVecY = NukkitMath.clamp(moveVecY, -1, 1);
                boolean isMobileAndClassicMovement = packet.getInputMode() == InputMode.TOUCH && packet.getInteractionModel() == AuthInteractionModel.CLASSIC;
                if (isMobileAndClassicMovement) {
                    // Press both left and right to move forward and press 1 to turn the boat.
                    boolean left = packet.getInputData().contains(AuthInputAction.PADDLE_LEFT), right = packet.getInputData().contains(AuthInputAction.PADDLE_RIGHT);
                    if (left && right) {
                        boat.onPlayerInput(handle.player, 0, 1);
                    } else {
                        boat.onPlayerInput(handle.player, left? 1: right? -1: 0, 0);
                    }
                } else {
                    boat.onPlayerInput(handle.player, moveVecX, moveVecY);
                }
                ignoreCoordinateMove = true;
            } else {
                if (packet.getInputData()
                        .contains(AuthInputAction.IN_CLIENT_PREDICTED_IN_VEHICLE)) {

                    if (boat.getId() == packet.getPredictedVehicle()
                            && boat.isControlling(player)) {

                        if (handle.getTemporalVector()
                                .setComponents(
                                        packet.getPosition().getX(),
                                        packet.getPosition().getY(),
                                        packet.getPosition().getZ())
                                .distanceSquared(boat) < 100) {

                            boat.onInput(
                                    packet.getPosition().getX(),
                                    packet.getPosition().getY(),
                                    packet.getPosition().getZ(),
                                    packet.getHeadYaw()
                            );
                            ignoreCoordinateMove = true;
                        }
                    }
                }
            }
        }

        if (packet.getInputData().contains(AuthInputAction.START_SPRINTING)) {
            PlayerToggleSprintEvent e = new PlayerToggleSprintEvent(player, true);

            if ((player.getFoodData().getFood() <= 6
                    && !player.getAdventureSettings().get(AdventureSettings.Type.FLYING))
                    || handle.getRiding() != null
                    || handle.getSleeping() != null
                    || player.hasEffect(EffectType.BLINDNESS)
                    || (player.isSneaking()
                    && !packet.getInputData().contains(AuthInputAction.STOP_SNEAKING))) {

                e.setCancelled(true);
            }

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setSprinting(true);
            }

            player.setUsingItem(false);
        }

        if (packet.getInputData().contains(AuthInputAction.STOP_SPRINTING)) {
            PlayerToggleSprintEvent e = new PlayerToggleSprintEvent(player, false);

            if (handle.getRiding() != null || handle.getSleeping() != null) {
                e.setCancelled(true);
            }

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setSprinting(false);
            }
        }

        if (packet.getInputData().contains(AuthInputAction.START_SNEAKING)) {
            PlayerToggleSneakEvent e = new PlayerToggleSneakEvent(player, true);

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setSneaking(true);
            }
        }

        if (packet.getInputData().contains(AuthInputAction.STOP_SNEAKING)) {
            PlayerToggleSneakEvent e = new PlayerToggleSneakEvent(player, false);

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setSneaking(false);
            }
        }

        if (packet.getInputData().contains(AuthInputAction.START_JUMPING)) {
            new PlayerJumpEvent(player).call();
        }

        if (packet.getInputData().contains(AuthInputAction.START_GLIDING)) {
            boolean withoutElytra = false;
            Item chestplate = player.getInventory().getChestplateFast();

            if (chestplate == null
                    || chestplate.getId() != ItemID.ELYTRA
                    || chestplate.getDamage() >= chestplate.getMaxDurability()) {
                withoutElytra = true;
            }

            if (withoutElytra && !server.getSettings().player().allowFlight()) {
                player.kick(PlayerKickEvent.Reason.FLYING_DISABLED, Player.MSG_FLYING_NOT_ENABLED, true);
                return;
            }

            PlayerToggleGlideEvent e = new PlayerToggleGlideEvent(player, true);

            if (handle.getRiding() != null || handle.getSleeping() != null || withoutElytra) {
                e.setCancelled(true);
            }

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setGliding(true);
            }
        }

        if (packet.getInputData().contains(AuthInputAction.STOP_GLIDING)) {
            PlayerToggleGlideEvent e = new PlayerToggleGlideEvent(player, false);

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setGliding(false);
            }
        }

        if (packet.getInputData().contains(AuthInputAction.START_SWIMMING)) {
            PlayerToggleSwimEvent e = new PlayerToggleSwimEvent(player, true);

            if (handle.getRiding() != null || handle.getSleeping() != null || !player.isInsideOfWater()) {
                e.setCancelled(true);
            }

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setSwimming(true);
            }
        }

        if (packet.getInputData().contains(AuthInputAction.STOP_SWIMMING)) {
            PlayerToggleSwimEvent e = new PlayerToggleSwimEvent(player, false);

            if (!e.call()) {
                handle.setNeedSendData(true);
            } else {
                player.setSwimming(false);
            }
        }

        if (protocol >= ProtocolInfo.v1_20_30_24) {
            if (protocol >= ProtocolInfo.v1_21_40) {
                if (packet.getInputData().contains(AuthInputAction.START_SPIN_ATTACK)) {
                    Enchantment riptide = player.getInventory()
                            .getItemInHandFast()
                            .getEnchantment(Enchantment.ID_TRIDENT_RIPTIDE);

                    if (riptide != null) {
                        PlayerToggleSpinAttackEvent e =
                                new PlayerToggleSpinAttackEvent(player, true);

                        if (riptide.getLevel() < 1) {
                            e.setCancelled(true);
                        } else {
                            boolean inWater = false;

                            for (Block block : player.getCollisionBlocks()) {
                                if (block instanceof BlockWater
                                        || block.level.isBlockWaterloggedAt(
                                        player.getChunk(),
                                        (int) block.x,
                                        (int) block.y,
                                        (int) block.z)) {
                                    inWater = true;
                                    break;
                                }
                            }

                            if (!(inWater || (player.getLevel().isRaining() && player.canSeeSky()))) {
                                e.setCancelled(true);
                            }
                        }

                        if (!e.call()) {
                            handle.setNeedSendData(true);
                        } else {
                            handle.onSpinAttack(riptide.getLevel());
                            player.setSpinAttack(true);
                            player.setUsingItem(false);
                            player.resetFallDistance();

                            int sound;
                            if (riptide.getLevel() >= 3) {
                                sound = LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_3;
                            } else if (riptide.getLevel() == 2) {
                                sound = LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_2;
                            } else {
                                sound = LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_1;
                            }

                            player.getLevel().addLevelSoundEvent(player, sound);
                        }
                    }
                }

                if (packet.getInputData().contains(AuthInputAction.STOP_SPIN_ATTACK)) {
                    PlayerToggleSpinAttackEvent e =
                            new PlayerToggleSpinAttackEvent(player, false);

                    if (!e.call()) {
                        handle.setNeedSendData(true);
                    } else {
                        player.setSpinAttack(false);
                    }
                }
            }

            if (packet.getInputData().contains(AuthInputAction.START_FLYING)) {
                if (!server.getSettings().player().allowFlight()
                        && !player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT)) {
                    player.kick(
                            PlayerKickEvent.Reason.FLYING_DISABLED,
                            "Flying is not enabled on this server"
                    );
                    return;
                }

                PlayerToggleFlightEvent e =
                        new PlayerToggleFlightEvent(player, true);

                if (player.isSpectator()) {
                    e.setCancelled();
                }

                if (!e.call()) {
                    handle.setNeedSendAdventureSettings(true);
                } else {
                    player.getAdventureSettings().set(AdventureSettings.Type.FLYING, e.isFlying());
                }
            }

            if (packet.getInputData().contains(AuthInputAction.STOP_FLYING)) {
                PlayerToggleFlightEvent e =
                        new PlayerToggleFlightEvent(player, false);

                if (player.isSpectator()) {
                    e.setCancelled();
                }

                if (!e.call()) {
                    handle.setNeedSendAdventureSettings(true);
                } else {
                    player.getAdventureSettings().set(AdventureSettings.Type.FLYING, e.isFlying());
                }
            }
        }

        if (protocol >= ProtocolInfo.v1_20_30_24
                || (protocol >= ProtocolInfo.v1_20_10_21
                && server.getSettings().features().enableExperimentMode())) {

            if (packet.getInputData().contains(AuthInputAction.START_CRAWLING)) {
                PlayerToggleCrawlEvent e =
                        new PlayerToggleCrawlEvent(player, true);

                if (handle.getRiding() != null || handle.getSleeping() != null) {
                    e.setCancelled(true);
                }

                if (!e.call()) {
                    handle.setNeedSendData(true);
                } else {
                    player.setCrawling(true);
                }
            }

            if (packet.getInputData().contains(AuthInputAction.STOP_CRAWLING)) {
                PlayerToggleCrawlEvent e =
                        new PlayerToggleCrawlEvent(player, false);

                if (!e.call()) {
                    handle.setNeedSendData(true);
                } else {
                    player.setCrawling(false);
                }
            }
        }

        Vector3 clientPosition = packet.getPosition()
                .subtract(
                        0,
                        handle.getRiding() == null
                                ? handle.getBaseOffset()
                                : handle.getRiding().getMountedOffset(player).getY(),
                        0
                ).asVector3();

        double distSqr = clientPosition.distanceSquared(player);

        if (distSqr == 0.0
                && packet.getYaw() % 360 == player.yaw
                && packet.getPitch() % 360 == player.pitch) {
            return;
        }

        if (handle.getLastTeleportTick() + 10 > server.getTick()
                && clientPosition.distance(
                handle.getTemporalVector()
                        .setComponents(handle.getLastX(), handle.getLastY(), handle.getLastZ())
        ) < 5) {
            return;
        }

        if (distSqr > 100) {
            if (handle.getLastTeleportTick() + 30 < server.getTick()) {
                handle.sendPosition(
                        player,
                        packet.getYaw(),
                        packet.getPitch(),
                        MovePlayerPacket.MODE_RESET
                );
            }
            return;
        }

        boolean revertMotion = false;
        if (!player.isAlive() || !handle.isSpawned()) {
            revertMotion = true;
            handle.setForceMovement(new Vector3(player.x, player.y, player.z));
        }

        if (handle.getForceMovement() != null
                && (clientPosition.distanceSquared(handle.getForceMovement()) > 0.1 || revertMotion)) {

            handle.sendPosition(
                    handle.getForceMovement(),
                    packet.getYaw(),
                    packet.getPitch(),
                    MovePlayerPacket.MODE_RESET
            );
        } else {
            float yaw = packet.getYaw() % 360;
            float headYaw = packet.getHeadYaw() % 360;
            float pitch = packet.getPitch() % 360;

            if (yaw < 0) yaw += 360;
            if (headYaw < 0) headYaw += 360;

            handle.setRotation(yaw, pitch, headYaw);

            if (!ignoreCoordinateMove) {
                handle.setNewPosition(clientPosition);
                handle.offerClientMovement(clientPosition);
            }

            handle.setForceMovement(null);
        }
    }


    @Override
    public int getPacketId() {
        return ProtocolInfo.PLAYER_AUTH_INPUT_PACKET;
    }

    @Override
    public Class<? extends DataPacket> getPacketClass() {
        return PlayerAuthInputPacket.class;
    }
}


