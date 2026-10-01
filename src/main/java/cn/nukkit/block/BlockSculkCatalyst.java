package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.block.data.BlockColor;

public class BlockSculkCatalyst extends BlockSolidMeta {

    public BlockSculkCatalyst() { this(0); }

    public BlockSculkCatalyst(int meta) { super(meta); }

    @Override
    public int getId() {
        return SCULK_CATALYST;
    }

    @Override
    public String getName() {
        return "Sculk Catalyst";
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_HOE;
    }

    @Override
    public double getHardness() {
        return 1;
    }

    @Override
    public double getResistance() {
        return 3;
    }

    @Override
    public int getLightLevel() {
        return 6;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BLACK_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.hasEnchantment(Enchantment.ID_SILK_TOUCH)) {
            return new Item[]{
                    this.toItem()
            };
        }
        return Item.EMPTY_ARRAY;
    }

    @Override
    public int getDropExp(Item item) {
        if(item.hasEnchantment(Enchantment.ID_SILK_TOUCH)) return 0;
        return 5;
    }

    public boolean isBlooming() {
        return false;
    }

    public void setBlooming(boolean blooming) {

    }
}
