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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
    private final Int2IntMap legacyToHashId = new Int2IntOpenHashMap();
    private final Int2IntMap hashIdToLegacy = new Int2IntOpenHashMap();

    @ApiStatus.Internal
    @Setter
    private int infoUpdateHashId;
    private volatile boolean locked;

    public BlockPalette(int protocol) {
        this.protocol = protocol;
        legacyToHashId.defaultReturnValue(-1);
        hashIdToLegacy.defaultReturnValue(-1);

        loadBlockStates(paletteFor(protocol));

        this.infoUpdateHashId = legacyToHashId.get(BlockID.INFO_UPDATE << Block.DATA_BITS);
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
        Int2ObjectMap<CompoundTag> hashIdToState = new Int2ObjectOpenHashMap<>();
        for (CompoundTag state : blockStates.getAll()) {
            if (!this.registerBlockState(state, false, hashIdToState)) {
                stateOverloads.add(state);
            }
        }

        for (CompoundTag state : stateOverloads) {
            log.debug("[{}] Registering block palette overload: {}", this.getProtocol(), state.getString("name"));
            this.registerBlockState(state, true, hashIdToState);
        }
    }

    private boolean registerBlockState(CompoundTag state, boolean force, Int2ObjectMap<CompoundTag> hashIdToState) {
        int id = state.getInt("id");
        int data = state.getShort("data");
        boolean stateOverload = state.getBoolean("stateOverload");

        if (stateOverload && !force) {
            return false;
        }

        int hashId = state.getInt("hashId");

        CompoundTag vanillaState = state
                .remove("id")
                .remove("data")
                .remove("hashId")
                .remove("stateOverload");
        CompoundTag previous = hashIdToState.putIfAbsent(hashId, vanillaState);
        if (previous != null && (!previous.getString("name").equals(vanillaState.getString("name")) ||
                !previous.getCompound("states").equals(vanillaState.getCompound("states")))) {
            throw new IllegalStateException("Block state hash collision for " + hashId + ": " + previous + " and " + vanillaState);
        }
        this.registerState(id, data, hashId);
        return true;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public Int2IntMap getLegacyToHashIdMap() {
        return Int2IntMaps.unmodifiable(this.legacyToHashId);
    }

    public Int2IntMap getLegacyToRuntimeIdMap() {
        return this.getLegacyToHashIdMap();
    }

    public void clearStates() {
        this.locked = false;
        this.legacyToHashId.clear();
        this.hashIdToLegacy.clear();
    }

    public void registerState(int blockId, int data, CompoundTag blockState) {
        registerState(blockId, data, Hash.hashBlock(blockState));
    }

    public void registerState(int blockId, int data, int runtimeId, CompoundTag blockState) {
        this.registerState(blockId, data, blockState);
    }

    public void registerState(int blockId, int data, int runtimeId, int hashId) {
        this.registerState(blockId, data, hashId);
    }

    public void registerState(int blockId, int data, int hashId) {
        if (this.locked) {
            throw new IllegalStateException("Block palette is already locked!");
        }

        int legacyId = blockId << Block.DATA_BITS | data;
        this.legacyToHashId.put(legacyId, hashId);
        this.hashIdToLegacy.putIfAbsent(hashId, legacyId);
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
                hashId = this.infoUpdateHashId;
            }
        }
        return hashId;
    }

    public void lock() {
        this.locked = true;
    }

    public void setInfoUpdate(int hashId) {
        this.setInfoUpdateHashId(hashId);
    }

    public int getRuntimeId(int id, int meta) {
        return this.getHashId(id, meta);
    }

    public int getLegacyFullId(int hashId) {
        return this.getLegacyFullIdFromHashId(hashId);
    }

}
