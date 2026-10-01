package cn.nukkit.block;

import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.object.tree.ObjectPoplarTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;

public class BlockPoplarSapling extends BlockSapling {

    public BlockPoplarSapling() {
        this(0);
    }

    public BlockPoplarSapling(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_SAPLING;
    }

    @Override
    public String getName() {
        return "Poplar Sapling";
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isSupportInvalid()) {
                getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (getLevel().getFullLight(add(0, 1, 0)) >= BlockCrops.MINIMUM_LIGHT_LEVEL) {
                if (isAged()) {
                    grow();
                } else {
                    setAged(true);
                    getLevel().setBlock(this, this, true);
                }
            }
            return Level.BLOCK_UPDATE_RANDOM;
        }
        return Level.BLOCK_UPDATE_NORMAL;
    }

    @Override
    public boolean grow() {
        BlockGrowEvent event = new BlockGrowEvent(this, Block.get(POPLAR_LOG));
        if (!event.call()) {
            return false;
        }

        NukkitRandom random = new NukkitRandom();
        int[] leaves = {RED_POPLAR_LEAVES, ORANGE_POPLAR_LEAVES, YELLOW_POPLAR_LEAVES};
        ObjectPoplarTree tree = new ObjectPoplarTree(leaves[random.nextBoundedInt(leaves.length)]);
        int meta = getDamage();
        getLevel().setBlock(this, Block.get(AIR), true, false);
        if (tree.generate(getLevel(), random, this)) {
            return true;
        }
        getLevel().setBlock(this, Block.get(POPLAR_SAPLING, meta), true, false);
        return false;
    }

    @Override
    public boolean isSameType(Vector3 pos, int type) {
        return level.getBlock(pos).getId() == POPLAR_SAPLING;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(POPLAR_SAPLING));
    }
}
