package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemCake;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.utils.BlockColor;

/**
 * @author Nukkit Project Team
 */
public class BlockCake extends BlockTransparentMeta {

    public BlockCake(int meta) {
        super(meta);
    }

    public BlockCake() {
        this(0);
    }

    @Override
    public String getName() {
        return "Cake Block";
    }

    @Override
    public int getId() {
        return CAKE_BLOCK;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public double getHardness() {
        return 0.5;
    }

    @Override
    public double getResistance() {
        return 0.5;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public boolean breaksWhenMoved() {
        return true;
    }

    @Override
    public boolean sticksToPiston() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        return new SimpleAxisAlignedBB(
                this.x + ((1 + (this.getDamage() << 1)) >> 4),
                this.y,
                this.z + 0.0625,
                this.x - 0.0625 + 1,
                this.y + 0.5,
                this.z - 0.0625 + 1
        );
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (down().getId() != Block.AIR) {
            getLevel().setBlock(block, this, true, true);

            return true;
        }
        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().getId() == Block.AIR) {
                getLevel().setBlock(this, Block.get(BlockID.AIR), true);

                return Level.BLOCK_UPDATE_NORMAL;
            }
        }

        return 0;
    }

    @Override
    public Item[] getDrops(Item item) {
        return Item.EMPTY_ARRAY;
    }

    @Override
    public Item toItem() {
        return new ItemCake();
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (this.getDamage() == 0 && item.getBlockId() >= BlockID.CANDLE && item.getBlockId() <= BlockID.BLACK_CANDLE) {
            return false;
        }
        if (player != null && player.canEat(false)) {
            if (getDamage() <= 0x06) setDamage(getDamage() + 1);
            if (getDamage() >= 0x06) {
                getLevel().setBlock(this, Block.get(BlockID.AIR), true);
            } else {
                player.getFoodData().addFood(2, 0.4F);
                getLevel().setBlock(this, this, true);
            }
            return true;
        }
        return false;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.AIR_BLOCK_COLOR;
    }

    @Override
    public int getComparatorInputOverride() {
        return (7 - this.getDamage()) << 1;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }
}
