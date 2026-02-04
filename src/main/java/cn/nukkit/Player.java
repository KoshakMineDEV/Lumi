package cn.nukkit;

import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.block.*;
import cn.nukkit.block.customblock.CustomBlock;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.blockentity.impl.BlockEntityCampfire;
import cn.nukkit.blockentity.impl.BlockEntityItemFrame;
import cn.nukkit.blockentity.impl.BlockEntitySign;
import cn.nukkit.bossbar.BossBarColor;
import cn.nukkit.bossbar.DummyBossBar;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.defaults.HelpCommand;
import cn.nukkit.command.utils.RawText;
import cn.nukkit.debugshape.DebugShape;
import cn.nukkit.entity.*;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.IntPositionEntityData;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.entity.data.attribute.EntityMovementSpeedModifier;
import cn.nukkit.entity.data.property.EntityProperty;
import cn.nukkit.entity.data.skin.Skin;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.item.*;
import cn.nukkit.entity.mob.EntityWalkingMob;
import cn.nukkit.entity.mob.EntityWolf;
import cn.nukkit.entity.passive.EntityVillager;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.EntityThrownTrident;
import cn.nukkit.entity.util.BlockIterator;
import cn.nukkit.event.block.WaterFrostEvent;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.entity.EntityDamageEvent.DamageModifier;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryPickupArrowEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.inventory.InventoryPickupTridentEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowDialog;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.*;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.*;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.trim.TrimFactory;
import cn.nukkit.lang.CommandOutputContainer;
import cn.nukkit.lang.LangCode;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.*;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.level.sound.ExperienceOrbSound;
import cn.nukkit.level.vibration.VanillaVibrationTypes;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.math.*;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.encryption.PrepareEncryptionTask;
import cn.nukkit.network.process.DataPacketManager;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.*;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.permission.PermissibleBase;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.plugin.InternalPlugin;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.recipe.impl.MultiRecipe;
import cn.nukkit.registry.Registries;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scoreboard.displayer.IScoreboardViewer;
import cn.nukkit.scoreboard.scoreboard.IScoreboard;
import cn.nukkit.scoreboard.scoreboard.IScoreboardLine;
import cn.nukkit.scoreboard.scorer.PlayerScorer;
import cn.nukkit.settings.GeneralSettings.ServerAuthoritativeMovement;
import cn.nukkit.utils.*;
import cn.nukkit.utils.compression.SnappyCompression;
import cn.nukkit.utils.compression.Zlib;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Player class
 *
 * @author MagicDroidX &amp; Box
 * Nukkit Project
 */
@Log4j2
public class Player extends EntityHuman implements CommandSender, InventoryHolder, ChunkLoader, IPlayer, IScoreboardViewer {

    public static final Player[] EMPTY_ARRAY = new Player[0];

    public static final int SURVIVAL = 0;
    public static final int CREATIVE = 1;
    public static final int ADVENTURE = 2;
    public static final int SPECTATOR = 3;
    public static final int VIEW = SPECTATOR;

    public static final int CRAFTING_SMALL = 0;
    public static final int CRAFTING_BIG = 1;
    public static final int CRAFTING_ANVIL = 2;
    public static final int CRAFTING_ENCHANT = 3;
    public static final int CRAFTING_BEACON = 4;
    public static final int CRAFTING_SMITHING = 1003;
    public static final int CRAFTING_LOOM = 1004;

    public static final int TRADE_WINDOW_ID = 500;
    public static final float MAXIMUM_SPEED = 0.5f;
    public static final float DEFAULT_FLY_SPEED = 0.05f;
    public static final float DEFAULT_VERTICAL_FLY_SPEED = 1.0f;

    public static final int PERMISSION_CUSTOM = 3;
    public static final int PERMISSION_OPERATOR = 2;
    public static final int PERMISSION_MEMBER = 1;
    public static final int PERMISSION_VISITOR = 0;

    public static final int ANVIL_WINDOW_ID = 2;
    public static final int ENCHANT_WINDOW_ID = 3;
    public static final int BEACON_WINDOW_ID = 4;
    public static final int LOOM_WINDOW_ID = 5;
    public static final int SMITHING_WINDOW_ID = 6;
    public static final int GRINDSTONE_WINDOW_ID = 7;
    /**
     * @since 649 1.20.60
     * 自1.20.60开始，需要发送ContainerOpenPacket给玩家才能正常打开讲台上的书
     * 在原版中id按顺序增加，但测试中采用固定id也可正常实现功能
     */
    public static final int LECTERN_WINDOW_ID = 7;

    // 后续创建的窗口应该从此数值开始
    public static final int MINIMUM_OTHER_WINDOW_ID = Utils.dynamic(10);

    public static final int RESOURCE_PACK_CHUNK_SIZE = 8 * 1024; // 8KB

    /**
     * Regular expression for validating player name. Allows only: Number nicknames, letter nicknames, number and letters nicknames, nicknames with underscores, nicknames with space in the middle
     */
    public static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("^(?! )[A-Za-z0-9_](?: ?(?! )?[A-Za-z0-9_]){1,14}$");

    protected final SourceInterface interfaz;
    protected final NetworkPlayerSession networkSession;

    public boolean playedBefore;
    public boolean spawned = false;
    public boolean loggedIn = false;
    protected boolean loginVerified = false;
    private int unverifiedPackets;
    protected boolean loginPacketReceived;
    protected boolean awaitingEncryptionHandshake;
    public int gamemode;
    public long lastBreak = -1;
    private BlockVector3 lastBreakPosition = new BlockVector3();

    protected int windowCnt = MINIMUM_OTHER_WINDOW_ID;

    protected final BiMap<Inventory, Integer> windows = HashBiMap.create();
    protected final BiMap<Integer, Inventory> windowIndex = windows.inverse();
    protected final Set<Integer> permanentWindows = new IntOpenHashSet();
    protected boolean inventoryOpen;
    protected int closingWindowId = Integer.MIN_VALUE;

    public Vector3 speed = null;

    protected final Queue<Vector3> clientMovements = PlatformDependent.newMpscQueue(4);

    public int craftingType = CRAFTING_SMALL;

    protected PlayerUIInventory playerUIInventory;
    protected CraftingGrid craftingGrid;
    protected CraftingTransaction craftingTransaction;
    protected EnchantTransaction enchantTransaction;
    protected RepairItemTransaction repairItemTransaction;
    protected LoomTransaction loomTransaction;
    protected SmithingTransaction smithingTransaction;
    protected TradingTransaction tradingTransaction;
    protected GrindstoneTransaction grindstoneTransaction;

    protected long randomClientId;

    protected Vector3 forceMovement = null;

    protected Vector3 teleportPosition = null;

    protected int lastTeleportTick = -1;

    protected boolean connected = true;
    protected final InetSocketAddress rawSocketAddress;
    protected InetSocketAddress socketAddress;
    protected boolean removeFormat = true;

    protected String username;
    protected String unverifiedUsername = "";
    protected String iusername;
    protected String displayName;

    /**
     * Client protocol version
     */
    public int protocol = Integer.MAX_VALUE;
    /**
     * Client RakNet protocol version
     */
    public int raknetProtocol;
    /**
     * Client version string
     */
    protected String version;

    protected int startAction = -1;

    protected Vector3 sleeping = null;

    private final int loaderId;

    public final Map<Long, Boolean> usedChunks = new Long2ObjectOpenHashMap<>();

    private int chunksSent = 0;
    private boolean hasSpawnChunks;
    protected final LongLinkedOpenHashSet loadQueue = new LongLinkedOpenHashSet();
    protected int nextChunkOrderRun = 1;

    protected final Map<UUID, Player> hiddenPlayers = new HashMap<>();

    protected Vector3 newPosition = null;

    protected int chunkRadius;
    protected int viewDistance;

    protected Position spawnPosition;
    protected Position spawnBlockPosition;

    protected int inAirTicks = 0;
    protected int startAirTicks = 5;
    protected int lastInAirTick = 0;

    protected AdventureSettings adventureSettings;

    protected boolean checkMovement = true;

    private PermissibleBase perm;
    /**
     * Option to hide admin permissions from player list tab in client.
     * Admin player shown in server list will look same as normal player.
     */
    private boolean showAdmin = true;
    /**
     * Option not to spawn the player for others.
     */
    public boolean showToOthers = true;

    /**
     * Player's client-side walk speed. Remember to call getAdventureSettings().update() if changed.
     */
    @Getter
    @Setter
    private float walkSpeed = DEFAULT_SPEED;
    /**
     * Player's client-side fly speed. Remember to call getAdventureSettings().update() if changed.
     */
    @Getter
    @Setter
    private float flySpeed = DEFAULT_FLY_SPEED;
    /**
     * Player's client-side vertical fly speed. Remember to call getAdventureSettings().update() if changed.
     */
    @Getter
    @Setter
    private float verticalFlySpeed = DEFAULT_VERTICAL_FLY_SPEED;

    private float speedToSend = DEFAULT_SPEED;

    private int exp = 0;
    private int expLevel = 0;

    protected PlayerFood foodData = null;

    private Entity killer = null;

    private final AtomicReference<Locale> locale = new AtomicReference<>(null);

    private int hash;

    private String buttonText = "";

    protected boolean enableClientCommand = true;

    private BlockEnderChest viewingEnderChest = null;

    protected LoginChainData loginChainData;

    public Block breakingBlock = null;
    protected PlayerBlockActionData lastBlockAction;
    protected long breakingBlockTime = 0;
    protected double blockBreakProgress = 0;
    public BlockFace breakingBlockFace = null;

    private static final int NO_SHIELD_DELAY = 10;
    private int noShieldTicks;

    public int pickedXPOrb = 0;
    private boolean canPickupXP = true;

    protected int formWindowCount = 0;
    public Map<Integer, FormWindow> formWindows = new Int2ObjectOpenHashMap<>();
    protected Map<Integer, FormWindow> serverSettings = new Int2ObjectOpenHashMap<>();

    protected Map<Long, DummyBossBar> dummyBossBars = new Long2ObjectLinkedOpenHashMap<>();

    protected Cache<String, FormWindowDialog> dialogWindows = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    protected AsyncTask preLoginEventTask = null;
    protected boolean shouldLogin = false;
    protected boolean shouldPack = false;

    private List<UUID> availableEmotes = new ArrayList<>();
    private int lastEmote;
    protected int lastEating;
    private int lastEnderPearlThrow = 20;
    private int lastWindChargeThrow = 10;
    private int lastChorusFruitTeleport = 20;
    public long lastSkinChange = -1;
    protected double lastRightClickTime = 0.0;
    protected BlockVector3 lastRightClickPos = null;
    public EntityFishingHook fishing = null;
    public boolean formOpen;
    public boolean locallyInitialized;
    protected int failedTransactions;
    protected int failedMobEquipmentPacket;
    private int timeSinceRest;
    private boolean inSoulSand;
    private boolean dimensionChangeInProgress;
    private int riptideTicks;

    @Setter
    private boolean needSendData;
    protected boolean needSendAdventureSettings;
    private boolean needSendFoodLevel;
    @Setter
    protected boolean needSendInventory;
    protected boolean needSendHeldItem;
    private boolean needSendRotation;
    private boolean dimensionFix560;
    private boolean needSendUpdateClientInputLocksPacket;

    /**
     * 用于修复1.20.0连续执行despawnFromAll和spawnToAll导致玩家移动不显示问题
     */
    private int lastDespawnFromAllTick;
    /**
     * 用于修复1.20.0连续执行despawnFromAll和spawnToAll导致玩家移动不显示问题
     */
    private boolean needSpawnToAll;

    private Boolean openSignFront = null;

    /**
     * Packets that can be received before the player has logged verified
     */
    private static final List<Integer> PRE_LOGIN_VERIFIED_PACKETS = Arrays.asList(
            ProtocolInfo.toNewProtocolID(ProtocolInfo.BATCH_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.LOGIN_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.CLIENT_TO_SERVER_HANDSHAKE_PACKET)
    );

    /**
     * Packets that can be received before the player has logged in
     */
    private static final List<Integer> PRE_LOGIN_PACKETS = Arrays.asList(
            ProtocolInfo.toNewProtocolID(ProtocolInfo.BATCH_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.LOGIN_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.CLIENT_TO_SERVER_HANDSHAKE_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.REQUEST_CHUNK_RADIUS_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.RESOURCE_PACK_CHUNK_REQUEST_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.CLIENT_CACHE_STATUS_PACKET),
            ProtocolInfo.toNewProtocolID(ProtocolInfo.PACKET_VIOLATION_WARNING_PACKET)
    );

    @Getter
    @Setter
    protected List<PlayerFogPacket.Fog> fogStack = new ArrayList<>();

    private final @NotNull PlayerHandle playerHandle = new PlayerHandle(this);

    /**
     * Default kick message for flying
     */
    public static final String MSG_FLYING_NOT_ENABLED = "Flying is not enabled on this server";

    private boolean lockCameraInput;

    private boolean lockMovementInput;

    public int getStartActionTick() {
        return startAction;
    }

    public void startAction() {
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, true);
        this.startAction = this.server.getTick();
    }

    public void stopAction() {
        this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
        this.startAction = -1;
    }

    public int getLastEnderPearlThrowingTick() {
        return lastEnderPearlThrow;
    }

    public void setLastEnderPearlThrowingTick() {
        this.lastEnderPearlThrow = this.server.getTick();
    }

    public int getLastWindChargeThrowingTick() {
        return lastWindChargeThrow;
    }

    public void setLastWindChargeThrowingTick() {
        this.lastWindChargeThrow = this.server.getTick();
    }

    public int getLastChorusFruitTeleport() {
        return lastChorusFruitTeleport;
    }

    public void onChorusFruitTeleport() {
        this.lastChorusFruitTeleport = this.server.getTick();
    }

    public int getLastInAirTick() {
        return this.lastInAirTick;
    }

    /**
     * Set last spin attack tick to current tick
     */
    public void onSpinAttack(int riptideLevel) {
        this.riptideTicks = 50 + (riptideLevel << 5);
    }

    public BlockEnderChest getViewingEnderChest() {
        return viewingEnderChest;
    }

    public void setViewingEnderChest(BlockEnderChest chest) {
        if (chest == null && this.viewingEnderChest != null) {
            this.viewingEnderChest.getViewers().remove(this);
        } else if (chest != null) {
            chest.getViewers().add(this);
        }
        this.viewingEnderChest = chest;
    }

    public TranslationContainer getLeaveMessage() {
        return new TranslationContainer(TextFormat.YELLOW + "%multiplayer.player.left", this.displayName);
    }

    /**
     * This might disappear in the future.
     * Please use getUniqueId() instead (IP + clientId + name combo, in the future it'll change to real UUID for online auth)
     *
     * @return random client id
     */
    public Long getClientId() {
        return randomClientId;
    }

    @Override
    public boolean isBanned() {
        return this.server.getNameBans().isBanned(this.username);
    }

    @Override
    public void setBanned(boolean value) {
        if (value) {
            this.server.getNameBans().addBan(this.username, null, null, null);
            this.kick(PlayerKickEvent.Reason.NAME_BANNED, "You are banned!");
        } else {
            this.server.getNameBans().remove(this.username);
        }
    }

    @Override
    public boolean isWhitelisted() {
        return this.server.isWhitelisted(this.username.toLowerCase(Locale.ROOT));
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value) {
            this.server.addWhitelist(this.username.toLowerCase(Locale.ROOT));
        } else {
            this.server.removeWhitelist(this.username.toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public Long getFirstPlayed() {
        return this.namedTag != null ? this.namedTag.getLong("firstPlayed") : null;
    }

    @Override
    public Long getLastPlayed() {
        return this.namedTag != null ? this.namedTag.getLong("lastPlayed") : null;
    }

    @Override
    public boolean hasPlayedBefore() {
        return this.playedBefore;
    }

    public AdventureSettings getAdventureSettings() {
        return adventureSettings;
    }

    public void setAdventureSettings(AdventureSettings adventureSettings) {
        this.adventureSettings = adventureSettings.clone(this);
        this.adventureSettings.update();
    }

    public void resetInAirTicks() {
        this.inAirTicks = 0;
    }

    public void setAllowFlight(boolean value) {
        this.adventureSettings.set(Type.ALLOW_FLIGHT, value);
        this.adventureSettings.update();
    }

    public boolean getAllowFlight() {
        return this.adventureSettings.get(Type.ALLOW_FLIGHT);
    }

    public void setAllowModifyWorld(boolean value) {
        this.adventureSettings.set(Type.WORLD_IMMUTABLE, !value);
        this.adventureSettings.set(Type.MINE, value);
        this.adventureSettings.set(Type.BUILD, value);
        this.adventureSettings.update();
    }

    public void setAllowInteract(boolean value) {
        setAllowInteract(value, value);
    }

    public void setAllowInteract(boolean value, boolean containers) {
        this.adventureSettings.set(Type.WORLD_IMMUTABLE, !value);
        this.adventureSettings.set(Type.DOORS_AND_SWITCHED, value);
        this.adventureSettings.set(Type.OPEN_CONTAINERS, containers);
        this.adventureSettings.update();
    }

    public void setAutoJump(boolean value) {
        this.adventureSettings.set(Type.AUTO_JUMP, value);
        this.adventureSettings.update();
    }

    public boolean hasAutoJump() {
        return this.adventureSettings.get(Type.AUTO_JUMP);
    }

    @Override
    public void spawnTo(Player player) {
        if (this.spawned && player.spawned &&
                this.isAlive() && player.isAlive()
                && player.getLevel() == this.level && player.canSee(this)) {
            this.isSpectator();
            if (this.showToOthers) {
                super.spawnTo(player);
                if (this.isSpectator()) {
                    UpdatePlayerGameTypePacket pk = new UpdatePlayerGameTypePacket();
                    pk.gameType = GameType.from(getClientFriendlyGamemode(gamemode));
                    pk.entityId = this.getId();
                    player.dataPacket(pk);
                }
            }
        }
    }

    public boolean getRemoveFormat() {
        return removeFormat;
    }

    public void setRemoveFormat() {
        this.setRemoveFormat(true);
    }

    public void setRemoveFormat(boolean remove) {
        this.removeFormat = remove;
    }

    public boolean canSee(Player player) {
        return !this.hiddenPlayers.containsKey(player.getUniqueId());
    }

    public void hidePlayer(Player player) {
        if (this == player) {
            return;
        }
        this.hiddenPlayers.put(player.getUniqueId(), player);
        player.despawnFrom(this);
    }

    public void showPlayer(Player player) {
        if (this == player) {
            return;
        }
        this.hiddenPlayers.remove(player.getUniqueId());
        if (player.isOnline()) {
            player.spawnTo(this);
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    public boolean canPickupXP() {
        return this.canPickupXP;
    }

    public void setCanPickupXP(boolean canPickupXP) {
        this.canPickupXP = canPickupXP;
    }

    @Override
    public void resetFallDistance() {
        super.resetFallDistance();
        if (this.inAirTicks != 0) {
            this.startAirTicks = 5;
        }
        this.inAirTicks = 0;
    }

    @Override
    public boolean isOnline() {
        return this.connected && this.loggedIn;
    }

    @Override
    public boolean isOp() {
        return this.server.isOp(this.username);
    }

    @Override
    public void setOp(boolean value) {
        if (value == this.isOp()) {
            return;
        }

        if (value) {
            this.server.addOp(this.username);
        } else {
            this.server.removeOp(this.username);
        }

        this.recalculatePermissions();
        this.adventureSettings.update();
        this.sendCommandData();
    }

    /**
     * Set visibility of player's admin status on the player list
     */
    public void setShowAdmin(boolean showAdmin) {
        this.showAdmin = showAdmin;
    }

    /**
     * Get visibility of player's admin status on the player list
     */
    public boolean showAdmin() {
        return this.showAdmin;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return this.perm.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String name) {
        return this.perm != null && this.perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return this.perm.hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return this.addAttachment(plugin, null);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name) {
        return this.addAttachment(plugin, name, null);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.server.getPluginManager().unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this);
        this.server.getPluginManager().unsubscribeFromPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this);

        if (this.perm == null) {
            return;
        }

        this.perm.recalculatePermissions();

        if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this);
        }

        if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this);
        }

        if (this.enableClientCommand && spawned) this.sendCommandData();
    }

    /**
     * Are commands enabled for this player on the client side
     *
     * @return commands enabled
     */
    public boolean isEnableClientCommand() {
        return this.enableClientCommand;
    }

    public void setEnableClientCommand(boolean enable) {
        this.enableClientCommand = enable;
        SetCommandsEnabledPacket pk = new SetCommandsEnabledPacket();
        pk.enabled = enable;
        this.dataPacket(pk);
        if (enable) this.sendCommandData();
    }

    public void sendCommandData() {
        Map<String, CommandDataVersions> data = new HashMap<>();

        for (Command command : this.server.getCommandMap().getCommands().values()) {
            //1.20.0+客户端自带help命令
            if (this.protocol >= ProtocolInfo.v1_20_0_23) {
                if (command instanceof HelpCommand || "help".equalsIgnoreCase(command.getName())) {
                    continue;
                }
            }
            if (!command.testPermissionSilent(this) || !command.isRegistered()) {
                continue;
            }

            data.put(command.getName(), command.generateCustomCommandData(this));
        }

        if (!data.isEmpty()) {
            AvailableCommandsPacket pk = new AvailableCommandsPacket();
            pk.commands = data;
            this.dataPacket(pk);
        }
    }

    @Override
    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    public Player(SourceInterface interfaz, Long clientID, InetSocketAddress socketAddress) {
        super(null, new CompoundTag());
        this.interfaz = interfaz;
        this.networkSession = interfaz.getSession(socketAddress);
        this.perm = new PermissibleBase(this);
        this.server = Server.getInstance();
        this.rawSocketAddress = socketAddress;
        this.socketAddress = socketAddress;
        this.loaderId = Level.generateChunkLoaderId(this);
        this.gamemode = this.server.getSettings().player().defaultGamemode();
        this.setLevel(this.server.getDefaultLevel());
        this.viewDistance = this.server.getSettings().world().viewDistance();
        this.chunkRadius = viewDistance;
        this.boundingBox = new SimpleAxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.addDefaultWindows();
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public Entity asEntity() {
        return this;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public Player asPlayer() {
        return this;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        if (this.spawned) {
            this.server.updatePlayerListData(this.getLoginUuid(), this.getId(), this.displayName, this.getSkin(), this.loginChainData.getXUID());
        }
    }

    @Override
    public void setSkin(Skin skin) {
        super.setSkin(skin);
        if (this.spawned) {
            this.server.updatePlayerListData(this.getLoginUuid(), this.getId(), this.displayName, skin, this.loginChainData.getXUID());
        }
    }

    public String getRawAddress() {
        return this.rawSocketAddress.getAddress().getHostAddress();
    }

    public int getRawPort() {
        return this.rawSocketAddress.getPort();
    }

    public InetSocketAddress getRawSocketAddress() {
        return this.rawSocketAddress;
    }

    public String getAddress() {
        return this.socketAddress.getAddress().getHostAddress();
    }

    public int getPort() {
        return this.socketAddress.getPort();
    }

    public InetSocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    public Position getNextPosition() {
        return this.newPosition != null ? new Position(this.newPosition.x, this.newPosition.y, this.newPosition.z, this.level) : this.getPosition();
    }

    public boolean isSleeping() {
        return this.sleeping != null;
    }

    public int getInAirTicks() {
        return this.inAirTicks;
    }

    /**
     * Returns whether the player is currently using an item (right-click and hold).
     *
     * @return whether the player is currently using an item
     */
    public boolean isUsingItem() {
        return this.getDataFlag(DATA_FLAGS, DATA_FLAG_ACTION) && this.startAction > -1;
    }

    public void setUsingItem(boolean value) {
        this.startAction = value ? this.server.getTick() : -1;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, value);
    }

    public String getButtonText() {
        return this.buttonText;
    }

    public void setButtonText(String text) {
        if (!text.equals(buttonText)) {
            this.buttonText = text;
            this.setDataPropertyAndSendOnlyToSelf(new StringEntityData(Entity.DATA_INTERACTIVE_TAG, this.buttonText));
        }
    }

    public void unloadChunk(int x, int z) {
        this.unloadChunk(x, z, null);
    }

    public void unloadChunk(int x, int z, Level level) {
        level = level == null ? this.level : level;
        long index = Level.chunkHash(x, z);
        if (this.usedChunks.containsKey(index)) {
            for (Entity entity : level.getChunkEntities(x, z).values()) {
                if (entity != this) {
                    entity.despawnFrom(this);
                }
            }

            this.usedChunks.remove(index);
        }
        level.unregisterChunkLoader(this, x, z);
        this.loadQueue.remove(index);
    }

    private void unloadChunks(boolean online) {
        for (long index : this.usedChunks.keySet()) {
            int chunkX = Level.getHashX(index);
            int chunkZ = Level.getHashZ(index);
            this.level.unregisterChunkLoader(this, chunkX, chunkZ);

            for (Entity entity : level.getChunkEntities(chunkX, chunkZ).values()) {
                if (entity != this) {
                    if (online) {
                        entity.despawnFrom(this);
                    } else {
                        entity.hasSpawned.remove(loaderId);
                    }
                }
            }
        }

        this.usedChunks.clear();
        this.loadQueue.clear();
    }

    public Position getSpawn() {
        if (this.spawnBlockPosition != null && this.spawnBlockPosition.isValid()) {
            return this.spawnBlockPosition;
        } else if (this.spawnPosition != null && this.spawnPosition.isValid()) {
            return this.spawnPosition;
        } else {
            return this.server.getDefaultLevel().getSafeSpawn();
        }
    }

    public void checkSpawnBlockPosition() {
        if (this.spawnBlockPosition != null && this.spawnBlockPosition.isValid()) {
            Block spawnBlock = spawnBlockPosition.getLevelBlock();
            if (spawnBlock == null || !isValidRespawnBlock(spawnBlock)) {
                this.spawnBlockPosition = null;
                this.sendMessage(new TranslationContainer(TextFormat.GRAY + "%tile." + (this.getLevel().getDimension() == Level.DIMENSION_OVERWORLD ? "bed" : "respawn_anchor") + ".notValid"));
            }
        }
    }

    protected boolean isValidRespawnBlock(Block block) {
        if (block.getId() == BlockID.RESPAWN_ANCHOR && block.getLevel().getDimension() == Level.DIMENSION_NETHER) {
            BlockRespawnAnchor anchor = (BlockRespawnAnchor) block;
            return anchor.getCharge() > 0;
        }
        if (block.getId() == BlockID.BED_BLOCK && block.getLevel().getDimension() == Level.DIMENSION_OVERWORLD) {
            BlockBed bed = (BlockBed) block;
            return bed.isBedValid();
        }

        return false;
    }

    public void sendChunk(int x, int z, DataPacket packet) {
        if (!this.connected) {
            return;
        }

        this.usedChunks.put(Level.chunkHash(x, z), Boolean.TRUE);

        this.dataPacket(packet);

        this.chunksSent++;

        if (this.spawned) {
            for (Entity entity : this.level.getChunkEntities(x, z).values()) {
                if (this != entity && !entity.closed && entity.isAlive()) {
                    entity.spawnTo(this);
                }
            }
        }

        for (BlockEntity blockEntity : this.level.getChunkBlockEntities(x, z).values()) {
            if (!(blockEntity instanceof BlockEntityItemFrame) && !(blockEntity instanceof BlockEntityCampfire))
                continue;
            ((BlockEntitySpawnable) blockEntity).spawnTo(this);
        }

        if (this.dimensionFix560) {
            this.dimensionFix560 = false;

            PlayerActionPacket playerActionPacket = new PlayerActionPacket();
            playerActionPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_SUCCESS;
            playerActionPacket.entityId = this.getId();
            this.dataPacket(playerActionPacket);
        }
    }

    @Deprecated
    public void sendChunk(int x, int z, int subChunkCount, byte[] payload) {
        log.warn("Player#sendChunk(int x, int z, int subChunkCount, byte[] payload) is deprecated");
        this.sendChunk(x, z, subChunkCount, payload, 0);
    }

    public void sendChunk(int x, int z, int subChunkCount, byte[] payload, int dimension) {
        if (!this.connected) {
            return;
        }

        LevelChunkPacket pk = new LevelChunkPacket();
        pk.chunkX = x;
        pk.chunkZ = z;
        pk.dimension = dimension;
        pk.subChunkCount = subChunkCount;
        pk.data = payload;

        this.sendChunk(x, z, pk);
    }

    protected void sendNextChunk() {
        if (!this.connected) {
            return;
        }

        if (!loadQueue.isEmpty()) {
            int count = 0;
            LongIterator iter = loadQueue.longIterator();
            while (iter.hasNext()) {
                if (count >= server.getSettings().world().chunk().sendingPerTick()) {
                    break;
                }

                long index = iter.nextLong();
                int chunkX = Level.getHashX(index);
                int chunkZ = Level.getHashZ(index);

                ++count;

                try {
                    this.usedChunks.put(index, false);
                    this.level.registerChunkLoader(this, chunkX, chunkZ, false);

                    if (!this.level.populateChunk(chunkX, chunkZ)) {
                        if (this.spawned && this.teleportPosition == null) {
                            continue;
                        } else {
                            break;
                        }
                    }

                    iter.remove();
                } catch (Exception ex) {
                    server.getLogger().logException(ex);
                    return;
                }

                PlayerChunkRequestEvent ev = new PlayerChunkRequestEvent(this, chunkX, chunkZ);
                this.server.getPluginManager().callEvent(ev);
                if (!ev.isCancelled()) {
                    this.level.requestChunk(chunkX, chunkZ, this);
                }
            }
        }

        if (!this.hasSpawnChunks && this.chunksSent >= server.getSettings().world().chunk().spawnChunksThreshold()) {
            this.hasSpawnChunks = true;
            this.sendPlayStatus(PlayStatusPacket.PLAYER_SPAWN);
        }
    }

    protected void doFirstSpawn() {
        this.locallyInitialized = true;

        if (this.spawned) {
            return;
        }

        if (this.protocol >= ProtocolInfo.v1_21_60) {
            this.sendRecipeList();
        }

        this.noDamageTicks = 60;
        this.setAirTicks(400);

        if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this);
        }

        if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this);
        }

        boolean dead = this.getHealth() < 1;
        this.checkSpawnBlockPosition();
        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(this, this.level.getSafeSpawn(dead ? this.getSpawn() : this), true);
        this.server.getPluginManager().callEvent(respawnEvent);

        if (dead) {
            if (this.server.getSettings().world().enableHardcore()) {
                this.setBanned(true);
                return;
            }

            this.teleport(respawnEvent.getRespawnPosition(), null);

            this.setHealth(this.getMaxHealth());
            this.foodData.setFood(20, 20);
            this.sendData(this);
        } else {
            this.setPosition(respawnEvent.getRespawnPosition());
            this.sendPosition(respawnEvent.getRespawnPosition(), yaw, pitch, MovePlayerPacket.MODE_RESET);

            this.getLevel().sendTime(this);
            this.getLevel().sendWeather(this);
        }

        this.spawned = true;

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(this,
                new TranslationContainer(TextFormat.YELLOW + "%multiplayer.player.joined", new String[]{this.displayName})
        );

        this.server.getPluginManager().callEvent(playerJoinEvent);

        if (!playerJoinEvent.getJoinMessage().toString().isBlank()) {
            this.server.broadcastMessage(playerJoinEvent.getJoinMessage());
        }

        for (long index : this.usedChunks.keySet()) {
            int chunkX = Level.getHashX(index);
            int chunkZ = Level.getHashZ(index);
            for (Entity entity : this.level.getChunkEntities(chunkX, chunkZ).values()) {
                if (this != entity && !entity.closed && entity.isAlive()) {
                    entity.spawnTo(this);
                }
            }
        }

        // Prevent PlayerTeleportEvent during player spawn
        //this.teleport(pos, null);

        if (!this.isSpectator()) {
            this.spawnToAll();
        }

        this.sendFogStack();
        this.sendCameraPresets();
    }

    private void sendRecipeList() {
        BatchPacket cachedPacket = Registries.RECIPE.getPacket(protocol);
        if (cachedPacket != null) { // Don't send recipes if they wouldn't work anyways
            this.dataPacket(cachedPacket);
        }
    }

    protected boolean orderChunks() {
        if (!this.connected) {
            return false;
        }

        this.nextChunkOrderRun = 20;

        loadQueue.clear();
        Long2ObjectOpenHashMap<Boolean> lastChunk = new Long2ObjectOpenHashMap<>(this.usedChunks);

        int centerX = (int) this.x >> 4;
        int centerZ = (int) this.z >> 4;

        int spawnThreshold = (int) Math.ceil(Math.sqrt(this.server.getSettings().world().chunk().spawnChunksThreshold()));
        int radius = spawned ? this.chunkRadius : spawnThreshold;
        int radiusSqr = radius * radius;

        long index;
        for (int x = 0; x <= radius; x++) {
            int xx = x * x;
            for (int z = 0; z <= x; z++) {
                int distanceSqr = xx + z * z;
                if (distanceSqr > radiusSqr) continue;

                /* Top right quadrant */
                if (this.usedChunks.get(index = Level.chunkHash(centerX + x, centerZ + z)) != Boolean.TRUE) {
                    this.loadQueue.add(index);
                }
                lastChunk.remove(index);
                /* Top left quadrant */
                if (this.usedChunks.get(index = Level.chunkHash(centerX - x - 1, centerZ + z)) != Boolean.TRUE) {
                    this.loadQueue.add(index);
                }
                lastChunk.remove(index);
                /* Bottom right quadrant */
                if (this.usedChunks.get(index = Level.chunkHash(centerX + x, centerZ - z - 1)) != Boolean.TRUE) {
                    this.loadQueue.add(index);
                }
                lastChunk.remove(index);
                /* Bottom left quadrant */
                if (this.usedChunks.get(index = Level.chunkHash(centerX - x - 1, centerZ - z - 1)) != Boolean.TRUE) {
                    this.loadQueue.add(index);
                }
                lastChunk.remove(index);
                if (x != z) {
                    /* Top right quadrant mirror */
                    if (this.usedChunks.get(index = Level.chunkHash(centerX + z, centerZ + x)) != Boolean.TRUE) {
                        this.loadQueue.add(index);
                    }
                    lastChunk.remove(index);
                    /* Top left quadrant mirror */
                    if (this.usedChunks.get(index = Level.chunkHash(centerX - z - 1, centerZ + x)) != Boolean.TRUE) {
                        this.loadQueue.add(index);
                    }
                    lastChunk.remove(index);
                    /* Bottom right quadrant mirror */
                    if (this.usedChunks.get(index = Level.chunkHash(centerX + z, centerZ - x - 1)) != Boolean.TRUE) {
                        this.loadQueue.add(index);
                    }
                    lastChunk.remove(index);
                    /* Bottom left quadrant mirror */
                    if (this.usedChunks.get(index = Level.chunkHash(centerX - z - 1, centerZ - x - 1)) != Boolean.TRUE) {
                        this.loadQueue.add(index);
                    }
                    lastChunk.remove(index);
                }
            }
        }

        LongIterator keys = lastChunk.keySet().iterator();
        while (keys.hasNext()) {
            index = keys.nextLong();
            this.unloadChunk(Level.getHashX(index), Level.getHashZ(index));
        }

        if (!loadQueue.isEmpty()) {
            NetworkChunkPublisherUpdatePacket packet = new NetworkChunkPublisherUpdatePacket();
            packet.position = this.asBlockVector3();
            packet.radius = this.chunkRadius << 4;
            this.dataPacket(packet);
        }

        return true;
    }

    @Deprecated
    public boolean batchDataPacket(DataPacket packet) {
        return this.dataPacket(packet);
    }

    /**
     * other is identifer
     *
     * @param packet packet to send
     * @return packet successfully sent
     */
    public boolean dataPacket(DataPacket packet) {
        if (!this.connected) {
            return false;
        }

        packet = packet.clone();
        packet.protocol = this.protocol;

        DataPacketSendEvent event = new DataPacketSendEvent(this, packet);
        if (!event.call()) {
            return false;
        }

        if (Nukkit.DEBUG > 2) {
            log.trace("Outbound {}: {}", this.getName(), packet);
        }

        if (packet instanceof BatchPacket) {
            this.networkSession.sendPacket(packet);
        } else {
            this.server.batchPackets(new Player[]{this}, new DataPacket[]{packet});
        }
        return true;
    }

    public int dataPacket(DataPacket packet, boolean needACK) {
        return this.dataPacket(packet) ? 0 : -1;
    }

    /**
     * 0 is true
     * -1 is false
     * other is identifer
     *
     * @param packet packet to send
     * @return packet successfully sent
     */
    public boolean directDataPacket(DataPacket packet) {
        return this.dataPacket(packet);
    }

    public int directDataPacket(DataPacket packet, boolean needACK) {
        return this.directDataPacket(packet) ? 0 : -1;
    }

    public void forceDataPacket(DataPacket packet, Runnable callback) {
        packet.protocol = this.protocol;
        this.networkSession.sendImmediatePacket(packet, (callback == null ? () -> {
        } : callback));
    }

    /**
     * Get network latency
     *
     * @return network latency in milliseconds
     */
    public int getPing() {
        return this.interfaz.getNetworkLatency(this);
    }

    public boolean sleepOn(Vector3 pos) {
        if (!this.isOnline()) {
            return false;
        }

        Entity[] e = this.level.getNearbyEntities(this.boundingBox.grow(2, 1, 2), this);
        for (Entity p : e) {
            if (p instanceof Player) {
                if (((Player) p).sleeping != null && pos.distance(((Player) p).sleeping) <= 0.1) {
                    return false;
                }
            }
        }

        PlayerBedEnterEvent ev;
        this.server.getPluginManager().callEvent(ev = new PlayerBedEnterEvent(this, this.level.getBlock(pos)));
        if (ev.isCancelled()) {
            return false;
        }

        this.sleeping = pos.clone();
        this.teleport(new Location(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, this.yaw, this.pitch, this.headYaw, this.level), null);

        this.setDataProperty(new IntPositionEntityData(DATA_PLAYER_BED_POSITION, (int) pos.x, (int) pos.y, (int) pos.z));
        this.setDataFlag(DATA_PLAYER_FLAGS, DATA_PLAYER_FLAG_SLEEP, true);

        this.setSpawnBlock(pos);
        this.sendTranslation(TextFormat.GRAY + "%tile.bed.respawnSet");

        this.level.sleepTicks = 60;
        this.timeSinceRest = 0;

        return true;
    }

    public void setSpawn(Vector3 pos) {
        Level level;
        if (!(pos instanceof Position)) {
            level = this.level;
        } else {
            level = ((Position) pos).getLevel();
        }
        this.spawnPosition = new Position(pos.x, pos.y, pos.z, level);
        this.sendSpawnPos((int) pos.x, (int) pos.y, (int) pos.z, level.getDimension());
    }

    /**
     * 设置保存玩家重生位置的方块的位置。当未知时可能为空。
     * <p>
     * Sets the position of the block that holds the player respawn position. May be null when unknown.
     * <p>
     * 设置保存着玩家重生位置的方块的位置。可以设置为空。
     *
     * @param spawnBlock 床位或重生锚的位置<br>The position of a bed or respawn anchor
     */
    public void setSpawnBlock(@Nullable Vector3 spawnBlock) {
        if (spawnBlock == null) {
            this.spawnBlockPosition = null;
        } else {
            Level level;
            if (spawnBlock instanceof Position position && position.isValid()) {
                level = position.level;
            } else {
                level = this.level;
            }
            this.spawnBlockPosition = new Position(spawnBlock.x, spawnBlock.y, spawnBlock.z, level);
            SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
            pk.spawnType = SetSpawnPositionPacket.TYPE_PLAYER_SPAWN;
            pk.x = this.spawnBlockPosition.getFloorX();
            pk.y = this.spawnBlockPosition.getFloorY();
            pk.z = this.spawnBlockPosition.getFloorZ();
            pk.dimension = this.spawnBlockPosition.level.getDimension();
            this.dataPacket(pk);
        }
    }

    /**
     * Internal: Send player spawn position
     */
    private void sendSpawnPos(int x, int y, int z, int dimension) {
        SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
        pk.spawnType = SetSpawnPositionPacket.TYPE_PLAYER_SPAWN;
        pk.x = x;
        pk.y = y;
        pk.z = z;
        pk.dimension = dimension;
        this.dataPacket(pk);
    }

    public void stopSleep() {
        if (this.sleeping != null) {
            this.server.getPluginManager().callEvent(new PlayerBedLeaveEvent(this, this.level.getBlock(this.sleeping)));

            this.sleeping = null;
            this.removeDataProperty(DATA_PLAYER_BED_POSITION);
            this.setDataFlag(DATA_PLAYER_FLAGS, DATA_PLAYER_FLAG_SLEEP, false);

            this.level.sleepTicks = 0;

            AnimatePacket pk = new AnimatePacket();
            pk.eid = this.id;
            pk.action = AnimatePacket.Action.WAKE_UP;
            this.dataPacket(pk);
        }
    }

    public Vector3 getSleepingPos() {
        return this.sleeping;
    }

    /**
     * Get player's gamemode
     * <p>
     * 0 = survival
     * 1 = creative
     * 2 = adventure
     * 3 = spectator
     *
     * @return gamemode (number)
     */
    public int getGamemode() {
        return gamemode;
    }

    /**
     * Returns a client-friendly gamemode of the specified real gamemode
     * This function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that includes Survival, Creative and Adventure)
     */
    private int getClientFriendlyGamemode(int gamemode) {
        gamemode &= 0x03;
        if (gamemode == Player.SPECTATOR) {
            return GameType.SPECTATOR.ordinal();
        }
        return gamemode;
    }

    /**
     * Set player's gamemode
     *
     * @param gamemode new gamemode
     * @return gamemode changed
     */
    public boolean setGamemode(int gamemode) {
        return this.setGamemode(gamemode, false, null);
    }

    public boolean setGamemode(int gamemode, boolean clientSide) {
        return this.setGamemode(gamemode, clientSide, null);
    }

    public boolean setGamemode(int gamemode, boolean clientSide, AdventureSettings newSettings) {
        if (gamemode == 5) {
            gamemode = this.server.getDefaultGamemode();
        } else if (gamemode == 6) {
            gamemode = SPECTATOR;
        }
        if (gamemode < 0 || gamemode > 3 || this.gamemode == gamemode) {
            return false;
        }

        if (newSettings == null) {
            newSettings = this.adventureSettings.clone(this);
            newSettings.set(Type.WORLD_IMMUTABLE, (gamemode & 0x02) > 0);
            newSettings.set(Type.MINE, (gamemode & 0x02) <= 0);
            newSettings.set(Type.BUILD, (gamemode & 0x02) <= 0);
            newSettings.set(Type.NO_PVM, gamemode == SPECTATOR);
            newSettings.set(Type.ALLOW_FLIGHT, (gamemode & 0x01) > 0);
            newSettings.set(Type.NO_CLIP, gamemode == SPECTATOR);
            newSettings.set(Type.FLYING, switch (gamemode) {
                case CREATIVE -> newSettings.get(Type.FLYING);
                case SPECTATOR -> true;
                default -> false;
            });
        }

        PlayerGameModeChangeEvent ev;
        this.server.getPluginManager().callEvent(ev = new PlayerGameModeChangeEvent(this, gamemode, newSettings));

        if (ev.isCancelled()) {
            return false;
        }

        this.gamemode = gamemode;

        List<Player> updatePlayers = this.hasSpawned.values().stream().filter(p -> p != this).toList();
        ArrayList<Player> spawnPlayers = new ArrayList<>(this.hasSpawned.values());
        spawnPlayers.removeAll(updatePlayers);

        if (this.isSpectator()) {
            this.keepMovement = true;
            this.onGround = false;
            spawnPlayers.forEach(this::despawnFrom);
        } else {
            this.keepMovement = false;
            spawnPlayers.forEach(this::spawnTo);
        }

        if (!clientSide) {
            UpdatePlayerGameTypePacket pk = new UpdatePlayerGameTypePacket();
            pk.gameType = GameType.from(getClientFriendlyGamemode(gamemode));
            pk.entityId = this.getId();
            Server.broadcastPacket(updatePlayers, pk);
        }

        this.namedTag.putInt("playerGameType", this.gamemode);

        if (!clientSide) {
            SetPlayerGameTypePacket pk = new SetPlayerGameTypePacket();
            pk.gamemode = getClientFriendlyGamemode(gamemode);
            this.dataPacket(pk);
        }

        if (this.isSpectator()) {
            // Tp not on ground to not fly slowly
            this.teleport(this.add(0, 0.0001, 0), null);
        }

        this.setAdventureSettings(ev.getNewAdventureSettings());

        if (this.isSpectator()) {
            this.noClip = true;
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_SILENT, true, false);
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_HAS_COLLISION, false);
        } else {
            this.noClip = false;
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_SILENT, false, false);
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_HAS_COLLISION, true);
        }

        this.resetFallDistance();

        this.inventory.sendContents(this);
        this.inventory.sendHeldItem(this.hasSpawned.values());
        this.offhandInventory.sendContents(this);
        this.offhandInventory.sendContents(this.getViewers().values());

        this.inventory.sendCreativeContents();
        return true;
    }

    /**
     * Send adventure settings
     */
    public void sendSettings() {
        this.adventureSettings.update();
    }

    /**
     * Check player game mode
     *
     * @return whether player is in survival mode
     */
    public boolean isSurvival() {
        return this.gamemode == SURVIVAL;
    }

    /**
     * Check player game mode
     *
     * @return whether player is in creative mode
     */
    public boolean isCreative() {
        return this.gamemode == CREATIVE;
    }

    /**
     * Check player game mode
     *
     * @return whether player is in spectator mode
     */
    public boolean isSpectator() {
        return this.gamemode == SPECTATOR;
    }

    /**
     * Check player game mode
     *
     * @return whether player is in adventure mode
     */
    public boolean isAdventure() {
        return this.gamemode == ADVENTURE;
    }

    @Override
    public Item[] getDrops() {
        if (!this.isCreative() && !this.isSpectator()) {
            if (this.inventory != null) {
                List<Item> drops = new ArrayList<>(this.inventory.getContents().values());
                drops.addAll(this.offhandInventory.getContents().values());
                drops.addAll(this.playerUIInventory.getContents().values());
                return drops.toArray(Item.EMPTY_ARRAY);
            }
            return Item.EMPTY_ARRAY;
        }

        return Item.EMPTY_ARRAY;
    }

    @Override
    protected void checkGroundState(double movX, double movY, double movZ, double dx, double dy, double dz) {
        if (!this.onGround || movX != 0 || movY != 0 || movZ != 0) {
            boolean onGround = false;

            AxisAlignedBB bb = this.boundingBox.clone();
            bb.setMaxY(bb.getMinY() + 0.5);
            bb.setMinY(bb.getMinY() - 1);

            AxisAlignedBB realBB = this.boundingBox.clone();
            realBB.setMaxY(realBB.getMinY() + 0.1);
            realBB.setMinY(realBB.getMinY() - 0.2);

            int minX = NukkitMath.floorDouble(bb.getMinX());
            int minY = NukkitMath.floorDouble(bb.getMinY());
            int minZ = NukkitMath.floorDouble(bb.getMinZ());
            int maxX = NukkitMath.ceilDouble(bb.getMaxX());
            int maxY = NukkitMath.ceilDouble(bb.getMaxY());
            int maxZ = NukkitMath.ceilDouble(bb.getMaxZ());

            for (int z = minZ; z <= maxZ; ++z) {
                for (int x = minX; x <= maxX; ++x) {
                    for (int y = minY; y <= maxY; ++y) {
                        Block block = this.level.getBlock(x, y, z, false);

                        if (!block.canPassThrough() && block.collidesWithBB(realBB)) {
                            onGround = true;
                            break;
                        }
                    }
                }
            }

            this.onGround = onGround;
        }

        this.isCollided = this.onGround;
    }

    @Override
    protected void checkBlockCollision() {
        if (this.isSpectator()) {
            if (this.blocksAround == null) {
                this.blocksAround = new ArrayList<>();
            }
            if (this.collisionBlocks == null) {
                this.collisionBlocks = new ArrayList<>();
            }
            return;
        }

        boolean portal = false;
        boolean endPortal = false;
        boolean scaffolding = false;
        boolean powderSnow = false;

        for (Block block : this.getCollisionBlocks()) {
            switch (block.getId()) {
                case Block.NETHER_PORTAL:
                    portal = true;
                    continue;
                case Block.END_PORTAL:
                    endPortal = true;
                    continue;
                case Block.SCAFFOLDING:
                    scaffolding = true;
                    break;
                case Block.POWDER_SNOW:
                    powderSnow = true;
                    break;
            }

            block.onEntityCollide(this);
            block.getLevelBlockAtLayer(1).onEntityCollide(this);
        }

        this.setDataFlag(DATA_FLAGS_EXTENDED, DATA_FLAG_IN_SCAFFOLDING, scaffolding);

        AxisAlignedBB scanBoundingBox = this.boundingBox.getOffsetBoundingBox(0, -0.125, 0);
        scanBoundingBox.setMaxY(this.boundingBox.getMinY());
        Block[] scaffoldingUnder = this.level.getCollisionBlocks(scanBoundingBox, true, true, b -> b.getId() == BlockID.SCAFFOLDING);
        this.setDataFlag(DATA_FLAGS_EXTENDED, DATA_FLAG_OVER_SCAFFOLDING, scaffoldingUnder.length > 0);

        if (endPortal) {
            inEndPortalTicks++;
        } else {
            this.inEndPortalTicks = 0;
        }

        if (server.getSettings().world().enableEnd() && inEndPortalTicks == (this.gamemode == CREATIVE ? 1 : 80)) {
            EntityPortalEnterEvent ev = new EntityPortalEnterEvent(this, EntityPortalEnterEvent.PortalType.END);

            if (this.portalPos == null) {
                ev.setCancelled();
            }

            this.getServer().getPluginManager().callEvent(ev);

            if (!ev.isCancelled()) {
                if (this.getLevel().isEnd) {
                    if (this.server.getSettings().features().vanillaPortals() && this.getSpawn().getLevel().getDimension() == Level.DIMENSION_OVERWORLD) {
                        this.teleport(this.getSpawn(), TeleportCause.END_PORTAL);
                    } else {
                        this.teleport(this.getServer().getDefaultLevel().getSafeSpawn(), TeleportCause.END_PORTAL);
                    }
                } else {
                    Level end = this.getServer().getLevelByName("the_end");
                    if (end != null) {
                        this.teleport(end.getSafeSpawn(), TeleportCause.END_PORTAL);
                    }
                }
            }
        }

        if (portal) {
            this.inPortalTicks++;
        } else {
            this.inPortalTicks = 0;
            this.portalPos = null;
        }

        if (this.server.getSettings().world().enableNether()) {
            if (this.server.getSettings().features().vanillaPortals() &&
                    (this.inPortalTicks == 40 || this.inPortalTicks == 10 && this.gamemode == CREATIVE) && this.portalPos == null) {
                Position portalPos = this.level.calculatePortalMirror(this);
                if (portalPos == null) {
                    return;
                }

                for (int x = -1; x < 2; x++) {
                    for (int z = -1; z < 2; z++) {
                        int chunkX = (portalPos.getFloorX() >> 4) + x, chunkZ = (portalPos.getFloorZ() >> 4) + z;
                        FullChunk chunk = portalPos.level.getChunk(chunkX, chunkZ, false);
                        if (chunk == null || !(chunk.isGenerated() || chunk.isPopulated())) {
                            portalPos.level.generateChunk(chunkX, chunkZ, true);
                        }
                    }
                }
                this.portalPos = portalPos;
            }

            if (this.inPortalTicks == this.server.getSettings().world().portalTicks() || (this.server.getSettings().features().vanillaPortals() && this.inPortalTicks == 25 && this.gamemode == CREATIVE)) {
                EntityPortalEnterEvent ev = new EntityPortalEnterEvent(this, EntityPortalEnterEvent.PortalType.NETHER);
                this.getServer().getPluginManager().callEvent(ev);

                if (ev.isCancelled()) {
                    this.portalPos = null;
                    return;
                }

                if (server.getSettings().features().vanillaPortals()) {
                    this.inPortalTicks = 81;
                    this.getServer().getScheduler().scheduleAsyncTask(InternalPlugin.INSTANCE, new AsyncTask() {
                        @Override
                        public void onRun() {
                            Position foundPortal = BlockNetherPortal.findNearestPortal(portalPos);
                            getServer().getScheduler().scheduleTask(InternalPlugin.INSTANCE, () -> {
                                if (foundPortal == null) {
                                    BlockNetherPortal.spawnPortal(portalPos);
                                    teleport(portalPos.add(1.5, 1, 0.5), TeleportCause.NETHER_PORTAL);
                                } else {
                                    teleport(BlockNetherPortal.getSafePortal(foundPortal), TeleportCause.NETHER_PORTAL);
                                }
                                portalPos = null;
                            });
                        }
                    });
                } else {
                    if (this.getLevel().getDimension() == Level.DIMENSION_NETHER) {
                        this.teleport(this.getServer().getDefaultLevel().getSafeSpawn(), TeleportCause.NETHER_PORTAL);
                    } else {
                        Level nether = this.getServer().getNetherWorld(this.level.getName());
                        if (nether != null) {
                            this.teleport(nether.getSafeSpawn(), TeleportCause.NETHER_PORTAL);
                        }
                    }
                }
            }
        }

        if (this.getFreezingTicks() < 140 && powderSnow) {
            if (getFreezingTicks() == 0) {
                this.setSprinting(false);
            }
            this.addFreezingTicks(1);
            EntityFreezeEvent event = new EntityFreezeEvent(this);
            this.server.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.addMovementSpeedModifier(new EntityMovementSpeedModifier("minecraft:freezing", getFreezingTicks() * (float) -3.58e-4, EntityMovementSpeedModifier.Operation.ADD));
            }
        }
        if (!powderSnow) {
            if (this.getFreezingTicks() > 0) {
                this.addFreezingTicks(-1);
                this.addMovementSpeedModifier(new EntityMovementSpeedModifier("minecraft:freezing", getFreezingTicks() * (float) -3.58e-4, EntityMovementSpeedModifier.Operation.ADD));
            } else {
                this.removeMovementSpeedModifier("minecraft:freezing");
            }
        }

        if (this.getFreezingTicks() == 140 && this.getServer().getTick() % 40 == 0) {
            this.attack(new EntityDamageEvent(this, DamageCause.FREEZING, getFrostbiteInjury()));
        }
    }

    /**
     * Internal: Check nearby entities and try to pick them up
     */
    protected void checkNearEntities() {
        Entity[] e = this.level.getNearbyEntities(this.boundingBox.grow(1, 0.5, 1), this);
        for (Entity entity : e) {
            //entity.scheduleUpdate();

            if (!entity.isAlive()) {
                continue;
            }

            this.pickupEntity(entity, true);
        }
    }

    protected void handleMovement(Vector3 clientPos) {
        if (!this.isAlive() || !this.spawned || this.teleportPosition != null || this.isSleeping()) {
            return;
        }

        double distanceSquared = clientPos.distanceSquared(this);
        if (distanceSquared == 0) {
            if (this.lastYaw != this.yaw || this.lastPitch != this.pitch) {
                if (!this.firstMove) {
                    Location from = new Location(this.x, this.y, this.z, this.lastYaw, this.lastPitch, this.level);
                    Location to = this.getLocation();

                    PlayerMoveEvent moveEvent = new PlayerMoveEvent(this, from, to);
                    this.server.getPluginManager().callEvent(moveEvent);

                    if (moveEvent.isCancelled()) {
                        this.teleport(from, null);
                        return;
                    }

                    this.lastYaw = to.yaw;
                    this.lastPitch = to.pitch;

                    if (!to.equals(moveEvent.getTo())) { // If plugins modify the destination
                        this.teleport(moveEvent.getTo(), null);
                    } else {
                        this.needSendRotation = true;
                    }
                } else {
                    this.lastYaw = this.yaw;
                    this.lastPitch = this.pitch;
                    this.needSendRotation = true;
                    this.firstMove = false;
                }
            }

            if (this.speed == null) speed = new Vector3(0, 0, 0);
            else this.speed.setComponents(0, 0, 0);
            return;
        }

        int maxDist = 9;
        if (this.riptideTicks > 95 || clientPos.y - this.y < 2 || this.isOnLadder()) { // TODO: Remove ladder/vines check when block collisions are fixed
            maxDist = 49;
        }

        if (distanceSquared > maxDist) {
            this.revertClientMotion(this);
            server.getLogger().debug(username + ": distanceSquared=" + distanceSquared + " > maxDist=" + maxDist);
            return;
        }

        if (this.chunk == null || !this.chunk.isGenerated()) {
            BaseFullChunk chunk = this.level.getChunk(clientPos.getChunkX(), clientPos.getChunkZ(), false);
            if (chunk == null || !chunk.isGenerated()) {
                this.nextChunkOrderRun = 0;
                this.revertClientMotion(this);
                return;
            } else {
                if (this.chunk != null) {
                    this.chunk.removeEntity(this);
                }
                this.chunk = chunk;
            }
        }

        double dx = clientPos.x - this.x;
        double dy = clientPos.y - this.y;
        double dz = clientPos.z - this.z;

        //the client likes to clip into blocks like stairs, but we do full server-side prediction of that without
        //help from the client's position changes, so we deduct the expected clip height from the moved distance.
        dy += this.ySize * (1 - STEP_CLIP_MULTIPLIER); // FIXME: ySize is always 0

        if (this.checkMovement && this.riptideTicks <= 0 && this.riding == null && !this.isGliding() && !this.getAllowFlight()) {
            double hSpeed = dx * dx + dz * dz;
            if (hSpeed > MAXIMUM_SPEED) {
                PlayerInvalidMoveEvent ev;
                this.getServer().getPluginManager().callEvent(ev = new PlayerInvalidMoveEvent(this, true));
                if (!ev.isCancelled()) {
                    server.getLogger().debug(username + ": hSpeed=" + hSpeed + " > MAXIMUM_SPEED=" + MAXIMUM_SPEED);
                    this.revertClientMotion(this);
                    return;
                }
            }
        }

        // Replacement for this.fastMove(dx, dy, dz) start
        if (this.isSpectator() || !this.level.hasCollision(this, this.boundingBox.getOffsetBoundingBox(dx, dy, dz).shrink(0.1, this.getStepHeight(), 0.1), false)) {
            this.x = clientPos.x;
            this.y = clientPos.y;
            this.z = clientPos.z;

            this.boundingBox.setBounds(this.x - 0.3, this.y, this.z - 0.3, this.x + 0.3, this.y + this.getHeight(), this.z + 0.3);
        }

        this.checkChunks();

        if (!this.isSpectator() && (!this.onGround || dy != 0)) {
            AxisAlignedBB bb = this.boundingBox.clone();
            bb.setMinY(bb.getMinY() - 0.75);

            // Hack: fix fall damage from walls while falling
            if (Math.abs(dy) > 0.01) {
                bb.setMinX(bb.getMinX() + 0.1);
                bb.setMaxX(bb.getMaxX() - 0.1);
                bb.setMinZ(bb.getMinZ() + 0.1);
                bb.setMaxZ(bb.getMaxZ() - 0.1);
            }

            this.onGround = this.level.hasCollisionBlocks(this, bb);
        }

        this.isCollided = this.onGround;
        this.updateFallState(this.onGround);
        // Replacement for this.fastMove(dx, dy, dz) end

        double corrX = this.x - clientPos.getX();
        double corrY = this.y - clientPos.getY();
        double corrZ = this.z - clientPos.getZ();

        double yS = this.getStepHeight() + this.ySize;
        if (corrY >= -yS || corrY <= yS) {
            corrY = 0;
        }

        // 瞬移检测
        Location from = new Location(
                this.lastX,
                this.lastY,
                this.lastZ,
                this.lastYaw,
                this.lastPitch,
                this.level);
        Location to = this.getLocation();

        if (!this.firstMove) {
            PlayerMoveEvent moveEvent = new PlayerMoveEvent(this, from, to);
            this.server.getPluginManager().callEvent(moveEvent);

            if (moveEvent.isCancelled()) {
                this.teleport(from, null);
                return;
            }

            this.lastX = to.x;
            this.lastY = to.y;
            this.lastZ = to.z;

            this.lastYaw = to.yaw;
            this.lastPitch = to.pitch;

            this.blocksAround = null;
            this.collisionBlocks = null;

            if (!to.equals(moveEvent.getTo())) { // If plugins modify the destination
                if (this.getGamemode() != Player.SPECTATOR)
                    this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, moveEvent.getTo().clone(), VanillaVibrationTypes.TELEPORT));
                this.teleport(moveEvent.getTo(), null);
            } else {
                if (this.getGamemode() != Player.SPECTATOR && (lastX != to.x || lastY != to.y || lastX != to.z)) {
                    if (this.isOnGround() && this.isGliding()) {
                        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this, VanillaVibrationTypes.ELYTRA_GLIDE));
                    } else if (this.isOnGround() && !(this.getSide(BlockFace.DOWN).getLevelBlock().hasBlockTag(BlockInternalTags.VIBRATION_DAMPER)) && !this.isSneaking()) {
                        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this, VanillaVibrationTypes.STEP));
                    } else if (this.isSwimming()) {
                        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this.getLocation().clone(), VanillaVibrationTypes.SWIM));
                    }
                }
                this.broadcastMovement();
            }
        } else {
            this.lastX = to.x;
            this.lastY = to.y;
            this.lastZ = to.z;

            this.lastYaw = to.yaw;
            this.lastPitch = to.pitch;

            this.firstMove = false;
        }

        if (this.speed == null) {
            speed = new Vector3(from.x - to.x, from.y - to.y, from.z - to.z);
        } else {
            this.speed.setComponents(from.x - to.x, from.y - to.y, from.z - to.z);
        }

        if (this.riding == null && this.inventory != null) {
            if (this.getFoodData().isEnabled() && this.server.getDifficulty() != Difficulty.PEACEFUL && distanceSquared >= 0.05) {
                double jump = 0;
                double swimming = this.isInsideOfWater() ? 0.015 * distanceSquared : 0;
                double distance2 = distanceSquared;
                if (swimming != 0) {
                    distance2 = 0;
                }
                if (this.isSprinting()) {
                    if (this.inAirTicks == 3 && swimming == 0) {
                        jump = 0.2;
                    }
                    this.getFoodData().exhaust(0.1 * distance2 + jump + swimming);
                } else if (this.isSneaking() && this.inAirTicks == 3) {
                    jump = 0.05;
                    this.getFoodData().exhaust(jump);
                } else {
                    if (this.inAirTicks == 3 && swimming == 0) {
                        jump = 0.05;
                    }
                    this.getFoodData().exhaust(jump + swimming);
                }
            }

            this.handleEnchantmentInMove();
        }

        this.forceMovement = null;
        if (distanceSquared != 0 && this.nextChunkOrderRun > 20) {
            this.nextChunkOrderRun = 20;
        }
        this.needSendRotation = false; // Sent with movement

        this.resetClientMovement();
    }

    protected void handleEnchantmentInMove() {
        Item boots = this.inventory.getBootsFast();

        Enchantment frostWalker = boots.getEnchantment(Enchantment.ID_FROST_WALKER);
        if (frostWalker != null && frostWalker.getLevel() > 0 && !this.isSpectator() && this.y >= this.level.getMinBlockY() && this.y <= this.level.getMaxBlockY()) {
            int radius = 2 + frostWalker.getLevel();
            for (int coordX = this.getFloorX() - radius; coordX < this.getFloorX() + radius + 1; coordX++) {
                for (int coordZ = this.getFloorZ() - radius; coordZ < this.getFloorZ() + radius + 1; coordZ++) {
                    Block up = level.getBlock(coordX, this.getFloorY(), coordZ);
                    Block block = level.getBlock(coordX, this.getFloorY() - 1, coordZ);
                    if (block instanceof BlockWater water && up.isAir()) {
                        if (water.getFluidHeightPercent() < 0.15) {
                            WaterFrostEvent ev = new WaterFrostEvent(block);
                            server.getPluginManager().callEvent(ev);
                            if (!ev.isCancelled()) {
                                level.setBlock(block, Block.get(Block.FROSTED_ICE), true, false);
                                level.scheduleUpdate(level.getBlock(block), ThreadLocalRandom.current().nextInt(20, 40));
                            }
                        }
                    }
                }
            }
        }

        Enchantment soulSpeedEnchantment = boots.getEnchantment(Enchantment.ID_SOUL_SPEED);
        if (soulSpeedEnchantment != null && soulSpeedEnchantment.getLevel() > 0) {
            int down = this.getLevel().getBlockIdAt(chunk, getFloorX(), getFloorY() - 1, getFloorZ());
            if (this.inSoulSand && down != BlockID.SOUL_SAND) {
                this.inSoulSand = false;
                this.removeMovementSpeedModifier("minecraft:soul_speed_enchantment");
            } else if (!this.inSoulSand && down == BlockID.SOUL_SAND) {
                this.inSoulSand = true;
                float soulSpeed = (soulSpeedEnchantment.getLevel() * 0.105f) + 1.3f;
                this.addMovementSpeedModifier(new EntityMovementSpeedModifier("minecraft:soul_speed_enchantment", soulSpeed, EntityMovementSpeedModifier.Operation.MULTIPLY));
            }
        }
    }

    protected void resetClientMovement() {
        this.newPosition = null;
    }

    protected void revertClientMotion(Location originalPos) {
        this.setLastLocation(originalPos);

        Vector3 syncPos = originalPos.add(0, 0.00001, 0);
        this.needSendRotation = false;
        this.sendPosition(syncPos, originalPos.getYaw(), originalPos.getPitch(), MovePlayerPacket.MODE_RESET);
        this.forceMovement = syncPos;

        if (this.speed == null) {
            this.speed = new Vector3(0, 0, 0);
        } else {
            this.speed.setComponents(0, 0, 0);
        }
    }

    protected void setLastLocation(Location location) {
        this.lastX = location.getX();
        this.lastY = location.getY();
        this.lastZ = location.getZ();
        this.lastYaw = location.getYaw();
        this.lastHeadYaw = location.getHeadYaw();
        this.lastPitch = location.getPitch();
    }

    @Override
    public void addMovement(double x, double y, double z, double yaw, double pitch, double headYaw) {
        //this.sendPosition(x, y, z, yaw, pitch, MovePlayerPacket.MODE_NORMAL, this.getViewers().values());
        this.addMovement(x, y, z, yaw, pitch, headYaw, this.getViewers().values());
    }

    public void addMovement(double x, double y, double z, double yaw, double pitch, double headYaw, Collection<Player> viewers) {
        this.sendPosition(x, y, z, yaw, pitch, MovePlayerPacket.MODE_NORMAL, viewers);
    }

    @Override
    public boolean setMotion(Vector3 motion) {
        if (super.setMotion(motion)) {
            if (this.chunk != null && this.spawned) {
                this.addMotion(this.motionX, this.motionY, this.motionZ); // Send to others
                SetEntityMotionPacket pk = new SetEntityMotionPacket();
                pk.eid = this.id;
                pk.motionX = (float) motion.x;
                pk.motionY = (float) motion.y;
                pk.motionZ = (float) motion.z;
                this.dataPacket(pk);
            }

            if (this.motionY > 0) {
                this.startAirTicks = (int) ((-(Math.log(this.getGravity() / (this.getGravity() + this.getDrag() * this.motionY))) / this.getDrag()) * 2 + 5);
            }

            return true;
        }

        return false;
    }

    /**
     * Set player's server side motion. Does not send updated motion to client.
     *
     * @param motion new motion vector
     */
    public void setMotionLocally(Vector3 motion) {
        if (!this.justCreated) {
            EntityMotionEvent ev = new EntityMotionEvent(this, motion);
            ev.call();
            if (ev.isCancelled()) {
                return;
            }
        }

        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;

        if (this.motionY > 0) {
            this.startAirTicks = (int) ((-(Math.log(this.getGravity() / (this.getGravity() + this.getDrag() * this.motionY))) / this.getDrag()) * 2 + 5);
        }
    }

    /**
     * Send all default attributes
     */
    public void sendAttributes() {
        UpdateAttributesPacket pk = new UpdateAttributesPacket();
        pk.entityId = this.getId();
        pk.entries = new Attribute[]{
                Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(this.getMaxHealth()).setValue(health > 0 ? (health < getMaxHealth() ? health : getMaxHealth()) : 0),
                Attribute.getAttribute(Attribute.MAX_HUNGER).setValue(this.foodData.getFood()).setDefaultValue(this.foodData.getMaxFood()),
                Attribute.getAttribute(Attribute.MOVEMENT_SPEED).setValue(this.getMovementSpeed()).setDefaultValue(this.getMovementSpeed()),
                Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(this.expLevel),
                Attribute.getAttribute(Attribute.EXPERIENCE).setValue(((float) this.exp) / calculateRequireExperience(this.expLevel))
        };
        this.dataPacket(pk);
    }

    public void sendFogStack() {
        PlayerFogPacket pk = new PlayerFogPacket();
        pk.setFogStack(this.fogStack);
        this.dataPacket(pk);
    }

    public void sendCameraPresets() {
        if (this.protocol < ProtocolInfo.v1_20_0_23) {
            return;
        }
        CameraPresetsPacket pk = new CameraPresetsPacket();
        pk.getPresets().addAll(CameraPresetManager.getPresets().values());
        this.dataPacket(pk);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (!this.loggedIn) {
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;

        if (tickDiff <= 0) {
            return true;
        }

        this.lastUpdate = currentTick;

        this.failedTransactions = 0;
        if (currentTick % 20 == 0) {
            this.failedMobEquipmentPacket = 0;
        }

        if (this.riptideTicks > 0) {
            this.riptideTicks -= tickDiff;
        }

        if (this.fishing != null && this.age % 20 == 0) {
            if (this.distanceSquared(fishing) > 1089) { // 33 blocks
                this.stopFishing(false);
            }
        }

        if (!this.isAlive() && this.spawned) {
            //++this.deadTicks;
            //if (this.deadTicks >= 10) {
            this.despawnFromAll(); // HACK: fix "dead" players
            //}
            return true;
        }

        if (this.spawned) {
            if (this.needSpawnToAll) {
                this.needSpawnToAll = false;
                this.spawnToAll();
            }

            while (!this.clientMovements.isEmpty()) {
                this.handleMovement(this.clientMovements.poll());
            }

            if (this.needSendRotation) {
                this.broadcastMovement();
                this.needSendRotation = false;
            }

            this.motionX = this.motionY = this.motionZ = 0; // HACK: fix player knockback being messed up

            if (!this.isSpectator() && this.isAlive()) {
                this.checkNearEntities();
            }

            this.entityBaseTick(tickDiff);

            if (this.server.getDifficulty() == Difficulty.PEACEFUL && this.level.getGameRules().getBoolean(GameRule.NATURAL_REGENERATION)) {
                if (this.getHealth() < this.getMaxHealth() && this.age % 20 == 0) {
                    this.heal(1);
                }
            }

            if (this.isOnFire() && this.lastUpdate % 10 == 0) {
                if (this.isCreative() && !this.isInsideOfFire()) {
                    this.extinguish();
                } else if (this.getLevel().isRaining() && this.canSeeSky()) {
                    this.extinguish();
                }
            }

            if (!this.isSpectator() && this.speed != null) {
                if (this.onGround) {
                    if (this.isGliding()) {
                        this.setGliding(false);
                    }
                    this.resetFallDistance();
                } else {
                    this.lastInAirTick = server.getTick();

                    if (this.checkMovement && !this.isGliding() && !server.getSettings().player().allowFlight() && this.inAirTicks > 20 && !this.getAllowFlight() && !this.isSleeping() && !this.isImmobile() && !this.isSwimming() && this.riding == null && !this.hasEffect(EffectType.LEVITATION) && !this.hasEffect(EffectType.SLOW_FALLING)) {
                        double expectedVelocity = (-this.getGravity()) / ((double) this.getDrag()) - ((-this.getGravity()) / ((double) this.getDrag())) * FastMath.exp(-((double) this.getDrag()) * ((double) (this.inAirTicks - this.startAirTicks)));
                        double diff = (this.speed.y - expectedVelocity) * (this.speed.y - expectedVelocity);

                        if (this.isOnLadder()) {
                            this.resetFallDistance();
                        } else {
                            if (diff > 2 && expectedVelocity < this.speed.y && this.speed.y != 0) {
                                if (this.inAirTicks < 150) {
                                    PlayerInvalidMoveEvent ev = new PlayerInvalidMoveEvent(this, true);
                                    this.getServer().getPluginManager().callEvent(ev);
                                    if (!ev.isCancelled()) {
                                        this.startAirTicks = this.inAirTicks - 5;
                                        this.setMotion(new Vector3(0, expectedVelocity, 0));
                                    }
                                } else if (this.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server", true, "type=MOVE, expectedVelocity=" + expectedVelocity + ", diff=" + diff + ", speed.y=" + speed.y)) {
                                    return false;
                                }
                            }
                        }
                    }

                    if (this.y > highestPosition) {
                        this.highestPosition = this.y;
                    }

                    // Wiki: 使用鞘翅滑翔时在垂直高度下降率低于每刻 0.5 格的情况下，摔落高度被重置为 1 格。
                    // Wiki: 玩家在较小的角度和足够低的速度上着陆不会受到坠落伤害。着陆时临界伤害角度为50°，伤害值等同于玩家从滑行的最高点直接摔落到着陆点受到的伤害。
                    if (this.isSwimming() || this.isGliding() && Math.abs(this.speed.y) < 0.5 && this.getPitch() <= 40) {
                        this.resetFallDistance();
                    } else if (this.isGliding()) {
                        this.resetInAirTicks();
                    } else {
                        ++this.inAirTicks;
                    }
                }

                if (this.getFoodData() != null) {
                    this.getFoodData().tick(tickDiff);
                }

                //鞘翅检查和耐久计算
                if (this.isGliding()) {
                    PlayerInventory playerInventory = this.getInventory();
                    if (playerInventory != null) {
                        Item chestplate = playerInventory.getChestplateFast();
                        if ((chestplate == null || chestplate.getId() != ItemID.ELYTRA)) {
                            this.setGliding(false);
                        } else if (!chestplate.isUnbreakable() && ((this.gamemode & 0x01) == 0 && this.age % (20 * (chestplate.getEnchantmentLevel(Enchantment.ID_DURABILITY) + 1)) == 0)) {
                            int newDamage = chestplate.getDamage() + 1;
                            if (newDamage < chestplate.getMaxDurability()) {
                                chestplate.setDamage(newDamage);
                                playerInventory.setChestplate(chestplate);
                            } else {
                                this.setGliding(false);
                            }
                        }
                    }
                }
            }
        }

        this.checkTeleportPosition();

        /*if (currentTick % 20 == 0) {
            this.checkInteractNearby();
        }*/

        if (this.spawned && !this.dummyBossBars.isEmpty() && currentTick % 100 == 0) {
            this.dummyBossBars.values().forEach(DummyBossBar::updateBossEntityPosition);
        }

        this.updateBlockingFlag();

        if (!this.isSleeping()) {
            this.timeSinceRest++;
        }

        if (protocol >= ProtocolInfo.v1_20_10_21) {
            if (this.age % 200 == 0) {
                this.dataPacket(new NetworkStackLatencyPacket());
            }
        }

        return true;
    }

    private void updateBlockingFlag() {
        boolean shouldBlock = getNoShieldTicks() == 0
                && (this.isSneaking() || getRiding() != null)
                && (this.getInventory().getItemInHand() instanceof ItemShield || this.getOffhandInventory().getItem(0) instanceof ItemShield);

        if (isBlocking() != shouldBlock) {
            this.setBlocking(shouldBlock);
        }

        /*boolean shieldInHand = this.getInventory().getItemInHandFast() instanceof ItemShield;
        boolean shieldInOffhand = this.getOffhandInventory().getItemFast(0) instanceof ItemShield;
        if (isBlocking()) {
            if (!isSneaking() || (!shieldInHand && !shieldInOffhand)) {
                this.setBlocking(false);
            }
        } else if (isSneaking() && (shieldInHand || shieldInOffhand)) {
            this.setBlocking(true);
        }*/
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        //解决插件异步卸载世界导致的问题
        if (this.level == null || this.level.getProvider() == null) {
            log.warn("Player {} has no valid level", this.getName());
            Level defaultLevel = this.server.getDefaultLevel();
            if (this.level == defaultLevel || defaultLevel == null || defaultLevel.getProvider() == null) {
                this.close(this.getLeaveMessage(), "Default level unload");
            } else {
                this.teleport(defaultLevel.getSafeSpawn(), null);
            }
        }

        boolean hasUpdated = false;
        if (isUsingItem()) {
            if (noShieldTicks < NO_SHIELD_DELAY) {
                noShieldTicks = NO_SHIELD_DELAY;
                hasUpdated = true;
            }
        } else {
            if (noShieldTicks > 0) {
                noShieldTicks -= tickDiff;
                hasUpdated = true;
            }
            if (noShieldTicks < 0) {
                noShieldTicks = 0;
                hasUpdated = true;
            }
        }

        if (this.isMovementServerAuthoritative()) {
            onBlockBreakContinue(breakingBlock, breakingBlockFace);
        }

        return super.entityBaseTick(tickDiff) || hasUpdated;
    }

    public void checkInteractNearby() {
        int interactDistance = isCreative() ? 5 : 3;
        if (canInteract(this, interactDistance)) {
            EntityInteractable e = getEntityPlayerLookingAt(interactDistance);
            if (e != null) {
                String buttonText = e.getInteractButtonText(this);
                if (buttonText == null) {
                    buttonText = "";
                }
                setButtonText(buttonText);
            } else {
                setButtonText("");
            }
        } else {
            setButtonText("");
        }
    }

    /**
     * Returns the Entity the player is looking at currently
     *
     * @param maxDistance the maximum distance to check for entities
     * @return Entity|null    either NULL if no entity is found or an instance of the entity
     */
    public EntityInteractable getEntityPlayerLookingAt(int maxDistance) {
        EntityInteractable entity = null;

        if (temporalVector != null) {
            Entity[] nearbyEntities = level.getNearbyEntities(boundingBox.grow(maxDistance, maxDistance, maxDistance), this);

            try {
                BlockIterator itr = new BlockIterator(level, getPosition(), getDirectionVector(), getEyeHeight(), maxDistance);
                if (itr.hasNext()) {
                    Block block;
                    while (itr.hasNext()) {
                        block = itr.next();
                        entity = getEntityAtPosition(nearbyEntities, block.getFloorX(), block.getFloorY(), block.getFloorZ());
                        if (entity != null) {
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return entity;
    }

    private static EntityInteractable getEntityAtPosition(Entity[] nearbyEntities, int x, int y, int z) {
        for (Entity nearestEntity : nearbyEntities) {
            if (nearestEntity.getFloorX() == x && nearestEntity.getFloorY() == y && nearestEntity.getFloorZ() == z
                    && nearestEntity instanceof EntityInteractable
                    && ((EntityInteractable) nearestEntity).canDoInteraction()) {
                return (EntityInteractable) nearestEntity;
            }
        }
        return null;
    }

    public void checkNetwork() {
        if (!this.isOnline()) {
            return;
        }

        Level nowLevel = this.getLevel();
        nowLevel.providerLock.readLock().lock();

        try {
            if (this.nextChunkOrderRun-- <= 0 || this.chunk == null) {
                this.orderChunks();
            }

            if (!this.loadQueue.isEmpty() || !this.spawned) {
                this.sendNextChunk();
            }
        } finally {
            nowLevel.providerLock.readLock().unlock();
        }
    }

    public boolean canInteract(Vector3 pos, double maxDistance) {
        return this.canInteract(pos, maxDistance, 6.0);
    }

    public boolean canInteract(Vector3 pos, double maxDistance, double maxDiff) {
        if (this.distanceSquared(pos) > maxDistance * maxDistance) {
            return false;
        }

        Vector2 dV = this.getDirectionPlane();
        return (dV.dot(new Vector2(pos.x, pos.z)) - dV.dot(new Vector2(this.x, this.z))) >= -maxDiff;
    }

    public boolean canInteractEntity(Vector3 pos, double maxDistance) {
        if (this.distanceSquared(pos) > Math.pow(maxDistance, 2)) {
            return false;
        }

        Vector2 dV = this.getDirectionPlane();
        return (dV.dot(new Vector2(pos.x, pos.z)) - dV.dot(new Vector2(this.x, this.z))) >= -0.87;
    }

    protected void processLogin() {
        String lowerName = this.username.toLowerCase(Locale.ROOT);
        if (!this.server.isWhitelisted(lowerName)) {
            this.kick(PlayerKickEvent.Reason.NOT_WHITELISTED, server.getSettings().player().whitelistReason());
            return;
        } else if (this.isBanned()) {
            String reason = this.server.getNameBans().getEntires().get(lowerName).getReason();
            this.kick(PlayerKickEvent.Reason.NAME_BANNED, "You are banned!" + (reason.isEmpty() ? "" : (" Reason: " + reason)));
            return;
        } else if (!server.getSettings().network().strongIpBans() && this.server.getIPBans().isBanned(this.getAddress())) {
            this.kick(PlayerKickEvent.Reason.IP_BANNED, "Your IP is banned!");
            return;
        }

        for (Player p : new ArrayList<>(this.server.playerList.values())) {
            if (p != this && p.username != null) {
                if (p.username.equalsIgnoreCase(this.username) || this.getUniqueId().equals(p.getUniqueId())) {
                    p.close("", "disconnectionScreen.loggedinOtherLocation");
                    break;
                }
            }
        }

        CompoundTag nbt;
        File legacyDataFile = new File(server.getDataPath() + "players/" + lowerName + ".dat");
        File dataFile = new File(server.getDataPath() + "players/" + this.uuid.toString() + ".dat");
        if (this.server.getSettings().player().savePlayerDataByUuid()) {
            boolean dataFound = dataFile.exists();
            if (!dataFound && legacyDataFile.exists()) {
                nbt = this.server.getOfflinePlayerData(lowerName, false);
                if (!legacyDataFile.delete()) {
                    this.server.getLogger().warning("Could not delete legacy player data for " + this.username);
                }
            } else {
                nbt = this.server.getOfflinePlayerData(this.uuid, !dataFound);
            }
        } else {
            boolean legacyMissing = !legacyDataFile.exists();
            if (legacyMissing && dataFile.exists()) {
                nbt = this.server.getOfflinePlayerData(this.uuid, false);
            } else {
                nbt = this.server.getOfflinePlayerData(lowerName, legacyMissing);
            }
        }

        if (nbt == null) {
            this.close(this.getLeaveMessage(), "Invalid data");
            return;
        }

        if (loginChainData.isXboxAuthed() || !server.getSettings().network().xboxAuth()) {
            server.updateName(this.uuid, this.username);
        }

        this.playedBefore = (nbt.getLong("lastPlayed") - nbt.getLong("firstPlayed")) > 1;

        nbt.putString("NameTag", this.username);

        this.setExperience(nbt.getInt("EXP"), nbt.getInt("expLevel"));

        if (this.server.getForceGamemode()) {
            this.gamemode = this.server.getSettings().player().defaultGamemode();
            nbt.putInt("playerGameType", this.gamemode);
        } else {
            this.gamemode = nbt.getInt("playerGameType") & 0x03;
        }

        this.adventureSettings = new AdventureSettings(this)
                .set(Type.WORLD_IMMUTABLE, isAdventure() || isSpectator())
                .set(Type.MINE, !isAdventure() && !isSpectator())
                .set(Type.BUILD, !isAdventure() && !isSpectator())
                .set(Type.NO_PVM, this.isSpectator())
                .set(Type.AUTO_JUMP, true)
                .set(Type.ALLOW_FLIGHT, isCreative() || isSpectator())
                .set(Type.NO_CLIP, isSpectator())
                .set(Type.FLYING, isSpectator());

        this.noClip = isSpectator();

        Level level;
        if ((level = this.server.getLevelByName(nbt.getString("Level"))) == null || nbt.getShort("Health") < 1) {
            this.setLevel(this.server.getDefaultLevel());
            nbt.putString("Level", this.level.getName());
            Position sp = this.level.getSpawnLocation();
            nbt.getList("Pos", DoubleTag.class)
                    .add(new DoubleTag("0", sp.x))
                    .add(new DoubleTag("1", sp.y))
                    .add(new DoubleTag("2", sp.z));
        } else {
            this.setLevel(level);
        }

        if (nbt.contains("SpawnLevel")) {
            Level spawnLevel = server.getLevelByName(nbt.getString("SpawnLevel"));
            if (spawnLevel != null) {
                this.spawnPosition = new Position(
                        nbt.getInt("SpawnX"),
                        nbt.getInt("SpawnY"),
                        nbt.getInt("SpawnZ"),
                        level
                );
            }
        }

        if (nbt.contains("SpawnBlockLevel")) {
            Level spawnBlockLevel = server.getLevelByName(nbt.getString("SpawnBlockLevel"));
            if (nbt.contains("SpawnBlockPositionX") && nbt.contains("SpawnBlockPositionY") && nbt.contains("SpawnBlockPositionZ")) {
                this.spawnBlockPosition = new Position(nbt.getInt("SpawnBlockPositionX"), nbt.getInt("SpawnBlockPositionY"), nbt.getInt("SpawnBlockPositionZ"), spawnBlockLevel);
            }
        }

        this.timeSinceRest = nbt.getInt("TimeSinceRest");

        ListTag<StringTag> fogIdentifiers = nbt.getList("fogIdentifiers", StringTag.class);
        ListTag<StringTag> userProvidedFogIds = nbt.getList("userProvidedFogIds", StringTag.class);
        for (int i = 0; i < fogIdentifiers.size(); i++) {
            this.fogStack.add(i, new PlayerFogPacket.Fog(Identifier.tryParse(fogIdentifiers.get(i).data), userProvidedFogIds.get(i).data));
        }

        nbt.putLong("lastPlayed", System.currentTimeMillis() / 1000);

        UUID uuid = getUniqueId();
        nbt.putLong("UUIDLeast", uuid.getLeastSignificantBits());
        nbt.putLong("UUIDMost", uuid.getMostSignificantBits());

        if (this.server.getAutoSave()) {
            if (this.server.getSettings().player().savePlayerDataByUuid()) {
                this.server.saveOfflinePlayerData(this.uuid, nbt, true);
            } else {
                this.server.saveOfflinePlayerData(this.username, nbt, true);
            }
        }

        this.sendPlayStatus(PlayStatusPacket.LOGIN_SUCCESS);

        ListTag<DoubleTag> posList = nbt.getList("Pos", DoubleTag.class);

        super.init(this.level.getChunk((int) posList.get(0).data >> 4, (int) posList.get(2).data >> 4, true), nbt);

        if (!this.namedTag.contains("foodLevel")) {
            this.namedTag.putInt("foodLevel", 20);
        }

        if (!this.namedTag.contains("foodSaturationLevel")) {
            this.namedTag.putFloat("foodSaturationLevel", 20);
        }

        this.foodData = new PlayerFood(this, this.namedTag.getInt("foodLevel"), this.namedTag.getFloat("foodSaturationLevel"));

        if (this.isSpectator()) {
            this.keepMovement = true;
            this.onGround = false;
        }

        this.forceMovement = this.teleportPosition = this.getPosition();

        ResourcePacksInfoPacket infoPacket = new ResourcePacksInfoPacket();

        infoPacket.resourcePackEntries = Arrays.stream(this.server.getResourcePackManager().getResourceStack())
                .filter(pack -> pack.getPackProtocol() <= protocol)
                .toArray(ResourcePack[]::new);

        infoPacket.mustAccept = this.server.getSettings().general().forceResources();
        this.dataPacket(infoPacket);
    }
    protected void completeLoginSequence() {
        if (this.loggedIn) {
            this.server.getLogger().debug("(BUG) Tried to call completeLoginSequence but player is already logged in");
            return;
        }

        PlayerLoginEvent ev;
        this.server.getPluginManager().callEvent(ev = new PlayerLoginEvent(this, "Plugin reason"));
        if (ev.isCancelled()) {
            this.close(this.getLeaveMessage(), ev.getKickMessage());
            return;
        }

        StartGamePacket startGamePacket = new StartGamePacket();
        startGamePacket.entityUniqueId = this.id;
        startGamePacket.entityRuntimeId = this.id;
        startGamePacket.playerGamemode = this.getClientFriendlyGamemode(this.gamemode);
        startGamePacket.x = (float) this.x;
        startGamePacket.y = (float) this.y;
        startGamePacket.z = (float) this.z;
        startGamePacket.yaw = (float) this.yaw;
        startGamePacket.pitch = (float) this.pitch;
        startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
        startGamePacket.worldGamemode = this.getClientFriendlyGamemode(this.gamemode);
        startGamePacket.difficulty = this.server.getDifficulty().getId();
        startGamePacket.spawnX = (int) this.x;
        startGamePacket.spawnY = (int) this.y;
        startGamePacket.spawnZ = (int) this.z;
        startGamePacket.commandsEnabled = this.enableClientCommand;
        startGamePacket.experiments.addAll(this.getExperiments());
        startGamePacket.gameRules = this.getLevel().getGameRules();
        startGamePacket.worldName = this.getServer().getNetwork().getName();
        startGamePacket.version = this.getLoginChainData().getGameVersion();
        startGamePacket.vanillaVersion = Utils.getVersionByProtocol(this.protocol);
        if (this.getLevel().isRaining()) {
            startGamePacket.rainLevel = this.getLevel().getRainTime();
            if (this.getLevel().isThundering()) {
                startGamePacket.lightningLevel = this.getLevel().getThunderTime();
            }
        }
        startGamePacket.isMovementServerAuthoritative = this.isMovementServerAuthoritative();
        startGamePacket.isServerAuthoritativeBlockBreaking = this.isServerAuthoritativeBlockBreaking();
        startGamePacket.playerPropertyData = EntityProperty.getPlayerPropertyCache();
        this.forceDataPacket(startGamePacket, null);

        this.loggedIn = true;
        this.server.getLogger().info(this.getServer().getLanguage().translateString("nukkit.player.logIn",
                TextFormat.AQUA + this.username + TextFormat.WHITE,
                this.getAddress(),
                String.valueOf(this.getPort()),
                this.protocol + " (" + Utils.getVersionByProtocol(this.protocol) + ")"));

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_CAN_CLIMB, true, false);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_CAN_SHOW_NAMETAG, true, false);
        this.setDataProperty(new ByteEntityData(DATA_ALWAYS_SHOW_NAMETAG, 1), false);

        try {
            //注册实体属性
            for (SyncEntityPropertyPacket pk : EntityProperty.getPacketCache()) {
                this.dataPacket(pk);
            }
            ItemComponentPacket itemComponentPacket = new ItemComponentPacket();
            if (this.protocol >= ProtocolInfo.v1_21_60) {
                Collection<ItemComponentPacket.ItemDefinition> vanillaItems = RuntimeItems.getMapping(this.protocol).getVanillaItemDefinitions();
                Set<Entry<String, CustomItemDefinition>> itemDefinitions = Registries.ITEM.getCustomItemDefinition().entrySet();
                List<ItemComponentPacket.ItemDefinition> entries = new ArrayList<>(vanillaItems.size() + itemDefinitions.size());
                entries.addAll(vanillaItems);
                if (this.server.getSettings().features().enableExperimentMode() && !itemDefinitions.isEmpty()) {
                    for (Entry<String, CustomItemDefinition> entry : itemDefinitions) {
                        try {
                            Item item = Item.get(entry.getKey());
                            entries.add(new ItemComponentPacket.ItemDefinition(
                                    entry.getKey(),
                                    item.getNetworkId(this.protocol),
                                    true,
                                    1,
                                    entry.getValue().getNbt(this.protocol)
                            ));
                        } catch (Exception e) {
                            log.error("ItemComponentPacket encoding error", e);
                        }
                    }
                }
                itemComponentPacket.setEntries(entries);
            } else {
                if (this.server.getSettings().features().enableExperimentMode() && !Registries.ITEM.getCustomItemDefinition().isEmpty()) {
                    Map<String, CustomItemDefinition> itemDefinition = Registries.ITEM.getCustomItemDefinition();
                    List<ItemComponentPacket.ItemDefinition> entries = new ArrayList<>(itemDefinition.size());
                    int i = 0;
                    for (var entry : itemDefinition.entrySet()) {
                        try {
                            Item item = Item.get(entry.getKey());
                            entries.add(new ItemComponentPacket.ItemDefinition(
                                    entry.getKey(),
                                    item.getNetworkId(this.protocol),
                                    true,
                                    1,
                                    entry.getValue().getNbt(this.protocol).putShort("minecraft:identifier", i)
                            ));
                            i++;
                        } catch (Exception e) {
                            log.error("ItemComponentPacket encoding error", e);
                        }
                    }
                    itemComponentPacket.setEntries(entries);
                }
            }
            this.dataPacket(itemComponentPacket);
            this.dataPacket(BiomeDefinitionListPacket.getCachedPacket(this.protocol));
            this.dataPacket(new AvailableEntityIdentifiersPacket());

            this.sendSpawnPos((int) this.x, (int) this.y, (int) this.z, this.level.getDimension());
            this.getLevel().sendTime(this);

            SetDifficultyPacket difficultyPacket = new SetDifficultyPacket();
            difficultyPacket.difficulty = this.server.getDifficulty().getId();
            this.dataPacket(difficultyPacket);

            SetCommandsEnabledPacket commandsPacket = new SetCommandsEnabledPacket();
            commandsPacket.enabled = this.isEnableClientCommand();
            this.dataPacket(commandsPacket);

            this.adventureSettings.update();

            GameRulesChangedPacket gameRulesPK = new GameRulesChangedPacket();
            gameRulesPK.gameRulesMap = level.getGameRules().getGameRules();
            this.dataPacket(gameRulesPK);

            this.server.sendFullPlayerListData(this);
            this.sendAttributes();

            this.inventory.sendCreativeContents();
            this.sendAllInventories();
            this.inventory.sendHeldItemIfNotAir(this);

            // BDS sends armor trim templates and materials before the CraftingDataPacket
            TrimDataPacket trimDataPacket = new TrimDataPacket();
            trimDataPacket.getMaterials().addAll(TrimFactory.trimMaterials);
            trimDataPacket.getPatterns().addAll(TrimFactory.trimPatterns);
            this.dataPacket(trimDataPacket);
            if (this.protocol < ProtocolInfo.v1_21_60) {
                this.sendRecipeList();
            }

            if (this.isEnableClientCommand()) {
                this.sendCommandData();
            }

            this.sendPotionEffects(this);

            if (this.isSpectator()) {
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_SILENT, true);
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_HAS_COLLISION, false);
            }
            this.sendData(this, this.dataProperties.clone());

            if (!this.server.getSettings().player().checkOpMovement() && this.isOp()) {
                this.setCheckMovement(false);
            }

            if (this.isOp() || this.hasPermission("nukkit.textcolor")) {
                this.setRemoveFormat(false);
            }

            this.server.onPlayerCompleteLoginSequence(this);
        } catch (Exception e) {
            this.close("", "Internal Server Error");
            this.server.getLogger().logException(e);
        }
    }

    public void handleDataPacket(DataPacket packet) {
        if (!connected) {
            return;
        }

        int pid = packet.packetId();
        if (!loginVerified && !PRE_LOGIN_VERIFIED_PACKETS.contains(pid)) {
            server.getLogger().warning("Ignoring " + packet.getClass().getSimpleName() + " from " + getAddress() + " due to player not verified yet");
            if (unverifiedPackets++ > 100) {
                this.close("", "Too many failed login attempts");
            }
            return;
        }

        if (!loggedIn && !PRE_LOGIN_PACKETS.contains(pid)) {
            server.getLogger().warning("Ignoring " + packet.getClass().getSimpleName() + " from " + username + " due to player not logged in yet");
            return;
        }

        if (packet.protocol == Integer.MAX_VALUE) {
            packet.protocol = this.protocol;
        }

        DataPacketReceiveEvent ev = new DataPacketReceiveEvent(this, packet);
        this.server.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }

        if (Nukkit.DEBUG > 2 /*&& !server.isIgnoredPacket(packet.getClass())*/) {
            log.trace("Inbound {}: {}", this.getName(), packet);
        }

        if (DataPacketManager.canProcess(packet.protocol, packet.getClass())) {
            DataPacketManager.processPacket(this.playerHandle, packet);
        } else {
            log.debug("Data packet processor not found for {}: {}", packet.getClass().getSimpleName(), packet);
        }
    }

    protected void onBlockBreakContinue(Vector3 pos, BlockFace face) {
        if (this.isBreakingBlock()) {
            var time = System.currentTimeMillis();
            Block block = this.level.getBlock(pos, false);

            double miningTimeRequired = this.breakingBlock.calculateBreakTime(this.inventory.getItemInHand(), this);

            if (miningTimeRequired > 0) {
                int breakTick = (int) Math.ceil(miningTimeRequired * 20);
                LevelEventPacket pk = new LevelEventPacket();
                pk.evid = LevelEventPacket.EVENT_BLOCK_UPDATE_BREAK;
                pk.x = (float) this.breakingBlock.x;
                pk.y = (float) this.breakingBlock.y;
                pk.z = (float) this.breakingBlock.z;
                pk.data = 65535 / breakTick;
                this.getLevel().addChunkPacket(this.breakingBlock.getFloorX() >> 4, this.breakingBlock.getFloorZ() >> 4, pk);
                this.level.addParticle(new PunchBlockParticle(pos, block, face));
                if (this.breakingBlock instanceof CustomBlock) {
                    var timeDiff = time - breakingBlockTime;
                    blockBreakProgress += timeDiff / (miningTimeRequired * 1000);
                    if (blockBreakProgress > 0.999999999999999999999) {
                        this.onBlockBreakAbort(pos, face);
                        this.onBlockBreakComplete(pos.asBlockVector3(), face);
                    }
                    breakingBlockTime = time;
                }
            }
        }
    }

    protected void onBlockBreakStart(Vector3 pos, BlockFace face) {
        BlockVector3 blockPos = pos.asBlockVector3();
        long currentBreak = System.currentTimeMillis();
        // HACK: Client spams multiple left clicks so we need to skip them.
        if ((this.lastBreakPosition.equals(blockPos) && (currentBreak - this.lastBreak) < 10) || pos.distanceSquared(this) > 100) {
            return;
        }

        Block target = this.level.getBlock(pos);
        PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face,
                target.getId() == 0 ? Action.LEFT_CLICK_AIR : Action.LEFT_CLICK_BLOCK);
        this.getServer().getPluginManager().callEvent(playerInteractEvent);
        if (playerInteractEvent.isCancelled()) {
            this.needSendHeldItem = true;
            return;
        }

        if (target.onTouch(pos, this.getInventory().getItemInHand(), face, 0, 0, 0, this, playerInteractEvent.getAction()) != 0) {
            return;
        }

        Block block = target.getSide(face);
        int blockId = block.getId();
        if (blockId == Block.FIRE || blockId == Block.SOUL_FIRE) {
            this.level.setBlock(block, Block.get(BlockID.AIR), true);
            this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
            return;
        }

        if (!this.isCreative()) {
            double breakTime = Math.ceil(target.calculateBreakTime(this.inventory.getItemInHand(), this) * 20);
            if (breakTime > 0) {
                LevelEventPacket pk = new LevelEventPacket();
                pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
                pk.x = (float) pos.x;
                pk.y = (float) pos.y;
                pk.z = (float) pos.z;
                pk.data = (int) (65535 / breakTime);
                this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
            }
        }

        if (this.getLevel().isAntiXrayEnabled() && this.getLevel().getAntiXraySystem().isPreDeObfuscate()) {
            this.getLevel().getAntiXraySystem().deObfuscateBlock(this, face, target);
        }

        this.breakingBlockTime = System.currentTimeMillis();
        this.breakingBlock = target;
        this.breakingBlockFace = face;
        this.lastBreak = currentBreak;
        this.lastBreakPosition = blockPos;
    }

    protected void onBlockBreakAbort(Vector3 pos, BlockFace face) {
        if (pos.distanceSquared(this) < 100) {
            LevelEventPacket pk = new LevelEventPacket();
            pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK;
            pk.x = (float) pos.x;
            pk.y = (float) pos.y;
            pk.z = (float) pos.z;
            pk.data = 0;
            this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
        }
        this.blockBreakProgress = 0;
        this.breakingBlock = null;
        this.breakingBlockFace = null;
    }

    protected void onBlockBreakComplete(BlockVector3 blockPos, BlockFace face) {
        if (!this.spawned || !this.isAlive()) {
            return;
        }

        this.resetCraftingGridType();

        Item handItem = this.getInventory().getItemInHand();
        Item clone = handItem.clone();

        boolean canInteract = this.canInteract(blockPos.add(0.5, 0.5, 0.5), this.isCreative() ? 13 : 7);
        if (canInteract) {
            handItem = this.level.useBreakOn(blockPos.asVector3(), face, handItem, this, true);
            if (handItem == null) {
                this.level.sendBlocks(new Player[]{this}, new Vector3[]{blockPos.asVector3()}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

                BlockEntity blockEntity = this.level.getBlockEntity(blockPos.asVector3());
                if (blockEntity instanceof BlockEntitySpawnable) {
                    ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                }
            } else if (this.isSurvival()) {
                this.getFoodData().exhaust(0.005);
                if (handItem.equals(clone) && handItem.getCount() == clone.getCount()) {
                    return;
                }

                if (clone.getId() == handItem.getId() || handItem.getId() == 0) {
                    inventory.setItemInHand(handItem);
                } else {
                    server.getLogger().debug("Tried to set item " + handItem.getId() + " but " + this.username + " had item " + clone.getId() + " in their hand slot");
                }
                inventory.sendHeldItem(this.getViewers().values());
            }
            return;
        }

        inventory.sendContents(this);
        inventory.sendHeldItem(this);

        if (blockPos.distanceSquared(this) < 10000) {
            Vector3 pos = blockPos.asVector3();
            this.level.sendBlocks(this, new Block[]{this.level.getBlock(pos, false)}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
            BlockEntity blockEntity = this.level.getBlockEntityIfLoaded(this.chunk, pos);
            if (blockEntity instanceof BlockEntitySpawnable) {
                ((BlockEntitySpawnable) blockEntity).spawnTo(this);
            }
        }
    }

    /**
     * Sends a chat message as this player
     *
     * @param message message to send
     * @return successful
     */
    public boolean chat(String message) {
        this.resetCraftingGridType();
        this.craftingType = CRAFTING_SMALL;

        if (this.removeFormat) {
            message = TextFormat.clean(message, true);
        }

        int maxMsgLength = 512;

        for (String msg : message.split("\n")) {
            if (!msg.trim().isEmpty() && msg.length() <= maxMsgLength) {
                PlayerChatEvent chatEvent = new PlayerChatEvent(this, msg);
                this.server.getPluginManager().callEvent(chatEvent);
                if (!chatEvent.isCancelled()) {
                    this.server.broadcastMessage(this.getServer().getLanguage().translateString(chatEvent.getFormat(), new String[]{chatEvent.getPlayer().displayName, chatEvent.getMessage()}), chatEvent.getRecipients());
                }
            }
        }

        return true;
    }

    public void emote(EmotePacket emote) {
        this.getViewers().values().forEach(player -> player.dataPacket(emote));
    }

    public boolean kick() {
        return this.kick("");
    }

    public boolean kick(String reason, boolean isAdmin) {
        return this.kick(PlayerKickEvent.Reason.UNKNOWN, reason, isAdmin);
    }

    public boolean kick(String reason) {
        return kick(PlayerKickEvent.Reason.UNKNOWN, reason);
    }

    public boolean kick(PlayerKickEvent.Reason reason) {
        return this.kick(reason, true);
    }

    public boolean kick(PlayerKickEvent.Reason reason, String reasonString) {
        return this.kick(reason, reasonString, true);
    }

    public boolean kick(PlayerKickEvent.Reason reason, boolean isAdmin) {
        return this.kick(reason, reason.toString(), isAdmin);
    }

    public boolean kick(PlayerKickEvent.Reason reason, String reasonString, boolean isAdmin) {
        return kick(reason, reasonString, isAdmin, "");
    }

    public boolean kick(PlayerKickEvent.Reason reason, String reasonString, boolean isAdmin, String extraData) {
        PlayerKickEvent ev;
        this.server.getPluginManager().callEvent(ev = new PlayerKickEvent(this, reason, reasonString, this.getLeaveMessage(), extraData));
        if (!ev.isCancelled()) {
            String message;
            if (isAdmin) {
                if (!this.isBanned()) {
                    message = "Kicked!" + (!reasonString.isEmpty() ? " Reason: " + reasonString : "");
                } else {
                    message = reasonString;
                }
            } else {
                if (reasonString.isEmpty()) {
                    message = "disconnectionScreen.noReason";
                } else {
                    message = reasonString;
                }
            }

            this.close(ev.getQuitMessage(), message);

            return true;
        }

        return false;
    }

    public void setViewDistance(int distance) {
        this.chunkRadius = distance;

        ChunkRadiusUpdatedPacket pk = new ChunkRadiusUpdatedPacket();
        pk.radius = distance;

        this.dataPacket(pk);
    }

    public int getViewDistance() {
        return this.chunkRadius;
    }

    @Override
    public void sendMessage(String message) {
        TextPacket pk = new TextPacket();
        pk.type = TextPacket.TYPE_RAW;
        pk.message = this.server.getLanguage().translateString(message);
        this.dataPacket(pk);
    }

    @Override
    public void sendMessage(TextContainer message) {
        if (message instanceof TranslationContainer) {
            this.sendTranslation(message.getText(), ((TranslationContainer) message).getParameters());
            return;
        }
        this.sendMessage(message.getText());
    }

    public void sendCommandOutput(CommandOutputContainer container) {
        if (this.level.getGameRules().getBoolean(GameRule.SEND_COMMAND_FEEDBACK)) {
            var pk = new CommandOutputPacket();
            pk.messages.addAll(container.getMessages());
            pk.commandOriginData = new CommandOriginData(CommandOriginData.Origin.PLAYER, this.getLoginUuid(), "", null);//Only players can effect
            pk.type = CommandOutputType.ALL_OUTPUT;//Useless
            pk.successCount = container.getSuccessCount();//Useless,maybe used for server-client interaction
            this.dataPacket(pk);
        }
    }

    public void sendRawTextMessage(RawText text) {
        TextPacket pk = new TextPacket();
        pk.type = TextPacket.TYPE_OBJECT;
        pk.message = text.toRawText();
        this.dataPacket(pk);
    }

    public void sendTranslation(String message) {
        this.sendTranslation(message, new String[0]);
    }

    public void sendTranslation(String message, String[] parameters) {
        TextPacket pk = new TextPacket();
        if (!this.server.isLanguageForced()) {
            pk.type = TextPacket.TYPE_TRANSLATION;
            pk.message = this.server.getLanguage().translateString(message, parameters, "nukkit.");
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = this.server.getLanguage().translateString(parameters[i], parameters, "nukkit.");
            }
            pk.parameters = parameters;
        } else {
            pk.type = TextPacket.TYPE_RAW;
            pk.message = this.server.getLanguage().translateString(message, parameters);
        }
        this.dataPacket(pk);
    }

    public void sendChat(String message) {
        this.sendChat("", message);
    }

    public void sendChat(String source, String message) {
        TextPacket pk = new TextPacket();
        pk.type = TextPacket.TYPE_CHAT;
        pk.source = source;
        pk.message = this.server.getLanguage().translateString(message);
        this.dataPacket(pk);
    }

    public void sendPopup(String message) {
        TextPacket pk = new TextPacket();
        pk.type = TextPacket.TYPE_POPUP;
        pk.message = message;
        this.dataPacket(pk);
    }

    public void sendPopup(String message, String subtitle) {
        this.sendPopup(message);
    }

    public void sendTip(String message) {
        TextPacket pk = new TextPacket();
        pk.type = TextPacket.TYPE_TIP;
        pk.message = message;
        this.dataPacket(pk);
    }

    public void clearTitle() {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_CLEAR;
        this.dataPacket(pk);
    }

    /**
     * Resets both title animation times and subtitle for the next shown title
     */
    public void resetTitleSettings() {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_RESET;
        this.dataPacket(pk);
    }

    public void setSubtitle(String subtitle) {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_SUBTITLE;
        pk.text = Strings.isNullOrEmpty(subtitle) ? " " : subtitle;
        this.dataPacket(pk);
    }

    /**
     * 设置一个JSON文本副标题。
     * <p>
     * Set a JSON text subtitle.
     *
     * @param text JSON文本<br>JSON text
     */
    public void setRawTextSubTitle(RawText text) {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_SUBTITLE_JSON;
        pk.text = text.toRawText();
        this.dataPacket(pk);
    }

    public void setTitleAnimationTimes(int fadein, int duration, int fadeout) {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_ANIMATION_TIMES;
        pk.fadeInTime = fadein;
        pk.stayTime = duration;
        pk.fadeOutTime = fadeout;
        this.dataPacket(pk);
    }

    private void setTitle(String text) {
        SetTitlePacket packet = new SetTitlePacket();
        packet.text = text;
        packet.type = SetTitlePacket.TYPE_TITLE;
        this.dataPacket(packet);
    }

    public void sendTitle(String title) {
        this.sendTitle(title, null, 20, 20, 5);
    }

    public void sendTitle(String title, String subtitle) {
        this.sendTitle(title, subtitle, 20, 20, 5);
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.setTitleAnimationTimes(fadeIn, stay, fadeOut);
        if (!Strings.isNullOrEmpty(subtitle)) {
            this.setSubtitle(subtitle);
        }
        // Title won't send if an empty string is used
        this.setTitle(Strings.isNullOrEmpty(title) ? " " : title);
    }

    /**
     * 设置一个JSON文本标题。
     * <p>
     * Set a JSON text title.
     *
     * @param text JSON文本<br>JSON text
     */
    public void setRawTextTitle(RawText text) {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_TITLE_JSON;
        pk.text = text.toRawText();
        this.dataPacket(pk);
    }

    public void sendActionBar(String title) {
        this.sendActionBar(title, 1, 0, 1);
    }

    public void sendActionBar(String title, int fadein, int duration, int fadeout) {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_ACTION_BAR;
        pk.text = Strings.isNullOrEmpty(title) ? " " : title;
        pk.fadeInTime = fadein;
        pk.stayTime = duration;
        pk.fadeOutTime = fadeout;
        this.dataPacket(pk);
    }

    /**
     * fadein=1,duration=0,fadeout=1
     *
     * @see #setRawTextActionBar(RawText, int, int, int)
     */
    public void setRawTextActionBar(RawText text) {
        this.setRawTextActionBar(text, 1, 0, 1);
    }

    /**
     * 设置一个JSON ActionBar消息。
     * <p>
     * Set a JSON ActionBar text.
     *
     * @param text     JSON文本<br>JSON text
     * @param fadein   淡入时间
     * @param duration 持续时间
     * @param fadeout  淡出时间
     */
    public void setRawTextActionBar(RawText text, int fadein, int duration, int fadeout) {
        SetTitlePacket pk = new SetTitlePacket();
        pk.type = SetTitlePacket.TYPE_ACTIONBAR_JSON;
        pk.text = text.toRawText();
        pk.fadeInTime = fadein;
        pk.stayTime = duration;
        pk.fadeOutTime = fadeout;
        this.dataPacket(pk);
    }

    /**
     * 设置指定itemCategory物品的冷却显示效果，注意该方法仅为客户端显示效果，冷却逻辑实现仍需自己实现
     * <p>
     * Set the cooling display effect of the specified itemCategory items, note that this method is only for client-side display effect, cooling logic implementation still needs to be implemented by itself
     *
     * @param coolDown     the cool down
     * @param itemCategory the item category
     */
    public void setItemCoolDown(int coolDown, String itemCategory) {
        PlayerStartItemCoolDownPacket pk = new PlayerStartItemCoolDownPacket();
        pk.setCoolDownDuration(coolDown);
        pk.setItemCategory(itemCategory);
        this.dataPacket(pk);
    }

    /**
     * 发送一个弹出式消息框给玩家
     * <p>
     * Send a Toast message box to the player
     *
     * @param title   the title
     * @param content the content
     */
    public void sendToast(String title, String content) {
        title = Strings.isNullOrEmpty(title) ? " " : title;
        content = Strings.isNullOrEmpty(content) ? " " : content;
        ToastRequestPacket pk = new ToastRequestPacket();
        pk.title = title;
        pk.content = content;
        this.dataPacket(pk);
    }

    public List<Long> sendDebugShape(DebugShape... shapes) {
        List<ScriptDebugShape> scriptDebugShapes = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        for (DebugShape shape : shapes) {
            scriptDebugShapes.add(shape.toNetworkData());
            ids.add(shape.getId());
        }

        ServerScriptDebugDrawerPacket packet = new ServerScriptDebugDrawerPacket();
        packet.setShapes(scriptDebugShapes);
        this.dataPacket(packet);

        return ids;
    }

    public void removeDebugShape(DebugShape... shapes) {
        List<ScriptDebugShape> scriptDebugShapes = new ArrayList<>();
        for (DebugShape shape : shapes) {
            scriptDebugShapes.add(shape.createRemovalNotice());
        }

        ServerScriptDebugDrawerPacket packet = new ServerScriptDebugDrawerPacket();
        packet.setShapes(scriptDebugShapes);
        this.dataPacket(packet);
    }

    public void removeDebugShape(int... ids) {
        List<ScriptDebugShape> scriptDebugShapes = new ArrayList<>();
        for(int id : ids) {
             scriptDebugShapes.add(new ScriptDebugShape(
                     id, null, null,
                     null, null, null,
                     null, 0, null, null,
                     null, null, null, null
             ));
        }

        ServerScriptDebugDrawerPacket packet = new ServerScriptDebugDrawerPacket();
        packet.setShapes(scriptDebugShapes);
        this.dataPacket(packet);
    }

    @Override
    public void spawnToAll() {
        // 在1.20.0中同一tick连续执行despawnFromAll和spawnToAll会导致玩家移动不可见
        if (this.server.getTick() == this.lastDespawnFromAllTick) {
            this.needSpawnToAll = true;
            return;
        }
        super.spawnToAll();
    }

    @Override
    public void despawnFromAll() {
        super.despawnFromAll();
        this.lastDespawnFromAllTick = this.server.getTick();
    }

    @Override
    public void close() {
        this.close("");
    }

    public void close(String message) {
        this.close(message, "generic");
    }

    public void close(String message, String reason) {
        this.close(message, reason, true);
    }

    public void close(String message, String reason, boolean notify) {
        this.close(new TextContainer(message), reason, notify);
    }

    public void close(TextContainer message) {
        this.close(message, "generic");
    }

    public void close(TextContainer message, String reason) {
        this.close(message, reason, true);
    }

    public void close(TextContainer message, String reason, boolean notify) {
        if (this.connected && !this.closed) {
            if (notify && !reason.isEmpty()) {
                DisconnectPacket pk = new DisconnectPacket();
                pk.message = reason;
                this.forceDataPacket(pk, null);
            }

            this.connected = false;
            PlayerQuitEvent ev = null;
            if (this.username != null && !this.username.isEmpty()) {
                this.server.getPluginManager().callEvent(ev = new PlayerQuitEvent(this, message, true, reason));
                if (this.loggedIn && ev.getAutoSave()) {
                    this.save();
                }
                if (this.fishing != null) {
                    this.stopFishing(false);
                }
            }

            for (Player player : new ArrayList<>(this.server.playerList.values())) {
                if (!player.canSee(this)) {
                    player.showPlayer(this);
                }
            }

            this.hiddenPlayers.clear();

            this.removeAllWindows(true);

            this.unloadChunks(false);

            super.close();

            this.interfaz.close(this, notify ? reason : "");

            if (this.loggedIn) {
                this.server.removeOnlinePlayer(this);
                this.loggedIn = false;
            }

            if (ev != null && !Objects.equals(this.username, "") && this.spawned && !Objects.equals(ev.getQuitMessage().toString(), "")) {
                this.server.broadcastMessage(ev.getQuitMessage());
            }

            this.server.getPluginManager().unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this);
            this.spawned = false;
            this.server.getLogger().info(this.getServer().getLanguage().translateString("nukkit.player.logOut",
                    TextFormat.AQUA + (this.getName() == null ? this.unverifiedUsername : this.getName()) + TextFormat.WHITE,
                    this.getAddress(),
                    String.valueOf(this.getPort()),
                    this.getServer().getLanguage().translateString(reason)));
            this.windows.clear();
            this.hasSpawned.clear();
            this.spawnPosition = null;

            if (this.riding instanceof EntityRideable) {
                this.riding.passengers.remove(this);
            }

            this.riding = null;
        }

        if (this.perm != null) {
            this.perm.clearPermissions();
            this.perm = null;
        }

        this.inventory = null;
        this.chunk = null;

        this.server.removePlayer(this);

        if (this.loggedIn) {
            this.server.getLogger().warning("(BUG) Player still logged in");
            this.interfaz.close(this, notify ? reason : "");
            this.server.removeOnlinePlayer(this);
            this.loggedIn = false;
        }
        this.clientMovements.clear();
    }

    /**
     * Save player data to disk
     */
    public void save() {
        this.save(false);
    }

    /**
     * Save player data to disk
     *
     * @param async save asynchronously
     */
    public void save(boolean async) {
        if (this.closed) {
            throw new IllegalStateException("Tried to save closed player");
        }

        super.saveNBT();

        if (this.level != null) {
            this.namedTag.putString("Level", this.level.getFolderName());
            if (this.spawnPosition != null && this.spawnPosition.getLevel() != null) {
                this.namedTag.putString("SpawnLevel", this.spawnPosition.getLevel().getFolderName());
                this.namedTag.putInt("SpawnX", (int) this.spawnPosition.x);
                this.namedTag.putInt("SpawnY", (int) this.spawnPosition.y);
                this.namedTag.putInt("SpawnZ", (int) this.spawnPosition.z);
            }

            if (spawnBlockPosition == null) {
                namedTag.remove("SpawnBlockPositionX").remove("SpawnBlockPositionY").remove("SpawnBlockPositionZ").remove("SpawnBlockLevel");
            } else if (spawnBlockPosition.isValid()) {
                namedTag.putInt("SpawnBlockPositionX", spawnBlockPosition.getFloorX())
                        .putInt("SpawnBlockPositionY", spawnBlockPosition.getFloorY())
                        .putInt("SpawnBlockPositionZ", spawnBlockPosition.getFloorZ())
                        .putString("SpawnBlockLevel", this.spawnBlockPosition.getLevel().getFolderName());
            }

            this.namedTag.putInt("playerGameType", this.gamemode);
            this.namedTag.putLong("lastPlayed", System.currentTimeMillis() / 1000);

            this.namedTag.putString("lastIP", this.getAddress());

            this.namedTag.putInt("EXP", this.exp);
            this.namedTag.putInt("expLevel", this.expLevel);

            this.namedTag.putInt("foodLevel", this.foodData.getFood());
            this.namedTag.putFloat("foodSaturationLevel", this.foodData.getSaturation());

            this.namedTag.putInt("TimeSinceRest", this.timeSinceRest);

            ListTag<StringTag> fogIdentifiers = new ListTag<>("fogIdentifiers");
            ListTag<StringTag> userProvidedFogIds = new ListTag<>("userProvidedFogIds");
            this.fogStack.forEach(fog -> {
                fogIdentifiers.add(new StringTag("", fog.identifier().toString()));
                userProvidedFogIds.add(new StringTag("", fog.userProvidedId()));
            });
            this.namedTag.putList(fogIdentifiers);
            this.namedTag.putList(userProvidedFogIds);

            if (!this.username.isEmpty() && this.namedTag != null) {
                if (this.server.getSettings().player().savePlayerDataByUuid()) {
                    this.server.saveOfflinePlayerData(this.uuid, this.namedTag, async);
                } else {
                    this.server.saveOfflinePlayerData(this.username, this.namedTag, async);
                }
            }
        }
    }

    /**
     * Get player's username
     *
     * @return username
     */
    @Override
    public String getName() {
        return this.username;
    }

    public LangCode getLanguageCode() {
        return LangCode.valueOf(this.getLoginChainData().getLanguageCode());
    }

    @Override
    public void kill() {
        if (!this.spawned) {
            return;
        }

        boolean showMessages = this.level.getGameRules().getBoolean(GameRule.SHOW_DEATH_MESSAGES);
        String message = "";
        List<String> params = new ArrayList<>();
        EntityDamageEvent cause = this.getLastDamageCause();

        if (showMessages) {
            params.add(this.displayName);

            switch (cause == null ? DamageCause.CUSTOM : cause.getCause()) {
                case ENTITY_ATTACK:
                case THORNS:
                    if (cause instanceof EntityDamageByEntityEvent) {
                        Entity e = ((EntityDamageByEntityEvent) cause).getDamager();
                        killer = e;
                        if (e instanceof Player) {
                            message = "death.attack.player";
                            params.add(((Player) e).displayName);
                            break;
                        } else if (e instanceof EntityLiving) {
                            message = "death.attack.mob";
                            params.add(!Objects.equals(e.getNameTag(), "") ? e.getNameTag() : e.getName());
                            break;
                        } else {
                            params.add("Unknown");
                        }
                    }
                    break;
                case PROJECTILE:
                    if (cause instanceof EntityDamageByEntityEvent) {
                        Entity e = ((EntityDamageByEntityEvent) cause).getDamager();
                        killer = e;
                        if (e instanceof Player) {
                            message = "death.attack.arrow";
                            params.add(((Player) e).displayName);
                        } else if (e instanceof EntityLiving) {
                            message = "death.attack.arrow";
                            params.add(!Objects.equals(e.getNameTag(), "") ? e.getNameTag() : e.getName());
                            break;
                        } else {
                            params.add("Unknown");
                        }
                    }
                    break;
                case VOID:
                    message = "death.attack.outOfWorld";
                    break;
                case FALL:
                    if (cause.getFinalDamage() > 2) {
                        message = "death.fell.accident.generic";
                        break;
                    }
                    message = "death.attack.fall";
                    break;

                case SUFFOCATION:
                    message = "death.attack.inWall";
                    break;

                case LAVA:
                    message = "death.attack.lava";
                    break;

                case MAGMA:
                    message = "death.attack.magma";
                    break;

                case FIRE:
                    message = "death.attack.onFire";
                    break;

                case FIRE_TICK:
                    message = "death.attack.inFire";
                    break;

                case DROWNING:
                    message = "death.attack.drown";
                    break;

                case CONTACT:
                    if (cause instanceof EntityDamageByBlockEvent) {
                        int id = ((EntityDamageByBlockEvent) cause).getDamager().getId();
                        if (id == Block.CACTUS) {
                            message = "death.attack.cactus";
                        } else if (id == Block.ANVIL) {
                            message = "death.attack.anvil";
                        }
                    }
                    break;

                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    if (cause instanceof EntityDamageByEntityEvent) {
                        Entity e = ((EntityDamageByEntityEvent) cause).getDamager();
                        killer = e;
                        if (e instanceof Player) {
                            message = "death.attack.explosion.player";
                            params.add(((Player) e).displayName);
                        } else if (e instanceof EntityLiving) {
                            message = "death.attack.explosion.player";
                            params.add(!Objects.equals(e.getNameTag(), "") ? e.getNameTag() : e.getName());
                            break;
                        } else {
                            message = "death.attack.explosion";
                        }
                    } else {
                        message = "death.attack.explosion";
                    }
                    break;
                case MAGIC:
                    message = "death.attack.magic";
                    break;
                case LIGHTNING:
                    message = "death.attack.lightningBolt";
                    break;
                case HUNGER:
                    message = "death.attack.starve";
                    break;
                default:
                    message = "death.attack.generic";
                    break;
            }
        }

        PlayerDeathEvent ev = new PlayerDeathEvent(this, this.getDrops(), new TranslationContainer(message, params.toArray(new String[0])), this.expLevel);
        ev.setKeepInventory(this.level.gameRules.getBoolean(GameRule.KEEP_INVENTORY));
        ev.setKeepExperience(ev.getKeepInventory()); // Same as above
        this.server.getPluginManager().callEvent(ev);

        if (!ev.isCancelled()) {
            if (this.fishing != null) {
                this.stopFishing(false);
            }

            this.extinguish();
            this.removeAllEffects(EntityEffectUpdateEvent.Cause.DEATH);
            this.health = 0;
            this.scheduleUpdate();
            this.timeSinceRest = 0;

            if (this.getKiller() != null && this.getKiller() instanceof EntityWalkingMob && ((EntityWalkingMob) this.getKiller()).isAngryTo == this.getId()) {
                ((EntityWalkingMob) this.getKiller()).isAngryTo = -1; // Reset golem target
                if (this.getKiller() instanceof EntityWolf) {
                    ((EntityWolf) this.getKiller()).setAngry(false);
                }
            }

            if (!ev.getKeepInventory() && this.level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
                for (Item item : ev.getDrops()) {
                    if (!item.hasEnchantment(Enchantment.ID_VANISHING_CURSE)) {
                        this.level.dropItem(this, item, null, true, 40);
                    }
                }

                if (this.inventory != null) {
                    this.inventory.clearAll();
                }

                // Offhand inventory is already cleared in inventory.clearAll()

                if (this.playerUIInventory != null) {
                    this.playerUIInventory.clearAll();
                }
            } else {
                // 发包给客户端清除不死图腾，防止影响自杀等操作
                if (this.getOffhandInventory().getItemFast(0) instanceof ItemTotem) {
                    InventorySlotPacket pk = new InventorySlotPacket();
                    pk.slot = 0;
                    pk.item = Item.AIR_ITEM;
                    int id = this.getWindowId(this.getOffhandInventory());
                    if (id != -1) {
                        pk.inventoryId = id;
                        this.dataPacket(pk);
                    }
                }
                int id = this.getWindowId(this.getInventory());
                if (id != -1) {
                    for (Entry<Integer, Item> entry : this.getInventory().getContents().entrySet()) {
                        if (entry.getValue() instanceof ItemTotem) {
                            InventorySlotPacket pk = new InventorySlotPacket();
                            pk.slot = entry.getKey();
                            pk.item = Item.AIR_ITEM;
                            pk.inventoryId = id;
                            this.dataPacket(pk);
                        }
                    }
                }
            }

            if (!ev.getKeepExperience() && this.level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
                if (this.isSurvival() || this.isAdventure()) {
                    int exp = ev.getExperience() * 7;
                    if (exp > 100) exp = 100;
                    this.getLevel().dropExpOrb(this, exp);
                }
                this.setExperience(0, 0);
            }

            if (showMessages && !ev.getDeathMessage().toString().isEmpty()) {
                this.server.broadcast(ev.getDeathMessage(), Server.BROADCAST_CHANNEL_USERS);

                DeathInfoPacket pk = new DeathInfoPacket();
                if (ev.getDeathMessage() instanceof TranslationContainer) {
                    pk.messageTranslationKey = this.server.getLanguage().translateString(ev.getDeathMessage().getText(), ((TranslationContainer) ev.getDeathMessage()).getParameters(), null);
                } else {
                    pk.messageTranslationKey = ev.getDeathMessage().getText();
                }
                this.dataPacket(pk);
            }

            RespawnPacket pk = new RespawnPacket();
            Position pos = this.getSpawn();
            pk.x = (float) pos.x;
            pk.y = (float) pos.y;
            pk.z = (float) pos.z;
            pk.respawnState = RespawnPacket.STATE_SEARCHING_FOR_SPAWN;
            this.dataPacket(pk);

            if (level.getGameRules().getBoolean(GameRule.DO_IMMEDIATE_RESPAWN)) {
                SetHealthPacket healthPk = new SetHealthPacket();
                healthPk.health = this.getMaxHealth();
                this.dataPacket(healthPk);
            }
        }
    }

    protected void respawn() {
        if (this.server.getSettings().world().enableHardcore()) {
            this.setBanned(true);
            return;
        }

        this.craftingType = CRAFTING_SMALL;
        this.resetCraftingGridType();

        this.checkSpawnBlockPosition();
        if (this.spawnBlockPosition != null && this.spawnBlockPosition.isValid()) {
            Block spawnBlock = this.spawnBlockPosition.getLevelBlock();
            if (spawnBlock.getId() == BlockID.RESPAWN_ANCHOR) {
                BlockRespawnAnchor respawnAnchor = (BlockRespawnAnchor) spawnBlock;
                respawnAnchor.setCharge(respawnAnchor.getCharge() - 1);
                respawnAnchor.getLevel().setBlock(respawnAnchor, spawnBlock);
                respawnAnchor.getLevel().scheduleUpdate(respawnAnchor, 10);
                respawnAnchor.getLevel().addSound(this, Sound.RESPAWN_ANCHOR_DEPLETE, 1, 1, this);
            }
        }
        PlayerRespawnEvent playerRespawnEvent = new PlayerRespawnEvent(this, this.getSpawn());
        this.server.getPluginManager().callEvent(playerRespawnEvent);

        Position respawnPos = playerRespawnEvent.getRespawnPosition();

        this.teleport(respawnPos, null);

        this.sendExperience();
        this.sendExperienceLevel();

        this.setSprinting(false, false);
        this.setSneaking(false);
        this.setSwimming(false);
        this.setGliding(false);

        this.extinguish();
        this.setDataProperty(new ShortEntityData(Player.DATA_AIR, 400), false);
        this.deadTicks = 0;
        this.noDamageTicks = 60;

        this.removeAllEffects(EntityEffectUpdateEvent.Cause.DEATH);
        this.setHealth(this.getMaxHealth());
        this.foodData.setFood(20, 20);

        this.sendData(this);

        this.recalculateMovementSpeed();

        this.adventureSettings.update();
        this.inventory.sendContents(this);
        this.inventory.sendArmorContents(this);
        this.offhandInventory.sendContents(this);

        this.spawnToAll();
        this.scheduleUpdate();
    }

    @Override
    public void setHealth(float newHealth) {
        if (newHealth < 1) {
            newHealth = 0;
        }

        super.setHealth(newHealth);

        // HACK: solve the client-side absorption bug
        if (this.spawned) {
            UpdateAttributesPacket pk = new UpdateAttributesPacket();
            pk.entries = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(this.getAbsorption() % 2 != 0 ? this.getMaxHealth() + 1 : this.getMaxHealth()).setValue(this.health > 0 ? (this.health < getMaxHealth() ? this.health : getMaxHealth()) : 0)};
            pk.entityId = this.id;
            this.dataPacket(pk);
        }
    }

    @Override
    public void setMaxHealth(int newMaxHealth) {
        super.setMaxHealth(newMaxHealth);

        if (this.spawned) {
            UpdateAttributesPacket pk = new UpdateAttributesPacket();
            pk.entries = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(this.getAbsorption() % 2 != 0 ? this.getMaxHealth() + 1 : this.getMaxHealth()).setValue(this.health > 0 ? (this.health < getMaxHealth() ? this.health : getMaxHealth()) : 0)};
            pk.entityId = this.id;
            this.dataPacket(pk);
        }
    }

    public int getExperience() {
        return this.exp;
    }

    public int getExperienceLevel() {
        return this.expLevel;
    }

    public void addExperience(int add) {
        if (add == 0) return;
        int added = this.exp + add;
        int level = this.expLevel;
        int most = calculateRequireExperience(level);
        while (added >= most) {
            added -= most;
            level++;
            most = calculateRequireExperience(level);
        }
        this.setExperience(added, level);
    }

    public static int calculateRequireExperience(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + (level << 1);
        }
    }

    public void setExperience(int exp) {
        setExperience(exp, this.expLevel);
    }

    public void setExperience(int exp, int level) {
        PlayerExperienceChangeEvent ev = new PlayerExperienceChangeEvent(this, this.exp, this.expLevel, exp, level);
        this.server.getPluginManager().callEvent(ev);

        if (ev.isCancelled()) {
            return;
        }

        this.exp = ev.getNewExperience();
        this.expLevel = ev.getNewExperienceLevel();

        this.sendExperienceLevel(this.expLevel);
        this.sendExperience(this.exp);
    }

    public void sendExperience() {
        sendExperience(this.exp);
    }

    public void sendExperience(int exp) {
        if (this.spawned) {
            this.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE).setValue(Math.max(0f, Math.min(1f, ((float) exp) / calculateRequireExperience(this.expLevel)))));
        }
    }

    public void sendExperienceLevel() {
        sendExperienceLevel(this.expLevel);
    }

    public void sendExperienceLevel(int level) {
        if (this.spawned) {
            this.setAttribute(Attribute.getAttribute(Attribute.EXPERIENCE_LEVEL).setValue(level));
        }
    }

    public void setAttribute(Attribute attribute) {
        UpdateAttributesPacket pk = new UpdateAttributesPacket();
        pk.entries = new Attribute[]{attribute};
        pk.entityId = this.id;
        this.dataPacket(pk);
    }

    @Override
    public void addMovementSpeedModifier(EntityMovementSpeedModifier modifier) {
        super.addMovementSpeedModifier(modifier);
        this.speedToSend = this.recalculateMovementSpeedToSend();
        this.sendMovementSpeed();
    }

    @Override
    public boolean removeMovementSpeedModifier(String identifier) {
        boolean isRemoved = super.removeMovementSpeedModifier(identifier);

        if (isRemoved) {
            this.speedToSend = this.recalculateMovementSpeedToSend();
            this.sendMovementSpeed();
        }

        return isRemoved;
    }

    public float recalculateMovementSpeedToSend() {
        float newMovementSpeed = DEFAULT_SPEED;
        for (EntityMovementSpeedModifier modifier : this.getMovementSpeedModifiers().values()) {
            float value = modifier.getValue();
            if (modifier.isSend()) {
                if (modifier.getOperation() == EntityMovementSpeedModifier.Operation.MULTIPLY) {
                    if (value != 0) {
                        newMovementSpeed *= value;
                    }
                } else {
                    newMovementSpeed += value;
                }
            }
        }
        return Math.max(newMovementSpeed, 0.00f);
    }

    public void sendMovementSpeed() {
        Attribute attribute = Attribute.getAttribute(Attribute.MOVEMENT_SPEED).setValue(this.speedToSend).setDefaultValue(this.speedToSend);
        this.setAttribute(attribute);
    }

    public Entity getKiller() {
        return killer;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (!this.isAlive()) {
            return false;
        }

        if (this.isSpectator() || (this.isCreative() && source.getCause() != DamageCause.SUICIDE)) {
            source.setCancelled();
            return false;
        } else if (source.getCause() == DamageCause.FALL && this.getAllowFlight()) {
            source.setCancelled();
            return false;
        } else if (source.getCause() == DamageCause.FALL) {
            Position pos = this.getPosition().floor().add(0.5, -1, 0.5);
            int block = this.getLevel().getBlockIdAt(chunk, (int) pos.x, (int) pos.y, (int) pos.z);
            if (block == Block.SLIME_BLOCK || block == Block.COBWEB) {
                if (!this.isSneaking()) {
                    source.setCancelled();
                    this.resetFallDistance();
                    return false;
                }
            }
        }

        if (super.attack(source)) {
            if (this.getLastDamageCause() == source && this.spawned) {
                if (source instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) source).getDamager();
                    if (damager instanceof Player player) {
                        player.foodData.exhaust(0.1);
                    }
                }
                EntityEventPacket pk = new EntityEventPacket();
                pk.eid = this.id;
                pk.event = EntityEventPacket.HURT_ANIMATION;
                this.dataPacket(pk);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Drops an item on the ground in front of the player. Returns if the item drop was successful.
     *
     * @param item to drop
     * @return bool if the item was dropped or if the item was null
     */
    public boolean dropItem(Item item) {
        if (!this.spawned || !this.isAlive()) {
            return false;
        }

        if (item.isNull()) {
            this.server.getLogger().debug(this.username + " attempted to drop a null item (" + item + ')');
            return true;
        }

        Vector3 motion = this.getDirectionVector().multiply(0.4);

        EntityItem entityItem = this.level.dropAndGetItem(this.add(0, 1.3, 0), item, motion, 40);
        if (entityItem != null) {
            entityItem.setOwner(this.getName());
        }

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, false);
        return true;
    }

    /**
     * Drops an item on the ground in front of the player. Returns the dropped item.
     *
     * @param item to drop
     * @return EntityItem if the item was dropped or null if the item was null
     */
    public EntityItem dropAndGetItem(Item item) {
        if (!this.spawned || !this.isAlive()) {
            return null;
        }

        if (item.isNull()) {
            this.server.getLogger().debug(this.getName() + " attempted to drop a null item (" + item + ')');
            return null;
        }

        Vector3 motion = this.getDirectionVector().multiply(0.4);

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, false);

        return this.level.dropAndGetItem(this.add(0, 1.3, 0), item, motion, 40);
    }

    public void sendPosition(Vector3 pos) {
        this.sendPosition(pos, this.yaw);
    }

    public void sendPosition(Vector3 pos, double yaw) {
        this.sendPosition(pos, yaw, this.pitch);
    }

    public void sendPosition(Vector3 pos, double yaw, double pitch) {
        this.sendPosition(pos, yaw, pitch, MovePlayerPacket.MODE_NORMAL);
    }

    public void sendPosition(Vector3 pos, double yaw, double pitch, int mode) {
        this.sendPosition(pos, yaw, pitch, mode, null);
    }

    public void sendPosition(Vector3 pos, double yaw, double pitch, int mode, Player[] targets) {
        MovePlayerPacket pk = new MovePlayerPacket();
        pk.eid = this.getId();
        pk.x = (float) pos.x;
        pk.y = (float) (pos.y + this.getBaseOffset());
        pk.z = (float) pos.z;
        pk.headYaw = (float) yaw;
        pk.pitch = (float) pitch;
        pk.yaw = (float) yaw;
        pk.mode = mode;
        pk.onGround = this.onGround;

        if (this.riding != null) {
            pk.ridingEid = this.riding.getId();
            pk.mode = MovePlayerPacket.MODE_PITCH;
        }

        this.ySize = 0;

        if (targets != null) {
            Server.broadcastPacket(targets, pk);
        } else {
            this.clientMovements.clear();

            this.dataPacket(pk);
        }
    }

    public void sendPosition(double x, double y, double z, double yaw, double pitch, int mode, Collection<Player> targets) {
        MovePlayerPacket pk = new MovePlayerPacket();
        pk.eid = this.getId();
        pk.x = (float) x;
        pk.y = (float) y + this.getBaseOffset();
        pk.z = (float) z;
        pk.headYaw = (float) yaw;
        pk.pitch = (float) pitch;
        pk.yaw = (float) yaw;
        pk.mode = mode;
        pk.onGround = this.onGround;

        if (this.riding != null) {
            pk.ridingEid = this.riding.getId();
            pk.mode = MovePlayerPacket.MODE_PITCH;
        }

        this.ySize = 0;

        if (targets != null) {
            Server.broadcastPacket(targets, pk);
        } else {
            this.dataPacket(pk);
        }
    }

    @Override
    protected void checkChunks() {
        if (this.chunk == null || (this.chunk.getX() != ((int) this.x >> 4) || this.chunk.getZ() != ((int) this.z >> 4))) {
            if (this.chunk != null) {
                this.chunk.removeEntity(this);
            }
            this.chunk = this.level.getChunk((int) this.x >> 4, (int) this.z >> 4, true);

            if (!this.justCreated) {
                Map<Integer, Player> newChunk = this.level.getChunkPlayers((int) this.x >> 4, (int) this.z >> 4);
                newChunk.remove(this.loaderId);

                for (Player player : new ArrayList<>(this.hasSpawned.values())) {
                    if (!newChunk.containsKey(player.loaderId)) {
                        this.despawnFrom(player);
                    } else {
                        newChunk.remove(player.loaderId);
                    }
                }

                for (Player player : newChunk.values()) {
                    this.spawnTo(player);
                }
            }

            if (this.chunk == null) {
                return;
            }

            this.chunk.addEntity(this);
        }
    }

    protected boolean checkTeleportPosition() {
        return checkTeleportPosition(false);
    }

    protected boolean checkTeleportPosition(boolean enderPearl) {
        if (this.teleportPosition != null) {
            int chunkX = (int) this.teleportPosition.x >> 4;
            int chunkZ = (int) this.teleportPosition.z >> 4;

            for (int X = -1; X <= 1; ++X) {
                for (int Z = -1; Z <= 1; ++Z) {
                    long index = Level.chunkHash(chunkX + X, chunkZ + Z);
                    if (!this.usedChunks.containsKey(index) || !this.usedChunks.get(index)) {
                        return false;
                    }
                }
            }

            this.spawnToAll();
            if (!enderPearl) {
                this.forceMovement = this.teleportPosition;
            }
            this.teleportPosition = null;
            return true;
        }

        return false;
    }

    protected void sendPlayStatus(int status) {
        sendPlayStatus(status, false);
    }

    protected void sendPlayStatus(int status, boolean immediate) {
        PlayStatusPacket pk = new PlayStatusPacket();
        pk.status = status;

        if (immediate) {
            this.forceDataPacket(pk, null);
        } else {
            this.dataPacket(pk);
        }
    }

    @Override
    public boolean teleport(Location location, TeleportCause cause) {
        if (!this.isOnline()) {
            return false;
        }

        Location from = this.getLocation();
        Location to = location;

        if (cause != null) {
            PlayerTeleportEvent event = new PlayerTeleportEvent(this, from, to, cause);
            this.server.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            to = event.getTo();
        }

        // HACK: solve the client-side teleporting bug (inside into the block)
        if (super.teleport(to.getY() == to.getFloorY() ? to.add(0, 0.00001, 0) : to, null)) { // null to prevent fire of duplicate EntityTeleportEvent
            //this.removeAllWindows();
            //this.formOpen = false;

            this.lastTeleportTick = this.server.getTick();
            this.teleportPosition = this;
            if (cause != TeleportCause.ENDER_PEARL) {
                this.forceMovement = this.teleportPosition;
            }

            if (this.dimensionChangeInProgress) {
                this.dimensionChangeInProgress = false;
            } else {
                this.sendPosition(this, this.yaw, this.pitch, MovePlayerPacket.MODE_TELEPORT);
                this.checkTeleportPosition(cause == TeleportCause.ENDER_PEARL);
                this.dummyBossBars.values().forEach(DummyBossBar::reshow);
            }

            this.resetFallDistance();
            this.nextChunkOrderRun = 0;
            this.resetClientMovement();

            this.stopFishing(false);
            return true;
        }

        return false;
    }

    protected void forceSendEmptyChunks() {
        int chunkPositionX = this.getFloorX() >> 4;
        int chunkPositionZ = this.getFloorZ() >> 4;
        for (int x = -chunkRadius; x < chunkRadius; x++) {
            for (int z = -chunkRadius; z < chunkRadius; z++) {
                LevelChunkPacket chunk = new LevelChunkPacket();
                chunk.chunkX = chunkPositionX + x;
                chunk.chunkZ = chunkPositionZ + z;
                chunk.dimension = this.level.getDimension();
                chunk.data = new byte[0];
                this.dataPacket(chunk);
            }
        }
    }

    public void teleportImmediate(Location location) {
        this.teleportImmediate(location, TeleportCause.PLUGIN);
    }

    public void teleportImmediate(Location location, TeleportCause cause) {
        Location from = this.getLocation();
        if (super.teleport(location.add(0, 0.00001, 0), cause)) {
            this.removeAllWindows();

            if (from.getLevel().getId() != location.getLevel().getId()) { // Different level, update compass position
                SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
                pk.spawnType = SetSpawnPositionPacket.TYPE_WORLD_SPAWN;
                Position spawn = location.getLevel().getSpawnLocation();
                pk.x = spawn.getFloorX();
                pk.y = spawn.getFloorY();
                pk.z = spawn.getFloorZ();
                pk.dimension = location.getLevel().getDimension();
                this.dataPacket(pk);
            }

            this.forceMovement = this;
            this.sendPosition(this, this.yaw, this.pitch, MovePlayerPacket.MODE_RESET);

            this.resetFallDistance();
            this.orderChunks();
            this.nextChunkOrderRun = 0;
            this.resetClientMovement();

            //DummyBossBar
            this.getDummyBossBars().values().forEach(DummyBossBar::reshow);
            // Weather
            this.getLevel().sendWeather(this);
            // Update time
            this.getLevel().sendTime(this);
        }
    }

    /**
     * Shows a new FormWindow to the player
     * You can find out FormWindow result by listening to PlayerFormRespondedEvent
     *
     * @param window to show
     * @return form id to use in {@link PlayerFormRespondedEvent}
     */
    public int showFormWindow(FormWindow window) {
        return showFormWindow(window, this.formWindowCount++);
    }

    /**
     * Shows a new FormWindow to the player
     * You can find out FormWindow result by listening to PlayerFormRespondedEvent
     *
     * @param window to show
     * @param id     form id
     * @return form id to use in {@link PlayerFormRespondedEvent}
     */
    public int showFormWindow(FormWindow window, int id) {
        if (formOpen) return -1;
        ModalFormRequestPacket packet = new ModalFormRequestPacket();
        packet.formId = id;
        packet.data = window.getJSONData(this.protocol);
        this.formWindows.put(packet.formId, window);
        this.dataPacket(packet);
        this.formOpen = true;
        return id;
    }

    /**
     * Close form windows sent with showFormWindow
     */
    public void closeFormWindows() {
        if (this.protocol < ProtocolInfo.v1_21_2) {
            return;
        }
        this.formWindows.clear();
        this.dataPacket(new ClientboundCloseFormPacket());
    }

    public void showDialogWindow(FormWindowDialog dialog) {
        showDialogWindow(dialog, true);
    }

    /**
     * 向玩家展示一个NPC对话框.
     * <p>
     * Show dialog window to the player.
     *
     * @param dialog NPC对话框<br>the dialog
     * @param book   如果为true,将会立即更新该{@link FormWindowDialog#getSceneName()}<br>If true, the {@link FormWindowDialog#getSceneName()} will be updated immediately.
     */
    public void showDialogWindow(FormWindowDialog dialog, boolean book) {
        String actionJson = dialog.getButtonJSONData();

        if (book && dialogWindows.getIfPresent(dialog.getSceneName()) != null) dialog.updateSceneName();
        dialog.getBindEntity().setDataProperty(new ByteEntityData(Entity.DATA_HAS_NPC_COMPONENT, 1));
        dialog.getBindEntity().setDataProperty(new StringEntityData(Entity.DATA_NPC_SKIN_DATA, dialog.getSkinData()));
        dialog.getBindEntity().setDataProperty(new StringEntityData(Entity.DATA_NPC_ACTIONS, actionJson));
        dialog.getBindEntity().setDataProperty(new StringEntityData(Entity.DATA_INTERACTIVE_TAG, dialog.getContent()));

        NPCDialoguePacket packet = new NPCDialoguePacket();
        packet.setUniqueEntityId(dialog.getEntityId());
        packet.setAction(NPCDialoguePacket.Action.OPEN);
        packet.setDialogue(dialog.getContent());
        packet.setNpcName(dialog.getTitle());
        if (book) packet.setSceneName(dialog.getSceneName());
        packet.setActionJson(dialog.getButtonJSONData());
        if (book) this.dialogWindows.put(dialog.getSceneName(), dialog);
        this.dataPacket(packet);
    }

    /**
     * Shows a new setting page in game settings
     * You can find out settings result by listening to PlayerFormRespondedEvent
     *
     * @param window to show on settings page
     * @return form id to use in {@link PlayerFormRespondedEvent}
     */
    public int addServerSettings(FormWindow window) {
        int id = this.formWindowCount++;

        this.serverSettings.put(id, window);
        return id;
    }

    /**
     * Creates and sends a BossBar to the player
     *
     * @param text   The BossBar message
     * @param length The BossBar percentage
     * @return bossBarId  The BossBar ID, you should store it if you want to remove or update the BossBar later
     */
    public long createBossBar(String text, int length) {
        return this.createBossBar(new DummyBossBar.Builder(this).text(text).length(length).build());
    }

    /**
     * Creates and sends a BossBar to the player
     *
     * @param dummyBossBar DummyBossBar Object (Instantiate it by the Class Builder)
     * @return bossBarId  The BossBar ID, you should store it if you want to remove or update the BossBar later
     * @see DummyBossBar.Builder
     */
    public long createBossBar(DummyBossBar dummyBossBar) {
        this.dummyBossBars.put(dummyBossBar.getBossBarId(), dummyBossBar);
        dummyBossBar.create();
        return dummyBossBar.getBossBarId();
    }

    /**
     * Get a DummyBossBar object
     *
     * @param bossBarId The BossBar ID
     * @return DummyBossBar object
     * @see DummyBossBar#setText(String) Set BossBar text
     * @see DummyBossBar#setLength(float) Set BossBar length
     * @see DummyBossBar#setColor(BossBarColor) Set BossBar color
     */
    public DummyBossBar getDummyBossBar(long bossBarId) {
        return this.dummyBossBars.getOrDefault(bossBarId, null);
    }

    /**
     * Get all DummyBossBar objects
     *
     * @return DummyBossBars Map
     */
    public Map<Long, DummyBossBar> getDummyBossBars() {
        return dummyBossBars;
    }

    /**
     * Updates a BossBar
     *
     * @param text      The new BossBar message
     * @param length    The new BossBar length
     * @param bossBarId The BossBar ID
     */
    public void updateBossBar(String text, int length, long bossBarId) {
        DummyBossBar bossBar = this.dummyBossBars.get(bossBarId);
        if (bossBar != null) {
            bossBar.setText(text);
            bossBar.setLength(length);
        }
    }

    /**
     * Removes a BossBar
     *
     * @param bossBarId The BossBar ID
     */
    public void removeBossBar(long bossBarId) {
        DummyBossBar bossBar = this.dummyBossBars.get(bossBarId);
        if (bossBar != null) {
            bossBar.destroy();
            this.dummyBossBars.remove(bossBarId);
        }
    }

    public int getWindowId(Inventory inventory) {
        if (this.windows.containsKey(inventory)) {
            return this.windows.get(inventory);
        }

        return -1;
    }

    public Inventory getWindowById(int id) {
        return this.windowIndex.get(id);
    }

    public int addWindow(Inventory inventory) {
        return this.addWindow(inventory, null);
    }

    public int addWindow(Inventory inventory, Integer forceId) {
        return addWindow(inventory, forceId, false);
    }

    public int addWindow(Inventory inventory, Integer forceId, boolean isPermanent) {
        return addWindow(inventory, forceId, isPermanent, false);
    }

    public int addWindow(Inventory inventory, Integer forceId, boolean isPermanent, boolean alwaysOpen) {
        if (this.windows.containsKey(inventory)) {
            return this.windows.get(inventory);
        }
        int cnt;
        if (forceId == null) {
            this.windowCnt = cnt = Math.max(MINIMUM_OTHER_WINDOW_ID, ++this.windowCnt % 99);
        } else {
            cnt = forceId;
        }
        this.windows.forcePut(inventory, cnt);

        if (isPermanent) {
            this.permanentWindows.add(cnt);
        }

        if (this.spawned && !this.inventoryOpen && inventory.open(this)) {
            return cnt;
        } else if (!alwaysOpen) {
            this.removeWindow(inventory);

            return -1;
        } else {
            inventory.getViewers().add(this);
        }

        return cnt;
    }

    public Optional<Inventory> getTopWindow() {
        for (Entry<Inventory, Integer> entry : this.windows.entrySet()) {
            if (!this.permanentWindows.contains(entry.getValue())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public void removeWindow(Inventory inventory) {
        this.removeWindow(inventory, false);
    }

    protected void removeWindow(Inventory inventory, boolean isResponse) {
        inventory.close(this);
        // TODO: This needs a proper fix
        // Requiring isResponse here causes issues with inventory events and an item duplication glitch
        if (/*isResponse &&*/ !this.permanentWindows.contains(this.getWindowId(inventory))) {
            this.windows.remove(inventory);
        }
    }

    public void sendAllInventories() {
        for (Inventory inv : this.windows.keySet()) {
            inv.sendContents(this);

            if (inv instanceof PlayerInventory) {
                ((PlayerInventory) inv).sendArmorContents(this);
            }
        }
    }

    protected void addDefaultWindows() {
        this.addWindow(this.getInventory(), ContainerIds.INVENTORY, true, true);

        this.playerUIInventory = new PlayerUIInventory(this);
        this.addWindow(this.playerUIInventory, ContainerIds.UI, true);
        this.addWindow(this.offhandInventory, ContainerIds.OFFHAND, true, true);

        this.craftingGrid = this.playerUIInventory.getCraftingGrid();
        this.addWindow(this.craftingGrid, ContainerIds.NONE);
    }

    public PlayerUIInventory getUIInventory() {
        return playerUIInventory;
    }

    public PlayerCursorInventory getCursorInventory() {
        return this.playerUIInventory.getCursorInventory();
    }

    public CraftingGrid getCraftingGrid() {
        return this.craftingGrid;
    }

    public TradeInventory getTradeInventory() {
        for (Inventory inv : this.windows.keySet()) {
            if (inv instanceof TradeInventory) {
                return (TradeInventory) inv;
            }
        }
        return null;
    }

    public void setCraftingGrid(CraftingGrid grid) {
        this.craftingGrid = grid;
        this.addWindow(grid, ContainerIds.NONE);
    }

    public void resetCraftingGridType() {
        if (this.playerUIInventory != null) {
            Item[] drops;

            if (this.craftingGrid != null) {
                drops = this.inventory.addItem(this.craftingGrid.getContents().values().toArray(Item.EMPTY_ARRAY));
                this.craftingGrid.clearAll();

                for (Item drop : drops) {
                    this.level.dropItem(this, drop);
                }
            }

            drops = this.inventory.addItem(this.getCursorInventory().getItem(0));
            this.playerUIInventory.getCursorInventory().clear(0);

            for (Item drop : drops) {
                PlayerDropItemEvent event = new PlayerDropItemEvent(this, drop);
                event.call();
                if (!event.isCancelled()) {
                    this.level.dropItem(this, drop);
                }
            }

            // Don't trust the client to handle this
            this.moveBlockUIContents(Player.ANVIL_WINDOW_ID); // LOOM_WINDOW_ID is the same as ANVIL_WINDOW_ID?
            this.moveBlockUIContents(Player.ENCHANT_WINDOW_ID);
            this.moveBlockUIContents(Player.BEACON_WINDOW_ID);
            this.moveBlockUIContents(Player.SMITHING_WINDOW_ID);

            this.playerUIInventory.clearAll();

            if (this.craftingGrid instanceof BigCraftingGrid && this.connected) {
                this.craftingGrid = this.playerUIInventory.getCraftingGrid();
                this.addWindow(this.craftingGrid, ContainerIds.NONE);
            }

        }
        this.craftingType = CRAFTING_SMALL;
    }

    /**
     * Move all block UI contents back to player inventory or drop them
     *
     * @param window window id
     */
    private void moveBlockUIContents(int window) {
        Inventory inventory = this.getWindowById(window);
        if (inventory instanceof FakeBlockUIComponent) {
            Item[] drops = this.inventory.addItem(inventory.getContents().values().toArray(Item.EMPTY_ARRAY));
            inventory.clearAll();
            for (Item drop : drops) {
                this.level.dropItem(this, drop);
            }
        }
    }

    /**
     * Remove all windows
     */
    public void removeAllWindows() {
        removeAllWindows(false);
    }

    /**
     * Remove all windows
     *
     * @param permanent remove permanent windows
     */
    public void removeAllWindows(boolean permanent) {
        for (Entry<Integer, Inventory> entry : new ArrayList<>(this.windowIndex.entrySet())) {
            if (!permanent && this.permanentWindows.contains(entry.getKey())) {
                continue;
            }

            this.removeWindow(entry.getValue());
        }
    }

    public int getClosingWindowId() {
        return this.closingWindowId;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        this.server.getPlayerMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return this.server.getPlayerMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return this.server.getPlayerMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        this.server.getPlayerMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public void onChunkChanged(FullChunk chunk) {
        this.usedChunks.remove(Level.chunkHash(chunk.getX(), chunk.getZ()));
    }

    @Override
    public void onChunkLoaded(FullChunk chunk) {
    }

    @Override
    public void onChunkPopulated(FullChunk chunk) {
    }

    @Override
    public void onChunkUnloaded(FullChunk chunk) {
    }

    @Override
    public void onBlockChanged(Vector3 block) {
    }

    @Override
    public int getLoaderId() {
        return this.loaderId;
    }

    @Override
    public boolean isLoaderActive() {
        return this.connected;
    }

    @Deprecated
    public static BatchPacket getChunkCacheFromData(int protocol, int chunkX, int chunkZ, int subChunkCount, byte[] payload) {
        log.warn("Player#getChunkCacheFromData(protocol, chunkX, chunkZ, subChunkCount, payload) is deprecated");
        return getChunkCacheFromData(protocol, chunkX, chunkZ, subChunkCount, payload, 0);
    }

    /**
     * Get chunk cache from data
     *
     * @param protocol      protocol version
     * @param chunkX        chunk x
     * @param chunkZ        chunk z
     * @param subChunkCount sub chunk count
     * @param payload       data
     * @return BatchPacket
     */
    public static BatchPacket getChunkCacheFromData(int protocol, int chunkX, int chunkZ, int subChunkCount, byte[] payload, int dimension) {
        LevelChunkPacket pk = new LevelChunkPacket();
        pk.chunkX = chunkX;
        pk.chunkZ = chunkZ;
        pk.dimension = dimension;
        pk.subChunkCount = subChunkCount;
        pk.data = payload;
        pk.protocol = protocol;
        pk.tryEncode();

        byte[] buf = pk.getBuffer();
        BinaryStream batched = new BinaryStream(new byte[5 + buf.length]).reset();
        batched.putUnsignedVarInt(buf.length);
        batched.put(buf);
        try {
            byte[] bytes = batched.getBuffer();
            BatchPacket compress = new BatchPacket();
            if (Server.getInstance().getSettings().network().compression().useSnappyCompression()) {
                compress.payload = SnappyCompression.compress(bytes);
            } else {
                compress.payload = Zlib.deflateRaw(bytes, Server.getInstance().getSettings().network().compression().compressionLevel());
            }
            return compress;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get player's food data
     *
     * @return food data
     */
    public PlayerFood getFoodData() {
        return this.foodData;
    }

    /**
     * Send dimension change
     *
     * @param dimension dimension id
     */
    public void setDimension(int dimension) {
        this.dimensionChangeInProgress = true;

        ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
        changeDimensionPacket.dimension = dimension;
        changeDimensionPacket.x = (float) this.x;
        changeDimensionPacket.y = (float) this.y;
        changeDimensionPacket.z = (float) this.z;
        changeDimensionPacket.respawn = !this.isAlive();
        this.dataPacket(changeDimensionPacket);

        NetworkChunkPublisherUpdatePacket chunkPublisherUpdatePacket = new NetworkChunkPublisherUpdatePacket();
        chunkPublisherUpdatePacket.position = new BlockVector3((int) this.x, (int) this.y, (int) this.z);
        chunkPublisherUpdatePacket.radius = this.chunkRadius << 4;
        this.dataPacket(chunkPublisherUpdatePacket);

        this.dimensionFix560 = true;
    }

    @Override
    protected void preSwitchLevel() {
        // Make sure batch packets from the previous world gets through first
        this.networkSession.flush();

        // Remove old chunks
        this.unloadChunks(true);
    }

    @Override
    protected void afterSwitchLevel() {
        // Send spawn to update compass position
        SetSpawnPositionPacket spawnPosition = new SetSpawnPositionPacket();
        spawnPosition.spawnType = SetSpawnPositionPacket.TYPE_WORLD_SPAWN;
        Position spawn = level.getSpawnLocation();
        spawnPosition.x = spawn.getFloorX();
        spawnPosition.y = spawn.getFloorY();
        spawnPosition.z = spawn.getFloorZ();
        spawnPosition.dimension = level.getDimension();
        this.dataPacket(spawnPosition);

        // Update time and weather
        level.sendTime(this);
        level.sendWeather(this);

        // Update game rules
        GameRulesChangedPacket packet = new GameRulesChangedPacket();
        packet.gameRulesMap = level.getGameRules().getGameRules();
        this.dataPacket(packet);

        // Reset sleeping timer
        this.timeSinceRest = 0;
    }

    /**
     * Enable or disable movement check
     *
     * @param checkMovement movement check enabled
     */
    public void setCheckMovement(boolean checkMovement) {
        this.checkMovement = checkMovement;
    }

    /**
     * @return player movement checks enabled
     */
    public boolean isCheckingMovement() {
        return this.checkMovement;
    }

    /**
     * Set locale
     *
     * @param locale locale
     */
    public synchronized void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    /**
     * Get locale
     *
     * @return locale
     */
    public synchronized Locale getLocale() {
        return this.locale.get();
    }

    @Override
    public void setSprinting(boolean value) {
        this.setSprinting(value, true);
    }

    /**
     * Update movement speed to start/stop sprinting
     *
     * @param value sprinting
     * @param send  send updated speed to client
     */
    public void setSprinting(boolean value, boolean send) {
        if (isSprinting() != value) {
            super.setSprinting(value);
            if (value) {
                this.addMovementSpeedModifier(new EntityMovementSpeedModifier("minecraft:sprinting", 1.3f, EntityMovementSpeedModifier.Operation.MULTIPLY, send));
            } else {
                this.removeMovementSpeedModifier("minecraft:sprinting");
            }
        }
    }

    @Override
    protected boolean canShortSneak() {
        return this.protocol >= ProtocolInfo.v1_20_10_21;
    }

    @Override
    public void setSneaking(boolean value) {
        if (isSneaking() != value) {
            super.setSneaking(value);
            if (value) {
                this.addMovementSpeedModifier(new EntityMovementSpeedModifier("minecraft:sneaking", 0.3f, EntityMovementSpeedModifier.Operation.MULTIPLY, false));
            } else {
                this.removeMovementSpeedModifier("minecraft:sneaking");
            }
        }
    }

    @Override
    public void setCrawling(boolean value) {
        if (isCrawling() != value) {
            super.setCrawling(value);
            if (value) {
                this.addMovementSpeedModifier(new EntityMovementSpeedModifier("minecraft:crawling", 0.3f, EntityMovementSpeedModifier.Operation.MULTIPLY, false));
            } else {
                this.removeMovementSpeedModifier("minecraft:crawling");
            }
        }
    }

    /**
     * Transfer player to other server
     *
     * @param address target server address
     */
    public void transfer(InetSocketAddress address) {
        transfer(address.getAddress().getHostAddress(), address.getPort());
    }

    /**
     * Transfer player to other server
     *
     * @param hostName target server address
     * @param port     target server port
     */
    public void transfer(String hostName, int port) {
        TransferPacket pk = new TransferPacket();
        pk.address = hostName;
        pk.port = port;
        this.dataPacket(pk);
    }

    /**
     * Get player's LoginChainData
     *
     * @return login chain data
     */
    public LoginChainData getLoginChainData() {
        return this.loginChainData;
    }

    /**
     * Try to pick up an entity
     *
     * @param entity target
     * @param near   near
     * @return success
     */
    public boolean pickupEntity(Entity entity, boolean near) {
        if (!this.spawned || !this.isAlive() || !this.isOnline() || this.isSpectator() || entity.isClosed()) {
            return false;
        }

        if (near) {
            if (entity instanceof EntityArrow entityArrow && entityArrow.hadCollision) {
                Item item;
                if (entityArrow.namedTag != null && entityArrow.namedTag.containsCompound("item")) {
                    CompoundTag tag = entityArrow.namedTag.getCompound("item");
                    item = Item.get(tag.getInt("id"), tag.getInt("Damage"), tag.getInt("Count"));
                    if (tag.containsCompound("tag")) {
                        item.setCompoundTag(tag.getCompound("tag"));
                    }
                } else {
                    item = new ItemArrow();
                }
                if (!this.isCreative() && !this.inventory.canAddItem(item)) {
                    return false;
                }

                InventoryPickupArrowEvent ev = new InventoryPickupArrowEvent(this.inventory, entityArrow);

                int pickupMode = entityArrow.getPickupMode();
                if (pickupMode == EntityArrow.PICKUP_NONE || (pickupMode == EntityArrow.PICKUP_CREATIVE && !this.isCreative())) {
                    ev.setCancelled();
                }

                this.server.getPluginManager().callEvent(ev);
                if (ev.isCancelled()) {
                    return false;
                }

                TakeItemEntityPacket pk = new TakeItemEntityPacket();
                pk.entityId = this.getId();
                pk.target = entity.getId();
                Server.broadcastPacket(entity.getViewers().values(), pk);
                this.dataPacket(pk);

                if (!this.isCreative()) {
                    this.inventory.addItem(item.clone());
                }
                entity.close();
                return true;
            }
            if (entity instanceof EntityThrownTrident) {
                // Check Trident is returning to shooter
                if (!((EntityThrownTrident) entity).hadCollision) {
                    if (entity.isNoClip()) {
                        if (!((EntityProjectile) entity).shootingEntity.equals(this)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                if (!((EntityThrownTrident) entity).isPlayer()) {
                    return false;
                }

                Item item = ((EntityThrownTrident) entity).getItem();
                if (!this.isCreative() && !this.inventory.canAddItem(item)) {
                    return false;
                }

                InventoryPickupTridentEvent ev = new InventoryPickupTridentEvent(this.inventory, (EntityThrownTrident) entity);

                int pickupMode = ((EntityThrownTrident) entity).getPickupMode();
                if (pickupMode == EntityThrownTrident.PICKUP_NONE || (pickupMode == EntityThrownTrident.PICKUP_CREATIVE && !this.isCreative())) {
                    ev.setCancelled();
                }

                this.server.getPluginManager().callEvent(ev);
                if (ev.isCancelled()) {
                    return false;
                }

                TakeItemEntityPacket pk = new TakeItemEntityPacket();
                pk.entityId = this.getId();
                pk.target = entity.getId();
                Server.broadcastPacket(entity.getViewers().values(), pk);
                this.dataPacket(pk);

                if (!((EntityThrownTrident) entity).isCreative()) {
                    if (inventory.getItem(((EntityThrownTrident) entity).getFavoredSlot()).getId() == Item.AIR) {
                        inventory.setItem(((EntityThrownTrident) entity).getFavoredSlot(), item.clone());
                    } else {
                        inventory.addItem(item.clone());
                    }
                }
                entity.close();
                return true;
            }
            if (entity instanceof EntityItem) {
                if (((EntityItem) entity).getPickupDelay() <= 0) {
                    Item item = ((EntityItem) entity).getItem();

                    if (item != null) {
                        if (!this.isCreative() && !this.inventory.canAddItem(item)) {
                            return false;
                        }

                        InventoryPickupItemEvent ev;
                        this.server.getPluginManager().callEvent(ev = new InventoryPickupItemEvent(this.inventory, (EntityItem) entity));
                        if (ev.isCancelled()) {
                            return false;
                        }

                        TakeItemEntityPacket pk = new TakeItemEntityPacket();
                        pk.entityId = this.getId();
                        pk.target = entity.getId();
                        Server.broadcastPacket(entity.getViewers().values(), pk);
                        this.dataPacket(pk);

                        this.inventory.addItem(item.clone());
                        entity.close();
                        return true;
                    }
                }
            }
        }

        if (pickedXPOrb < server.getTick() && entity instanceof EntityXPOrb xpOrb && this.boundingBox.isVectorInside(entity)) {
            if (xpOrb.getPickupDelay() <= 0) {
                int exp = xpOrb.getExp();
                entity.close();
                this.getLevel().addSound(new ExperienceOrbSound(this));
                pickedXPOrb = server.getTick();

                ArrayList<Integer> itemsWithMending = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Item item = inventory.getArmorItem(i);
                    if (item.getDamage() != 0 && item.hasEnchantment(Enchantment.ID_MENDING)) {
                        itemsWithMending.add(inventory.getSize() + i);
                    }
                }

                Item hand = inventory.getItemInHandFast();
                if (hand.getDamage() != 0 && hand.hasEnchantment(Enchantment.ID_MENDING)) {
                    itemsWithMending.add(inventory.getHeldItemIndex());
                }

                Item offhand = this.getOffhandInventory().getItem(0);
                if (offhand.getId() == Item.SHIELD && offhand.getDamage() != 0 && offhand.hasEnchantment(Enchantment.ID_MENDING)) {
                    itemsWithMending.add(-1);
                }

                if (!itemsWithMending.isEmpty()) {
                    int itemToRepair = itemsWithMending.get(ThreadLocalRandom.current().nextInt(itemsWithMending.size()));
                    boolean isOffhand = itemToRepair == -1;

                    Item toRepair = isOffhand ? offhand : this.inventory.getItem(itemToRepair);
                    if (toRepair instanceof ItemDurable) {
                        if (toRepair.getDamage() > 0) {
                            int dmg = toRepair.getDamage() - (exp << 1); // repair 2 points per xp
                            if (dmg < 0) {
                                dmg = 0;
                            }
                            toRepair.setDamage(dmg);
                            if (isOffhand) {
                                this.getOffhandInventory().setItem(0, toRepair);
                            } else {
                                this.inventory.setItem(itemToRepair, toRepair);
                            }
                            return true;
                        }
                    }
                }

                this.addExperience(exp);
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if ((this.hash == 0) || (this.hash == 485)) {
            this.hash = (485 + (getUniqueId() != null ? getUniqueId().hashCode() : 0));
        }

        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        Player other = (Player) obj;
        return Objects.equals(this.getUniqueId(), other.getUniqueId()) && this.getId() == other.getId();
    }

    public boolean isBreakingBlock() {
        return this.breakingBlock != null;
    }

    /**
     * Show a window of a XBOX account's profile
     *
     * @param xuid XUID
     */
    public void showXboxProfile(String xuid) {
        ShowProfilePacket pk = new ShowProfilePacket();
        pk.xuid = xuid;
        this.dataPacket(pk);
    }

    /**
     * Start fishing
     *
     * @param fishingRod fishing rod item
     */
    public void startFishing(Item fishingRod) {
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", x))
                        .add(new DoubleTag("", y + this.getEyeHeight()))
                        .add(new DoubleTag("", z)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", -Math.sin(yaw / 180 + Math.PI) * Math.cos(pitch / 180 * Math.PI)))
                        .add(new DoubleTag("", -Math.sin(pitch / 180 * Math.PI)))
                        .add(new DoubleTag("", Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI))))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", (float) yaw))
                        .add(new FloatTag("", (float) pitch)));
        double f = 1.1;
        EntityFishingHook fishingHook = (EntityFishingHook) Entity.createEntity("FishingHook", chunk, nbt, this);
        fishingHook.setMotion(new Vector3(-Math.sin(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * f * f, -Math.sin(FastMath.toRadians(pitch)) * f * f,
                Math.cos(FastMath.toRadians(yaw)) * Math.cos(FastMath.toRadians(pitch)) * f * f));
        ProjectileLaunchEvent ev = new ProjectileLaunchEvent(fishingHook);
        this.getServer().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            fishingHook.close();
        } else {
            this.fishing = fishingHook;
            fishingHook.rod = fishingRod;
            fishingHook.checkLure();
            fishingHook.spawnToAll();
            if (protocol >= ProtocolInfo.v1_20_0_23) {
                this.level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_THROW, -1, "minecraft:player", false, false);
            }
        }
    }

    /**
     * Stop fishing
     *
     * @param click clicked or forced
     */
    public void stopFishing(boolean click) {
        if (this.fishing != null && click) {
            fishing.reelLine();
        } else if (this.fishing != null) {
            this.fishing.close();
        }

        this.fishing = null;
    }

    @Override
    public boolean doesTriggerPressurePlate() {
        return this.gamemode != SPECTATOR;
    }

    public int getNoShieldTicks() {
        return this.noShieldTicks;
    }

    public void setNoShieldTicks(int noShieldTicks) {
        this.noShieldTicks = noShieldTicks;
    }

    @Override
    protected void onBlock(Entity entity, EntityDamageEvent event, boolean animate) {
        super.onBlock(entity, event, animate);
        if (event.isBreakShield()) {
            this.setNoShieldTicks(event.getShieldBreakCoolDown());
            this.setItemCoolDown(event.getShieldBreakCoolDown(), "shield");
        }
        if (animate) {
            this.setDataFlag(DATA_FLAGS_EXTENDED, DATA_FLAG_BLOCKED_USING_DAMAGED_SHIELD, true);
            this.getServer().getScheduler().scheduleTask(InternalPlugin.INSTANCE, () -> {
                if (this.isOnline()) {
                    this.setDataFlag(DATA_FLAGS_EXTENDED, DATA_FLAG_BLOCKED_USING_DAMAGED_SHIELD, false);
                }
            });
        }
    }

    /**
     * Get ticks since sleeping in the current world last time
     *
     * @return ticks since sleeping
     */
    public int getTimeSinceRest() {
        return timeSinceRest;
    }

    /**
     * Set ticks since sleeping in the current world last time
     *
     * @param ticks ticks since sleeping
     */
    public void setTimeSinceRest(int ticks) {
        this.timeSinceRest = ticks;
    }

    public NetworkPlayerSession getNetworkSession() {
        return this.networkSession;
    }

    protected void processPreLogin() {
        this.loginVerified = true;
        final Player playerInstance = this;

        this.preLoginEventTask = new AsyncTask() {
            private PlayerAsyncPreLoginEvent event;

            @Override
            public void onRun() {
                this.event = new PlayerAsyncPreLoginEvent(username, uuid, loginChainData, playerInstance.getSkin(), playerInstance.getAddress(), playerInstance.getPort());
                server.getPluginManager().callEvent(this.event);
            }

            @Override
            public void onCompletion(Server server) {
                if (!playerInstance.connected) {
                    return;
                }

                if (this.event.getLoginResult() == PlayerAsyncPreLoginEvent.LoginResult.KICK) {
                    playerInstance.close(this.event.getKickMessage(), this.event.getKickMessage());
                } else if (playerInstance.shouldLogin) {
                    playerInstance.setSkin(this.event.getSkin());
                    playerInstance.completeLoginSequence();
                    for (Consumer<Server> action : this.event.getScheduledActions()) {
                        action.accept(server);
                    }
                }
            }
        };

        this.server.getScheduler().scheduleAsyncTask(InternalPlugin.INSTANCE, this.preLoginEventTask);
        this.processLogin();
    }

    public boolean shouldLogin() {
        return this.shouldLogin;
    }

    @Override
    public String toString() {
        return "Player(name='" + getName() + "', location=" + super.toString() + ')';
    }

    @Override
    public void setAirTicks(int ticks) {
        if (this.airTicks != ticks) {
            if (this.spawned || ticks > this.airTicks) { // Don't consume air before spawned
                this.airTicks = ticks;
                this.setDataPropertyAndSendOnlyToSelf(new ShortEntityData(DATA_AIR, ticks));
            }
        }
    }

    private static boolean canGoThrough(Block block) {
        switch (block.getId()) {
            case BlockID.GLASS:
            case BlockID.ICE:
            case BlockID.GLOWSTONE:
            case BlockID.BEACON:
            case BlockID.SEA_LANTERN:
            case BlockID.STAINED_GLASS:
            case BlockID.HARD_GLASS:
            case BlockID.HARD_STAINED_GLASS:
            case BlockID.BARRIER: {
                return true;
            }
        }
        return false;
    }

    /**
     * 将物品添加到玩家的主要库存中，并将任何多余的物品丢在地上。
     * <p>
     * Add items to the player's main inventory and drop any excess items on the ground.
     *
     * @param items The items to give to the player.
     */
    public void giveItem(Item... items) {
        for (Item failed : getInventory().addItem(items)) {
            getLevel().dropItem(this, failed);
        }
    }

    public boolean isMovementServerAuthoritative() {
        return this.server.getSettings().general().serverAuthoritativeMovement() == ServerAuthoritativeMovement.SERVER_AUTH;
    }

    public boolean isServerAuthoritativeBlockBreaking() {
        return this.server.getSettings().general().serverAuthoritativeBlockBreaking() && this.isMovementServerAuthoritative();
    }

    public boolean isEnableNetworkEncryption() {
        return this.server.getSettings().network().encryption() /*&& loginChainData.isXboxAuthed()*/;
    }

    protected List<ExperimentData> getExperiments() {
        List<ExperimentData> experiments = new ObjectArrayList<>();
        //TODO Multiversion 当新版本删除部分实验性玩法时，这里也需要加上判断
        if (this.server.getSettings().features().enableExperimentMode()) {
            experiments.add(new ExperimentData("data_driven_items", true));
            experiments.add(new ExperimentData("experimental_custom_ui", true));
            experiments.add(new ExperimentData("upcoming_creator_features", true));
            experiments.add(new ExperimentData("experimental_molang_features", true));
            if (protocol >= ProtocolInfo.v1_20_0_23) {
                experiments.add(new ExperimentData("cameras", true));
                if (protocol >= ProtocolInfo.v1_20_10_21 && protocol < ProtocolInfo.v1_20_30_24) {
                    experiments.add(new ExperimentData("short_sneaking", true));
                }
            }
            if (protocol >= ProtocolInfo.v1_21_80) {
                experiments.add(new ExperimentData("experimental_graphics", true));
            }
            if (protocol >= ProtocolInfo.v1_21_100) {
                experiments.add(new ExperimentData("y_2025_drop_3", true));
            }
        }
        return experiments;
    }

    @Override
    public void display(IScoreboard scoreboard, DisplaySlot slot) {
        SetDisplayObjectivePacket pk = new SetDisplayObjectivePacket();
        pk.displaySlot = slot;
        pk.objectiveId = scoreboard.getObjectiveName();
        pk.displayName = scoreboard.getDisplayName();
        pk.criteria = scoreboard.getCriteriaName();
        pk.sortOrder = scoreboard.getSortOrder();
        this.dataPacket(pk);

        SetScorePacket pk2 = new SetScorePacket();
        pk2.infos = scoreboard.getLines().values().stream()
                .map(IScoreboardLine::toNetworkInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        pk2.action = SetScorePacket.Action.SET;
        this.dataPacket(pk2);

        PlayerScorer scorer = new PlayerScorer(this);
        IScoreboardLine line = scoreboard.getLine(scorer);
        if (slot == DisplaySlot.BELOW_NAME && line != null) {
            this.setScoreTag(line.getScore() + " " + scoreboard.getDisplayName());
        }
    }

    @Override
    public void hide(DisplaySlot slot) {
        SetDisplayObjectivePacket pk = new SetDisplayObjectivePacket();
        pk.displaySlot = slot;
        pk.objectiveId = "";
        pk.displayName = "";
        pk.criteria = "";
        pk.sortOrder = SortOrder.ASCENDING;
        this.dataPacket(pk);

        if (slot == DisplaySlot.BELOW_NAME) {
            this.setScoreTag("");
        }
    }


    @Override
    public void removeScoreboard(IScoreboard scoreboard) {
        RemoveObjectivePacket pk = new RemoveObjectivePacket();
        pk.objectiveId = scoreboard.getObjectiveName();

        this.dataPacket(pk);
    }

    @Override
    public void removeLine(IScoreboardLine line) {
        SetScorePacket packet = new SetScorePacket();
        packet.action = SetScorePacket.Action.REMOVE;
        SetScorePacket.ScoreInfo networkInfo = line.toNetworkInfo();
        if (networkInfo != null)
            packet.infos.add(networkInfo);
        this.dataPacket(packet);

        PlayerScorer scorer = new PlayerScorer(this);
        if (line.getScorer().equals(scorer) && line.getScoreboard().getViewers(DisplaySlot.BELOW_NAME).contains(this)) {
            this.setScoreTag("");
        }
    }

    @Override
    public void updateScore(IScoreboardLine line) {
        SetScorePacket packet = new SetScorePacket();
        packet.action = SetScorePacket.Action.SET;
        SetScorePacket.ScoreInfo networkInfo = line.toNetworkInfo();
        if (networkInfo != null) packet.infos.add(networkInfo);
        this.dataPacket(packet);

        PlayerScorer scorer = new PlayerScorer(this);
        if (line.getScorer().equals(scorer) && line.getScoreboard().getViewers(DisplaySlot.BELOW_NAME).contains(this)) {
            this.setScoreTag(line.getScore() + " " + line.getScoreboard().getDisplayName());
        }
    }

    public Map<Integer, FormWindow> getServerSettings() {
        return serverSettings;
    }

    public String getUsername() {
        return username;
    }

    public List<UUID> getAvailableEmotes() {
        return availableEmotes;
    }

    public int getLastEmote() {
        return lastEmote;
    }

    public void setLastEmote(int lastEmote) {
        this.lastEmote = lastEmote;
    }

    public static boolean validateVehicleInput(float value) {
        return -1.1f <= value && value <= 1.1f; //data from ecnk
    }

    /**
     * Send current held item to client
     */
    private void syncHeldItem() {
        InventorySlotPacket pk = new InventorySlotPacket();
        pk.slot = this.inventory.getHeldItemIndex();
        pk.item = this.inventory.getItem(pk.slot);
        pk.inventoryId = ContainerIds.INVENTORY;
        this.dataPacket(pk);
    }

    /**
     * Run every tick to send updated data if needed
     */
    void resetPacketCounters() {
        if (this.needSendAdventureSettings) {
            this.needSendAdventureSettings = false;
            this.adventureSettings.update(false);
        }
        if (this.needSendData) {
            this.needSendData = false;
            this.sendData(this); // Send data only once even if multiple actions fail
        }
        if (this.needSendFoodLevel) {
            this.needSendFoodLevel = false;
            this.foodData.sendFood();
        }
        if (this.needSendInventory && this.spawned) {
            this.needSendInventory = false;
            this.getCursorInventory().sendContents(this);
            this.sendAllInventories();
        }
        if (this.needSendHeldItem && this.spawned) {
            this.needSendHeldItem = false;
            this.syncHeldItem();
        }
        if (this.needSendUpdateClientInputLocksPacket) {
            this.needSendUpdateClientInputLocksPacket = false;
            this.sendUpdateClientInputLocksPacket();
        }
    }

    /**
     * Check whether player can eat (difficulty, gamemode, current food level)
     *
     * @param update send current food level to client
     * @return can eat
     */
    public boolean canEat(boolean update) {
        if (this.foodData.isHungry() || this.isCreative() || this.server.getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }
        if (update) {
            this.needSendFoodLevel = true;
        }
        return false;
    }

    public void setLockCameraInput(boolean lockCameraInput) {
        if (this.lockCameraInput != lockCameraInput) {
            this.lockCameraInput = lockCameraInput;
            this.needSendUpdateClientInputLocksPacket = true;
        }
    }

    public void setLockMovementInput(boolean lockMovementInput) {
        if (this.lockMovementInput != lockMovementInput) {
            this.lockMovementInput = lockMovementInput;
            this.needSendUpdateClientInputLocksPacket = true;
        }
    }

    protected void sendUpdateClientInputLocksPacket() {
        UpdateClientInputLocksPacket packet = new UpdateClientInputLocksPacket();
        if (this.lockCameraInput) {
            packet.lockComponentData |= UpdateClientInputLocksPacket.FLAG_CAMERA;
        }
        if (this.lockMovementInput) {
            packet.lockComponentData |= UpdateClientInputLocksPacket.FLAG_MOVEMENT;
        }
        packet.setServerPosition(this.getLocation().add(0, this.getBaseOffset(), 0).asVector3f());

        this.dataPacket(packet);
    }

    public boolean isLockCameraInput() {
        return this.lockCameraInput;
    }

    public boolean isLockMovementInput() {
        return this.lockMovementInput;
    }

    public Boolean isOpenSignFront() {
        return openSignFront;
    }

    public void setOpenSignFront(Boolean frontSide) {
        openSignFront = frontSide;
    }

    public void openSignEditor(Vector3 position, boolean frontSide) {
        if (openSignFront == null) {
            BlockEntity blockEntity = this.getLevel().getBlockEntity(position);
            if (blockEntity instanceof BlockEntitySign blockEntitySign) {
                //if (blockEntitySign.getEditorEntityRuntimeId() == -1) {
                blockEntitySign.setEditorEntityRuntimeId(this.getId());
                OpenSignPacket openSignPacket = new OpenSignPacket();
                openSignPacket.setPosition(position.asBlockVector3());
                openSignPacket.setFrontSide(frontSide);
                this.dataPacket(openSignPacket);
                setOpenSignFront(frontSide);
                //}
            } else {
                throw new IllegalArgumentException("Block at this position is not a sign");
            }
        }
    }

    protected void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    protected void setRawUuid(byte[] uuid) {
        this.rawUUID = uuid;
    }

    protected void setLoginUuid(UUID uuid) {
        this.loginUuid = uuid;
    }

    @Override
    protected float getBaseOffset() {
        return super.getBaseOffset();
    }
}