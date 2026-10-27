package cn.nukkit.level.format.leveldb.updater;

import org.cloudburstmc.blockstateupdater.BlockStateUpdater;
import org.cloudburstmc.blockstateupdater.util.tagupdater.CompoundTagUpdaterContext;

/** Adds the properties introduced by protocol 2225; the chunk migration resolves their connections. */
public final class BlockStateUpdater_1_26_60 implements BlockStateUpdater {
    public static final BlockStateUpdater INSTANCE = new BlockStateUpdater_1_26_60();

    private BlockStateUpdater_1_26_60() {
    }

    @Override
    public void registerUpdaters(CompoundTagUpdaterContext ctx) {
        for (String name : new String[]{"chorus_plant", "fire"}) {
            for (String side : new String[]{"west", "south", "north", "east", "up", "down"}) {
                if (name.equals("fire") && side.equals("down")) {
                    continue;
                }
                ctx.addUpdater(1, 26, 60, true)
                        .match("name", "minecraft:" + name)
                        .visit("states")
                        .tryAdd("minecraft:connection_" + side, (byte) 0);
            }
        }
        for (String side : new String[]{"west", "south", "north", "east"}) {
            ctx.addUpdater(1, 26, 60, true)
                    .match("name", "minecraft:redstone_wire")
                    .visit("states")
                    .tryAdd("redstone_" + side, "none");
        }
    }
}
