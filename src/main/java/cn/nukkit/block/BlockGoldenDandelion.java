package cn.nukkit.block;

import cn.nukkit.item.Item;

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
public class BlockGoldenDandelion extends BlockFlower {

    public BlockGoldenDandelion() {
        this(0);
    }

    public BlockGoldenDandelion(int meta) {
        super(0);
    }

    @Override
    public String getName() {
        return "Golden Dandelion";
    }

    @Override
    public int getId() {
        return GOLDEN_DANDELION;
    }

    @Override
    public boolean canBeActivated() {
        return false;
    }

    @Override
    public boolean onActivate(Item item) {
        return false;
    }
}
