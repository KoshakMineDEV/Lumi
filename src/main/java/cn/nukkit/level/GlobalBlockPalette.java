package cn.nukkit.level;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Log4j2
public class GlobalBlockPalette {
    private static boolean initialized;

    private static final Int2ObjectMap<BlockPalette> PALETTES = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Int2ObjectMap<Item>> DOWNGRADES = new Int2ObjectOpenHashMap<>();
    private static final int[] PROTOCOLS = new int[] {
            ProtocolInfo.v1_20_0_23,
            ProtocolInfo.v1_20_10_21,
            ProtocolInfo.v1_20_30_24,
            ProtocolInfo.v1_20_40,
            ProtocolInfo.v1_20_50,
            ProtocolInfo.v1_20_60,
            ProtocolInfo.v1_20_70,
            ProtocolInfo.v1_20_80,
            ProtocolInfo.v1_21_0,
            ProtocolInfo.v1_21_20,
            ProtocolInfo.v1_21_30,
            ProtocolInfo.v1_21_40,
            ProtocolInfo.v1_21_50_26,
            ProtocolInfo.v1_21_60,
            ProtocolInfo.v1_21_70_24,
            ProtocolInfo.v1_21_80,
            ProtocolInfo.v1_21_90,
            ProtocolInfo.v1_21_100,
            ProtocolInfo.v1_21_110_26,
            ProtocolInfo.v1_26_10,
            ProtocolInfo.v1_26_20_26,
            ProtocolInfo.v1_26_30,
            ProtocolInfo.v1_26_40,
            ProtocolInfo.v1_26_50
    };

    static {
        for(int protocol : PROTOCOLS) {
            PALETTES.put(protocol, new BlockPalette(protocol));
        }

        for(int i = 0;i < ProtocolInfo.SUPPORTED_PROTOCOLS.size();i++) {
            int protocol = ProtocolInfo.SUPPORTED_PROTOCOLS.get(i);

            if(PALETTES.containsKey(protocol)) {
                continue;
            }

            PALETTES.put(protocol, PALETTES.get(i == 0 ? ProtocolInfo.v1_20_0_23 : ProtocolInfo.SUPPORTED_PROTOCOLS.get(i - 1)));
        }
    }

    public static void init() {
        if (initialized) {
            throw new IllegalStateException("BlockPalette was already generated!");
        }
        initialized = true;
    }

    private static int getLegacyId(String id) {
        Block block = Item.get(id).getBlock();
        return block.getId() << Block.DATA_BITS | block.getDamage();
    }

    private static String getIdentifier(int id, int meta) {
        return Block.get(id, meta).getIdentifier();
    }

    public static void downgradePalettes() {
        try {
            final JsonObject mapping = new JsonParser().parse(new String(GlobalBlockPalette.class.getClassLoader().getResourceAsStream("internal/downgrade_palette.json").readAllBytes())).getAsJsonObject();
            final Set<BlockPalette> paletteList = getAllPalette();

            int preProtocol = 0;

            for(Map.Entry<String, IntList> entry : getAllNoneBlocks(key -> mapping.has(key.toString())).entrySet()) {
                final String namespace = entry.getKey();
                final IntList list = entry.getValue();

                for (BlockPalette palette : paletteList) {
                    final int legacyId = getLegacyId(namespace);
                    if (palette.getLegacyToHashIdMap().containsKey(legacyId)) {
                        break;
                    }

                    final JsonElement description = mapping.get(namespace);
                    final JsonObject json;

                    if (description instanceof JsonObject jsonObject) {
                        json = jsonObject;
                    } else {
                        json = new JsonObject();
                        json.addProperty("id", description.getAsString());
                    }

                    MappingFunction function = (json.has("type") ? MappingType.valueOf(json.get("type").getAsString().toUpperCase()) : MappingType.DEFAULT).getFunction();

                    list.forEach(mapId -> {
                        final int id = mapId >> Block.DATA_BITS;
                        final int meta = mapId & Block.DATA_MASK;

                        final int legacyIdMapping = function.map(json, id, meta);

                        palette.registerState(id, meta, palette.getHashId(legacyIdMapping >> Block.DATA_BITS, legacyIdMapping & Block.DATA_MASK));
                    });

                    {
                        int orgId = list.getFirst() >> Block.DATA_BITS;
                        int id = function.map(json, orgId, 0) >> Block.DATA_BITS;
                        Item item = Block.get(id).toItem();

                        for(int protocol = preProtocol;protocol < palette.getProtocol();protocol++) {
                            DOWNGRADES.computeIfAbsent(protocol, (k) -> new Int2ObjectArrayMap<>()).put(orgId, item);
                        }
                    }

                    preProtocol = palette.getProtocol();

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, IntList> getAllNoneBlocks(Function<String, Boolean> has) {
        final Int2IntMap firstMap = GlobalBlockPalette.getPaletteByProtocol(ProtocolInfo.v1_20_0).getLegacyToHashIdMap();
        final Int2IntMap lastMap = GlobalBlockPalette.getPaletteByProtocol(ProtocolInfo.CURRENT_PROTOCOL).getLegacyToHashIdMap();
        final IntList noneBlocks = new IntArrayList();
        final Map<String, IntList> ids = new HashMap<>();

        for(var entry : lastMap.int2IntEntrySet()) {
            int legacyId = entry.getIntKey();

            if(!firstMap.containsKey(legacyId)) {
                noneBlocks.add(legacyId);
            }
        }

        for(int legacyId : noneBlocks) {
            int id = legacyId >> Block.DATA_BITS;
            int meta = legacyId & Block.DATA_MASK;

            try {
                String namespace = getIdentifier(id, meta);
                if(has.apply(namespace)) {
                    ids.computeIfAbsent(namespace, key -> new IntArrayList()).add(legacyId);
                }
            } catch (Throwable ignore) {}
        }

        return ids;
    }

    private static Set<BlockPalette> getAllPalette() {
        final Set<BlockPalette> paletteList = new ObjectArraySet<>();

        for(int protocol : ProtocolInfo.SUPPORTED_PROTOCOLS) {
            paletteList.add(GlobalBlockPalette.getPaletteByProtocol(protocol));
        }

        return paletteList;
    }

    public static BlockPalette getPaletteByProtocol(int protocol) {
        BlockPalette palette = PALETTES.get(protocol);

        if(palette != null) {
            return palette;
        }

        throw new IllegalArgumentException("Tried to get BlockPalette for unsupported protocol version: " + protocol);
    }

    public static int getOrCreateRuntimeId(int protocol, int id, int meta) {
        return getOrCreateHashId(protocol, id, meta);
    }

    public static int getOrCreateRuntimeId(int protocol, int legacyId) throws NoSuchElementException {
        return getOrCreateHashId(protocol, legacyId);
    }

    /**
     * Get full legacy block ID from hash ID
     * <p>
     * Used to convert hash ID used in newer protocols (1.19.80+) back to internal legacy block ID
     *
     * @param protocolId game version
     * @param hashId hash ID of the block state
     * @return full legacy block ID
     * @throws IllegalArgumentException if protocol version is not supported
     */
    public static int getLegacyFullIdFromHashId(int protocolId, int hashId) {
        BlockPalette blockPalette = getPaletteByProtocol(protocolId);
        return blockPalette.getLegacyFullIdFromHashId(hashId);
    }

    public static int getLegacyFullId(int protocolId, CompoundTag blockState) {
        BlockPalette blockPalette = getPaletteByProtocol(protocolId);
        if (blockPalette != null) {
            return blockPalette.getLegacyFullId(blockState);
        }
        throw new IllegalArgumentException("Tried to get legacyFullId for unsupported protocol version: " + protocolId);
    }

    /**
     * Get or create hash ID of a block
     * <p>
     * Returns the block's hash ID if hashed block network IDs are enabled, otherwise returns -1
     *
     * @param gameVersion game version
     * @param id block ID
     * @param meta  block metadata value
     * @return hash ID of the block, returns -1 if feature is not enabled
     */
    public static int getOrCreateHashId(int gameVersion, int id, int meta) {
        return getPaletteByProtocol(gameVersion).getHashId(id, meta);
    }

    /**
     * Get or create hash ID from legacy block ID
     * <p>
     * Returns the block's hash ID if hashed block network IDs are enabled, otherwise returns -1
     *
     * @param gameVersion game version
     * @param legacyId (blockId << Block.DATA_BITS | meta) / full legacy block ID
     * @return hash ID of the block, returns -1 if feature is not enabled
     * @throws NoSuchElementException if block is not found
     */
    public static int getOrCreateHashId(int gameVersion, int legacyId) throws NoSuchElementException {
        return getPaletteByProtocol(gameVersion).getHashId(legacyId >> Block.DATA_BITS, legacyId & Block.DATA_MASK);
    }

    public static int getLegacyFullId(int protocolId, int hashId) {
        return getLegacyFullIdFromHashId(protocolId, hashId);
    }

    public static Item getDowngradedItemBlock(int protocolId, int id) {
        final Int2ObjectMap<Item> set = DOWNGRADES.get(protocolId);
        if(set == null) return null;
        return set.get(id);
    }

    private interface MappingFunction {
        int map(JsonObject description, int originalId, int originalMeta);
    }

    @AllArgsConstructor
    @Getter
    private enum MappingType {
        DEFAULT((json, orgId, orgMeta) -> {
            int legacyId = getId(json);
            return (legacyId >> Block.DATA_BITS) << Block.DATA_BITS | (json.has("meta") ? json.get("meta").getAsInt() : 0);
        }),
        SAVE_META((json, orgId, orgMeta) -> {
            int legacyId = getId(json);
            return (legacyId >> Block.DATA_BITS) << Block.DATA_BITS | orgMeta;
        });

        private final MappingFunction function;

        private static int getId(JsonObject json) {
            return getLegacyId(json.get("id").getAsString());
        }
    }
}
