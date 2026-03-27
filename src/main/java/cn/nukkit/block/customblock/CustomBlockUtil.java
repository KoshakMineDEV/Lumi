package cn.nukkit.block.customblock;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.customblock.comparator.HashedPaletteComparator;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.BlockProperty;
import cn.nukkit.block.customblock.properties.EnumBlockProperty;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.level.BlockPalette;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.format.leveldb.BlockStateMapping;
import cn.nukkit.level.format.leveldb.LevelDBConstants;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.registry.Registries;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.cloudburstmc.nbt.*;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Slf4j
@UtilityClass
public class CustomBlockUtil {

    public static final Path VANILLA_PALETTES_PATH = Paths.get("vanilla_palettes/");

    public static Path getVanillaPalettesPath() {
        return Paths.get(Server.getInstance().getDataPath()).resolve(VANILLA_PALETTES_PATH);
    }

    public static Path getVanillaPalettePath(int protocol) {
        return CustomBlockUtil.getVanillaPalettesPath().resolve("vanilla_palette_" + protocol + ".nbt");
    }

    private static void generateVariants(BlockProperties properties, String[] states, List<Map<String, Serializable>> variants, Map<String, Serializable> temp, int offset) {
        if (states.length - offset >= 0) {
            final String currentState = states[states.length - offset];

            properties.getBlockProperty(currentState).forEach((value) -> {
                temp.put(currentState, value);
                if (!variants.contains(temp)) {
                    variants.add(new HashMap<>(temp));
                }
                generateVariants(properties, states, variants, temp, offset + 1);
            });

            temp.put(currentState, 0); //default value
        }
    }

    public static List<Map<String, Serializable>> generateVariants(BlockProperties properties, String[] states) {
        final Map<String, Serializable> temp = new HashMap<>();
        for (String state : states) {
            temp.put(state, 0); // default value
        }

        final List<Map<String, Serializable>> variants = new ArrayList<>();
        if (states.length == 0) {
            variants.add(temp);
        }

        generateVariants(properties, states, variants, temp, 1);
        return variants;
    }

    public static CustomBlockState createBlockState(String identifier, int legacyId, BlockProperties properties, BlockPropertiesHelper block) {
        int meta = legacyId & Block.DATA_MASK;

        NbtMapBuilder statesBuilder = NbtMap.builder();
        if (properties != null) {
            for (String propertyName : properties.getNames()) {
                BlockProperty<?> property = properties.getBlockProperty(propertyName);
                if (property instanceof EnumBlockProperty) {
                    statesBuilder.put(property.getPersistenceName(), properties.getPersistenceValue(meta, propertyName));
                } else {
                    statesBuilder.put(property.getPersistenceName(), properties.getValue(meta, propertyName));
                }
            }
        }

        NbtMap state = NbtMap.builder()
                .putString("name", identifier)
                .putCompound("states", statesBuilder.build())
                .putInt("version", LevelDBConstants.STATE_VERSION)
                .build();
        return new CustomBlockState(identifier, legacyId, state, block);
    }

    public static List<NbtMap> loadVanillaPalette(int version) throws FileNotFoundException {
        Path path = CustomBlockUtil.getVanillaPalettePath(version);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Missing vanilla palette for version " + version);
        }

        try (InputStream stream = Files.newInputStream(path)) {
            return ((NbtMap) NbtUtils.createGZIPReader(stream).readTag()).getList("blocks", NbtType.COMPOUND);
        } catch (Exception e) {
            throw new AssertionError("Error while loading vanilla palette", e);
        }
    }

    private static int convertLegacyToFullId(int legacyId) {
        int blockId = legacyId >> Block.DATA_BITS;
        int meta = legacyId & Block.DATA_MASK;
        return (blockId << Block.DATA_BITS) | meta;
    }

    public static CompoundTag convertNbtMap(NbtMap nbt) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (stream; NBTOutputStream nbtOutputStream = NbtUtils.createWriter(stream)) {
                nbtOutputStream.writeTag(nbt);
            }
            return NBTIO.read(stream.toByteArray(), ByteOrder.BIG_ENDIAN, false);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert NbtMap: " + nbt, e);
        }
    }

    public static void recreateBlockPalette(BlockPalette palette) throws IOException {
        List<NbtMap> vanillaPalette = new ObjectArrayList<>(loadVanillaPalette(palette.getProtocol()));
        recreateBlockPalette(palette, vanillaPalette);
    }

    public static void recreateBlockPalette(BlockPalette palette, List<NbtMap> vanillaPalette) {
        Map<String, List<NbtMap>> vanillaPaletteList = new Object2ObjectRBTreeMap<>(HashedPaletteComparator.INSTANCE);

        int paletteVersion = -1;
        String lastName = null;
        List<NbtMap> group = new ObjectArrayList<>();
        int runtimeId = 0;
        Int2ObjectMap<NbtMap> runtimeId2State = new Int2ObjectOpenHashMap<>();
        for (NbtMap state : vanillaPalette) {
            //删除不属于原版的内容
            if (state.containsKey("network_id") || state.containsKey("name_hash") || state.containsKey("block_id")) {
                NbtMapBuilder builder = NbtMapBuilder.from(state);
                builder.remove("network_id");
                builder.remove("name_hash");
                builder.remove("block_id");
                state = builder.build();
            }

            int version = state.getInt("version");
            if (version != paletteVersion) {
                paletteVersion = version;
            }

            String name = state.getString("name");
            if (lastName != null && !name.equals(lastName)) {
                vanillaPaletteList.put(lastName, group);
                group = new ObjectArrayList<>();
            }
            group.add(state);
            runtimeId2State.put(runtimeId++, state);
            lastName = name;
        }
        if (lastName != null) {
            vanillaPaletteList.put(lastName, group);
        }

        Object2ObjectMap<NbtMap, IntSet> state2Legacy = new Object2ObjectLinkedOpenHashMap<>();

        for (Int2IntMap.Entry entry : palette.getLegacyToRuntimeIdMap().int2IntEntrySet()) {
            int rid = entry.getIntValue();
            NbtMap state = runtimeId2State.get(rid);
            if (state == null) {
                log.info("Unknown runtime ID {}! protocol={}", rid, palette.getProtocol());
                continue;
            }
            IntSet legacyIds = state2Legacy.computeIfAbsent(state, s -> new IntOpenHashSet());
            legacyIds.add(entry.getIntKey());
        }

        for(List<CustomBlockState> variants : Registries.BLOCK.getLegacy2CustomState().values()) {
            for (CustomBlockState definition : variants) {
                NbtMap state = definition.getBlockState();

                final List<NbtMap> states = vanillaPaletteList.computeIfAbsent(state.getString("name"), (k) -> new ObjectArrayList<>());

                if (state.getInt("version") != paletteVersion) {
                    state = state.toBuilder().putInt("version", paletteVersion).build();
                }

                states.add(definition.getBlockState());
                state2Legacy.computeIfAbsent(state, s -> new IntOpenHashSet()).add(convertLegacyToFullId(definition.getLegacyId()));
            }
        }

        palette.clearStates();
        boolean levelDb = palette.getProtocol() == GlobalBlockPalette.getPaletteByProtocol(LevelDBConstants.PALETTE_VERSION).getProtocol(); //防止小版本不相等问题
        if (levelDb) {
            BlockStateMapping.get().clearMapping();
        }

        runtimeId = 0;
        for (List<NbtMap> states : vanillaPaletteList.values()) {
            for (NbtMap state : states) {
                if (!levelDb || !BlockStateMapping.get().containsState(state)) {
                    if (levelDb) {
                        BlockStateMapping.get().registerState(runtimeId, state);
                    }

                    IntSet legacyIds = state2Legacy.get(state);
                    if (legacyIds != null) {
                        for (Integer fullId : legacyIds) {
                            palette.registerState(fullId >> Block.DATA_BITS, (fullId & Block.DATA_MASK), runtimeId);
                        }
                    }
                }
                runtimeId++;
            }
        }
    }

    @Data
    public static class CustomBlockState {
        private final String identifier;
        private final int legacyId;
        private final NbtMap blockState;
        private final BlockPropertiesHelper block;
    }
}
