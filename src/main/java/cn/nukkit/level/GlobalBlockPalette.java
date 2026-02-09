package cn.nukkit.level;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.ByteOrder;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

@Log4j2
public class GlobalBlockPalette {
    private static boolean initialized;

    private static final Int2ObjectMap<BlockPalette> PALETTES = new Int2ObjectOpenHashMap<>();
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
            ProtocolInfo.v1_21_110_26
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

    public static BlockPalette getPaletteByProtocol(int protocol) {
        BlockPalette palette = PALETTES.get(protocol);

        if(palette != null) {
            return palette;
        }

        throw new IllegalArgumentException("Tried to get BlockPalette for unsupported protocol version: " + protocol);
    }

    public static int getOrCreateRuntimeId(int protocol, int id, int meta) {
        return getPaletteByProtocol(protocol).getRuntimeId(id, meta);
    }

    public static int getOrCreateRuntimeId(int protocol, int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(protocol, legacyId >> Block.DATA_BITS, legacyId & Block.DATA_MASK);
    }

    public static int getLegacyFullId(int protocolId, int runtimeId) {
        return getPaletteByProtocol(protocolId).getLegacyFullId(runtimeId);
    }

    @Deprecated
    public static int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        Server.mvw("GlobalBlockPalette#getOrCreateRuntimeId(int)");
        return getOrCreateRuntimeId(ProtocolInfo.CURRENT_PROTOCOL, legacyId >> 4, legacyId & 0xf);
    }

    @Deprecated
    public static int getLegacyFullId(int runtimeId) {
        Server.mvw("GlobalBlockPalette#getLegacyFullId(int)");
        return getLegacyFullId(ProtocolInfo.CURRENT_PROTOCOL, runtimeId);
    }

    @SuppressWarnings("unused")
    private static class TableEntry {
        private int id;
        private int data;
        private String name;
    }

    @SuppressWarnings("unused")
    private static class TableEntryOld {
        private int id;
        private int data;
        private int runtimeID;
        private String name;
    }
}
