package cn.nukkit.level.format.leveldb;

import org.cloudburstmc.blockstateupdater.BlockStateUpdater_1_26_50;
import org.cloudburstmc.nbt.NbtMap;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cloudburstmc.blockstateupdater.util.tagupdater.CompoundTagUpdaterContext.makeVersion;

/** 
 * Detects saved block layouts that need their neighbour-dependent states rebuilt. 
*/
public final class ConnectionStateMigration {
    public static final int REVISION = 3;
    private static final int CONNECTION_LAYOUT_VERSION = makeVersion(1, 26, 50);
    private static final int EXTENDED_LAYOUT_VERSION = makeVersion(1, 26, 60);
    private static final Set<String> STAIRS = Arrays.stream(BlockStateUpdater_1_26_50.STAIRS)
            .map(name -> "minecraft:" + name).collect(Collectors.toUnmodifiableSet());
    private static final Set<String> CONNECTABLES = Arrays.stream(BlockStateUpdater_1_26_50.CONNECTABLES)
            // Hard glass panes still use the legacy Lumi layout.
            .filter(name -> !name.startsWith("hard_"))
            .map(name -> "minecraft:" + name).collect(Collectors.toUnmodifiableSet());
    private static final Set<String> EXTENDED_CONNECTABLES = Set.of(
            "minecraft:chorus_plant", "minecraft:fire", "minecraft:redstone_wire");

    private ConnectionStateMigration() {
    }

    // Preserve unfinished work even if an earlier save already upgraded its NBT palettes.
    public static boolean persistedPending(byte[] marker) {
        return marker != null && marker.length == 1 && Byte.toUnsignedInt(marker[0]) < REVISION;
    }

    public static boolean detectLegacyPalette(byte[] marker) {
        return marker == null || marker.length != 1;
    }

    // Inspect the original tag before the updater supplies missing properties.
    public static boolean requiresLegacyRefresh(NbtMap originalState) {
        if (!(originalState.get("version") instanceof Integer version) || version < 0) {
            return false;
        }
        String name = originalState.getString("name");
        NbtMap states = originalState.getCompound("states");
        if (version < EXTENDED_LAYOUT_VERSION && EXTENDED_CONNECTABLES.contains(name)) {
            if (name.equals("minecraft:redstone_wire")) {
                return !states.containsKey("redstone_west") || !states.containsKey("redstone_south")
                        || !states.containsKey("redstone_north") || !states.containsKey("redstone_east");
            }
            return missingHorizontalConnections(states) || !states.containsKey("minecraft:connection_up")
                    || name.equals("minecraft:chorus_plant") && !states.containsKey("minecraft:connection_down");
        }
        if (version >= CONNECTION_LAYOUT_VERSION) {
            return false;
        }
        if (STAIRS.contains(name)) {
            return !states.containsKey("minecraft:corner");
        }
        return CONNECTABLES.contains(name) && missingHorizontalConnections(states);
    }

    private static boolean missingHorizontalConnections(NbtMap states) {
        return !states.containsKey("minecraft:connection_north") || !states.containsKey("minecraft:connection_east")
                || !states.containsKey("minecraft:connection_south") || !states.containsKey("minecraft:connection_west");
    }

    public static boolean requiresLegacyNumericRefresh(String name) {
        return STAIRS.contains(name) || CONNECTABLES.contains(name) || EXTENDED_CONNECTABLES.contains(name);
    }
}
