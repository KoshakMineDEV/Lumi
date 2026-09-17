package cn.nukkit.level.format.leveldb;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.BlockPalette;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.utils.Hash;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static cn.nukkit.level.format.leveldb.LevelDBConstants.PALETTE_VERSION;

public class NukkitLegacyMapper implements LegacyStateMapper {

    public static void registerStates(BlockStateMapping blockStateMapping) {
        List<NbtMap> list = NukkitLegacyMapper.loadBlockPalette();
        for (NbtMap paletteState : list) {
            NbtMap nbtMap = paletteState;
            // remove fields not related to vanilla
            if (nbtMap.containsKey("network_id") || nbtMap.containsKey("name_hash") || nbtMap.containsKey("block_id")) {
                NbtMapBuilder builder = NbtMapBuilder.from(nbtMap);
                builder.remove("network_id");
                builder.remove("name_hash");
                builder.remove("block_id");
                nbtMap = builder.build();
            }
            //noinspection ResultOfMethodCallIgnored
            nbtMap.hashCode(); // cache hashCode
            blockStateMapping.registerState(Hash.hashBlock(nbtMap), nbtMap);
        }
    }

    public static List<NbtMap> loadBlockPalette() {
        List<NbtMap> nbtMaps;
        try (InputStream stream = Server.class.getClassLoader().getResourceAsStream("internal/leveldb_palette.nbt")) {
            nbtMaps = ((NbtMap) NbtUtils.createGZIPReader(Objects.requireNonNull(stream)).readTag()).getList("blocks", NbtType.COMPOUND);
        } catch (Exception e) {
            throw new AssertionError("Error loading block palette leveldb_palette.nbt", e);
        }
        return nbtMaps;
    }

    private final BlockPalette blockPalette = GlobalBlockPalette.getPaletteByProtocol(PALETTE_VERSION);

    @Override
    public int legacyToHashId(int legacyId, int meta) {
        return blockPalette.getHashId(legacyId, meta);
    }

    @Override
    public int hashIdToFullId(int hashId) {
        return blockPalette.getLegacyFullIdFromHashId(hashId);
    }

    @Override
    public int hashIdToLegacyId(int hashId) {
        int fullId = this.hashIdToFullId(hashId);
        return fullId == -1 ? -1 : fullId >> Block.DATA_BITS;
    }

    @Override
    public int hashIdToLegacyData(int hashId) {
        int fullId = this.hashIdToFullId(hashId);
        return fullId == -1 ? -1 : fullId & Block.DATA_MASK;
    }

    @Override
    public int legacyToRuntime(int legacyId, int meta) {
        return this.legacyToHashId(legacyId, meta);
    }

    @Override
    public int runtimeToFullId(int runtimeId) {
        return this.hashIdToFullId(runtimeId);
    }

    @Override
    public int runtimeToLegacyId(int runtimeId) {
        return this.hashIdToLegacyId(runtimeId);
    }

    @Override
    public int runtimeToLegacyData(int runtimeId) {
        return this.hashIdToLegacyData(runtimeId);
    }

}
