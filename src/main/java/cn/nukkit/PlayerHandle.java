package cn.nukkit;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.Event;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowDialog;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.transaction.*;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.types.ExperimentData;
import cn.nukkit.network.protocol.types.PlayerBlockActionData;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.LoginChainData;
import com.google.common.cache.Cache;
import com.google.common.collect.BiMap;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A PlayerHandle is used to access a player's protected data.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class PlayerHandle {
    public final @NotNull Player player;

    public PlayerHandle(@NotNull Player player) {
        this.player = player;
    }

    public int getProtocol() {
        return player.protocol;
    }

    public NetworkPlayerSession getNetworkSession() {
        return player.networkSession;
    }

    public void setClosingWindowId(int closingWindowId) {
        player.closingWindowId = closingWindowId;
    }

    public void setRandomClientId(long randomClientId) {
        player.randomClientId = randomClientId;
    }

    public Vector3 getForceMovement() {
        return player.forceMovement;
    }

    public void setForceMovement(Vector3 forceMovement) {
        player.forceMovement = forceMovement;
    }

    public Vector3 getTeleportPosition() {
        return player.teleportPosition;
    }

    public String getUsername() {
        return player.username;
    }

    public void setUsername(String username) {
        player.username = username;
    }

    public String getIusername() {
        return player.iusername;
    }

    public void setIusername(String iusername) {
        player.iusername = iusername;
    }

    public String getDisplayName() {
        return player.displayName;
    }

    public void setDisplayName(String displayName) {
        player.displayName = displayName;
    }

    public Vector3 getSleeping() {
        return player.sleeping;
    }

    public void setNewPosition(Vector3 newPosition) {
        player.newPosition = newPosition;
    }

    public int getChunkRadius() {
        return player.chunkRadius;
    }

    public void setChunkRadius(int chunkRadius) {
        player.chunkRadius = chunkRadius;
    }

    public boolean isCheckMovement() {
        return player.checkMovement;
    }

    public Map<Integer, FormWindow> getFormWindows() {
        return player.formWindows;
    }
    public Map<Integer, FormWindow> getServerSettings() {
        return player.serverSettings;
    }

    public Cache<String, FormWindowDialog> getDialogWindows() {
        return player.dialogWindows;
    }

    public void setShouldLogin(boolean shouldLogin) {
        player.shouldLogin = shouldLogin;
    }

    public void setLoginChainData(LoginChainData loginChainData) {
        player.loginChainData = loginChainData;
    }

    public boolean isAwaitingEncryptionHandshake() {
        return player.awaitingEncryptionHandshake;
    }

    public void setAwaitingEncryptionHandshake(boolean awaitingEncryptionHandshake) {
        player.awaitingEncryptionHandshake = awaitingEncryptionHandshake;
    }

    public AsyncTask getPreLoginEventTask() {
        return player.preLoginEventTask;
    }
    public void processPreLogin() {
        player.processPreLogin();
    }

    public void doFirstSpawn() {
        player.doFirstSpawn();
    }

    public boolean isLoginPacketReceived() {
        return player.loginPacketReceived;
    }

    public int getFailedMobEquipmentPacket() {
        return player.failedMobEquipmentPacket;
    }

    public void setFailedMobEquipmentPacket(int failedCount) {
        player.failedMobEquipmentPacket = failedCount;
    }

    public void setLoginPacketReceived(boolean value) {
        player.loginPacketReceived = value;
    }

    public void setProtocol(int protocol) {
        player.protocol = protocol;
    }

    public String getUnverifiedUsername() {
        return player.unverifiedUsername;
    }

    public void setUnverifiedUsername(String name) {
        player.unverifiedUsername = name;
    }

    public void setSocketAddress(InetSocketAddress address) {
        player.socketAddress = address;
    }

    public void setVersion(String version) {
        player.version = version;
    }

    public void setLoginUuid(UUID uuid) {
        player.setLoginUuid(uuid);
    }

    public void setUuid(UUID uuid) {
        player.setUuid(uuid);
    }

    public void setRawUUID(byte[] raw) {
        player.setRawUuid(raw);
    }

    public void close(String reason, String message) {
        player.close(reason, message);
    }

    public boolean isShouldPack() {
        return player.shouldPack;
    }

    public void setShouldPack(boolean value) {
        player.shouldPack = value;
    }

    public List<ExperimentData> getExperiments() {
        return player.getExperiments();
    }

    public boolean isSpawned() {
        return player.spawned;
    }

    public boolean isAlive() {
        return player.isAlive();
    }

    public float getBaseOffset() {
        return player.getBaseOffset();
    }


    public boolean isMovementServerAuthoritative() {
        return player.isMovementServerAuthoritative();
    }

    public boolean isLockMovementInput() {
        return player.isLockMovementInput();
    }

    public int getLastTeleportTick() {
        return player.lastTeleportTick;
    }

    public double getLastX() {
        return player.lastX;
    }

    public double getLastY() {
        return player.lastY;
    }

    public double getLastZ() {
        return player.lastZ;
    }

    public Vector3 getTemporalVector() {
        return player.temporalVector;
    }

    public Entity getRiding() {
        return player.riding;
    }

    public void setRotation(float yaw, float pitch, float headYaw) {
        player.setRotation(yaw, pitch, headYaw);
    }

    public void offerClientMovement(Vector3 pos) {
        player.clientMovements.offer(pos);
    }


    public void sendPosition(Vector3 pos, float yaw, float pitch, int mode) {
        player.sendPosition(pos, yaw, pitch, mode);
    }

    public void sendPosition(Player p, float yaw, float pitch, int mode) {
        player.sendPosition(p, yaw, pitch, mode);
    }


    public void setNeedSendData(boolean v) {
        player.setNeedSendData(v);
    }

    public void setNeedSendAdventureSettings(boolean v) {
        player.needSendAdventureSettings = v;
    }

    public PlayerBlockActionData getLastBlockAction() {
        return player.lastBlockAction;
    }

    public void setLastBlockAction(PlayerBlockActionData action) {
        player.lastBlockAction = action;
    }
    public void onBlockBreakComplete(BlockVector3 pos, BlockFace face) {
        player.onBlockBreakComplete(pos, face);
    }

    public void onSpinAttack(int level) {
        player.onSpinAttack(level);
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    public boolean isCreative() {
        return player.isCreative();
    }

    public boolean isSwimming() {
        return player.isSwimming();
    }
    public boolean isGliding() {
        return player.isGliding();
    }

    public boolean isInsideOfWater() {
        return player.isInsideOfWater();
    }

    public boolean isServerAuthoritativeBlockBreaking() {
        return player.isServerAuthoritativeBlockBreaking();
    }

    public void respawn() {
        player.respawn();
    }

    public void stopSleep() {
        player.stopSleep();
    }

    public void kick(PlayerKickEvent.Reason reason, String message) {
        player.kick(reason, message);
    }

    public void kick(PlayerKickEvent.Reason reason, String message, boolean notify, String extra) {
        player.kick(reason, message, notify, extra);
    }

    public void onBlockBreakStart(Vector3 pos, BlockFace face) {
        player.onBlockBreakStart(pos, face);
    }

    public void onBlockBreakAbort(Vector3 pos, BlockFace face) {
        player.onBlockBreakAbort(pos, face);
    }

    public void onBlockBreakContinue(Vector3 pos, BlockFace face) {
        player.onBlockBreakContinue(pos, face);
    }

    public void setSprinting(boolean value) {
        player.setSprinting(value);
    }

    public void setSneaking(boolean value) {
        player.setSneaking(value);
    }

    public void setSwimming(boolean value) {
        player.setSwimming(value);
    }

    public void setGliding(boolean value) {
        player.setGliding(value);
    }

    public void setCrawling(boolean value) {
        player.setCrawling(value);
    }

    public void setUsingItem(boolean value) {
        player.setUsingItem(value);
    }

    public Item getChestplate() {
        return player.getInventory().getChestplateFast();
    }

    public void callEvent(Event event) {
        player.getServer().getPluginManager().callEvent(event);
    }

    public void addSound(Vector3 pos, Sound sound) {
        player.level.addSound(pos, sound);
    }

    public boolean checkFlightAllowed() {
        return player.getServer().getSettings().player().allowFlight() || player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
    }

    public double getYaw() {
        return player.yaw;
    }

    public double getPitch() {
        return player.pitch;
    }

    public boolean hasRidingOrSleeping() {
        return player.riding != null || player.sleeping != null;
    }

    public int getInAirTicks() {
        return player.inAirTicks;
    }

    public void setButtonText(String text) {
        player.setButtonText(text);
    }

    public boolean isInventoryOpen() {
        return player.inventoryOpen;
    }

    public void openInventory(InventoryHolder holder) {
        player.addWindow(holder.getInventory());
    }

    public long getId() {
        return player.getId();
    }

    public void setInventoryOpen(boolean open) {
        player.inventoryOpen = open;
    }

    public int getLastEating() {
        return player.lastEating;
    }

    public void setLastEating(int ticks) {
        player.lastEating = ticks;
    }

    public void setCraftingType(int type) {
        player.craftingType = type;
    }

    public void resetCraftingGridType() {
        player.resetCraftingGridType();
    }

    public int getCraftingType() {
        return player.craftingType;
    }

    public void removeWindow(Inventory key, boolean b) {
        player.removeWindow(key, b);
    }

    public BiMap<Inventory, Integer> getWindows() {
        return player.windows;
    }

    public BiMap<Integer, Inventory> getWindowIndex() {
        return player.windowIndex;
    }
}