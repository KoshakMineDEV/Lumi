package cn.nukkit.block;

import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.object.tree.ObjectPaleOakTree;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;

public class BlockPaleOakSapling extends BlockSapling {

    public BlockPaleOakSapling() {
        this(0);
    }

    public BlockPaleOakSapling(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PALE_OAK_SAPLING;
    }

    @Override
    public String getName() {
        return "Pale Oak Sapling";
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isSupportInvalid()) {
                this.getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
            if (getLevel().getFullLight(add(0, 1, 0)) >= BlockCrops.MINIMUM_LIGHT_LEVEL) {
                if (isAged()) {
                    this.grow();
                } else {
                    setAged(true);
                    this.getLevel().setBlock(this, this, true);
                    return Level.BLOCK_UPDATE_RANDOM;
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM;
            }
        }
        return Level.BLOCK_UPDATE_NORMAL;
    }

    @Override
    public boolean grow() {
        BlockGrowEvent event = new BlockGrowEvent(this, Block.get(BlockID.PALE_OAK_LOG));
        if(event.call()) {
            for (int x = 0; x >= -1; --x) {
                for (int z = 0; z >= -1; --z) {
                    if (this.findSaplings(x, z, 0)) {
                        Block air = Block.get(BlockID.AIR);
                        this.level.setBlock(this.add(x, 0, z), air, true, false);
                        this.level.setBlock(this.add(x + 1, 0, z), air, true, false);
                        this.level.setBlock(this.add(x, 0, z + 1), air, true, false);
                        this.level.setBlock(this.add(x + 1, 0, z + 1), air, true, false);
                        new ObjectPaleOakTree().generate(this.getLevel(), new NukkitRandom(), this.add(x, 0, z));
                    }
                }
            }

            return true;
        }

        return false;
    }

    private boolean findSaplings(int x, int z, int type) {
        return this.isSameType(this.add(x, 0, z), type) && this.isSameType(this.add(x + 1, 0, z), type) && this.isSameType(this.add(x, 0, z + 1), type) && this.isSameType(this.add(x + 1, 0, z + 1), type);
    }

    @Override
    public boolean isSameType(Vector3 pos, int type) {
        Block block = this.level.getBlock(pos);
        return block.getId() == PALE_OAK_SAPLING;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(PALE_OAK_SAPLING));
    }
}
