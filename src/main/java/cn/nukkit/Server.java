package cn.nukkit;

import cn.nukkit.block.Block;
import cn.nukkit.blockentity.*;
import cn.nukkit.blockentity.impl.*;
import cn.nukkit.command.*;
import cn.nukkit.console.NukkitConsole;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.data.profession.Profession;
import cn.nukkit.entity.data.property.EntityProperty;
import cn.nukkit.entity.item.*;
import cn.nukkit.entity.mob.*;
import cn.nukkit.entity.passive.*;
import cn.nukkit.entity.projectile.*;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.level.LevelInitEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.server.PlayerDataSerializeEvent;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.event.server.ServerStopEvent;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.BaseLang;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.level.EnumLevel;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.LevelProviderManager;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.leveldb.LevelDBProvider;
import cn.nukkit.level.generator.*;
import cn.nukkit.level.tickingarea.manager.SimpleTickingAreaManager;
import cn.nukkit.level.tickingarea.manager.TickingAreaManager;
import cn.nukkit.level.tickingarea.storage.JSONTickingAreaStorage;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.metadata.EntityMetadataStore;
import cn.nukkit.metadata.LevelMetadataStore;
import cn.nukkit.metadata.PlayerMetadataStore;
import cn.nukkit.metrics.NukkitMetrics;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.BatchingHelper;
import cn.nukkit.network.Network;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerListPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.query.QueryHandler;
import cn.nukkit.network.rcon.RCON;
import cn.nukkit.permission.BanEntry;
import cn.nukkit.permission.BanList;
import cn.nukkit.permission.DefaultPermissions;
import cn.nukkit.permission.Permissible;
import cn.nukkit.plugin.*;
import cn.nukkit.plugin.service.NKServiceManager;
import cn.nukkit.plugin.service.ServiceManager;
import cn.nukkit.registry.Registries;
import cn.nukkit.resourcepacks.ResourcePackManager;
import cn.nukkit.resourcepacks.loader.JarPluginResourcePackLoader;
import cn.nukkit.resourcepacks.loader.ZippedResourcePackLoader;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scoreboard.manager.IScoreboardManager;
import cn.nukkit.scoreboard.manager.ScoreboardManager;
import cn.nukkit.scoreboard.storage.JSONScoreboardStorage;
import cn.nukkit.settings.ServerSettings;
import cn.nukkit.settings.converter.LegacyPropertiesConverter;
import cn.nukkit.settings.initializer.ServerSettingsConfigInitializer;
import cn.nukkit.utils.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import eu.okaeri.configs.ConfigManager;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.extern.log4j.Log4j2;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * The main server class
 *
 * @author MagicDroidX
 * @author Box
 */
@Log4j2
public class Server {

    public static final String BROADCAST_CHANNEL_ADMINISTRATIVE = "nukkit.broadcast.admin";
    public static final String BROADCAST_CHANNEL_USERS = "nukkit.broadcast.user";

    private static Server instance;

    private final BanList banByName;
    private final BanList banByIP;
    private final Config operators;
    private final Config whitelist;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private boolean hasStopped;

    private final PluginManager pluginManager;
    private final ServerScheduler scheduler;

    private int tickCounter;
    private long nextTick;
    private final float[] tickAverage = {20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};
    private final float[] useAverage = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private float maxTick = 20;
    private float maxUse = 0;

    private final NukkitConsole console;
    private final ConsoleThread consoleThread;

    private final SimpleCommandMap commandMap;
    private final CraftingManager craftingManager;
    private final ResourcePackManager resourcePackManager;
    private final ConsoleCommandSender consoleSender;
    private final IScoreboardManager scoreboardManager;

    private final TickingAreaManager tickingAreaManager;

    private RCON rcon;

    private final EntityMetadataStore entityMetadata;
    private final PlayerMetadataStore playerMetadata;
    private final LevelMetadataStore levelMetadata;
    private final Network network;
    private int autoSaveTicker;
    private final BaseLang baseLang;
    private final String filePath;
    private final String dataPath;
    private final String pluginPath;
    private QueryHandler queryHandler;
    private QueryRegenerateEvent queryRegenerateEvent;
    private final UUID serverID;
    private final ServerSettings settings;

    private final Map<InetSocketAddress, Player> players = new HashMap<>();
    final Map<UUID, Player> playerList = new HashMap<>();

    private static final Pattern uuidPattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}.dat$");

    private final Map<Integer, Level> levels = new ConcurrentHashMap<>() {
        @Override
        public Level put(@NotNull Integer key, @NotNull Level value) {
            Level result = super.put(key, value);
            levelArray = levels.values().toArray(new Level[0]);
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean result = super.remove(key, value);
            levelArray = levels.values().toArray(new Level[0]);
            return result;
        }

        @Override
        public Level remove(@NotNull Object key) {
            Level result = super.remove(key);
            levelArray = levels.values().toArray(new Level[0]);
            return result;
        }
    };

    private Level[] levelArray = new Level[0];
    private final ServiceManager serviceManager = new NKServiceManager();
    private Level defaultLevel;
    private final Thread currentThread;
    /**
     * This is needed for structure generation
     */
    public final ForkJoinPool computeThreadPool;
    private Watchdog watchdog;
    private final DB nameLookup;
    private PlayerDataSerializer playerDataSerializer;
    private SpawnerTask spawnerTask;
    private final BatchingHelper batchingHelper;

    Server(final String filePath, String dataPath, String pluginPath, boolean loadPlugins, boolean debug) {
        Preconditions.checkState(instance == null, "Already initialized!");
        currentThread = Thread.currentThread(); // Saves the current thread instance as a reference, used in Server#isPrimaryThread()
        instance = this;

        this.filePath = filePath;
        if (!new File(dataPath + "worlds/").exists()) {
            new File(dataPath + "worlds/").mkdirs();
        }

        if (!new File(pluginPath).exists()) {
            new File(pluginPath).mkdirs();
        }

        this.dataPath = new File(dataPath).getAbsolutePath() + '/';
        this.pluginPath = new File(pluginPath).getAbsolutePath() + '/';

        this.playerDataSerializer = new DefaultPlayerDataSerializer(this);

        this.console = new NukkitConsole();
        this.consoleThread = new ConsoleThread();
        this.consoleThread.start();
        this.console.setExecutingCommands(true);

        log.info("Loading server settings...");
        this.settings = ConfigManager.create(ServerSettings.class, new ServerSettingsConfigInitializer(Path.of(this.dataPath + "/settings.yml")));

        LegacyPropertiesConverter legacyPropertiesConverter = new LegacyPropertiesConverter(settings);
        legacyPropertiesConverter.convert();

        if (!settings.network().encryption()) {
            log.warn("Encryption is not enabled. For better security, it's recommended to enable it if you don't use a proxy software.");
        }

        int debugLvl = NukkitMath.clamp(settings.general().debugLevel(), 1, 3);
        if (debug && debugLvl < 2) {
            debugLvl = 2;
        }
        Nukkit.DEBUG = debugLvl;

        if (!new File(dataPath + "players/").exists() && settings.player().savePlayerData()) {
            new File(dataPath + "players/").mkdirs();
        }

        this.baseLang = new BaseLang(settings.general().language());

        this.computeThreadPool = new ForkJoinPool(Math.min(0x7fff, Runtime.getRuntime().availableProcessors()), new ComputeThreadPoolThreadFactory(), null, false);

        Object poolSize = settings.performance().asyncWorkers();
        if (!(poolSize instanceof Integer)) {
            try {
                poolSize = Integer.valueOf((String) poolSize);
            } catch (Exception e) {
                poolSize = Math.max(Runtime.getRuntime().availableProcessors() + 1, 4);
            }
        }

        ServerScheduler.WORKERS = (int) poolSize;

        Zlib.setProvider(settings.network().compression().zlibProvider());

        this.scheduler = new ServerScheduler();

        this.batchingHelper = new BatchingHelper();

        if (settings.network().rcon().enable()) {
            try {
                this.rcon = new RCON(this, settings.network().rcon().password(), (!this.getIp().isEmpty()) ? this.getIp() : "0.0.0.0", settings.network().rcon().port());
            } catch (IllegalArgumentException e) {
                log.error(baseLang.translateString(e.getMessage(), e.getCause().getMessage()));
            }
        }

        this.entityMetadata = new EntityMetadataStore();
        this.playerMetadata = new PlayerMetadataStore();
        this.levelMetadata = new LevelMetadataStore();
        this.scoreboardManager = new ScoreboardManager(new JSONScoreboardStorage(this.dataPath + "scoreboard.json"));
        this.tickingAreaManager = new SimpleTickingAreaManager(new JSONTickingAreaStorage(this.dataPath + "worlds/"));

        this.operators = new Config(this.dataPath + "ops.txt", Config.ENUM);
        this.whitelist = new Config(this.dataPath + "white-list.txt", Config.ENUM);
        this.banByName = new BanList(this.dataPath + "banned-players.json");
        this.banByName.load();
        this.banByIP = new BanList(this.dataPath + "banned-ips.json");
        this.banByIP.load();

        this.setAutoSave(settings.world().autoSave().enable());

        if (this.settings.world().enableHardcore() && this.getDifficulty() != Difficulty.HARD) {
            this.setDifficulty(Difficulty.HARD);
        }

        org.apache.logging.log4j.Level currentLevel = Nukkit.getLogLevel();
        for (org.apache.logging.log4j.Level level : org.apache.logging.log4j.Level.values()) {
            if (level.intLevel() == (Nukkit.DEBUG + 3) * 100 && level.intLevel() > currentLevel.intLevel()) {
                Nukkit.setLogLevel(level);
                break;
            }
        }

        log.info("\u00A7b-- \u00A7dLumi \u00A7b--");

        this.consoleSender = new ConsoleCommandSender();

        Registries.BLOCK_TO_ITEM.init();
        Registries.EFFECT.init();
        Registries.POTION.init();

        Registries.ENTITY.init();
        registerProfessions();
        Registries.BLOCK_ENTITY.init();
        Registries.ENCHANTMENT.init();

        Block.init();
        GlobalBlockPalette.init();
        RuntimeItems.init();
        Item.init();
        EnumBiome.values();
        Attribute.init();
        GlobalBlockPalette.getOrCreateRuntimeId(ProtocolInfo.CURRENT_PROTOCOL, 0, 0);

        Registries.FUEL.init();
        Registries.DISPENSE_BEHAVIOR.init();

        this.commandMap = new SimpleCommandMap(this);

        // Convert legacy data before plugins get the chance to mess with it
        try {
            nameLookup = Iq80DBFactory.factory.open(new File(dataPath, "players"), new Options()
                    .createIfMissing(true)
                    .compressionType(CompressionType.ZLIB_RAW));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (this.settings.player().savePlayerDataByUuid()) {
            convertLegacyPlayerData();
        }

        this.serverID = UUID.randomUUID();

        this.craftingManager = new CraftingManager();
        this.resourcePackManager = new ResourcePackManager(
                new ZippedResourcePackLoader(new File(Nukkit.DATA_PATH, "resource_packs")),
                new JarPluginResourcePackLoader(new File(this.pluginPath))
        );

        this.pluginManager = new PluginManager(this, this.commandMap);
        this.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this.consoleSender);

        this.pluginManager.registerInterface(JavaPluginLoader.class);

        this.queryRegenerateEvent = new QueryRegenerateEvent(this, 5);

        log.info(this.baseLang.translateString("nukkit.server.networkStart", new String[]{this.getIp().isEmpty() ? "*" : this.getIp(), String.valueOf(this.getPort())}));
        this.network = new Network(this);
        this.network.setName(this.settings.general().motd());
        this.network.setSubName(this.settings.general().subMotd());
        this.network.registerInterface(new RakNetInterface(this));

        this.pluginManager.loadInternalPlugin();
        if (loadPlugins) {
            this.pluginManager.loadPlugins(this.pluginPath);
            if (this.settings.general().enableSpark()) {
                SparkInstaller.initSpark(this);
            }
            this.enablePlugins(PluginLoadOrder.STARTUP);
        }

        Item.initCreativeItems();
        Block.initCustomBlocks();

        LevelProviderManager.addProvider(this, Anvil.class);
        LevelProviderManager.addProvider(this, LevelDBProvider.class);

        Generator.addGenerator(Flat.class, "flat", Generator.TYPE_FLAT);
        Generator.addGenerator(Normal.class, "normal", Generator.TYPE_INFINITE);
        Generator.addGenerator(Normal.class, "default", Generator.TYPE_INFINITE);
        Generator.addGenerator(OldNormal.class, "oldnormal", Generator.TYPE_INFINITE);
        Generator.addGenerator(Nether.class, "nether", Generator.TYPE_NETHER);
        Generator.addGenerator(End.class, "the_end", Generator.TYPE_THE_END);
        Generator.addGenerator(cn.nukkit.level.generator.Void.class, "void", Generator.TYPE_VOID);

        if (this.defaultLevel == null) {
            String defaultName = this.settings.world().defaultWorldName();
            if (defaultName == null || defaultName.trim().isEmpty()) {
                defaultName = "world";
                this.getLogger().warning("default-world-name cannot be null, using default");
                this.settings.world().defaultWorldName(defaultName);
            }

            if (!this.loadLevel(defaultName)) {
                long seed;
                var seedString = settings.world().defaultWorldSeed();
                try {
                    seed = Long.parseLong(seedString);
                } catch (NumberFormatException e) {
                    seed = seedString.hashCode();
                }
                this.generateLevel(defaultName, seed == 0 ? System.currentTimeMillis() : seed);
            }

            this.setDefaultLevel(this.getLevelByName(defaultName));
        }

        if (this.defaultLevel == null) {
            this.getLogger().emergency(this.baseLang.translateString("nukkit.level.defaultError"));
            this.forceShutdown();
            return;
        }

        for (Map.Entry<Integer, Level> entry : this.getLevels().entrySet()) {
            Level level = entry.getValue();
            this.getLogger().debug("Preparing spawn region for level " + level.getName());
            Position spawn = level.getSpawnLocation();
            level.populateChunk(spawn.getChunkX(), spawn.getChunkZ(), true);
        }

        // Load levels
        if (this.settings.world().loadAllWorlds()) {
            try {
                for (File fs : new File(new File("").getCanonicalPath() + "/worlds/").listFiles()) {
                    if ((fs.isDirectory() && !this.isLevelLoaded(fs.getName()))) {
                        this.loadLevel(fs.getName());
                    }
                }
                EnumLevel.initLevels();
            } catch (Exception e) {
                this.getLogger().error("Unable to load levels", e);
            }
        }

        if (loadPlugins) {
            this.enablePlugins(PluginLoadOrder.POSTWORLD);
        }

        EntityProperty.init();
        EntityProperty.buildPacket();
        EntityProperty.buildPlayerProperty();

        if (this.settings.performance().threadWatchdog()) {
            this.watchdog = new Watchdog(this, this.settings.performance().threadWatchdogTick());
            this.watchdog.start();
        }

        if (this.settings.world().entity().entityAutoSpawnTask()) {
            this.spawnerTask = new SpawnerTask();
            int spawnerTicks = Math.max(this.settings.world().entity().ticksPerEntitySpawns(), 2) >> 1; // Run the spawner on 2x speed but spawn only either monsters or animals
            this.scheduler.scheduleDelayedRepeatingTask(InternalPlugin.INSTANCE, this.spawnerTask, spawnerTicks, spawnerTicks);
        }

        if (this.settings.general().bstatsMetrics()) {
            new NukkitMetrics(this);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::forceShutdown));

        this.start();
    }

    public int broadcastMessage(String message) {
        return this.broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    public int broadcastMessage(TextContainer message) {
        return this.broadcast(message, BROADCAST_CHANNEL_USERS);
    }

    public int broadcastMessage(String message, CommandSender[] recipients) {
        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.length;
    }

    public int broadcastMessage(String message, Collection<? extends CommandSender> recipients) {
        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    public int broadcastMessage(TextContainer message, Collection<? extends CommandSender> recipients) {
        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    public int broadcast(String message, String permissions) {
        Set<CommandSender> recipients = new HashSet<>();

        for (String permission : permissions.split(";")) {
            for (Permissible permissible : this.pluginManager.getPermissionSubscriptions(permission)) {
                if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                    recipients.add((CommandSender) permissible);
                }
            }
        }

        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }

    public int broadcast(TextContainer message, String permissions) {
        Set<CommandSender> recipients = new HashSet<>();

        for (String permission : permissions.split(";")) {
            for (Permissible permissible : this.pluginManager.getPermissionSubscriptions(permission)) {
                if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                    recipients.add((CommandSender) permissible);
                }
            }
        }

        for (CommandSender recipient : recipients) {
            recipient.sendMessage(message);
        }

        return recipients.size();
    }


    public static void broadcastPacket(Collection<Player> players, DataPacket packet) {
        for (Player player : players) {
            player.dataPacket(packet);
        }
    }

    public static void broadcastPacket(Player[] players, DataPacket packet) {
        for (Player player : players) {
            player.dataPacket(packet);
        }
    }

    public static void broadcastPackets(Player[] players, DataPacket[] packets) {
        for (Player player : players) {
            for (DataPacket packet : packets) {
                player.dataPacket(packet);
            }
        }
    }

    public void batchPackets(Player[] players, DataPacket[] packets) {
        this.batchingHelper.batchPackets(players, packets);
    }

    @Deprecated
    public void batchPackets(Player[] players, DataPacket[] packets, boolean forceSync) {
        this.batchingHelper.batchPackets(players, packets);
    }

    public void enablePlugins(PluginLoadOrder type) {
        for (Plugin plugin : new ArrayList<>(this.pluginManager.getPlugins().values())) {
            if (!plugin.isEnabled() && type == plugin.getDescription().getOrder()) {
                this.enablePlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            DefaultPermissions.registerCorePermissions();
        }
    }

    public void enablePlugin(Plugin plugin) {
        this.pluginManager.enablePlugin(plugin);
    }

    public void disablePlugins() {
        this.pluginManager.disablePlugins();
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine) throws ServerException {
        // First we need to check if this command is on the main thread or not, if not, warn the user
        if (!this.isPrimaryThread()) {
            this.getLogger().warning("Command Dispatched Async: " + commandLine);
        }
        if (sender == null) {
            throw new ServerException("CommandSender is not valid");
        }

        return this.commandMap.dispatch(sender, commandLine);
    }

    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }

    public IScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public void reload() {
        log.info("Reloading...");

        log.info("Saving levels...");

        for (Level level : this.levelArray) {
            level.save();
        }

        this.pluginManager.clearPlugins();
        this.commandMap.clearCommands();

        log.info("Reloading server settings...");
        this.settings.load();

        if (this.settings.world().enableHardcore() && this.getDifficulty() != Difficulty.HARD) {
            this.setDifficulty(Difficulty.HARD);
        }

        this.banByIP.load();
        this.banByName.load();
        this.reloadWhitelist();
        this.operators.reload();

        for (BanEntry entry : this.banByIP.getEntires().values()) {
            try {
                this.network.blockAddress(InetAddress.getByName(entry.getName()), -1);
            } catch (UnknownHostException ignore) {
            }
        }

        this.pluginManager.registerInterface(JavaPluginLoader.class);
        this.pluginManager.loadPlugins(this.pluginPath);
        if (this.settings.general().enableSpark()) {
            SparkInstaller.initSpark(this);
        }
        this.enablePlugins(PluginLoadOrder.STARTUP);
        this.enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    public void shutdown() {
        isRunning.compareAndSet(true, false);
    }

    public void forceShutdown() {
        this.forceShutdown(settings.general().shutdownMessage());
    }

    public void forceShutdown(String reason) {
        if (this.hasStopped) {
            return;
        }

        try {
            isRunning.compareAndSet(true, false);

            this.hasStopped = true;

            ServerStopEvent serverStopEvent = new ServerStopEvent();
            pluginManager.callEvent(serverStopEvent);

            if (this.rcon != null) {
                this.getLogger().debug("Closing RCON...");
                this.rcon.close();
            }

            this.getLogger().debug("Disconnecting all players...");
            for (Player player : new ArrayList<>(this.players.values())) {
                player.close(player.getLeaveMessage(), reason);
            }

            this.getLogger().debug("Disabling all plugins...");
            this.disablePlugins();

            this.getLogger().debug("Unloading all levels...");
            for (Level level : this.levelArray) {
                this.unloadLevel(level, true);
                this.nextTick = System.currentTimeMillis(); // Fix Watchdog killing the server while saving worlds
            }

            this.getLogger().debug("Removing event handlers...");
            HandlerList.unregisterAll();

            this.getLogger().debug("Stopping all tasks...");
            this.scheduler.cancelAllTasks();
            this.scheduler.mainThreadHeartbeat(Integer.MAX_VALUE);

            this.getLogger().debug("Closing console...");
            this.consoleThread.interrupt();

            this.getLogger().debug("Closing BatchingHelper...");
            this.batchingHelper.shutdown();

            this.getLogger().debug("Stopping network interfaces...");
            for (SourceInterface interfaz : this.network.getInterfaces()) {
                interfaz.shutdown();
                this.network.unregisterInterface(interfaz);
            }

            this.batchingHelper.shutdown();

            if (nameLookup != null) {
                this.getLogger().debug("Closing name lookup DB...");
                nameLookup.close();
            }

            if (this.watchdog != null) {
                this.getLogger().debug("Stopping Watchdog...");
                this.watchdog.kill();
            }
        } catch (Exception e) {
            log.fatal("Exception happened while shutting down, exiting the process", e);
            System.exit(1);
        }
    }

    public void start() {
        if (this.settings.network().enableQuery()) {
            this.queryHandler = new QueryHandler();
        }

        for (BanEntry entry : this.banByIP.getEntires().values()) {
            try {
                this.network.blockAddress(InetAddress.getByName(entry.getName()), -1);
            } catch (UnknownHostException ignore) {
            }
        }

        this.tickCounter = 0;

        log.info(this.baseLang.translateString("nukkit.server.startFinished", String.valueOf((double) (System.currentTimeMillis() - Nukkit.START_TIME) / 1000)));

        this.tickProcessor();
        this.forceShutdown();
    }

    private static final byte[] QUERY_PREFIX = {(byte) 0xfe, (byte) 0xfd};

    /**
     * Internal: Handle query
     *
     * @param address sender address
     * @param payload payload
     */
    public void handlePacket(InetSocketAddress address, ByteBuf payload) {
        try {
            if (this.queryHandler == null || !payload.isReadable(3)) {
                return;
            }
            byte[] prefix = new byte[2];
            payload.readBytes(prefix);
            if (Arrays.equals(prefix, QUERY_PREFIX)) {
                this.queryHandler.handle(address, payload);
            }
        } catch (Exception e) {
            log.error("Error whilst handling packet", e);

            this.network.blockAddress(address.getAddress(), -1);
        }
    }

    private int lastLevelGC;

    /**
     * Internal: Tick the server
     */
    public void tickProcessor() {
        this.nextTick = System.currentTimeMillis();
        try {
            while (this.isRunning.get()) {
                try {
                    this.tick();

                    long next = this.nextTick;
                    long current = System.currentTimeMillis();

                    if (next - 0.1 > current) {
                        long allocated = next - current - 1;

                        if (settings.world().doWorldGc()) { // Instead of wasting time, do something potentially useful
                            int offset = 0;
                            for (int i = 0; i < levelArray.length; i++) {
                                offset = (i + lastLevelGC) % levelArray.length;
                                Level level = levelArray[offset];
                                if (!level.isBeingConverted) {
                                    level.doGarbageCollection(allocated - 1);
                                }
                                allocated = next - System.currentTimeMillis();
                                if (allocated <= 0) break;
                            }
                            lastLevelGC = offset + 1;
                        }

                        if (allocated > 0 || !settings.world().doWorldGc()) {
                            try {
                                //noinspection BusyWait
                                Thread.sleep(allocated, 900000);
                            } catch (Exception e) {
                                this.getLogger().logException(e);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    log.error("A RuntimeException happened while ticking the server", e);
                }
            }
        } catch (Throwable e) {
            log.fatal("Exception happened while ticking server\n{}", Utils.getAllThreadDumps(), e);
        }
    }

    public void onPlayerCompleteLoginSequence(Player player) {
        this.playerList.put(player.getUniqueId(), player);
        this.updatePlayerListData(player.getLoginUuid(), player.getId(), player.getDisplayName(), player.getSkin(), player.getLoginChainData().getXUID());
    }

    public void addPlayer(InetSocketAddress socketAddress, Player player) {
        this.players.put(socketAddress, player);
    }

    public void addOnlinePlayer(Player player) {
        this.playerList.put(player.getUniqueId(), player);
        this.updatePlayerListData(player.getLoginUuid(), player.getId(), player.getDisplayName(), player.getSkin(), player.getLoginChainData().getXUID());
    }

    public void removeOnlinePlayer(Player player) {
        if (player.getUniqueId() == null) {
            return;
        }
        if (this.playerList.containsKey(player.getUniqueId())) {
            this.playerList.remove(player.getUniqueId());

            PlayerListPacket pk = new PlayerListPacket();
            pk.type = PlayerListPacket.TYPE_REMOVE;
            pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(player.getLoginUuid())};

            Server.broadcastPacket(this.playerList.values(), pk);
        }
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin) {
        this.updatePlayerListData(uuid, entityId, name, skin, "", this.playerList.values());
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin, String xboxUserId) {
        this.updatePlayerListData(uuid, entityId, name, skin, xboxUserId, this.playerList.values());
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin, Player[] players) {
        this.updatePlayerListData(uuid, entityId, name, skin, "", players);
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin, String xboxUserId, Player[] players) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_ADD;
        pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(uuid, entityId, name, skin, xboxUserId)};
        this.batchPackets(players, new DataPacket[]{pk}); // This is sent "directly" so it always gets thru before possible TYPE_REMOVE packet for NPCs etc.
    }

    public void updatePlayerListData(UUID uuid, long entityId, String name, Skin skin, String xboxUserId, Collection<Player> players) {
        this.updatePlayerListData(uuid, entityId, name, skin, xboxUserId, players.toArray(Player.EMPTY_ARRAY));
    }

    public void removePlayerListData(UUID uuid) {
        this.removePlayerListData(uuid, this.playerList.values());
    }

    public void removePlayerListData(UUID uuid, Player[] players) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_REMOVE;
        pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(uuid)};
        for (Player player : players) {
            player.dataPacket(pk);
        }
    }

    public void removePlayerListData(UUID uuid, Collection<Player> players) {
        this.removePlayerListData(uuid, players.toArray(Player.EMPTY_ARRAY));
    }

    public void removePlayerListData(UUID uuid, Player player) {
        PlayerListPacket pk = new PlayerListPacket();
        pk.type = PlayerListPacket.TYPE_REMOVE;
        pk.entries = new PlayerListPacket.Entry[]{new PlayerListPacket.Entry(uuid)};
        player.dataPacket(pk);
    }

    public void sendFullPlayerListData(Player player) {
        PlayerListPacket.Entry[] array = this.playerList.values().stream()
                .map(p -> new PlayerListPacket.Entry(
                        p.getLoginUuid(),
                        p.getId(),
                        p.getDisplayName(),
                        p.getSkin(),
                        p.getLoginChainData().getXUID()))
                .toArray(PlayerListPacket.Entry[]::new);
        Object[][] splitArray = Utils.splitArray(array, 50);
        if (splitArray != null) {
            for (Object[] a : splitArray) {
                PlayerListPacket pk = new PlayerListPacket();
                pk.type = PlayerListPacket.TYPE_ADD;
                pk.entries = (PlayerListPacket.Entry[]) a;
                player.dataPacket(pk);
            }
        }
    }

    public void sendRecipeList(Player player) {
        BatchPacket cachedPacket = this.craftingManager.getCachedPacket(player.protocol);
        if (cachedPacket != null) { // Don't send recipes if they wouldn't work anyways
            player.dataPacket(cachedPacket);
        }
    }

    private void checkTickUpdates(int currentTick) {
        if (this.settings.performance().alwaysTickPlayers()) {
            for (Player p : new ArrayList<>(this.players.values())) {
                p.onUpdate(currentTick);
            }
        }

        for (Player p : this.getOnlinePlayers().values()) {
            p.resetPacketCounters();
        }

        int baseTickRate = this.settings.performance().baseTickRate();

        // Do level ticks
        for (Level level : this.levelArray) {
            if (level.isBeingConverted || (level.getTickRate() > baseTickRate && --level.tickRateCounter > 0)) {
                continue;
            }

            try {
                long levelTime = System.currentTimeMillis();
                level.providerLock.readLock().lock();
                if (level.getProvider() == null) {//世界在其他线程上卸载
                    continue;
                }
                level.doTick(currentTick);
                int tickMs = (int) (System.currentTimeMillis() - levelTime);
                level.tickRateTime = tickMs;

                if (this.settings.performance().autoTickRate()) {
                    if (tickMs < 50 && level.getTickRate() > baseTickRate) {
                        int r;
                        level.setTickRate(r = level.getTickRate() - 1);
                        if (r > baseTickRate) {
                            level.tickRateCounter = level.getTickRate();
                        }
                        this.getLogger().debug("Raising level \"" + level.getName() + "\" tick rate to " + level.getTickRate() + " ticks");
                    } else if (tickMs >= 50) {
                        if (level.getTickRate() == baseTickRate) {
                            level.setTickRate(Math.max(baseTickRate + 1, Math.min(this.settings.performance().autoTickRateLimit(), tickMs / 50)));
                            this.getLogger().debug("Level \"" + level.getName() + "\" took " + tickMs + "ms, setting tick rate to " + level.getTickRate() + " ticks");
                        } else if ((tickMs / level.getTickRate()) >= 50 && level.getTickRate() < this.settings.performance().autoTickRateLimit()) {
                            level.setTickRate(level.getTickRate() + 1);
                            this.getLogger().debug("Level \"" + level.getName() + "\" took " + tickMs + "ms, setting tick rate to " + level.getTickRate() + " ticks");
                        }
                        level.tickRateCounter = level.getTickRate();
                    }
                }
            } catch (Exception e) {
                log.error(this.baseLang.translateString("nukkit.level.tickError", new String[]{level.getFolderName(), Utils.getExceptionMessage(e)}));
            } finally {
                level.providerLock.readLock().unlock();
            }
        }
    }

    public void doAutoSave() {
        if (this.settings.world().autoSave().enable()) {
            for (Player player : new ArrayList<>(this.players.values())) {
                if (player.isOnline()) {
                    player.save(true);
                } else if (!player.isConnected()) {
                    this.removePlayer(player);
                }
            }

            for (Level level : this.levelArray) {
                if (!settings.world().autoSave().disabledWorlds().contains(level.getName())) {
                    level.save();
                }
            }
        }
    }

    private void tick() {
        long tickTime = System.currentTimeMillis();

        long time = tickTime - this.nextTick;
        if (time < -25) {
            try {
                Thread.sleep(Math.max(5, -time - 25));
            } catch (InterruptedException e) {
                Server.getInstance().getLogger().logException(e);
            }
        }

        long tickTimeNano = System.nanoTime();
        if ((tickTime - this.nextTick) < -25) {
            return;
        }

        ++this.tickCounter;

        this.network.processInterfaces();

        if (this.rcon != null) {
            this.rcon.check();
        }

        this.scheduler.mainThreadHeartbeat(this.tickCounter);

        this.checkTickUpdates(this.tickCounter);

        for (Player player : new ArrayList<>(this.players.values())) {
            player.checkNetwork();
        }

        if ((this.tickCounter & 0b1111) == 0) {

            this.network.resetStatistics();
            this.maxTick = 20;
            this.maxUse = 0;

            if ((this.tickCounter & 0b111111111) == 0) {
                try {
                    this.pluginManager.callEvent(this.queryRegenerateEvent = new QueryRegenerateEvent(this, 5));
                    if (this.queryHandler != null) {
                        this.queryHandler.regenerateInfo();
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }

            this.network.updateName();
        }

        if (++this.autoSaveTicker >= this.settings.world().autoSave().perTicks()) {
            this.autoSaveTicker = 0;
            this.doAutoSave();
        }

        if (this.tickCounter % 100 == 0) {
            for (Level level : this.levelArray) {
                if (!level.isBeingConverted) {
                    level.doChunkGarbageCollection();
                }
            }
        }

        long nowNano = System.nanoTime();

        float tick = (float) Math.min(20, 1000000000 / Math.max(1000000, ((double) nowNano - tickTimeNano)));
        float use = (float) Math.min(1, ((double) (nowNano - tickTimeNano)) / 50000000);

        if (this.maxTick > tick) {
            this.maxTick = tick;
        }

        if (this.maxUse < use) {
            this.maxUse = use;
        }

        System.arraycopy(this.tickAverage, 1, this.tickAverage, 0, this.tickAverage.length - 1);
        this.tickAverage[this.tickAverage.length - 1] = tick;

        System.arraycopy(this.useAverage, 1, this.useAverage, 0, this.useAverage.length - 1);
        this.useAverage[this.useAverage.length - 1] = use;

        if ((this.nextTick - tickTime) < -1000) {
            this.nextTick = tickTime;
        } else {
            this.nextTick += 50;
        }
    }

    public long getNextTick() {
        return nextTick;
    }

    public QueryRegenerateEvent getQueryInformation() {
        return this.queryRegenerateEvent;
    }

    public String getName() {
        return Nukkit.NUKKIT;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public String getNukkitVersion() {
        return Nukkit.VERSION;
    }

    public String getCodename() {
        return Nukkit.CODENAME;
    }

    public String getVersion() {
        return ProtocolInfo.MINECRAFT_VERSION;
    }

    public String getApiVersion() {
        return Nukkit.API_VERSION;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public ServerSettings getSettings() {
        return settings;
    }

    public int getMaxPlayers() {
        return this.settings.general().maxPlayers();
    }

    public void setMaxPlayers(int maxPlayers) {
        this.settings.general().maxPlayers(maxPlayers);
    }

    public Difficulty getDifficulty() {
        return this.settings.world().difficulty();
    }

    public void setDifficulty(Difficulty difficulty) {
        this.settings.world().difficulty(difficulty);
    }

    public int getPort() {
        return this.settings.general().serverPort();
    }

    public String getIp() {
        return this.settings.general().serverIp();
    }

    public UUID getServerUniqueId() {
        return this.serverID;
    }

    public boolean getAutoSave() {
        return this.settings.world().autoSave().enable();
    }

    public void setAutoSave(boolean autoSave) {
        this.settings.world().autoSave().enable(autoSave);
        for (Level level : this.levelArray) {
            level.setAutoSave(autoSave);
        }
    }

    public int getDefaultGamemode() {
        return this.settings.player().defaultGamemode();
    }

    public boolean getForceGamemode() {
        return this.settings.player().forceGamemode();
    }

    public static String getGamemodeString(int mode) {
        return getGamemodeString(mode, false);
    }

    public static String getGamemodeString(int mode, boolean direct) {
        return switch (mode) {
            case Player.SURVIVAL -> direct ? "Survival" : "%gameMode.survival";
            case Player.CREATIVE -> direct ? "Creative" : "%gameMode.creative";
            case Player.ADVENTURE -> direct ? "Adventure" : "%gameMode.adventure";
            case Player.SPECTATOR -> direct ? "Spectator" : "%gameMode.spectator";
            default -> "UNKNOWN";
        };
    }

    public static int getGamemodeFromString(String str) {
        return switch (str.trim().toLowerCase(Locale.ROOT)) {
            case "0", "survival", "s" -> Player.SURVIVAL;
            case "1", "creative", "c" -> Player.CREATIVE;
            case "2", "adventure", "a" -> Player.ADVENTURE;
            case "3", "spectator", "spc", "view", "v" -> Player.SPECTATOR;
            default -> -1;
        };
    }

    public MainLogger getLogger() {
        return MainLogger.getLogger();
    }

    public EntityMetadataStore getEntityMetadata() {
        return entityMetadata;
    }

    public PlayerMetadataStore getPlayerMetadata() {
        return playerMetadata;
    }

    public LevelMetadataStore getLevelMetadata() {
        return levelMetadata;
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public CraftingManager getCraftingManager() {
        return craftingManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    public ServerScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Get current tick
     *
     * @return current tick
     */
    public int getTick() {
        return tickCounter;
    }

    /**
     * Get ticks per second
     *
     * @return TPS
     */
    public float getTicksPerSecond() {
        return ((float) Math.round(this.maxTick * 100)) / 100;
    }

    /**
     * Get average ticks per second
     *
     * @return average TPS
     */
    public float getTicksPerSecondAverage() {
        float sum = 0;
        int count = this.tickAverage.length;
        for (float aTickAverage : this.tickAverage) {
            sum += aTickAverage;
        }
        return (float) NukkitMath.round(sum / count, 2);
    }

    public float getTickUsage() {
        return (float) NukkitMath.round(this.maxUse * 100, 2);
    }

    public float getTickUsageAverage() {
        float sum = 0;
        for (float aUseAverage : this.useAverage) {
            sum += aUseAverage;
        }
        return ((float) Math.round(sum / this.useAverage.length * 100)) / 100;
    }

    public SimpleCommandMap getCommandMap() {
        return commandMap;
    }

    public Map<UUID, Player> getOnlinePlayers() {
        return ImmutableMap.copyOf(playerList);
    }

    public int getOnlinePlayersCount() {
        return this.playerList.size();
    }

    public void addRecipe(Recipe recipe) {
        this.craftingManager.registerRecipe(recipe);
    }

    public Optional<Player> getPlayer(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        return Optional.ofNullable(playerList.get(uuid));
    }

    public Optional<UUID> lookupName(String name) {
        byte[] nameBytes = name.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        byte[] uuidBytes = nameLookup.get(nameBytes);
        if (uuidBytes == null) {
            return Optional.empty();
        }

        if (uuidBytes.length != 16) {
            log.warn("Invalid uuid in name lookup database detected! Removing...");
            nameLookup.delete(nameBytes);
            return Optional.empty();
        }

        ByteBuffer buffer = ByteBuffer.wrap(uuidBytes);
        return Optional.of(new UUID(buffer.getLong(), buffer.getLong()));
    }

    void updateName(UUID uuid, String name) {
        byte[] nameBytes = name.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        nameLookup.put(nameBytes, buffer.array());
    }

    public IPlayer getOfflinePlayer(final String name) {
        IPlayer result = this.getPlayerExact(name.toLowerCase(Locale.ROOT));
        if (result != null) {
            return result;
        }

        return lookupName(name).map(uuid -> new OfflinePlayer(this, uuid, name))
                .orElse(new OfflinePlayer(this, name));
    }

    public IPlayer getOfflinePlayer(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        Optional<Player> onlinePlayer = getPlayer(uuid);
        if (onlinePlayer.isPresent()) {
            return onlinePlayer.get();
        }

        return new OfflinePlayer(this, uuid);
    }

    public CompoundTag getOfflinePlayerData(UUID uuid) {
        return getOfflinePlayerData(uuid, false);
    }

    public CompoundTag getOfflinePlayerData(UUID uuid, boolean create) {
        return getOfflinePlayerDataInternal(uuid.toString(), true, create);
    }

    public CompoundTag getOfflinePlayerData(String name) {
        return getOfflinePlayerData(name, false);
    }

    public CompoundTag getOfflinePlayerData(String name, boolean create) {
        if (this.settings.player().savePlayerDataByUuid()) {
            Optional<UUID> uuid = lookupName(name);
            return getOfflinePlayerDataInternal(uuid.map(UUID::toString).orElse(name), true, create);
        } else {
            return getOfflinePlayerDataInternal(name.toLowerCase(Locale.ROOT), true, create);
        }
    }

    private CompoundTag getOfflinePlayerDataInternal(String name, boolean runEvent, boolean create) {
        Preconditions.checkNotNull(name, "name");

        PlayerDataSerializeEvent event = new PlayerDataSerializeEvent(name, playerDataSerializer);
        if (runEvent) {
            pluginManager.callEvent(event);
        }

        Optional<InputStream> dataStream = Optional.empty();
        try {
            dataStream = event.getSerializer().read(name, event.getUuid().orElse(null));
            if (dataStream.isPresent()) {
                return NBTIO.readCompressed(dataStream.get());
            }
        } catch (IOException e) {
            log.warn(this.getLanguage().translateString("nukkit.data.playerCorrupted", name));
            log.throwing(e);
            create = true;
        } finally {
            if (dataStream.isPresent()) {
                try {
                    dataStream.get().close();
                } catch (IOException e) {
                    log.throwing(e);
                }
            }
        }
        CompoundTag nbt = null;
        if (create) {
            Position spawn = this.getDefaultLevel().getSafeSpawn();
            long time = System.currentTimeMillis();
            nbt = new CompoundTag()
                    .putLong("firstPlayed", time / 1000)
                    .putLong("lastPlayed", time / 1000)
                    .putList(new ListTag<DoubleTag>("Pos")
                            .add(new DoubleTag("0", spawn.x))
                            .add(new DoubleTag("1", spawn.y))
                            .add(new DoubleTag("2", spawn.z)))
                    .putString("Level", this.getDefaultLevel().getName())
                    .putList(new ListTag<>("Inventory"))
                    .putInt("playerGameType", this.settings.player().defaultGamemode())
                    .putList(new ListTag<DoubleTag>("Motion")
                            .add(new DoubleTag("0", 0))
                            .add(new DoubleTag("1", 0))
                            .add(new DoubleTag("2", 0)))
                    .putList(new ListTag<FloatTag>("Rotation")
                            .add(new FloatTag("0", 0))
                            .add(new FloatTag("1", 0)))
                    .putFloat("FallDistance", 0)
                    .putShort("Fire", 0)
                    .putShort("Air", 300)
                    .putBoolean("OnGround", true)
                    .putBoolean("Invulnerable", false);

            this.saveOfflinePlayerData(name, nbt, true, runEvent);
        }
        return nbt;
    }

    public void saveOfflinePlayerData(UUID uuid, CompoundTag tag) {
        this.saveOfflinePlayerData(uuid, tag, false);
    }

    public void saveOfflinePlayerData(String name, CompoundTag tag) {
        this.saveOfflinePlayerData(name, tag, false);
    }

    public void saveOfflinePlayerData(UUID uuid, CompoundTag tag, boolean async) {
        this.saveOfflinePlayerData(uuid.toString(), tag, async);
    }

    public void saveOfflinePlayerData(String name, CompoundTag tag, boolean async) {
        if (this.settings.player().savePlayerDataByUuid()) {
            Optional<UUID> uuid = lookupName(name);
            saveOfflinePlayerData(uuid.map(UUID::toString).orElse(name), tag, async, true);
        } else {
            saveOfflinePlayerData(name, tag, async, true);
        }
    }

    private void saveOfflinePlayerData(String name, CompoundTag tag, boolean async, boolean runEvent) {
        String nameLower = name.toLowerCase(Locale.ROOT);
        if (this.settings.player().savePlayerData()) {
            PlayerDataSerializeEvent event = new PlayerDataSerializeEvent(nameLower, playerDataSerializer);
            if (runEvent) {
                pluginManager.callEvent(event);
            }

            if (async) {
                this.getScheduler().scheduleTask(InternalPlugin.INSTANCE, new Task() {
                    boolean hasRun = false;

                    @Override
                    public void onRun(int currentTick) {
                        this.onCancel();
                    }

                    // Doing it like this ensures that the player data will be saved in a server shutdown
                    @Override
                    public void onCancel() {
                        if (!this.hasRun) {
                            this.hasRun = true;
                            saveOfflinePlayerDataInternal(event.getSerializer(), tag, nameLower, event.getUuid().orElse(null));
                        }
                    }
                }, true);
            } else {
                saveOfflinePlayerDataInternal(event.getSerializer(), tag, nameLower, event.getUuid().orElse(null));
            }
        }
    }

    /**
     * Internal: Save offline player data
     *
     * @param serializer serializer
     * @param tag        compound tag
     * @param name       player name
     * @param uuid       player uuid
     */
    private void saveOfflinePlayerDataInternal(PlayerDataSerializer serializer, CompoundTag tag, String name, UUID uuid) {
        try (OutputStream dataStream = serializer.write(name, uuid)) {
            NBTIO.writeGZIPCompressed(tag, dataStream, ByteOrder.BIG_ENDIAN);
        } catch (Exception e) {
            log.error(this.getLanguage().translateString("nukkit.data.saveError", name, e));
        }
    }

    /**
     * Internal: Convert legacy player saves to the uuid based saving
     */
    private void convertLegacyPlayerData() {
        File dataDirectory = new File(getDataPath(), "players/");

        File[] files = dataDirectory.listFiles(file -> {
            String name = file.getName();
            return !uuidPattern.matcher(name).matches() && name.endsWith(".dat");
        });

        if (files == null) {
            return;
        }

        for (File legacyData : files) {
            String name = legacyData.getName();
            // Remove file extension
            name = name.substring(0, name.length() - 4);

            log.debug("Attempting legacy player data conversion for {}", name);

            CompoundTag tag = getOfflinePlayerDataInternal(name, false, false);

            if (tag == null || !tag.contains("UUIDLeast") || !tag.contains("UUIDMost")) {
                // No UUID so we cannot convert. Wait until player logs in.
                continue;
            }

            UUID uuid = new UUID(tag.getLong("UUIDMost"), tag.getLong("UUIDLeast"));
            if (!tag.contains("NameTag")) {
                tag.putString("NameTag", name);
            }

            if (new File(getDataPath() + "players/" + uuid.toString() + ".dat").exists()) {
                // We don't want to overwrite existing data.
                continue;
            }

            saveOfflinePlayerData(uuid.toString(), tag, false, false);

            // Add name to lookup table
            updateName(uuid, name);

            // Delete legacy data
            if (!legacyData.delete()) {
                log.warn("Unable to delete legacy data for {}", name);
            }
        }
    }

    /**
     * Get an online player by name
     *
     * @param name player name
     * @return Player or null
     */
    public Player getPlayer(String name) {
        Player found = null;
        name = name.toLowerCase(Locale.ROOT);
        int delta = Integer.MAX_VALUE;
        for (Player player : this.getOnlinePlayers().values()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(name)) {
                int curDelta = player.getName().length() - name.length();
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) {
                    break;
                }
            }
        }

        return found;
    }

    /**
     * Get an online player by exact player name
     *
     * @param name exact player name
     * @return Player or null
     */
    public Player getPlayerExact(String name) {
        for (Player player : this.getOnlinePlayers().values()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }

        return null;
    }

    /**
     * Get players that match with the name
     *
     * @param partialName name
     * @return matching players
     */
    public Player[] matchPlayer(String partialName) {
        partialName = partialName.toLowerCase(Locale.ROOT);
        List<Player> matchedPlayer = new ArrayList<>();
        for (Player player : this.getOnlinePlayers().values()) {
            if (player.getName().toLowerCase(Locale.ROOT).equals(partialName)) {
                return new Player[]{player};
            } else if (player.getName().toLowerCase(Locale.ROOT).contains(partialName)) {
                matchedPlayer.add(player);
            }
        }

        return matchedPlayer.toArray(Player.EMPTY_ARRAY);
    }

    /**
     * Internal: Remove a player from the server
     *
     * @param player player
     */
    public void removePlayer(Player player) {
        if (this.players.remove(player.getRawSocketAddress()) != null) {
            return;
        }

        for (InetSocketAddress socketAddress : new ArrayList<>(this.players.keySet())) {
            if (player == this.players.get(socketAddress)) {
                this.players.remove(socketAddress);
                break;
            }
        }
    }

    /**
     * Get all levels
     *
     * @return levels
     */
    public Map<Integer, Level> getLevels() {
        return levels;
    }

    /**
     * Get default level
     *
     * @return default level
     */
    public Level getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * Change the default level
     *
     * @param defaultLevel new default level
     */
    public void setDefaultLevel(Level defaultLevel) {
        if (defaultLevel == null || (this.isLevelLoaded(defaultLevel.getFolderName()) && defaultLevel != this.defaultLevel)) {
            this.defaultLevel = defaultLevel;
        }
    }

    /**
     * Check whether a level is loaded
     *
     * @param name level name
     * @return is loaded
     */
    public boolean isLevelLoaded(String name) {
        return this.getLevelByName(name) != null;
    }

    /**
     * Get a level by ID
     *
     * @param levelId level ID
     * @return Level or null
     */
    public Level getLevel(int levelId) {
        if (this.levels.containsKey(levelId)) {
            return this.levels.get(levelId);
        }
        return null;
    }

    /**
     * Get a level by name
     *
     * @param name level name
     * @return Level or null
     */
    public Level getLevelByName(String name) {
        for (Level level : this.levelArray) {
            if (level.getFolderName().equalsIgnoreCase(name)) {
                return level;
            }
        }

        return null;
    }

    /**
     * Unload a level
     * <p>
     * Notice: the default level cannot be unloaded without forceUnload=true
     *
     * @param level Level
     * @return unloaded
     */
    public boolean unloadLevel(Level level) {
        return this.unloadLevel(level, false);
    }

    /**
     * Unload a level
     * <p>
     * Notice: the default level cannot be unloaded without forceUnload=true
     *
     * @param level       Level
     * @param forceUnload force unload (ignore cancelled events and default level)
     * @return unloaded
     */
    public boolean unloadLevel(Level level, boolean forceUnload) {
        if (level == this.defaultLevel && !forceUnload) {
            throw new IllegalStateException("The default level cannot be unloaded while running, please switch levels.");
        }

        return level.unload(forceUnload);
    }

    /**
     * Load a level by name
     *
     * @param name level name
     * @return loaded
     */
    public boolean loadLevel(String name) {
        if (Objects.equals(name.trim(), "")) {
            throw new LevelException("Invalid empty level name");
        }

        if (this.isLevelLoaded(name)) {
            return true;
        } else if (!this.isLevelGenerated(name)) {
            log.warn(this.baseLang.translateString("nukkit.level.notFound", name));
            return false;
        }

        String path;

        if (name.contains("/") || name.contains("\\")) {
            path = name;
        } else {
            path = this.dataPath + "worlds/" + name + '/';
        }

        Class<? extends LevelProvider> provider = LevelProviderManager.getProvider(path);

        if (provider == null) {
            log.error(this.baseLang.translateString("nukkit.level.loadError", new String[]{name, "Unknown provider"}));
            return false;
        }

        Level level;
        try {
            level = new Level(this, name, path, provider);
        } catch (Exception e) {
            log.error(this.baseLang.translateString("nukkit.level.loadError", new String[]{name, e.getMessage()}));
            return false;
        }

        this.levels.put(level.getId(), level);

        level.initLevel();
        level.setTickRate(this.settings.performance().baseTickRate());

        this.pluginManager.callEvent(new LevelLoadEvent(level));
        return true;
    }

    /**
     * Generate a new level
     *
     * @param name level name
     * @return generated
     */
    public boolean generateLevel(String name) {
        return this.generateLevel(name, Utils.random.nextLong());
    }

    /**
     * Generate a new level
     *
     * @param name level name
     * @param seed level seed
     * @return generated
     */
    public boolean generateLevel(String name, long seed) {
        return this.generateLevel(name, seed, null);
    }

    /**
     * Generate a new level
     *
     * @param name      level name
     * @param seed      level seed
     * @param generator level generator
     * @return generated
     */
    public boolean generateLevel(String name, long seed, Class<? extends Generator> generator) {
        return this.generateLevel(name, seed, generator, new HashMap<>());
    }

    /**
     * Generate a new level
     *
     * @param name      level name
     * @param seed      level seed
     * @param generator level generator
     * @param options   level generator options
     * @return generated
     */
    public boolean generateLevel(String name, long seed, Class<? extends Generator> generator, Map<String, Object> options) {
        return generateLevel(name, seed, generator, options, null);
    }

    /**
     * Generate a new level
     *
     * @param name      level name
     * @param seed      level seed
     * @param generator level generator
     * @param options   level generator options
     * @param provider  level provider
     * @return generated
     */
    public boolean generateLevel(String name, long seed, Class<? extends Generator> generator, Map<String, Object> options, Class<? extends LevelProvider> provider) {
        if (Objects.equals(name.trim(), "") || this.isLevelGenerated(name)) {
            return false;
        }

        if (!options.containsKey("preset")) {
            options.put("preset", this.settings.world().generatorSettings());
        }

        if (generator == null) {
            generator = Generator.getGenerator(this.settings.world().defaultWorldType());
        }

        if (provider == null) {
            provider = LevelProviderManager.getProviderByName("leveldb");
        }

        String path;

        if (name.contains("/") || name.contains("\\")) {
            path = name;
        } else {
            path = this.dataPath + "worlds/" + name + '/';
        }

        Level level;
        try {
            provider.getMethod("generate", String.class, String.class, long.class, Class.class, Map.class).invoke(null, path, name, seed, generator, options);

            level = new Level(this, name, path, provider);
            this.levels.put(level.getId(), level);

            level.initLevel();
            level.setTickRate(this.settings.performance().baseTickRate());
        } catch (Exception e) {
            log.error(this.baseLang.translateString("nukkit.level.generationError", new String[]{name, Utils.getExceptionMessage(e)}));
            return false;
        }

        this.pluginManager.callEvent(new LevelInitEvent(level));
        this.pluginManager.callEvent(new LevelLoadEvent(level));
        return true;
    }

    /**
     * Check whether a level by name is generated
     *
     * @param name level name
     * @return level found
     */
    public boolean isLevelGenerated(String name) {
        if (Objects.equals(name.trim(), "")) {
            return false;
        }

        if (this.getLevelByName(name) == null) {
            String path;

            if (name.contains("/") || name.contains("\\")) {
                path = name;
            } else {
                path = this.dataPath + "worlds/" + name + '/';
            }

            return LevelProviderManager.getProvider(path) != null;
        }

        return true;
    }

    /**
     * Get BaseLang (server's default language)
     *
     * @return BaseLang
     */
    public BaseLang getLanguage() {
        return baseLang;
    }

    /**
     * Is forcing language enabled
     *
     * @return force-language enabled
     */
    public boolean isLanguageForced() {
        return this.settings.general().forceLanguage();
    }

    /**
     * Get Network
     *
     * @return Network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Get plugin commands
     *
     * @param name command name
     * @return PluginIdentifiableCommand or null
     */
    public PluginIdentifiableCommand getPluginCommand(String name) {
        Command command = this.commandMap.getCommand(name);
        if (command instanceof PluginIdentifiableCommand) {
            return (PluginIdentifiableCommand) command;
        } else {
            return null;
        }
    }

    /**
     * Get list of banned players
     *
     * @return ban list
     */
    public BanList getNameBans() {
        return this.banByName;
    }

    /**
     * Get list of IP bans
     *
     * @return IP bans
     */
    public BanList getIPBans() {
        return this.banByIP;
    }

    /**
     * Give player the operator status
     *
     * @param name player name
     */
    public void addOp(String name) {
        this.operators.set(name.toLowerCase(Locale.ROOT), true);
        Player player = this.getPlayerExact(name);
        if (player != null) {
            player.recalculatePermissions();
        }
        this.operators.save(true);
    }

    /**
     * Remove player's operator status
     *
     * @param name player name
     */
    public void removeOp(String name) {
        this.operators.remove(name.toLowerCase(Locale.ROOT));
        Player player = this.getPlayerExact(name);
        if (player != null) {
            player.recalculatePermissions();
        }
        this.operators.save();
    }

    /**
     * Add a player to whitelist
     *
     * @param name player name
     */
    public void addWhitelist(String name) {
        this.whitelist.set(name.toLowerCase(Locale.ROOT), true);
        this.whitelist.save(true);
    }

    /**
     * Remove a player from whitelist
     *
     * @param name player name
     */
    public void removeWhitelist(String name) {
        this.whitelist.remove(name.toLowerCase(Locale.ROOT));
        this.whitelist.save(true);
    }

    /**
     * Check whether a player is whitelisted
     *
     * @param name player name
     * @return is whitelisted or whitelist is not enabled
     */
    public boolean isWhitelisted(String name) {
        return !this.settings.player().whitelist() || this.operators.exists(name, true) || this.whitelist.exists(name, true);
    }

    /**
     * Check whether a player is an operator
     *
     * @param name player name
     * @return is operator
     */
    public boolean isOp(String name) {
        return name != null && this.operators.exists(name, true);
    }

    /**
     * Get whitelist config
     *
     * @return whitelist
     */
    public Config getWhitelist() {
        return whitelist;
    }

    /**
     * Get operator list config
     *
     * @return operators
     */
    public Config getOps() {
        return operators;
    }

    /**
     * Reload whitelist
     */
    public void reloadWhitelist() {
        this.whitelist.reload();
    }

    /**
     * Get service manager
     *
     * @return service manager
     */
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    /**
     * Get nether world for a level
     *
     * @param world level
     * @return nether world for that level
     */
    public Level getNetherWorld(String world) {
        return this.settings.world().multiNetherWorlds().contains(world) ? this.getLevelByName(world + "-nether") : this.getLevelByName("nether");
    }

    /**
     * Sort players by protocol version
     *
     * @param players players
     * @return players sorted by protocol
     */
    public static Int2ObjectMap<ObjectList<Player>> sortPlayers(Player[] players) {
        Int2ObjectMap<ObjectList<Player>> targets = new Int2ObjectOpenHashMap<>();
        for (Player player : players) {
            targets.computeIfAbsent(player.protocol, i -> new ObjectArrayList<>()).add(player);
        }
        return targets;
    }

    /**
     * Sort players by protocol version
     *
     * @param players players
     * @return players sorted by protocol
     */
    public static Int2ObjectMap<ObjectList<Player>> sortPlayers(Collection<Player> players) {
        Int2ObjectMap<ObjectList<Player>> targets = new Int2ObjectOpenHashMap<>();
        for (Player player : players) {
            targets.computeIfAbsent(player.protocol, i -> new ObjectArrayList<>()).add(player);
        }
        return targets;
    }

    /**
     * Checks the current thread against the expected primary thread for the server.
     *
     * <b>Note:</b> this method should not be used to indicate the current synchronized state of the runtime. A current thread matching the main thread indicates that it is synchronized, but a mismatch does not preclude the same assumption.
     *
     * @return true if the current thread matches the expected primary thread, false otherwise
     */
    public boolean isPrimaryThread() {
        return (Thread.currentThread() == currentThread);
    }

    /**
     * Get server's primary thread
     *
     * @return primary thread
     */
    public Thread getPrimaryThread() {
        return currentThread;
    }

    private void registerProfessions() {
        Profession.init();
    }

    /**
     * Get player data serializer that is used to save player data
     *
     * @return player data serializer
     */
    public PlayerDataSerializer getPlayerDataSerializer() {
        return playerDataSerializer;
    }

    /**
     * Set player data serializer that is used to save player data
     *
     * @param playerDataSerializer player data serializer
     */
    public void setPlayerDataSerializer(PlayerDataSerializer playerDataSerializer) {
        this.playerDataSerializer = Preconditions.checkNotNull(playerDataSerializer, "playerDataSerializer");
    }

    public TickingAreaManager getTickingAreaManager() {
        return tickingAreaManager;
    }

    /**
     * Get the Server instance
     *
     * @return Server
     */
    public static Server getInstance() {
        return instance;
    }

    /**
     * Get the mob spawner task
     *
     * @return spawner task
     */
    public SpawnerTask getSpawnerTask() {
        return this.spawnerTask;
    }

    /**
     * Internal: Warn user about non multiversion compatible plugins.
     */
    public static void mvw(String action) {
        if (getInstance().getSettings().general().multiversion().minProtocol() != ProtocolInfo.CURRENT_PROTOCOL) {
            if (Nukkit.DEBUG > 1) {
                getInstance().getLogger().logException(new PluginException("Default " + action + " used by a plugin. This can cause instability with the multiversion."));
            } else {
                getInstance().getLogger().warning("Default " + action + " used by a plugin. This can cause instability with the multiversion.");
            }
        }
    }

    private class ConsoleThread extends Thread implements InterruptibleThread {
        @Override
        public void run() {
            console.start();
        }
    }

    private static class ComputeThread extends ForkJoinWorkerThread {
        ComputeThread(final ForkJoinPool pool, final AtomicInteger threadCount) {
            super(pool);
            setName("ComputeThreadPool-thread-" + threadCount.getAndIncrement());
        }
    }

    private static class ComputeThreadPoolThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AtomicInteger threadCount = new AtomicInteger(0);

        @SuppressWarnings("removal")
        private static final AccessControlContext ACC = contextWithPermissions(
                new RuntimePermission("getClassLoader"),
                new RuntimePermission("setContextClassLoader")
        );

        @SuppressWarnings("removal")
        static AccessControlContext contextWithPermissions(final Permission... perms) {
            final Permissions permissions = new Permissions();
            for (final Permission perm : perms) {
                permissions.add(perm);
            }
            return new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, permissions)});
        }

        @Override
        @SuppressWarnings("removal")
        public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
            return AccessController.doPrivileged((PrivilegedAction<ForkJoinWorkerThread>) () -> new ComputeThread(pool, threadCount), ACC);
        }
    }
}