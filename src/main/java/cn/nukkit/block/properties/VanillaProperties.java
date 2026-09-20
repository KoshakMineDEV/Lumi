package cn.nukkit.block.properties;

import cn.nukkit.block.customblock.properties.BlockProperty;
import cn.nukkit.block.customblock.properties.BooleanBlockProperty;
import cn.nukkit.block.customblock.properties.EnumBlockProperty;
import cn.nukkit.block.properties.enums.Corner;
import cn.nukkit.block.properties.enums.CrackedState;
import cn.nukkit.block.properties.enums.TurtleEggCount;
import cn.nukkit.math.BlockFace;

public interface VanillaProperties {

    BooleanBlockProperty UPPER_BLOCK = new BooleanBlockProperty("upper_block_bit", false);

    BooleanBlockProperty UPSIDE_DOWN_BIT = new BooleanBlockProperty("upside_down_bit", false);

    BooleanBlockProperty CONNECTION_WEST = new BooleanBlockProperty("minecraft:connection_west", false);

    BooleanBlockProperty CONNECTION_SOUTH = new BooleanBlockProperty("minecraft:connection_south", false);

    BooleanBlockProperty CONNECTION_NORTH = new BooleanBlockProperty("minecraft:connection_north", false);

    BooleanBlockProperty CONNECTION_EAST = new BooleanBlockProperty("minecraft:connection_east", false);

    BlockProperty<BlockFace> DIRECTION = new EnumBlockProperty<>("direction", false,
            new BlockFace[]{ BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST }).ordinal(true);

    BlockProperty<BlockFace> CARDINAL_DIRECTION = new EnumBlockProperty<>("minecraft:cardinal_direction", false,
            new BlockFace[]{ BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST }).ordinal(true);

    BlockProperty<BlockFace> FACING_DIRECTION = new EnumBlockProperty<>("facing_direction", false,
            new BlockFace[] { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST }).ordinal(true);

    BlockProperty<BlockFace> WEIRDO_DIRECTION = new EnumBlockProperty<>("weirdo_direction", false,
            new BlockFace[]{ BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH }).ordinal(true);

    EnumBlockProperty<CrackedState> CRACKED_STATE = new EnumBlockProperty<>("cracked_state", false, CrackedState.class);

    EnumBlockProperty<Corner> CORNER = new EnumBlockProperty<>("minecraft:corner", false, Corner.class);

    EnumBlockProperty<TurtleEggCount> TURTLE_EGG_COUNT = new EnumBlockProperty<>("turtle_egg_count", false, TurtleEggCount.class);
}
