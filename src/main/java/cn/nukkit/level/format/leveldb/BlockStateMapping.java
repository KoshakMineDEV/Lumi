package cn.nukkit.level.format.leveldb;

import cn.nukkit.block.Block;
import cn.nukkit.level.format.leveldb.structure.BlockStateSnapshot;
import cn.nukkit.level.format.leveldb.updater.BlockStateUpdaterVanilla;
import cn.nukkit.level.format.leveldb.updater.BlockStateUpdater_1_21_110;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.extern.log4j.Log4j2;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.cloudburstmc.blockstateupdater.*;
import org.cloudburstmc.blockstateupdater.util.tagupdater.CompoundTagUpdaterContext;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.common.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cn.nukkit.level.format.leveldb.LevelDBConstants.PALETTE_VERSION;

@Log4j2
public class BlockStateMapping {

    private static final BlockStateMapping INSTANCE = new BlockStateMapping(PALETTE_VERSION);
    private static final CompoundTagUpdaterContext CONTEXT;
    private static final int LATEST_UPDATER_VERSION;

    private final int version;

    private LegacyStateMapper legacyMapper;

    private int defaultHashId = -1;
    private BlockStateSnapshot defaultState;

    private static final ExpiringMap<NbtMap, NbtMap> BLOCK_UPDATE_CACHE = ExpiringMap.builder()
            .maxSize(1024)
            .expiration(60L, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .build();
    private final Int2ObjectMap<BlockStateSnapshot> hash2State = new Int2ObjectOpenHashMap<>();
    private final Object2ObjectMap<NbtMap, BlockStateSnapshot> paletteMap = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
        @Override
        public int hashCode(NbtMap nbtMap) {
            return nbtMap.hashCode();
        }

        @Override
        public boolean equals(NbtMap nbtMap, NbtMap nbtMap2) {
            return Objects.equals(nbtMap, nbtMap2);
        }
    });
    private final Object2ObjectMap<NbtMap, BlockStateSnapshot> customCacheMap = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
        @Override
        public int hashCode(NbtMap nbtMap) {
            return nbtMap.hashCode();
        }

        @Override
        public boolean equals(NbtMap nbtMap, NbtMap nbtMap2) {
            return Objects.equals(nbtMap, nbtMap2);
        }
    });

    static {
        INSTANCE.setLegacyMapper(new NukkitLegacyMapper());
        NukkitLegacyMapper.registerStates(INSTANCE);

        List<BlockStateUpdater> blockStateUpdaters = new ArrayList<>();
        blockStateUpdaters.add(BlockStateUpdaterBase.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_10_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_12_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_13_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_14_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_15_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_16_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_16_210.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_17_30.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_17_40.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_18_10.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_18_30.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_19_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_19_20.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_19_70.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_19_80.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_10.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_30.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_40.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_50.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_60.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_70.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_20_80.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_0.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_10.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_20.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_30.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_40.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_60.INSTANCE);
        blockStateUpdaters.add(BlockStateUpdater_1_21_110.INSTANCE);

        //blockStateUpdaters.add(BlockStateUpdater_1_26_50.INSTANCE);

        blockStateUpdaters.add(BlockStateUpdaterVanilla.INSTANCE);

        CompoundTagUpdaterContext context = new CompoundTagUpdaterContext();
        blockStateUpdaters.forEach(updater -> updater.registerUpdaters(context));
        CONTEXT = context;
        LATEST_UPDATER_VERSION = context.getLatestVersion();
    }

    public static BlockStateMapping get() {
        return INSTANCE;
    }

    public BlockStateMapping(int version) {
        this(version, null);
    }

    public BlockStateMapping(int version, LegacyStateMapper legacyStateMapper) {
        this.version = version;
        this.legacyMapper = legacyStateMapper;
    }

    public boolean containsState(NbtMap state) {
        return paletteMap.containsKey(state);
    }

    public void registerState(int hashId, NbtMap state) {
        BlockStateSnapshot hashState = this.hash2State.get(hashId);
        Preconditions.checkArgument(hashState == null || Objects.equals(hashState.getVanillaState(), state),
                "Block state hash collision for " + hashId + ": " +
                        (hashState == null ? null : hashState.getVanillaState()) + " and " + state);
        Preconditions.checkArgument(!this.paletteMap.containsKey(state),
                "Mapping for state is already created: " + state);

        BlockStateSnapshot blockState = BlockStateSnapshot.builder()
                .version(this.version)
                .vanillaState(state)
                .hashId(hashId)
                .build();
        this.hash2State.put(hashId, blockState);
        this.paletteMap.put(state, blockState);
    }

    public void clearMapping() {
        this.hash2State.clear();
        this.paletteMap.clear();
        this.customCacheMap.clear();
        this.defaultHashId = -1;
        this.defaultState = null;
    }

    public void setLegacyMapper(LegacyStateMapper legacyStateMapper) {
        this.legacyMapper = legacyStateMapper;
    }

    public LegacyStateMapper getLegacyMapper() {
        return this.legacyMapper;
    }

    public int getVersion() {
        return this.version;
    }

    public BlockStateSnapshot getBlockStateFromFullId(int fullId) {
        return getState(fullId >> Block.DATA_BITS, fullId & Block.DATA_MASK);
    }

    public BlockStateSnapshot getState(int legacyId, int data) {
        int hashId = this.legacyMapper.legacyToHashId(legacyId, data);
        if (hashId == -1) {
            log.warn("Can not find state! No legacy-to-hash mapping for {}:{}", legacyId, data);
            return this.getDefaultState();
        }
        return this.getState(hashId);
    }

    public BlockStateSnapshot getState(int hashId) {
        BlockStateSnapshot blockStateSnapshot = this.hash2State.get(hashId);
        if (blockStateSnapshot == null) {
            log.warn("Can not find state! No hash-to-state mapping for {}", hashId);
            return this.getDefaultState();
        }
        return blockStateSnapshot;
    }

    public BlockStateSnapshot getState(NbtMap vanillaState) {
        BlockStateSnapshot blockStateSnapshot = this.paletteMap.get(vanillaState);
        if (blockStateSnapshot == null) {
            log.warn("Can not find block state! " + vanillaState);
            return this.getDefaultState();
        }
        return blockStateSnapshot;
    }

    public BlockStateSnapshot getStateUnsafe(NbtMap vanillaState) {
        return this.paletteMap.get(vanillaState);
    }

    public BlockStateSnapshot getBlockState(NbtMap tag, NbtMap newTag) {
        BlockStateSnapshot blockStateSnapshot = this.getStateUnsafe(newTag);
        if (blockStateSnapshot != null) {
            return blockStateSnapshot;
        }
        log.debug("Unknown block state: " + tag);
        return BlockStateSnapshot.builder().vanillaState(tag).hashId(this.getDefaultState().getHashId()).version(this.version).custom(true).build();
    }

    public int getHashId(int legacyId, int data) {
        int hashId = this.legacyMapper.legacyToHashId(legacyId, data);
        if (hashId == -1) {
            log.warn("Can not find hashId! No legacy-to-hash mapping for {}:{}", legacyId, data);
            return this.getDefaultHashId();
        }
        return hashId;
    }

    public int getRuntimeId(int legacyId, int data) {
        return this.getHashId(legacyId, data);
    }

    public int getFullId(int hashId) {
        int fullId = this.legacyMapper.hashIdToFullId(hashId);
        if (fullId == -1) {
            log.warn("Can not find legacyId! No hash-to-fullId mapping for {}", hashId);
            fullId = this.legacyMapper.hashIdToFullId(this.getDefaultHashId());
            Preconditions.checkArgument(fullId != -1, "Can not find fullId for default hashId: " + this.getDefaultHashId());
        }
        return fullId;
    }

    public int getLegacyId(int hashId) {
        int legacyId = this.legacyMapper.hashIdToLegacyId(hashId);
        if (legacyId == -1) {
            log.warn("Can not find legacyId! No hash-to-legacy mapping for {}", hashId);
            legacyId = this.legacyMapper.hashIdToLegacyId(this.getDefaultHashId());
            Preconditions.checkArgument(legacyId != -1, "Can not find legacyId for default hashId: " + this.getDefaultHashId());
        }
        return legacyId;
    }

    public int getLegacyData(int hashId) {
        int data = this.legacyMapper.hashIdToLegacyData(hashId);
        if (data == -1) {
            log.warn("Can not find legacy data! No hash-to-legacy mapping for {}", hashId);
            data = this.legacyMapper.hashIdToLegacyData(this.getDefaultHashId());
            Preconditions.checkArgument(data != -1, "Can not find legacyData for default hashId: " + this.getDefaultHashId());
        }
        return data;
    }

    public void setDefaultBlock(int legacyId, int legacyData) {
        int hashId = this.legacyMapper.legacyToHashId(legacyId, legacyData);
        Preconditions.checkArgument(hashId != -1, "Can not find hashId mapping for default block: " + legacyId + ":" + legacyData);
        this.defaultHashId = hashId;

        BlockStateSnapshot state = this.hash2State.get(hashId);
        Preconditions.checkNotNull(state, "Can not find state for default block: " + legacyId + ":" + legacyData);
        this.defaultState = state;
    }

    public int getDefaultHashId() {
        if (this.defaultHashId == -1) {
            this.setDefaultBlock(Block.INFO_UPDATE, 0);
        }
        return this.defaultHashId;
    }

    public int getDefaultRuntimeId() {
        return this.getDefaultHashId();
    }

    public BlockStateSnapshot getDefaultState() {
        if (this.defaultState == null) {
            this.setDefaultBlock(Block.INFO_UPDATE, 0);
        }
        return this.defaultState;
    }

    public BlockStateSnapshot updateState(NbtMap state) {
        BlockStateSnapshot blockState = this.paletteMap.get(state);
        if (blockState == null) {
            blockState = this.updateStateUnsafe(state);
        }
        return blockState;
    }

    public BlockStateSnapshot updateStateUnsafe(NbtMap state) {
        return this.getState(this.updateVanillaState(state));
    }

    public BlockStateSnapshot getUpdatedState(NbtMap state) {
        if (this.paletteMap.get(state) == null) {
            return this.getState(this.updateVanillaState(state));
        }
        return null;
    }

    public NbtMap updateVanillaState(NbtMap state) {
        NbtMap cached = BLOCK_UPDATE_CACHE.get(state);
        if (cached == null) {
            int version = state.getInt("version"); // TODO: validate this when updating next time
            cached = CONTEXT.update(state, LATEST_UPDATER_VERSION == version ? version - 1 : version);
            BLOCK_UPDATE_CACHE.put(state, cached);
        }
        return cached;
    }

    public BlockStateSnapshot getUpdatedOrCustom(NbtMap state) {
        return this.getUpdatedOrCustom(state, this.updateVanillaState(state));
    }

    public BlockStateSnapshot getUpdatedOrCustom(NbtMap state, NbtMap updated) {
        BlockStateSnapshot blockState = this.getStateUnsafe(updated);
        if (blockState != null) {
            return blockState;
        }

        blockState = this.customCacheMap.get(state);
        if (blockState != null) {
            return blockState;
        }

        blockState = BlockStateSnapshot.builder()
                .vanillaState(state)
                .hashId(this.getDefaultState().getHashId())
                .version(this.version)
                .custom(true)
                .build();
        this.customCacheMap.put(state, blockState);
        return blockState;
    }
}
