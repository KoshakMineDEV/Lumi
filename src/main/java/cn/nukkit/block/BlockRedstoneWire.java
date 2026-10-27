package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.data.BlockColor;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.block.properties.enums.RedstoneConnection;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemRedstone;
import cn.nukkit.level.Level;
import cn.nukkit.level.redstone.WireUpdateHandler;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;

/**
 * Redstone dust with network power propagation inspired by Alternate Current.
 * WireUpdateHandler applies its HORIZONTAL_FIRST_OUTWARD notification
 *
 * @author Angelic47 (Nukkit Project)
 * @see WireUpdateHandler
 */
public class BlockRedstoneWire extends BlockFlowable implements BlockPropertiesHelper {
    private static final BlockFace[] HORIZONTAL = {BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH};
    private static final BlockProperties PROPERTIES = new BlockProperties(
            VanillaProperties.REDSTONE_SIGNAL, VanillaProperties.REDSTONE_WEST,
            VanillaProperties.REDSTONE_SOUTH, VanillaProperties.REDSTONE_NORTH, VanillaProperties.REDSTONE_EAST);

    public BlockRedstoneWire() {
        this(0);
    }

    public BlockRedstoneWire(int meta) {
        super(meta);
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    public int getSignal() {
        return this.getIntValue(VanillaProperties.REDSTONE_SIGNAL);
    }

    public void setSignal(int signal) {
        this.setIntValue(VanillaProperties.REDSTONE_SIGNAL, signal);
    }

    public boolean updateConnections() {
        int previous = this.getDamage();
        boolean openAbove = !this.up().isNormalBlock();
        this.setDamage(connectionMeta(previous, connection(BlockFace.NORTH, openAbove),
                connection(BlockFace.SOUTH, openAbove), connection(BlockFace.WEST, openAbove),
                connection(BlockFace.EAST, openAbove)));
        return previous != this.getDamage();
    }

    public static int connectionMeta(int meta, RedstoneConnection north, RedstoneConnection south,
                                     RedstoneConnection west, RedstoneConnection east) {
        boolean northSouth = north != RedstoneConnection.NONE || south != RedstoneConnection.NONE;
        boolean eastWest = east != RedstoneConnection.NONE || west != RedstoneConnection.NONE;
        meta = PROPERTIES.setValue(meta, VanillaProperties.REDSTONE_NORTH.getName(),
                north == RedstoneConnection.NONE && !eastWest ? RedstoneConnection.SIDE : north);
        meta = PROPERTIES.setValue(meta, VanillaProperties.REDSTONE_SOUTH.getName(),
                south == RedstoneConnection.NONE && !eastWest ? RedstoneConnection.SIDE : south);
        meta = PROPERTIES.setValue(meta, VanillaProperties.REDSTONE_WEST.getName(),
                west == RedstoneConnection.NONE && !northSouth ? RedstoneConnection.SIDE : west);
        return PROPERTIES.setValue(meta, VanillaProperties.REDSTONE_EAST.getName(),
                east == RedstoneConnection.NONE && !northSouth ? RedstoneConnection.SIDE : east);
    }

    private RedstoneConnection connection(BlockFace face, boolean openAbove) {
        Block neighbor = this.getSide(face);
        if (openAbove && neighbor.isNormalBlock() && canConnectUpwardsTo(neighbor.up())) {
            return RedstoneConnection.UP;
        }
        if (canConnectTo(neighbor, face) || !neighbor.isNormalBlock() && canConnectUpwardsTo(neighbor.down())) {
            return RedstoneConnection.SIDE;
        }
        return RedstoneConnection.NONE;
    }

    @Override
    public String getName() {
        return "Redstone Wire";
    }

    @Override
    public int getId() {
        return REDSTONE_WIRE;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (!canBePlacedOn(block.down())) {
            return false;
        }
        this.updateConnections();
        if (!this.level.setBlock(block, this, true, false)) {
            return false;
        }
        this.level.getWireUpdateHandler().added(this);
        return true;
    }

    @Override
    public boolean onBreak(Item item) {
        if (!this.level.setBlock(this, Block.get(AIR), true, true)) {
            return false;
        }
        this.level.getWireUpdateHandler().removed(this);
        return true;
    }

    @Override
    public Item toItem() {
        return new ItemRedstone();
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.AIR_BLOCK_COLOR;
    }

    @Override
    public int onUpdate(int type) {
        if (type != Level.BLOCK_UPDATE_NORMAL && type != Level.BLOCK_UPDATE_REDSTONE) {
            return 0;
        }
        if (type == Level.BLOCK_UPDATE_NORMAL && !canBePlacedOn(this.down())) {
            this.level.useBreakOn(this, null, null, true);
            return Level.BLOCK_UPDATE_NORMAL;
        }
        this.level.getWireUpdateHandler().update(this, type == Level.BLOCK_UPDATE_NORMAL);
        return Level.BLOCK_UPDATE_REDSTONE;
    }

    public boolean canBePlacedOn(Vector3 pos) {
        return canBePlacedOn(this.level.getBlock(pos));
    }

    public boolean canBePlacedOn(Block support) {
        return support.isSolid(BlockFace.UP);
    }

    @Override
    public int getStrongPower(BlockFace side) {
        return getWeakPower(side);
    }

    @Override
    public int getWeakPower(BlockFace side) {
        int power = getSignal();
        if (power == 0 || side == BlockFace.DOWN) {
            return 0;
        }
        if (side == BlockFace.UP) {
            return power;
        }
        int connections = 0;
        boolean openAbove = !blockAt(this.getFloorX(), this.getFloorY() + 1, this.getFloorZ()).isNormalBlock();
        for (int direction = 0; direction < HORIZONTAL.length; direction++) {
            if (isPowerSourceAt(HORIZONTAL[direction], openAbove)) {
                connections |= 1 << direction;
            }
        }
        if (connections == 0) {
            return power;
        }
        int direction = switch (side) {
            case WEST -> 0;
            case NORTH -> 1;
            case EAST -> 2;
            case SOUTH -> 3;
            default -> throw new IllegalArgumentException("Expected a horizontal face: " + side);
        };
        int perpendicular = (1 << ((direction + 1) & 3)) | (1 << ((direction + 3) & 3));
        return (connections & (1 << direction)) != 0 && (connections & perpendicular) == 0 ? power : 0;
    }

    private Block blockAt(int x, int y, int z) {
        return this.level.getWireUpdateHandler().getBlock(x, y, z);
    }

    private boolean isPowerSourceAt(BlockFace side, boolean openAbove) {
        int x = this.getFloorX() + side.getXOffset();
        int y = this.getFloorY();
        int z = this.getFloorZ() + side.getZOffset();
        Block neighbor = blockAt(x, y, z);
        return openAbove && neighbor.isNormalBlock() && canConnectUpwardsTo(blockAt(x, y + 1, z))
                || canConnectTo(neighbor, side)
                || !neighbor.isNormalBlock() && canConnectUpwardsTo(blockAt(x, y - 1, z));
    }

    protected static boolean canConnectUpwardsTo(Level level, Vector3 pos) {
        return canConnectUpwardsTo(level.getBlock(pos));
    }

    protected static boolean canConnectUpwardsTo(Block block) {
        return canConnectTo(block, null);
    }

    protected static boolean canConnectTo(Block block, BlockFace side) {
        if (block.getId() == REDSTONE_WIRE) {
            return true;
        }
        if (BlockRedstoneDiode.isDiode(block)) {
            BlockFace face = ((BlockRedstoneDiode) block).getFacing();
            return face == side || face.getOpposite() == side;
        }
        return block.isPowerSource() && side != null;
    }

    @Override
    public boolean isPowerSource() {
        return true;
    }
}
