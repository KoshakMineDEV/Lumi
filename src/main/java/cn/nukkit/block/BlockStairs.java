package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.block.properties.enums.Corner;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.block.data.Faceable;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public abstract class BlockStairs extends BlockSolidMeta implements Faceable, BlockPropertiesHelper {

    private static final BlockProperties PROPERTIES = new BlockProperties(VanillaProperties.CORNER, VanillaProperties.UPSIDE_DOWN_BIT, VanillaProperties.WEIRDO_DIRECTION);

    protected BlockStairs(int meta) {
        super(meta);
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        if ((this.getDamage() & 0x04) > 0) {
            return new SimpleAxisAlignedBB(
                    this.x,
                    this.y + 0.5,
                    this.z,
                    this.x + 1,
                    this.y + 1,
                    this.z + 1
            );
        } else {
            return new SimpleAxisAlignedBB(
                    this.x,
                    this.y,
                    this.z,
                    this.x + 1,
                    this.y + 0.5,
                    this.z + 1
            );
        }
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (player != null) {
            setBlockFace(player.getDirection());
        }

        if ((fy > 0.5 && face != BlockFace.UP) || face == BlockFace.DOWN) {
            setUpsideDown(true);
        }
        autoConfigureState();
        this.getLevel().setBlock(block, this, true, true);

        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (autoConfigureState()) {
                level.setBlock(this, this, true);
            }
            return type;
        }
        return super.onUpdate(type);
    }

    public boolean autoConfigureState() {
        final int previous = this.getDamage();
        setPropertyValue(VanillaProperties.CORNER, computeCorner());
        return this.getDamage() != previous;
    }

    private Corner computeCorner() {
        final BlockFace facing = getBlockFace();

        final BlockFace ahead = adjoiningStairsFacing(facing);
        if (ahead != null && canTakeShape(ahead.getOpposite())) {
            return ahead == facing.rotateYCCW() ? Corner.OUTER_LEFT : Corner.OUTER_RIGHT;
        }

        final BlockFace behind = adjoiningStairsFacing(facing.getOpposite());
        if (behind != null && canTakeShape(behind)) {
            return behind == facing.rotateYCCW() ? Corner.INNER_LEFT : Corner.INNER_RIGHT;
        }

        return Corner.NONE;
    }

    private BlockStairs neighbourStairs(BlockFace side) {
        return getSideAtLayer(0, side) instanceof BlockStairs neighbour ? neighbour : null;
    }

    private BlockFace adjoiningStairsFacing(BlockFace side) {
        final BlockStairs neighbour = neighbourStairs(side);
        if (neighbour == null || neighbour.isUpsideDown() != isUpsideDown()) {
            return null;
        }
        final BlockFace neighbourFacing = neighbour.getBlockFace();
        return neighbourFacing.getAxis() == getBlockFace().getAxis() ? null : neighbourFacing;
    }

    private boolean canTakeShape(BlockFace side) {
        final BlockStairs neighbour = neighbourStairs(side);
        return neighbour == null
                || neighbour.getBlockFace() != getBlockFace()
                || neighbour.isUpsideDown() != isUpsideDown();
    }

    @Override
    public Item toItem() {
        Item item = super.toItem();
        item.setDamage(0);
        return item;
    }

    @Override
    public boolean collidesWithBB(AxisAlignedBB bb) {
        int damage = this.getDamage();
        int side = damage & 0x03;
        double f = 0;
        double f1 = 0.5;
        double f2 = 0.5;
        double f3 = 1;
        if ((damage & 0x04) > 0) {
            f = 0.5;
            f1 = 1;
            f2 = 0;
            f3 = 0.5;
        }

        if (bb.intersectsWith(new SimpleAxisAlignedBB(
                this.x,
                this.y + f,
                this.z,
                this.x + 1,
                this.y + f1,
                this.z + 1
        ))) {
            return true;
        }


        if (side == 0) {
            return bb.intersectsWith(new SimpleAxisAlignedBB(
                    this.x + 0.5,
                    this.y + f2,
                    this.z,
                    this.x + 1,
                    this.y + f3,
                    this.z + 1
            ));
        } else if (side == 1) {
            return bb.intersectsWith(new SimpleAxisAlignedBB(
                    this.x,
                    this.y + f2,
                    this.z,
                    this.x + 0.5,
                    this.y + f3,
                    this.z + 1
            ));
        } else if (side == 2) {
            return bb.intersectsWith(new SimpleAxisAlignedBB(
                    this.x,
                    this.y + f2,
                    this.z + 0.5,
                    this.x + 1,
                    this.y + f3,
                    this.z + 1
            ));
        } else if (side == 3) {
            return bb.intersectsWith(new SimpleAxisAlignedBB(
                    this.x,
                    this.y + f2,
                    this.z,
                    this.x + 1,
                    this.y + f3,
                    this.z + 0.5
            ));
        }

        return false;
    }


    public void setUpsideDown(boolean upsideDown) {
        setPropertyValue(VanillaProperties.UPSIDE_DOWN_BIT, upsideDown);
    }

    public boolean isUpsideDown() {
        return getPropertyValue(VanillaProperties.UPSIDE_DOWN_BIT);
    }

    @Override
    public BlockFace getBlockFace() {
        return getPropertyValue(VanillaProperties.WEIRDO_DIRECTION);
    }

    @Override
    public void setBlockFace(BlockFace face) {
        this.setPropertyValue(VanillaProperties.WEIRDO_DIRECTION, face);
    }
}
