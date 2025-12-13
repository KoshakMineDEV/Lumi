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
import cn.nukkit.math.Vector3;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.network.protocol.types.PlayerActionType;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;
import org.jetbrains.annotations.NotNull;

/**
 * @author SocialMoods
 */
public class PlayerAuthInputProcessor extends DataPacketProcessor<PlayerAuthInputPacket> {

    public static final PlayerAuthInputProcessor INSTANCE = new PlayerAuthInputProcessor();

    @Override
    public void handle(@NotNull PlayerHandle h, @NotNull PlayerAuthInputPacket authPacket) {
        Player p = h.player;
        Server server = p.getServer();
        Level level = p.getLevel();
        int protocol = h.getProtocol();

        if (!h.isMovementServerAuthoritative()) {
            return;
        }

        if (!authPacket.getBlockActionData().isEmpty()) {
            for (PlayerBlockActionData action : authPacket.getBlockActionData().values()) {
                BlockVector3 blockPos = action.getPosition();
                BlockFace blockFace = BlockFace.fromIndex(action.getFacing());

                if (h.getLastBlockAction() != null
                        && h.getLastBlockAction().getAction() == PlayerActionType.PREDICT_DESTROY_BLOCK
                        && action.getAction() == PlayerActionType.CONTINUE_DESTROY_BLOCK) {
                    h.onBlockBreakStart(blockPos.asVector3(), blockFace);
                }

                BlockVector3 lastBreakPos = h.getLastBlockAction() == null
                        ? null
                        : h.getLastBlockAction().getPosition();

                if (lastBreakPos != null
                        && (lastBreakPos.getX() != blockPos.getX()
                        || lastBreakPos.getY() != blockPos.getY()
                        || lastBreakPos.getZ() != blockPos.getZ())) {

                    h.onBlockBreakAbort(lastBreakPos.asVector3(), BlockFace.DOWN);
                    h.onBlockBreakStart(blockPos.asVector3(), blockFace);
                }

                switch (action.getAction()) {
                    case START_DESTROY_BLOCK ->
                            h.onBlockBreakStart(blockPos.asVector3(), blockFace);
                    case ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK ->
                            h.onBlockBreakAbort(blockPos.asVector3(), blockFace);
                    case CONTINUE_DESTROY_BLOCK ->
                            h.onBlockBreakContinue(blockPos.asVector3(), blockFace);
                    case PREDICT_DESTROY_BLOCK -> {
                        h.onBlockBreakAbort(blockPos.asVector3(), blockFace);
                        h.onBlockBreakComplete(blockPos, blockFace);
                    }
                }

                h.setLastBlockAction(action);
            }
        }

        if (protocol >= ProtocolInfo.v1_20_10_21
                && authPacket.getInputData().contains(AuthInputAction.MISSED_SWING)) {

            PlayerMissedSwingEvent pmse = new PlayerMissedSwingEvent(p);
            if (h.player.isSpectator()) {
                pmse.setCancelled();
            }

            server.getPluginManager().callEvent(pmse);

            if (!pmse.isCancelled()) {
                level.addLevelSoundEvent(
                        p,
                        LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE,
                        -1,
                        "minecraft:player",
                        false,
                        false
                );
            }
        }

        if (h.isLockMovementInput()) {
            return;
        }

        if (h.getTeleportPosition() != null) {
            return;
        }

        boolean ignoreCoordinateMove = false;

        if (h.getRiding() instanceof EntityMinecartAbstract minecart) {
            double inputY = authPacket.getMotion().getY();
            if (inputY >= -1.001 && inputY <= 1.001) {
                minecart.setCurrentSpeed(inputY);
            }
        } else if (h.getRiding() instanceof EntityBoat boat) {
            if (protocol >= ProtocolInfo.v1_21_130) {
                if (boat.isControlling(p)) {
                    if (h.getTemporalVector()
                            .setComponents(
                                    authPacket.getPosition().getX(),
                                    authPacket.getPosition().getY(),
                                    authPacket.getPosition().getZ())
                            .distanceSquared(boat) < 100) {

                        boat.onPlayerInput(
                                p,
                                authPacket.getMotion().getX(),
                                authPacket.getMotion().getY()
                        );
                        ignoreCoordinateMove = true;
                    }
                }
            } else {
                if (authPacket.getInputData()
                        .contains(AuthInputAction.IN_CLIENT_PREDICTED_IN_VEHICLE)) {

                    if (boat.getId() == authPacket.getPredictedVehicle()
                            && boat.isControlling(p)) {

                        if (h.getTemporalVector()
                                .setComponents(
                                        authPacket.getPosition().getX(),
                                        authPacket.getPosition().getY(),
                                        authPacket.getPosition().getZ())
                                .distanceSquared(boat) < 100) {

                            boat.onInput(
                                    authPacket.getPosition().getX(),
                                    authPacket.getPosition().getY(),
                                    authPacket.getPosition().getZ(),
                                    authPacket.getHeadYaw()
                            );
                            ignoreCoordinateMove = true;
                        }
                    }
                }
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.START_SPRINTING)) {
            PlayerToggleSprintEvent e = new PlayerToggleSprintEvent(p, true);

            if ((p.getFoodData().getFood() <= 6
                    && !p.getAdventureSettings().get(AdventureSettings.Type.FLYING))
                    || h.getRiding() != null
                    || h.getSleeping() != null
                    || p.hasEffect(EffectType.BLINDNESS)
                    || (p.isSneaking()
                    && !authPacket.getInputData().contains(AuthInputAction.STOP_SNEAKING))) {

                e.setCancelled(true);
            }

            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setSprinting(true);
            }

            p.setUsingItem(false);
        }

        if (authPacket.getInputData().contains(AuthInputAction.STOP_SPRINTING)) {
            PlayerToggleSprintEvent e = new PlayerToggleSprintEvent(p, false);

            if (h.getRiding() != null || h.getSleeping() != null) {
                e.setCancelled(true);
            }

            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setSprinting(false);
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.START_SNEAKING)) {
            PlayerToggleSneakEvent e = new PlayerToggleSneakEvent(p, true);
            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setSneaking(true);
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.STOP_SNEAKING)) {
            PlayerToggleSneakEvent e = new PlayerToggleSneakEvent(p, false);
            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setSneaking(false);
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.START_JUMPING)) {
            server.getPluginManager().callEvent(new PlayerJumpEvent(p));
        }

        if (authPacket.getInputData().contains(AuthInputAction.START_GLIDING)) {
            boolean withoutElytra = false;
            Item chestplate = p.getInventory().getChestplateFast();

            if (chestplate == null
                    || chestplate.getId() != ItemID.ELYTRA
                    || chestplate.getDamage() >= chestplate.getMaxDurability()) {
                withoutElytra = true;
            }

            if (withoutElytra && !server.getSettings().player().allowFlight()) {
                p.kick(PlayerKickEvent.Reason.FLYING_DISABLED, Player.MSG_FLYING_NOT_ENABLED, true);
                return;
            }

            PlayerToggleGlideEvent e = new PlayerToggleGlideEvent(p, true);

            if (h.getRiding() != null || h.getSleeping() != null || withoutElytra) {
                e.setCancelled(true);
            }

            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setGliding(true);
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.STOP_GLIDING)) {
            PlayerToggleGlideEvent e = new PlayerToggleGlideEvent(p, false);
            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setGliding(false);
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.START_SWIMMING)) {
            PlayerToggleSwimEvent e = new PlayerToggleSwimEvent(p, true);

            if (h.getRiding() != null || h.getSleeping() != null || !p.isInsideOfWater()) {
                e.setCancelled(true);
            }

            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setSwimming(true);
            }
        }

        if (authPacket.getInputData().contains(AuthInputAction.STOP_SWIMMING)) {
            PlayerToggleSwimEvent e = new PlayerToggleSwimEvent(p, false);
            server.getPluginManager().callEvent(e);

            if (e.isCancelled()) {
                h.setNeedSendData(true);
            } else {
                p.setSwimming(false);
            }
        }

        if (protocol >= ProtocolInfo.v1_20_30_24) {

            if (protocol >= ProtocolInfo.v1_21_40) {

                if (authPacket.getInputData().contains(AuthInputAction.START_SPIN_ATTACK)) {
                    Enchantment riptide = p.getInventory()
                            .getItemInHandFast()
                            .getEnchantment(Enchantment.ID_TRIDENT_RIPTIDE);

                    if (riptide != null) {
                        PlayerToggleSpinAttackEvent e =
                                new PlayerToggleSpinAttackEvent(p, true);

                        if (riptide.getLevel() < 1) {
                            e.setCancelled(true);
                        } else {
                            boolean inWater = false;

                            for (Block block : p.getCollisionBlocks()) {
                                if (block instanceof BlockWater
                                        || block.level.isBlockWaterloggedAt(
                                        p.getChunk(),
                                        (int) block.x,
                                        (int) block.y,
                                        (int) block.z)) {
                                    inWater = true;
                                    break;
                                }
                            }

                            if (!(inWater || (p.getLevel().isRaining() && p.canSeeSky()))) {
                                e.setCancelled(true);
                            }
                        }

                        server.getPluginManager().callEvent(e);

                        if (e.isCancelled()) {
                            h.setNeedSendData(true);
                        } else {
                            h.onSpinAttack(riptide.getLevel());
                            p.setSpinAttack(true);
                            p.setUsingItem(false);
                            p.resetFallDistance();

                            int sound;
                            if (riptide.getLevel() >= 3) {
                                sound = LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_3;
                            } else if (riptide.getLevel() == 2) {
                                sound = LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_2;
                            } else {
                                sound = LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_1;
                            }

                            p.getLevel().addLevelSoundEvent(p, sound);
                        }
                    }
                }

                if (authPacket.getInputData().contains(AuthInputAction.STOP_SPIN_ATTACK)) {
                    PlayerToggleSpinAttackEvent e =
                            new PlayerToggleSpinAttackEvent(p, false);

                    server.getPluginManager().callEvent(e);

                    if (e.isCancelled()) {
                        h.setNeedSendData(true);
                    } else {
                        p.setSpinAttack(false);
                    }
                }
            }

            if (authPacket.getInputData().contains(AuthInputAction.START_FLYING)) {
                if (!server.getSettings().player().allowFlight()
                        && !p.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT)) {
                    p.kick(
                            PlayerKickEvent.Reason.FLYING_DISABLED,
                            "Flying is not enabled on this server"
                    );
                    return;
                }

                PlayerToggleFlightEvent e =
                        new PlayerToggleFlightEvent(p, true);

                if (p.isSpectator()) {
                    e.setCancelled();
                }

                server.getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    h.setNeedSendAdventureSettings(true);
                } else {
                    p.getAdventureSettings().set(AdventureSettings.Type.FLYING, e.isFlying());
                }
            }

            if (authPacket.getInputData().contains(AuthInputAction.STOP_FLYING)) {
                PlayerToggleFlightEvent e =
                        new PlayerToggleFlightEvent(p, false);

                if (p.isSpectator()) {
                    e.setCancelled();
                }

                server.getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    h.setNeedSendAdventureSettings(true);
                } else {
                    p.getAdventureSettings().set(AdventureSettings.Type.FLYING, e.isFlying());
                }
            }
        }

        if (protocol >= ProtocolInfo.v1_20_30_24
                || (protocol >= ProtocolInfo.v1_20_10_21
                && server.getSettings().features().enableExperimentMode())) {

            if (authPacket.getInputData().contains(AuthInputAction.START_CRAWLING)) {
                PlayerToggleCrawlEvent e =
                        new PlayerToggleCrawlEvent(p, true);

                if (h.getRiding() != null || h.getSleeping() != null) {
                    e.setCancelled(true);
                }

                server.getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    h.setNeedSendData(true);
                } else {
                    p.setCrawling(true);
                }
            }

            if (authPacket.getInputData().contains(AuthInputAction.STOP_CRAWLING)) {
                PlayerToggleCrawlEvent e =
                        new PlayerToggleCrawlEvent(p, false);

                server.getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    h.setNeedSendData(true);
                } else {
                    p.setCrawling(false);
                }
            }
        }

        Vector3 clientPosition = authPacket.getPosition()
                .subtract(
                        0,
                        h.getRiding() == null
                                ? h.getBaseOffset()
                                : h.getRiding().getMountedOffset(p).getY(),
                        0
                ).asVector3();

        double distSqr = clientPosition.distanceSquared(p);

        if (distSqr == 0.0
                && authPacket.getYaw() % 360 == p.yaw
                && authPacket.getPitch() % 360 == p.pitch) {
            return;
        }

        if (h.getLastTeleportTick() + 10 > server.getTick()
                && clientPosition.distance(
                h.getTemporalVector()
                        .setComponents(h.getLastX(), h.getLastY(), h.getLastZ())
        ) < 5) {
            return;
        }

        if (distSqr > 100) {
            if (h.getLastTeleportTick() + 30 < server.getTick()) {
                h.sendPosition(
                        p,
                        authPacket.getYaw(),
                        authPacket.getPitch(),
                        MovePlayerPacket.MODE_RESET
                );
            }
            return;
        }

        boolean revertMotion = false;
        if (!p.isAlive() || !h.isSpawned()) {
            revertMotion = true;
            h.setForceMovement(new Vector3(p.x, p.y, p.z));
        }

        if (h.getForceMovement() != null
                && (clientPosition.distanceSquared(h.getForceMovement()) > 0.1 || revertMotion)) {

            h.sendPosition(
                    h.getForceMovement(),
                    authPacket.getYaw(),
                    authPacket.getPitch(),
                    MovePlayerPacket.MODE_RESET
            );
        } else {
            float yaw = authPacket.getYaw() % 360;
            float headYaw = authPacket.getHeadYaw() % 360;
            float pitch = authPacket.getPitch() % 360;

            if (yaw < 0) yaw += 360;
            if (headYaw < 0) headYaw += 360;

            h.setRotation(yaw, pitch, headYaw);

            if (!ignoreCoordinateMove) {
                h.setNewPosition(clientPosition);
                h.offerClientMovement(clientPosition);
            }

            h.setForceMovement(null);
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


