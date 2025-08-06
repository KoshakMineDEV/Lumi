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

    private static final AtomicInteger runtimeIdAllocator407 = new AtomicInteger(0);
    private static final Int2IntMap legacyToRuntimeId407 = new Int2IntOpenHashMap();

    private static BlockPalette blockPalette419;
    private static BlockPalette blockPalette428;
    private static BlockPalette blockPalette440;
    private static BlockPalette blockPalette448;
    private static BlockPalette blockPalette465;
    private static BlockPalette blockPalette471;
    private static BlockPalette blockPalette486;
    private static BlockPalette blockPalette503;
    private static BlockPalette blockPalette527;
    private static BlockPalette blockPalette544;
    private static BlockPalette blockPalette560;
    private static BlockPalette blockPalette567;
    private static BlockPalette blockPalette575;
    private static BlockPalette blockPalette582;
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

    private static byte[] compiledTable407;

    static {
        legacyToRuntimeId407.defaultReturnValue(-1);

        getPaletteByProtocol(ProtocolInfo.CURRENT_PROTOCOL); // cache current block palette
    }

    public static void init() {
        if (initialized) {
            throw new IllegalStateException("BlockPalette was already generated!");
        }
        initialized = true;
        log.debug("Loading block palette...");
        // 407
        ListTag<CompoundTag> tag407;
        try (InputStream stream407 = Server.class.getClassLoader().getResourceAsStream("runtime_block_states_407.dat")) {
            if (stream407 == null) {
                throw new AssertionError("Unable to locate block state nbt 407");
            }
            //noinspection unchecked
            tag407 = (ListTag<CompoundTag>) NBTIO.readTag(new BufferedInputStream(new GZIPInputStream(stream407)), ByteOrder.BIG_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette 407", e);
        }
        for (CompoundTag state : tag407.getAll()) {
            int id = state.getInt("id");
            int data = state.getShort("data");
            int runtimeId = runtimeIdAllocator407.getAndIncrement();
            int legacyId = id << 6 | data;
            legacyToRuntimeId407.put(legacyId, runtimeId);
            state.remove("data");
        }
        try {
            compiledTable407 = NBTIO.write(tag407, ByteOrder.LITTLE_ENDIAN, true);
        } catch (IOException e) {
            throw new AssertionError("Unable to write block palette 407", e);
        }
    }

    public static BlockPalette getPaletteByProtocol(int protocol) {
        if(protocol >= ProtocolInfo.v1_21_100) {
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
        } else if (protocol >= ProtocolInfo.v1_19_80) {
            if (blockPalette582 == null) {
                blockPalette582 = new BlockPalette(ProtocolInfo.v1_19_80);
            }
            return blockPalette582;
        } else if (protocol >= ProtocolInfo.v1_19_70_24) {
            if (blockPalette575 == null) {
                blockPalette575 = new BlockPalette(ProtocolInfo.v1_19_70);
            }
            return blockPalette575;
        } else if (protocol >= ProtocolInfo.v1_19_60) {
            if (blockPalette567 == null) {
                blockPalette567 = new BlockPalette(ProtocolInfo.v1_19_60);
            }
            return blockPalette567;
        } else if (protocol >= ProtocolInfo.v1_19_50_20) {
            if (blockPalette560 == null) {
                blockPalette560 = new BlockPalette(ProtocolInfo.v1_19_50);
            }
            return blockPalette560;
        } else if (protocol >= ProtocolInfo.v1_19_20) {
            if (blockPalette544 == null) {
                blockPalette544 = new BlockPalette(ProtocolInfo.v1_19_20);
            }
            return blockPalette544;
        } else if (protocol >= ProtocolInfo.v1_19_0_29) {
            if (blockPalette527 == null) {
                blockPalette527 = new BlockPalette(ProtocolInfo.v1_19_0);
            }
            return blockPalette527;
        } else if (protocol >= ProtocolInfo.v1_18_30) {
            if (blockPalette503 == null) {
                blockPalette503 = new BlockPalette(ProtocolInfo.v1_18_30);
            }
            return blockPalette503;
        } else if (protocol >= ProtocolInfo.v1_18_10_26) {
            if (blockPalette486 == null) {
                blockPalette486 = new BlockPalette(ProtocolInfo.v1_18_10);
            }
            return blockPalette486;
        } else if (protocol >= ProtocolInfo.v1_17_40) {
            if (blockPalette471 == null) {
                blockPalette471 = new BlockPalette(ProtocolInfo.v1_17_40);
            }
            return blockPalette471;
        } else if (protocol >= ProtocolInfo.v1_17_30) {
            if (blockPalette465 == null) {
                blockPalette465 = new BlockPalette(ProtocolInfo.v1_17_30);
            }
            return blockPalette465;
        } else if (protocol >= ProtocolInfo.v1_17_10) {
            if (blockPalette448 == null) {
                blockPalette448 = new BlockPalette(ProtocolInfo.v1_17_10);
            }
            return blockPalette448;
        } else if (protocol >= ProtocolInfo.v1_17_0) {
            if (blockPalette440 == null) {
                blockPalette440 = new BlockPalette(ProtocolInfo.v1_17_0);
            }
            return blockPalette440;
        } else if (protocol >= ProtocolInfo.v1_16_210) {
            if (blockPalette428 == null) {
                blockPalette428 = new BlockPalette(ProtocolInfo.v1_16_210);
            }
            return blockPalette428;
        } else if (protocol >= ProtocolInfo.v1_16_100) {
            if (blockPalette419 == null) {
                blockPalette419 = new BlockPalette(ProtocolInfo.v1_16_100);
            }
            return blockPalette419;
        }

        throw new IllegalArgumentException("Tried to get BlockPalette for unsupported protocol version: " + protocol);
    }

    public static int getOrCreateRuntimeId(int protocol, int id, int meta) {
        if (protocol >= ProtocolInfo.v1_16_100) {
            return getPaletteByProtocol(protocol).getRuntimeId(id, meta);
        }

        if (protocol < 407) throw new IllegalArgumentException("Tried to get block runtime id for unsupported protocol version: " + protocol);
        int legacyId = id << 6 | meta;
        int runtimeId;
        switch (protocol) {
            case 407:
            case 408:
            case 409:
            case 410:
            case 411:
                runtimeId = legacyToRuntimeId407.get(legacyId);
                if (runtimeId == -1) {
                    runtimeId = legacyToRuntimeId407.get(id << 6);
                    if (runtimeId == -1) runtimeId = legacyToRuntimeId407.get(BlockID.INFO_UPDATE << 6);
                }
                return runtimeId;
            default:
                throw new IllegalArgumentException("Tried to get legacyToRuntimeIdMap for unsupported protocol version: " + protocol);
        }
    }

    public static byte[] getCompiledTable(int protocol) {
        switch (protocol) {
            // Versions before this doesn't send compiled table in StartGamePacket
            case 407:
            case 408:
            case 409:
            case 410:
            case 411:
                return compiledTable407;
            default: // Unused since 1.16.100 (419)
                throw new IllegalArgumentException("Tried to get compiled block runtime id table for unsupported protocol version: " + protocol);
        }
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

    public static int getLegacyFullId(int protocolId, CompoundTag compoundTag) {
        BlockPalette blockPalette = getPaletteByProtocol(protocolId);
        if (blockPalette != null) {
            return blockPalette.getLegacyFullId(compoundTag);
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
