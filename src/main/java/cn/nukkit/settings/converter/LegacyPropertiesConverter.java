package cn.nukkit.settings.converter;

import cn.nukkit.Difficulty;
import cn.nukkit.settings.GeneralSettings;
import cn.nukkit.settings.PlayerSettings;
import cn.nukkit.settings.ServerSettings;
import cn.nukkit.settings.WorldSettings;
import cn.nukkit.settings.WorldSettings.AntiXraySettings.AntiXrayMode;
import cn.nukkit.utils.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static cn.nukkit.settings.GeneralSettings.*;

@Slf4j
public class LegacyPropertiesConverter {

    private final ServerSettings settings;
    private final File file;
    private Config config;

    public LegacyPropertiesConverter(ServerSettings settings) {
        this.settings = settings;
        this.file = new File("server.properties");
        if (!file.exists()) {
            return;
        }
        this.config = new Config(file, Config.PROPERTIES);
    }

    public void convert() {
        if (config == null) return;

        log.info("Converting the old server.properties configuration to the new format...");

        // General Settings
        var general = settings.general();
        general.motd(this.getPropertyString("motd", general.motd()));
        general.subMotd(this.getPropertyString("sub-motd", general.subMotd()));
        general.serverPort(this.getPropertyInt("server-port", general.serverPort()));
        general.serverIp(this.getPropertyString("server-ip", general.serverIp()));
        general.maxPlayers(this.getPropertyInt("max-players", general.maxPlayers()));
        general.language(this.getPropertyString("language", general.language()));
        general.forceLanguage(this.getPropertyBoolean("force-language", general.forceLanguage()));
        general.forceResources(this.getPropertyBoolean("force-resources", general.forceResources()));
        general.forceResourcesAllowClientPacks(this.getPropertyBoolean("force-resources-allow-client-packs", general.forceResourcesAllowClientPacks()));
        general.shutdownMessage(this.getPropertyString("shutdown-message", general.shutdownMessage()));
        general.bstatsMetrics(this.getPropertyBoolean("bstats-metrics", general.bstatsMetrics()));
        general.debugLevel(this.getPropertyInt("debug-level", general.debugLevel()));
        general.enableSpark(this.getPropertyBoolean("enable-spark", general.enableSpark()));
        general.useWaterdog(this.getPropertyBoolean("use-waterdog", general.useWaterdog()));
        general.serverAuthoritativeMovement(switch (this.getPropertyString("server-authoritative-movement", "server-auth")) {
            case "client-auth" -> ServerAuthoritativeMovement.CLIENT_AUTH;
            case "server-auth-with-rewind" -> ServerAuthoritativeMovement.SERVER_AUTH_WITH_REWIND;
            default -> ServerAuthoritativeMovement.SERVER_AUTH;
        });
        general.serverAuthoritativeBlockBreaking(this.getPropertyBoolean("server-authoritative-block-breaking", general.serverAuthoritativeBlockBreaking()));

        // Multiversion settings
        general.multiversion().minProtocol(this.getPropertyInt("multiversion-min-protocol", general.multiversion().minProtocol()));
        general.multiversion().maxProtocol(this.getPropertyInt("multiversion-max-protocol", general.multiversion().maxProtocol()));

        // Network Settings
        var network = settings.network();
        network.xboxAuth(this.getPropertyBoolean("xbox-auth", network.xboxAuth()));
        network.xboxAuthTempIpBan(this.getPropertyBoolean("temp-ip-ban-failed-xbox-auth", network.xboxAuthTempIpBan()));
        network.strongIpBans(this.getPropertyBoolean("strong-ip-bans", network.strongIpBans()));
        network.enableRakSendCookie(this.getPropertyBoolean("enable-rak-send-cookie", network.enableRakSendCookie()));
        network.rakPacketLimit(this.getPropertyInt("rak-packet-limit", network.rakPacketLimit()));
        network.timeoutMilliseconds(this.getPropertyInt("timeout-milliseconds", network.timeoutMilliseconds()));
        network.enableQuery(this.getPropertyBoolean("enable-query", network.enableQuery()));
        network.queryPlugins(this.getPropertyBoolean("query-plugins", network.queryPlugins()));
        network.encryption(this.getPropertyBoolean("encryption", network.encryption()));

        // RCON Settings
        network.rcon().enable(this.getPropertyBoolean("enable-rcon", network.rcon().enable()));
        network.rcon().password(this.getPropertyString("rcon.password", network.rcon().password()));
        network.rcon().port(this.getPropertyInt("rcon.port", network.rcon().port()));

        // Compression Settings
        network.compression().zlibProvider(this.getPropertyInt("zlib-provider", network.compression().zlibProvider()));
        network.compression().compressionLevel(this.getPropertyInt("compression-level", network.compression().compressionLevel()));
        network.compression().compressionThreshold(this.getPropertyInt("compression-threshold", network.compression().compressionThreshold()));
        network.compression().useSnappyCompression(this.getPropertyBoolean("use-snappy-compression", network.compression().useSnappyCompression()));

        // World Settings
        var world = settings.world();
        world.defaultWorldName(this.getPropertyString("level-name", world.defaultWorldName()));
        world.defaultWorldSeed(this.getPropertyString("level-seed", world.defaultWorldSeed()));
        world.defaultWorldType(this.getPropertyString("level-type", world.defaultWorldType()));
        world.generatorSettings(this.getPropertyString("generator-settings", world.generatorSettings()));
        world.loadAllWorlds(this.getPropertyBoolean("load-all-worlds", world.loadAllWorlds()));
        world.viewDistance(this.getPropertyInt("view-distance", world.viewDistance()));
        world.spawnProtection(this.getPropertyInt("spawn-protection", world.spawnProtection()));
        world.difficulty(Difficulty.byName(
                this.getPropertyString("difficulty", world.difficulty().getLowerName())
        ));
        world.allowPvp(this.getPropertyBoolean("pvp", world.allowPvp()));
        world.enableHardcore(this.getPropertyBoolean("hardcore", world.enableHardcore()));
        world.enableNether(this.getPropertyBoolean("nether", world.enableNether()));
        world.enableEnd(this.getPropertyBoolean("end", world.enableEnd()));
        world.dropSpawners(this.getPropertyBoolean("drop-spawners", world.dropSpawners()));
        world.explosionBreakBlocks(this.getPropertyBoolean("explosion-break-blocks", world.explosionBreakBlocks()));
        world.portalTicks(this.getPropertyInt("portal-ticks", world.portalTicks()));
        world.worldAutoCompaction(this.getPropertyBoolean("level-auto-compaction", world.worldAutoCompaction()));
        world.worldAutoCompactionTicks(this.getPropertyInt("level-auto-compaction-ticks", world.worldAutoCompactionTicks()));
        world.lightUpdates(this.getPropertyBoolean("light-updates", world.lightUpdates()));
        world.clearChunkTickList(this.getPropertyBoolean("clear-chunk-tick-list", world.clearChunkTickList()));
        world.leveldbCacheMb(this.getPropertyInt("leveldb-cache-mb", world.leveldbCacheMb()));

        // Multi-nether worlds
        String multiNetherWorlds = this.getPropertyString("multi-nether-worlds", "");
        if (!multiNetherWorlds.isEmpty()) {
            world.multiNetherWorlds(Arrays.asList(multiNetherWorlds.split(",")));
        }

        // Do not tick worlds
        String doNotTickWorlds = this.getPropertyString("do-not-tick-worlds", "");
        if (!doNotTickWorlds.isEmpty()) {
            world.doNotTickWorlds(Arrays.asList(doNotTickWorlds.split(",")));
        }

        world.doWorldGc(this.getPropertyBoolean("do-level-gc", world.doWorldGc()));

        // Entity Settings
        world.entity().spawnAnimals(this.getPropertyBoolean("spawn-animals", world.entity().spawnAnimals()));
        world.entity().spawnMobs(this.getPropertyBoolean("spawn-mobs", world.entity().spawnMobs()));
        world.entity().entityAutoSpawnTask(this.getPropertyBoolean("entity-auto-spawn-task", world.entity().entityAutoSpawnTask()));
        world.entity().entityDespawnTask(this.getPropertyBoolean("entity-despawn-task", world.entity().entityDespawnTask()));
        world.entity().ticksPerEntitySpawns(this.getPropertyInt("ticks-per-entity-spawns", world.entity().ticksPerEntitySpawns()));
        world.entity().ticksPerEntityDespawns(this.getPropertyInt("ticks-per-entity-despawns", world.entity().ticksPerEntityDespawns()));
        world.entity().mobAi(this.getPropertyBoolean("mob-ai", world.entity().mobAi()));

        // Worlds with disabled entity spawning
        String worldsEntitySpawningDisabled = this.getPropertyString("worlds-entity-spawning-disabled", "");
        if (!worldsEntitySpawningDisabled.isEmpty()) {
            world.entity().worldsEntitySpawningDisabled(Arrays.asList(worldsEntitySpawningDisabled.split(",")));
        }

        // Chunk Settings
        world.chunk().cacheChunks(this.getPropertyBoolean("cache-chunks", world.chunk().cacheChunks()));
        world.chunk().asyncChunks(this.getPropertyBoolean("async-chunks", world.chunk().asyncChunks()));
        world.chunk().spawnChunksThreshold(this.getPropertyInt("spawn-threshold", world.chunk().spawnChunksThreshold()));
        world.chunk().compressionLevel(Math.max(Math.min(this.getPropertyInt("chunk-compression-level", 7), 9), 1));
        world.chunk().sendingPerTick(this.getPropertyInt("chunk-sending-per-tick", world.chunk().sendingPerTick()));
        world.chunk().tickingPerTick(this.getPropertyInt("chunk-ticking-per-tick", world.chunk().tickingPerTick()));
        world.chunk().tickingRadius(this.getPropertyInt("chunk-ticking-radius", world.chunk().tickingRadius()));
        world.chunk().generationQueueSize(this.getPropertyInt("chunk-generation-queue-size", world.chunk().generationQueueSize()));
        world.chunk().generationPopulationQueueSize(this.getPropertyInt("chunk-generation-population-queue-size", world.chunk().generationPopulationQueueSize()));

        // Auto-save Settings
        world.autoSave().enable(this.getPropertyBoolean("auto-save", world.autoSave().enable()));
        world.autoSave().perTicks(this.getPropertyInt("ticks-per-autosave", world.autoSave().perTicks()));
        String disabledWorlds = this.getPropertyString("worlds-level-auto-save-disabled", "");
        if (!disabledWorlds.isEmpty()) {
            world.autoSave().disabledWorlds(Arrays.asList(disabledWorlds.split(",")));
        }

        // Anti-Xray Settings
        String antiXrayWorlds = this.getPropertyString("anti-xray-worlds", "");
        if (!antiXrayWorlds.isEmpty()) {
            AntiXrayMode mode = AntiXrayMode.valueOf(this.getPropertyString("anti-xray-mode", "LOW"));
            boolean preDeobfuscate = this.getPropertyBoolean("anti-xray-pre-deobfuscate", false);

            Map<String, WorldSettings.AntiXraySettings> antiXrayMap = new HashMap<>();
            for (String worldName : antiXrayWorlds.split(",")) {
                WorldSettings.AntiXraySettings antiXraySettings = new WorldSettings.AntiXraySettings();
                antiXraySettings.mode(mode);
                antiXraySettings.preDeobfuscate(preDeobfuscate);
                antiXrayMap.put(worldName.trim(), antiXraySettings);
            }
            world.antiXray().putAll(antiXrayMap);
        }

        // Features Settings
        var features = settings.features();
        features.enableExperimentMode(this.getPropertyBoolean("enable-experiment-mode", features.enableExperimentMode()));
        features.vanillaPortals(this.getPropertyBoolean("vanilla-portals", features.vanillaPortals()));
        features.enableNewPaintings(this.getPropertyBoolean("enable-new-paintings", features.enableNewPaintings()));
        features.enableNewChickenEggsLaying(this.getPropertyBoolean("enable-new-chicken-eggs-laying", features.enableNewChickenEggsLaying()));

        // Player Settings
        var player = settings.player();
        player.defaultGamemode(this.getPropertyInt("gamemode", player.defaultGamemode()));
        player.forceGamemode(this.getPropertyBoolean("force-gamemode", player.forceGamemode()));
        player.whitelist(this.getPropertyBoolean("white-list", player.whitelist()));
        player.whitelistReason(this.getPropertyString("whitelist-reason", player.whitelistReason()));
        player.savePlayerData(this.getPropertyBoolean("save-player-data", player.savePlayerData()));
        player.savePlayerDataByUuid(this.getPropertyBoolean("save-player-data-by-uuid", player.savePlayerDataByUuid()));
        player.skinChangeCooldown(this.getPropertyInt("skin-change-cooldown", player.skinChangeCooldown()));
        player.doNotLimitSkinGeometry(this.getPropertyBoolean("do-not-limit-skin-geometry", player.doNotLimitSkinGeometry()));
        player.doNotLimitInteractions(this.getPropertyBoolean("do-not-limit-interactions", player.doNotLimitInteractions()));
        player.personaSkins(this.getPropertyBoolean("persona-skins", player.personaSkins()));
        player.checkOpMovement(this.getPropertyBoolean("check-op-movement", player.checkOpMovement()));
        player.checkOpMovement(this.getPropertyBoolean("allow-flight", player.allowFlight()));
        player.stopInGame(this.getPropertyBoolean("stop-in-game", player.stopInGame()));
        player.opInGame(this.getPropertyBoolean("op-in-game", player.opInGame()));
        player.forcedSafetyEnchant(this.getPropertyBoolean("forced-safety-enchant", player.forcedSafetyEnchant()));
        player.spaceNameMode(PlayerSettings.SpaceNameMode.valueOf(
                this.getPropertyString("space-name-mode", player.spaceNameMode().name()).toUpperCase()
        ));

        // Performance Settings
        var performance = settings.performance();
        performance.asyncWorkers(this.getPropertyString("async-workers", String.valueOf(performance.asyncWorkers())));
        performance.autoTickRate(this.getPropertyBoolean("auto-tick-rate", performance.autoTickRate()));
        performance.autoTickRateLimit(this.getPropertyInt("auto-tick-rate-limit", performance.autoTickRateLimit()));
        performance.baseTickRate(this.getPropertyInt("base-tick-rate", performance.baseTickRate()));
        performance.alwaysTickPlayers(this.getPropertyBoolean("always-tick-players", performance.alwaysTickPlayers()));
        performance.threadWatchdog(this.getPropertyBoolean("thread-watchdog", performance.threadWatchdog()));
        performance.threadWatchdogTick(this.getPropertyInt("thread-watchdog-tick", performance.threadWatchdogTick()));

        settings.save();

        if (file.delete()) {
            log.info("Server configuration successfully converted");
        } else {
            log.warn("Failed to delete the old configuration");
        }
    }

    public String getPropertyString(String key, String defaultValue) {
        return this.config.exists(key) ? this.config.getString(key) : defaultValue;
    }

    public int getPropertyInt(String variable, Integer defaultValue) {
        Object value = this.config.get(variable);
        if (value == null) {
            value = defaultValue;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        String trimmed = String.valueOf(value).trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(trimmed);
    }

    public boolean getPropertyBoolean(String variable, boolean defaultValue) {
        Object value = this.config.exists(variable) ? this.config.get(variable) : defaultValue;
        if (value instanceof Boolean bool) {
            return bool;
        }
        return switch (String.valueOf(value).trim().toLowerCase(Locale.ROOT)) {
            case "on", "true", "1", "yes" -> true;
            default -> false;
        };
    }
}
