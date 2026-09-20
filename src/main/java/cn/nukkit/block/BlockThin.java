package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.BooleanBlockProperty;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.level.LevelException;

/**
 * Created on 2015/12/6 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
public abstract class BlockThin extends BlockTransparentMeta implements BlockPropertiesHelper {

    private static final BlockProperties PROPERTIES = new BlockProperties(VanillaProperties.CONNECTION_WEST, VanillaProperties.CONNECTION_SOUTH, VanillaProperties.CONNECTION_NORTH, VanillaProperties.CONNECTION_EAST);

    protected BlockThin() {
        this(0);
    }

    protected BlockThin(int meta) {
        super(meta);
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        double f = 0.4375;
        double f1 = 0.5625;
        double f2 = 0.4375;
        double f3 = 0.5625;
        try {
            boolean flag = this.canConnect(this.north());
            boolean flag1 = this.canConnect(this.south());
            boolean flag2 = this.canConnect(this.west());
            boolean flag3 = this.canConnect(this.east());
            if ((!flag2 || !flag3) && (flag2 || flag3 || flag || flag1)) {
                if (flag2) {
                    f = 0;
                } else if (flag3) {
                    f1 = 1;
                }
            } else {
                f = 0;
                f1 = 1;
            }
            if ((!flag || !flag1) && (flag2 || flag3 || flag || flag1)) {
                if (flag) {
                    f2 = 0;
                } else if (flag1) {
                    f3 = 1;
                }
            } else {
                f2 = 0;
                f3 = 1;
            }
        } catch (LevelException ignore) {}
        return new SimpleAxisAlignedBB(
                this.x + f,
                this.y,
                this.z + f2,
                this.x + f1,
                this.y + 1,
                this.z + f3
        );
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0, 1);
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
        return block.isSolid() || block.hasBlockTag(BlockInternalTags.THIN_BLOCK);
    }
}
