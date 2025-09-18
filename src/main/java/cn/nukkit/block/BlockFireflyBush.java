package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;

public class BlockFireflyBush extends BlockFlowable {
    public BlockFireflyBush() {
        this(0);
    }

    public BlockFireflyBush(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Firefly Bush";
    }

    @Override
    public int getId() {
        return FIREFLY_BUSH;
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
    public int getLightLevel() {
        return 2;
    }
}
