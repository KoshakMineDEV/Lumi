package cn.nukkit.level.format.leveldb.updater;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloudburstmc.blockstateupdater.BlockStateUpdater;
import org.cloudburstmc.blockstateupdater.BlockStateUpdater_1_26_50;
import org.cloudburstmc.blockstateupdater.util.tagupdater.CompoundTagUpdaterContext;
import org.cloudburstmc.nbt.NbtMap;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.nukkit.level.format.leveldb.LevelDBConstants.*;

/**
 * This is updater for vanilla worlds
 * Convert some blocks to blocks supported by nk
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockStateUpdaterVanilla implements BlockStateUpdater {

    public static final BlockStateUpdater INSTANCE = new BlockStateUpdaterVanilla();

    private static final Set<String> STAIRS = Arrays.stream(BlockStateUpdater_1_26_50.STAIRS)
            .map(name -> "minecraft:" + name)
            .collect(Collectors.toUnmodifiableSet());
    // These blocks still use legacy layouts in Lumi and cannot be reconstructed from four connection bits.
    private static final Set<String> CONNECTABLES = Arrays.stream(BlockStateUpdater_1_26_50.CONNECTABLES)
            .filter(name -> !name.equals("trip_wire") && !name.startsWith("hard_"))
            .map(name -> "minecraft:" + name)
            .collect(Collectors.toUnmodifiableSet());

    public static boolean requiresConnectionStateRefresh(NbtMap state) {
        String name = state.getString("name");
        if (STAIRS.contains(name)) {
            return !state.getCompound("states").containsKey("minecraft:corner");
        }
        if (!CONNECTABLES.contains(name)) {
            return false;
        }
        NbtMap states = state.getCompound("states");
        return !states.containsKey("minecraft:connection_north")
                || !states.containsKey("minecraft:connection_east")
                || !states.containsKey("minecraft:connection_south")
                || !states.containsKey("minecraft:connection_west");
    }

    @Override
    public void registerUpdaters(CompoundTagUpdaterContext ctx) {
        ctx.addUpdater(STATE_MAYOR_VERSION, STATE_MINOR_VERSION, STATE_PATCH_VERSION, true)
                .match("name", "minecraft:water")
                .visit("states")
                .tryAdd("liquid_depth", (int) 0);

        ctx.addUpdater(STATE_MAYOR_VERSION, STATE_MINOR_VERSION, STATE_PATCH_VERSION, true)
                .match("name", "minecraft:polished_blackstone_double_slab")
                .visit("states")
                .tryAdd("top_slot_bit", (byte) 0);

        this.replaceState(ctx, "minecraft:wood", "pillar_axis", "y");
    }

    private void replaceState(CompoundTagUpdaterContext ctx, String identifier, String propertyName, Object value) {
        ctx.addUpdater(STATE_MAYOR_VERSION, STATE_MINOR_VERSION, STATE_PATCH_VERSION, true)
                .match("name", identifier)
                .visit("states")
                .edit(propertyName, helper -> helper.replaceWith(propertyName, value));
    }

}
