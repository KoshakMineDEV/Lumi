package cn.nukkit.block.customblock;

import cn.nukkit.block.Block;
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
import cn.nukkit.utils.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@UtilityClass
public class CustomBlockUtil {

    private static Serializable getDefaultValue(BlockProperties properties, String name) {
        AtomicReference<Serializable> value = new AtomicReference<>();
        properties.getBlockProperty(name).forEach(serializable -> {
            if(value.get() == null) value.set(serializable);
        });
        return value.get();
    }

    private static void generateVariants(BlockProperties properties, String[] states, List<Map<String, Serializable>> variants, Map<String, Serializable> temp, int offset) {
        if (states.length - offset >= 0) {
            final String currentState = states[states.length - offset];

            properties.getBlockProperty(currentState).forEach((value) -> {
                temp.put(currentState, value);

                for(Map<String, Serializable> checkState : variants) {
                    if(checkState.toString().equals(temp.toString())) {
                        generateVariants(properties, states, variants, temp, offset + 1);
                        return;
                    }
                }

                variants.add(new HashMap<>(temp));
                generateVariants(properties, states, variants, temp, offset + 1);
            });

            temp.put(currentState, getDefaultValue(properties, currentState));
        }
    }

    public static List<Map<String, Serializable>> generateVariants(BlockProperties properties, String[] states) {
        final Map<String, Serializable> temp = new HashMap<>();
        for (String state : states) {
            temp.put(state, getDefaultValue(properties, state)); // default value
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

    public static void recreateBlockPalette(BlockPalette palette) {
        List<CustomBlockState> states = new ArrayList<>();
        for (List<CustomBlockState> variants : Registries.BLOCK.getLegacy2CustomState().values()) {
            states.addAll(variants);
        }
        recreateBlockPalette(palette, states);
    }

    static void recreateBlockPalette(BlockPalette palette, Iterable<CustomBlockState> customStates) {
        Int2ObjectMap<NbtMap> hashId2State = new Int2ObjectOpenHashMap<>();
        List<CustomBlockState> definitions = new ArrayList<>();
        IntList hashIds = new IntArrayList();
        for (CustomBlockState definition : customStates) {
            definitions.add(definition);
            NbtMap state = definition.getBlockState();
            int hashId = Hash.hashBlock(state);
            hashIds.add(hashId);
            NbtMap previous = hashId2State.putIfAbsent(hashId, state);
            if (previous != null && (!previous.getString("name").equals(state.getString("name")) ||
                    !previous.getCompound("states").equals(state.getCompound("states")))) {
                throw new IllegalStateException("Block state hash collision for " + hashId + ": " + previous + " and " + state);
            }
        }

        palette.clearCustomStates();
        boolean levelDb = palette.getProtocol() == GlobalBlockPalette.getPaletteByProtocol(LevelDBConstants.PALETTE_VERSION).getProtocol();
        if (levelDb) {
            BlockStateMapping.get().clearCustomStates();
            for (Int2ObjectMap.Entry<NbtMap> entry : hashId2State.int2ObjectEntrySet()) {
                BlockStateMapping.get().registerCustomState(entry.getIntKey(), entry.getValue());
            }
        }

        for (int i = 0; i < definitions.size(); i++) {
            CustomBlockState definition = definitions.get(i);
            int fullId = definition.getLegacyId();
            palette.registerCustomState(fullId >> Block.DATA_BITS, fullId & Block.DATA_MASK, hashIds.getInt(i));
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
