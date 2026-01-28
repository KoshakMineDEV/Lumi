package cn.nukkit.settings;

import cn.nukkit.Difficulty;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(fluent = true)
public class WorldSettings extends OkaeriConfig {

    @CustomKey("default-world-name")
    private String defaultWorldName = "world";

    @CustomKey("default-world-seed")
    private String defaultWorldSeed = "";

    @CustomKey("default-world-type")
    private String defaultWorldType = "default";

    @CustomKey("generator-settings")
    @Comment("Settings for selected world generator")
    private String generatorSettings = "";

    @CustomKey("load-all-worlds")
    private boolean loadAllWorlds;

    @CustomKey("view-distance")
    private int viewDistance = 8;

    @CustomKey("spawn-protection")
    private int spawnProtection = 10;

    private Difficulty difficulty = Difficulty.NORMAL;

    @CustomKey("allow-pvp")
    private boolean allowPvp = true;

    @CustomKey("enable-hardcore")
    private boolean enableHardcore = false;

    @CustomKey("enable-nether")
    private boolean enableNether = true;

    @CustomKey("enable-end")
    private boolean enableEnd = true;

    @CustomKey("drop-spawners")
    private boolean dropSpawners = false;

    @CustomKey("explosion-break-blocks")
    private boolean explosionBreakBlocks;

    @CustomKey("multi-nether-worlds")
    @Comment("Ability to have separate nether world for every other overworld")
    private List<String> multiNetherWorlds = new ArrayList<>();

    @CustomKey("portal-ticks")
    @Comment("Time in ticks before player will be teleported to another dimension in portal")
    private int portalTicks = 80;

    @CustomKey("do-not-tick-worlds")
    private List<String> doNotTickWorlds = new ArrayList<>();

    @CustomKey("do-world-gc")
    private boolean doWorldGc = true;

    @CustomKey("world-auto-compaction")
    private boolean worldAutoCompaction = true;

    @CustomKey("world-auto-compaction-ticks")
    private int worldAutoCompactionTicks = 36000;

    @CustomKey("light-updates")
    private boolean lightUpdates = true;

    @CustomKey("clear-chunk-tick-list")
    private boolean clearChunkTickList = true;

    @CustomKey("leveldb-cache-mb")
    private int leveldbCacheMb = 80;

    @Setter(value = AccessLevel.NONE)
    @CustomKey("entity")
    @Comment("World entities settings")
    private EntitySettings entity = new EntitySettings();

    @Setter(value = AccessLevel.NONE)
    @CustomKey("chunk")
    @Comment("World chunks settings")
    private ChunkSettings chunk = new ChunkSettings();

    @Setter(value = AccessLevel.NONE)
    @CustomKey("auto-save")
    @Comment("World auto save settings")
    private AutoSaveSettings autoSave = new AutoSaveSettings();

    @CustomKey("anti-xray")
    @Comment("AntiXray settings")
    @Comment("Example:")
    @Comment("anti-xray:")
    @Comment("  world:")
    @Comment("      mode: LOW")
    @Comment("      pre-deobfuscate: false")
    @Setter(value = AccessLevel.NONE)
    private Map<String, AntiXraySettings> antiXray = new HashMap<>();

    @Getter
    @Setter
    public static class EntitySettings extends OkaeriConfig {

        @CustomKey("spawn-animals")
        private boolean spawnAnimals = true;

        @CustomKey("spawn-mobs")
        private boolean spawnMobs = true;

        @CustomKey("entity-auto-spawn-task")
        private boolean entityAutoSpawnTask = true;

        @CustomKey("entity-despawn-task")
        private boolean entityDespawnTask = true;

        @CustomKey("ticks-per-entity-spawns")
        private int ticksPerEntitySpawns = 200;

        @CustomKey("ticks-per-entity-despawns")
        private int ticksPerEntityDespawns = 12000;

        @CustomKey("mob-ai")
        private boolean mobAi = true;

        @CustomKey("worlds-entity-spawning-disabled")
        @Comment("List of worlds where entity shouldn't spawn")
        private List<String> worldsEntitySpawningDisabled = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ChunkSettings extends OkaeriConfig {

        @CustomKey("cache-chunks")
        private boolean cacheChunks = false;

        @CustomKey("async-chunks")
        private boolean asyncChunks = true;

        @CustomKey("compression-level")
        private int compressionLevel;

        @CustomKey("spawn-chunks-threshold")
        private int spawnChunksThreshold = 56;

        @CustomKey("sending-per-tick")
        private int sendingPerTick = 4;

        @CustomKey("ticking-per-tick")
        private int tickingPerTick = 40;

        @CustomKey("ticking-radius")
        private int tickingRadius = 3;

        @CustomKey("generation-queue-size")
        private int generationQueueSize = 8;

        @CustomKey("generation-population-queue-size")
        private int generationPopulationQueueSize = 8;
    }

    @Getter
    @Setter
    public static class AutoSaveSettings extends OkaeriConfig {

        @CustomKey("enable")
        private boolean enable = true;

        @CustomKey("per-ticks")
        @Comment("Save worlds per ticks")
        private int perTicks = 6000;

        @CustomKey("disabled-worlds")
        private List<String> disabledWorlds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class AntiXraySettings extends OkaeriConfig {

        private AntiXrayMode mode = AntiXrayMode.LOW;

        @CustomKey("pre-deobfuscate")
        private boolean preDeobfuscate;

        public enum AntiXrayMode {
            LOW,
            MEDIUM,
            HIGH
        }
    }
}
