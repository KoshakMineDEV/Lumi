package cn.nukkit.level;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.Hash;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.ApiStatus;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Log4j2
public class BlockPalette {

    private final int protocol;
    private final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    private final Int2IntMap legacyToHashId = new Int2IntOpenHashMap();
    private final Int2IntMap hashIdToLegacy = new Int2IntOpenHashMap();

    @ApiStatus.Internal
    @Setter
    private int infoUpdate;
    private volatile boolean locked;

    public BlockPalette(int protocol) {
        this.protocol = protocol;
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);
        legacyToHashId.defaultReturnValue(-1);
        hashIdToLegacy.defaultReturnValue(-1);

        loadBlockStates(paletteFor(protocol));

        this.infoUpdate = legacyToRuntimeId.get(BlockID.INFO_UPDATE << Block.DATA_BITS);
    }

    private ListTag<CompoundTag> paletteFor(int protocol) {
        ListTag<CompoundTag> tag;
        try (InputStream stream = Server.class.getClassLoader().getResourceAsStream("gamedata/block/runtime/runtime_block_states_" + protocol + ".dat")) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state nbt " + protocol);
            }
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new BufferedInputStream(new GZIPInputStream(stream)), ByteOrder.BIG_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette " + protocol, e);
        }
        return tag;
    }

    private void loadBlockStates(ListTag<CompoundTag> blockStates) {
        List<CompoundTag> stateOverloads = new ObjectArrayList<>();
        for (CompoundTag state : blockStates.getAll()) {
            if (!this.registerBlockState(state, false)) {
                stateOverloads.add(state);
            }
        }

        for (CompoundTag state : stateOverloads) {
            log.debug("[{}] Registering block palette overload: {}", this.getProtocol(), state.getString("name"));
            this.registerBlockState(state, true);
        }
    }

    private boolean registerBlockState(CompoundTag state, boolean force) {
        int id = state.getInt("id");
        int data = state.getShort("data");
        int runtimeId = state.getInt("runtimeId");
        boolean stateOverload = state.getBoolean("stateOverload");

        if (stateOverload && !force) {
            return false;
        }

        CompoundTag vanillaState = state
                .remove("id")
                .remove("data")
                .remove("runtimeId")
                .remove("stateOverload");
        this.registerState(id, data, runtimeId, vanillaState);
        return true;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public Int2IntMap getLegacyToRuntimeIdMap() {
        return Int2IntMaps.unmodifiable(this.legacyToRuntimeId);
    }

    public void clearStates() {
        this.locked = false;
        this.legacyToRuntimeId.clear();
        this.runtimeIdToLegacy.clear();
        this.legacyToHashId.clear();
        this.hashIdToLegacy.clear();
    }

    public void registerState(int blockId, int data, int runtimeId, CompoundTag blockState) {
        registerState(blockId, data, runtimeId, Hash.hashBlock(blockState));
    }

    public void registerState(int blockId, int data, int runtimeId, int stateHash) {
        if (this.locked) {
            throw new IllegalStateException("Block palette is already locked!");
        }

        int legacyId = blockId << Block.DATA_BITS | data;
        this.legacyToRuntimeId.put(legacyId, runtimeId);
        this.runtimeIdToLegacy.putIfAbsent(runtimeId, legacyId);
        this.legacyToHashId.putIfAbsent(legacyId, stateHash);
        this.hashIdToLegacy.putIfAbsent(stateHash, legacyId);
    }

    /**
     * Get full legacy block ID from hash ID
     * <p>
     * Hash ID is calculated from block state NBT and used for block network transmission in newer versions
     *
     * @param hashId hash ID of the block state
     * @return full legacy block ID, returns -1 if not found
     */
    public int getLegacyFullIdFromHashId(int hashId) {
        return hashIdToLegacy.get(hashId);
    }

    public int getLegacyFullId(CompoundTag blockState) {
        return hashIdToLegacy.getOrDefault(Hash.hashBlock(blockState), -1);
    }

    /**
     * Get hash ID of a block (using default meta value 0)
     *
     * @param id block ID
     * @return hash ID of the block
     */
    public int getHashId(int id) {
        return this.getHashId(id, 0);
    }

    /**
     * Get hash ID of a block
     * <p>
     * Hash ID is used for block network transmission in newer protocols (1.19.80+), based on block state NBT hash
     *
     * @param id block ID
     * @param meta block metadata value
     * @return hash ID of the block, returns INFO_UPDATE block's hash ID if not found
     */
    public int getHashId(int id, int meta) {
        int legacyId = id << Block.DATA_BITS | meta;
        int hashId = legacyToHashId.get(legacyId);
        if (hashId == -1) {
            hashId = legacyToHashId.get(id << Block.DATA_BITS);
            if (hashId == -1) {
                hashId = legacyToHashId.get(BlockID.INFO_UPDATE << Block.DATA_BITS);
            }
        }
        return hashId;
    }

    public void lock() {
        this.locked = true;
    }

    public int getRuntimeId(int id, int meta) {
        int legacyId = id << Block.DATA_BITS | meta;
        int runtimeId;
        runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << Block.DATA_BITS);
            if (runtimeId == -1) {
                Server.getInstance().getLogger().debug("(" + protocol + ") Missing block runtime id mappings for " + id + ':' + meta);
                runtimeId = infoUpdate;
                legacyToRuntimeId.put(legacyId, runtimeId);
            }
        }
        return runtimeId;
    }

    public int getLegacyFullId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }

}
