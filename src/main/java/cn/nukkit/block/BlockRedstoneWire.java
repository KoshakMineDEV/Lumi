package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.block.properties.enums.RedstoneConnection;
import cn.nukkit.event.block.BlockRedstoneEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemRedstone;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockFace.Plane;
import cn.nukkit.math.Vector3;
import cn.nukkit.block.data.BlockColor;

import java.util.EnumSet;

/**
 * @author Angelic47
 * Nukkit Project
 */
public class BlockRedstoneWire extends BlockFlowable implements BlockPropertiesHelper {

    private static final BlockProperties PROPERTIES = new BlockProperties(
            VanillaProperties.REDSTONE_SIGNAL, VanillaProperties.REDSTONE_WEST,
            VanillaProperties.REDSTONE_SOUTH, VanillaProperties.REDSTONE_NORTH, VanillaProperties.REDSTONE_EAST);

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
        RedstoneConnection north = connection(BlockFace.NORTH);
        RedstoneConnection south = connection(BlockFace.SOUTH);
        RedstoneConnection west = connection(BlockFace.WEST);
        RedstoneConnection east = connection(BlockFace.EAST);
        boolean northSouth = north != RedstoneConnection.NONE || south != RedstoneConnection.NONE;
        boolean eastWest = east != RedstoneConnection.NONE || west != RedstoneConnection.NONE;
        if (!eastWest) {
            if (north == RedstoneConnection.NONE) north = RedstoneConnection.SIDE;
            if (south == RedstoneConnection.NONE) south = RedstoneConnection.SIDE;
        }
        if (!northSouth) {
            if (west == RedstoneConnection.NONE) west = RedstoneConnection.SIDE;
            if (east == RedstoneConnection.NONE) east = RedstoneConnection.SIDE;
        }
        this.setPropertyValue(VanillaProperties.REDSTONE_NORTH, north);
        this.setPropertyValue(VanillaProperties.REDSTONE_SOUTH, south);
        this.setPropertyValue(VanillaProperties.REDSTONE_WEST, west);
        this.setPropertyValue(VanillaProperties.REDSTONE_EAST, east);
        return previous != this.getDamage();
    }

    private RedstoneConnection connection(BlockFace face) {
        Block neighbor = this.getSide(face);
        boolean normal = neighbor.isNormalBlock();
        if (!this.up().isNormalBlock() && normal && canConnectUpwardsTo(neighbor.up())) {
            return RedstoneConnection.UP;
        }
        if (canConnectTo(neighbor, face) || !normal && canConnectUpwardsTo(neighbor.down())) {
            return RedstoneConnection.SIDE;
        }
        return RedstoneConnection.NONE;
    }

    private boolean canProvidePower = true;

    public BlockRedstoneWire() {
        this(0);
    }

    public BlockRedstoneWire(int meta) {
        super(meta);
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
        this.getLevel().setBlock(block, this, true, false);
        this.updateNeighbourConnections();
        this.calculateCurrentChanges(true, true);
        Vector3 pos = getLocation();

        for (BlockFace blockFace : Plane.VERTICAL) {
            this.level.updateAroundRedstone(pos.getSide(blockFace), blockFace.getOpposite());
        }

        for (BlockFace blockFace : Plane.VERTICAL) {
            this.updateAround(pos.getSide(blockFace), blockFace.getOpposite());
        }

        for (BlockFace blockFace : Plane.HORIZONTAL) {
            Vector3 v = pos.getSide(blockFace);

            if (this.level.getBlock(v).isNormalBlock()) {
                this.updateAround(v.up(), BlockFace.DOWN);
            } else {
                this.updateAround(v.down(), BlockFace.UP);
            }
        }
        return true;
    }

    private void updateNeighbourConnections() {
        for (BlockFace face : Plane.HORIZONTAL) {
            Block neighbor = this.getSide(face);
            this.updateNeighbourConnection(neighbor);
            this.updateNeighbourConnection(neighbor.isNormalBlock() ? neighbor.up() : neighbor.down());
        }
    }

    private void updateNeighbourConnection(Block neighbor) {
        if (neighbor instanceof BlockRedstoneWire wire && wire.updateConnections()) {
            this.level.setBlock(wire, wire, true, false);
        }
    }

    private void updateAround(Vector3 pos, BlockFace face) {
        if (this.level.getBlock(pos).getId() == Block.REDSTONE_WIRE) {
            this.level.updateAroundRedstone(pos, face);

            for (BlockFace side : BlockFace.values()) {
                this.level.updateAroundRedstone(pos.getSide(side), side.getOpposite());
            }
        }
    }

    private void calculateCurrentChanges(boolean force, boolean stillExists) {
        Vector3 pos = this.getLocation();

        int meta = this.getSignal();
        int maxStrength = meta;
        this.canProvidePower = false;
        int power = this.getIndirectPower();

        this.canProvidePower = true;

        if (power > 0 && power > maxStrength - 1) {
            maxStrength = power;
        }

        int strength = 0;

        for (BlockFace face : Plane.HORIZONTAL) {
            Vector3 v = pos.getSide(face);

            if (v.getX() == this.getX() && v.getZ() == this.getZ()) {
                continue;
            }


            strength = this.getMaxCurrentStrength(v, strength);

            boolean vNormal = this.level.getBlock(v).isNormalBlock();

            if (vNormal && !this.level.getBlock(pos.up()).isNormalBlock()) {
                strength = this.getMaxCurrentStrength(v.up(), strength);
            } else if (!vNormal) {
                strength = this.getMaxCurrentStrength(v.down(), strength);
            }
        }

        if (strength > maxStrength) {
            maxStrength = strength - 1;
        } else if (maxStrength > 0) {
            --maxStrength;
        } else {
            maxStrength = 0;
        }

        if (power > maxStrength - 1) {
            maxStrength = power;
        } else if (power < maxStrength && strength <= maxStrength) {
            maxStrength = Math.max(power, strength - 1);
        }

        if (meta != maxStrength) {
            if (stillExists) {
                new BlockRedstoneEvent(this, meta, maxStrength).call();

                this.setSignal(maxStrength);
                this.level.setBlock(this, this, false, false);
            }

            this.level.updateAroundRedstone(this, null);
            for (BlockFace face : BlockFace.values()) {
                this.level.updateAroundRedstone(pos.getSide(face), face.getOpposite());
            }
        } else if (force) {
            for (BlockFace face : BlockFace.values()) {
                this.level.updateAroundRedstone(pos.getSide(face), face.getOpposite());
            }
        }
    }

    private int getMaxCurrentStrength(Vector3 pos, int maxStrength) {
        if (this.level.getBlockIdAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ()) != REDSTONE_WIRE) {
            return maxStrength;
        } else {
            int strength = this.level.getBlockDataAt(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ()) & 0xf;
            return Math.max(strength, maxStrength);
        }
    }

    @Override
    public boolean onBreak(Item item) {
        this.getLevel().setBlock(this, Block.get(BlockID.AIR), true, true);
        this.updateNeighbourConnections();

        Vector3 pos = getLocation();

        this.calculateCurrentChanges(false, false);

        for (BlockFace blockFace : BlockFace.values()) {
            this.level.updateAroundRedstone(pos.getSide(blockFace), null);
        }

        for (BlockFace blockFace : Plane.HORIZONTAL) {
            Vector3 v = pos.getSide(blockFace);

            if (this.level.getBlock(v).isNormalBlock()) {
                this.updateAround(v.up(), BlockFace.DOWN);
            } else {
                this.updateAround(v.down(), BlockFace.UP);
            }
        }
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

        if (type == Level.BLOCK_UPDATE_NORMAL && !this.canBePlacedOn(this.down())) {
            this.getLevel().useBreakOn(this, null, null, true);
            return Level.BLOCK_UPDATE_NORMAL;
        }

        if (this.updateConnections()) {
            this.level.setBlock(this, this, true, false);
        }

        // Redstone event
        RedstoneUpdateEvent ev = new RedstoneUpdateEvent(this);
        if (!ev.call()) {
            return 0;
        }

        this.calculateCurrentChanges(false, true);

        return Level.BLOCK_UPDATE_REDSTONE;
    }

    public boolean canBePlacedOn(Vector3 pos) {
        return this.canBePlacedOn(this.level.getBlock(pos));
    }

    public boolean canBePlacedOn(Block support) {
        return support.isSolid(BlockFace.UP);
    }

    @Override
    public int getStrongPower(BlockFace side) {
        return !this.canProvidePower ? 0 : getWeakPower(side);
    }

    @Override
    public int getWeakPower(BlockFace side) {
        if (!this.canProvidePower) {
            return 0;
        } else {
            int power = this.getSignal();

            if (power == 0) {
                return 0;
            } else if (side == BlockFace.UP) {
                return power;
            } else {
                EnumSet<BlockFace> enumset = EnumSet.noneOf(BlockFace.class);

                for (BlockFace face : Plane.HORIZONTAL) {
                    if (this.isPowerSourceAt(face)) {
                        enumset.add(face);
                    }
                }

                if (side.getAxis().isHorizontal() && enumset.isEmpty()) {
                    return power;
                } else if (enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY())) {
                    return power;
                } else {
                    return 0;
                }
            }
        }
    }

    private boolean isPowerSourceAt(BlockFace side) {
        Vector3 pos = getLocation();
        Vector3 v = pos.getSide(side);
        Block block = this.level.getBlock(v);
        boolean flag = block.isNormalBlock();
        boolean flag1 = this.level.getBlock(pos.up()).isNormalBlock();
        return !flag1 && flag && canConnectUpwardsTo(this.level, v.up()) || (canConnectTo(block, side) || !flag && canConnectUpwardsTo(this.level, block.down()));
    }

    protected static boolean canConnectUpwardsTo(Level level, Vector3 pos) {
        return canConnectUpwardsTo(level.getBlock(pos));
    }

    protected static boolean canConnectUpwardsTo(Block block) {
        return canConnectTo(block, null);
    }

    protected static boolean canConnectTo(Block block, BlockFace side) {
        if (block.getId() == Block.REDSTONE_WIRE) {
            return true;
        } else if (BlockRedstoneDiode.isDiode(block)) {
            BlockFace face = ((BlockRedstoneDiode) block).getFacing();
            return face == side || face.getOpposite() == side;
        } else {
            return block.isPowerSource() && side != null;
        }
    }

    @Override
    public boolean isPowerSource() {
        return this.canProvidePower;
    }

    private int getIndirectPower() {
        int power = 0;
        Vector3 pos = getLocation();

        for (BlockFace face : BlockFace.values()) {
            int blockPower = this.getIndirectPower(pos.getSide(face), face);

            if (blockPower >= 15) {
                return 15;
            }

            if (blockPower > power) {
                power = blockPower;
            }
        }

        return power;
    }

    private int getIndirectPower(Vector3 pos, BlockFace face) {
        Block block = this.level.getBlock(pos);
        if (block.getId() == Block.REDSTONE_WIRE) {
            return 0;
        }
        return block.isNormalBlock() ? getStrongPower(pos.getSide(face), face) : block.getWeakPower(face);
    }

    private int getStrongPower(Vector3 pos, BlockFace direction) {
        Block block = this.level.getBlock(pos);

        if (block.getId() == Block.REDSTONE_WIRE) {
            return 0;
        }

        return block.getStrongPower(direction);
    }
}
