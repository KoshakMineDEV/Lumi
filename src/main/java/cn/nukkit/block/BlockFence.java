package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.BooleanBlockProperty;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.block.data.BlockColor;

public abstract class BlockFence extends BlockTransparentMeta implements BlockPropertiesHelper {

    private static final BlockProperties PROPERTIES = new BlockProperties(VanillaProperties.CONNECTION_WEST, VanillaProperties.CONNECTION_SOUTH, VanillaProperties.CONNECTION_NORTH, VanillaProperties.CONNECTION_EAST);

    protected BlockFence() {
        this(0);
    }

    protected BlockFence(int meta) {
        super(meta);
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public double getHardness() {
        return 2;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public double getResistance() {
        return 15;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        boolean north = this.canConnect(this.north());
        boolean south = this.canConnect(this.south());
        boolean west = this.canConnect(this.west());
        boolean east = this.canConnect(this.east());
        double n = north ? 0 : 0.375;
        double s = south ? 1 : 0.625;
        double w = west ? 0 : 0.375;
        double e = east ? 1 : 0.625;
        return new SimpleAxisAlignedBB(
                this.x + w,
                this.y,
                this.z + n,
                this.x + e,
                this.y + 1.5,
                this.z + s
        );
    }

    @Override
    public int getBurnChance() {
        return 5;
    }

    @Override
    public int getBurnAbility() {
        return 20;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        this.updateConnections();
        return super.place(item, block, target, face, fx, fy, fz, player);
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (this.updateConnections()) {
                this.getLevel().setBlock(this, this, true);
            }
            return type;
        }
        return 0;
    }

    public boolean updateConnections() {
        int previous = this.getDamage();

        for (BlockFace face : BlockFace.Plane.HORIZONTAL) {
            this.setPropertyValue(getPropertyFromBlockFace(face), this.canConnect(this.getSide(face)));
        }

        return this.getDamage() != previous;
    }

    private BooleanBlockProperty getPropertyFromBlockFace(BlockFace face) {
        return switch (face) {
            case NORTH -> VanillaProperties.CONNECTION_NORTH;
            case SOUTH -> VanillaProperties.CONNECTION_SOUTH;
            case WEST -> VanillaProperties.CONNECTION_WEST;
            case EAST -> VanillaProperties.CONNECTION_EAST;
            default -> throw new IllegalArgumentException("Invalid face " + face);
        };
    }

    public boolean canConnect(Block block) {
        return (block instanceof BlockFence || block instanceof BlockFenceGate) || block.isSolid() && !block.isTransparent();
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0);
    }
}
