package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;

public class BlockBush extends BlockFlowable {
    public BlockBush() {
        this(0);
    }

    public BlockBush(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Bush";
    }

    @Override
    public int getId() {
        return BUSH;
    }

    @Override
    public boolean canBeReplaced() {
        return true;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (BlockSweetBerryBush.isSupportValid(down())) {
            this.getLevel().setBlock(block, this, true);
            return true;
        }
        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!BlockSweetBerryBush.isSupportValid(down())) {
                this.getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
        }

        return 0;
    }

    @Override
    public Item[] getDrops(Item item) {
        if(item.isShears()) {
            return new Item[]{new ItemBlock(this)};
        }

        return Item.EMPTY_ARRAY;
    }
}
