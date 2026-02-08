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
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.ByteOrder;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

@Log4j2
public class GlobalBlockPalette {
    private static boolean initialized;

    private static BlockPalette blockPalette589;
    private static BlockPalette blockPalette594;
    private static BlockPalette blockPalette618;
    private static BlockPalette blockPalette622;
    private static BlockPalette blockPalette630;
    private static BlockPalette blockPalette649;
    private static BlockPalette blockPalette662;
    private static BlockPalette blockPalette671;
    private static BlockPalette blockPalette685;
    private static BlockPalette blockPalette712;
    private static BlockPalette blockPalette729;
    private static BlockPalette blockPalette748;
    private static BlockPalette blockPalette766;
    private static BlockPalette blockPalette776;
    private static BlockPalette blockPalette786;
    private static BlockPalette blockPalette800;
    private static BlockPalette blockPalette818;
    private static BlockPalette blockPalette827;
    private static BlockPalette blockPalette844;

    static {
        getPaletteByProtocol(ProtocolInfo.CURRENT_PROTOCOL); // cache current block palette
    }

    public static void init() {
        if (initialized) {
            throw new IllegalStateException("BlockPalette was already generated!");
        }
        initialized = true;
    }

    public static BlockPalette getPaletteByProtocol(int protocol) {
        if (protocol >= ProtocolInfo.v1_21_110_26) {
            if (blockPalette844 == null) {
                blockPalette844 = new BlockPalette(ProtocolInfo.v1_21_111);
            }
            return blockPalette844;
        } else if (protocol >= ProtocolInfo.v1_21_100) {
            if (blockPalette827 == null) {
                blockPalette827 = new BlockPalette(ProtocolInfo.v1_21_100);
            }
            return blockPalette827;
        } else if (protocol >= ProtocolInfo.v1_21_93) {
            if (blockPalette818 == null) {
                blockPalette818 = new BlockPalette(ProtocolInfo.v1_21_90);
            }
            return blockPalette818;
        } else if (protocol >= ProtocolInfo.v1_21_90) {
            if (blockPalette818 == null) {
                blockPalette818 = new BlockPalette(ProtocolInfo.v1_21_90);
            }
            return blockPalette818;
        } else if (protocol >= ProtocolInfo.v1_21_80) {
            if (blockPalette800 == null) {
                blockPalette800 = new BlockPalette(ProtocolInfo.v1_21_80);
            }
            return blockPalette800;
        } else if (protocol >= ProtocolInfo.v1_21_70_24) {
            if (blockPalette786 == null) {
                blockPalette786 = new BlockPalette(ProtocolInfo.v1_21_70);
            }
            return blockPalette786;
        } else if (protocol >= ProtocolInfo.v1_21_60) {
            if (blockPalette776 == null) {
                blockPalette776 = new BlockPalette(ProtocolInfo.v1_21_60);
            }
            return blockPalette776;
        } else if (protocol >= ProtocolInfo.v1_21_50_26) {
            if (blockPalette766 == null) {
                blockPalette766 = new BlockPalette(ProtocolInfo.v1_21_50);
            }
            return blockPalette766;
        } else if (protocol >= ProtocolInfo.v1_21_40) {
            if (blockPalette748 == null) {
                blockPalette748 = new BlockPalette(ProtocolInfo.v1_21_40);
            }
            return blockPalette748;
        } else if (protocol >= ProtocolInfo.v1_21_30) {
            if (blockPalette729 == null) {
                blockPalette729 = new BlockPalette(ProtocolInfo.v1_21_30);
            }
            return blockPalette729;
        } else if (protocol >= ProtocolInfo.v1_21_20) {
            if (blockPalette712 == null) {
                blockPalette712 = new BlockPalette(ProtocolInfo.v1_21_20);
            }
            return blockPalette712;
        } else if (protocol >= ProtocolInfo.v1_21_0) {
            if (blockPalette685 == null) {
                blockPalette685 = new BlockPalette(ProtocolInfo.v1_21_0);
            }
            return blockPalette685;
        } else if (protocol >= ProtocolInfo.v1_20_80) {
            if (blockPalette671 == null) {
                blockPalette671 = new BlockPalette(ProtocolInfo.v1_20_80);
            }
            return blockPalette671;
        } else if (protocol >= ProtocolInfo.v1_20_70) {
            if (blockPalette662 == null) {
                blockPalette662 = new BlockPalette(ProtocolInfo.v1_20_70);
            }
            return blockPalette662;
        } else if (protocol >= ProtocolInfo.v1_20_60) {
            if (blockPalette649 == null) {
                blockPalette649 = new BlockPalette(ProtocolInfo.v1_20_60);
            }
            return blockPalette649;
        } else if (protocol >= ProtocolInfo.v1_20_50) {
            if (blockPalette630 == null) {
                blockPalette630 = new BlockPalette(ProtocolInfo.v1_20_50);
            }
            return blockPalette630;
        } else if (protocol >= ProtocolInfo.v1_20_40) {
            if (blockPalette622 == null) {
                blockPalette622 = new BlockPalette(ProtocolInfo.v1_20_40);
            }
            return blockPalette622;
        } else if (protocol >= ProtocolInfo.v1_20_30_24) {
            if (blockPalette618 == null) {
                blockPalette618 = new BlockPalette(ProtocolInfo.v1_20_30);
            }
            return blockPalette618;
        } else if (protocol >= ProtocolInfo.v1_20_10_21) {
            if (blockPalette594 == null) {
                blockPalette594 = new BlockPalette(ProtocolInfo.v1_20_10);
            }
            return blockPalette594;
        } else if (protocol >= ProtocolInfo.v1_20_0_23) {
            if (blockPalette589 == null) {
                blockPalette589 = new BlockPalette(ProtocolInfo.v1_20_0);
            }
            return blockPalette589;
        }

        throw new IllegalArgumentException("Tried to get BlockPalette for unsupported protocol version: " + protocol);
    }

    public static int getOrCreateRuntimeId(int protocol, int id, int meta) {
        return getPaletteByProtocol(protocol).getRuntimeId(id, meta);
    }

    public static int getOrCreateRuntimeId(int protocol, int legacyId) throws NoSuchElementException {
        if (protocol < 407) throw new IllegalArgumentException("Tried to get block runtime id for unsupported protocol version: " + protocol);
        return getOrCreateRuntimeId(protocol, legacyId >> Block.DATA_BITS, legacyId & Block.DATA_MASK);
    }

    public static int getLegacyFullId(int protocolId, int runtimeId) {
        BlockPalette blockPalette = getPaletteByProtocol(protocolId);
        if (blockPalette != null) {
            return blockPalette.getLegacyFullId(runtimeId);
        }
        throw new IllegalArgumentException("Tried to get legacyFullId for unsupported protocol version: " + protocolId);
    }

    public static int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        Server.mvw("GlobalBlockPalette#getOrCreateRuntimeId(int)");
        return getOrCreateRuntimeId(ProtocolInfo.CURRENT_PROTOCOL, legacyId >> 4, legacyId & 0xf);
    }

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
