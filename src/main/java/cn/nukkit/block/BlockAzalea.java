package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.material.tags.BlockTags;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBoneMeal;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.generator.object.tree.ObjectAzaleaTree;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public class BlockAzalea extends BlockTransparent {

    public BlockAzalea() {
        // Does nothing
    }

    @Override
    public String getName() {
        return "Azalea";
    }

    @Override
    public int getId() {
        return AZALEA;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (item instanceof ItemBoneMeal) {
            if (player != null && !player.isCreative()) {
                item.count--;
            }

            this.level.addParticle(new BoneMealParticle(this));
            if (ThreadLocalRandom.current().nextInt(4) == 0) {
                this.grow(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!this.canPlaceOn(down(), down())) {
                this.getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) { //Growth
            if (Utils.rand(1, 7) == 1) {
                this.grow(false);
            } else {
                return Level.BLOCK_UPDATE_RANDOM;
            }
        }
        return 1;
    }

    @Override
    public boolean canPlaceOn(Block floor, Position pos) {
        return floor.hasBlockTag(BlockTags.DIRT);
    }

    public void grow(boolean flowering) {
        ObjectAzaleaTree generator = new ObjectAzaleaTree();
        generator.grow(this.getLevel(), new NukkitRandom(), this, flowering);
        this.level.setBlock(this, Block.get(LOG));
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.FLOW_INTO_BLOCK;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean canBeClimbed() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeFlowedInto() {
        return true;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public double getHardness() {
        return 0;
    }

    @Override
    public double getResistance() {
        return 0;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        return null;
    }

    @Override
    public Item[] getDrops(Item item) {
        return Item.EMPTY_ARRAY;
    }
}
