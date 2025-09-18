package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.material.tags.BlockTags;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBoneMeal;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.block.data.BlockColor;
import cn.nukkit.utils.Utils;

/**
 * Created on 2015/11/23 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
public abstract class BlockFlower extends BlockFlowable {

    public BlockFlower() {
        this(0);
    }

    public BlockFlower(int meta) {
        super(meta);
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        Block down = this.down();
        if (down.hasBlockTag(BlockTags.DIRT)) {
            this.getLevel().setBlock(block, this, true);

            return true;
        }
        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!down().hasBlockTag(BlockTags.DIRT)) {
                this.getLevel().useBreakOn(this, null, null, true);

                return Level.BLOCK_UPDATE_NORMAL;
            }
        }

        return 0;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.FOLIAGE_BLOCK_COLOR;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (item instanceof ItemBoneMeal) {
            if (player != null && !player.isCreative()) {
                item.count--;
            }

            this.level.addParticle(new BoneMealParticle(this));

            for (int i = 0; i < 8; i++) {
                Vector3 vec = this.add(
                        Utils.random.nextInt(-3, 4),
                        Utils.random.nextInt(-1, 2),
                        Utils.random.nextInt(-3, 4));

                if (level.getBlock(vec).getId() == AIR && level.getBlock(vec.down()).getId() == GRASS && vec.getY() >= 0 && vec.getY() < 256) {
                    if (Utils.random.nextInt(10) == 0) {
                        this.level.setBlock(vec, this.getUncommonFlower(), true);
                    } else {
                        this.level.setBlock(vec, get(this.getId()), true);
                    }
                }
            }

            return true;
        }

        return false;
    }

    protected Block getUncommonFlower() {
        return get(DANDELION);
    }
}
