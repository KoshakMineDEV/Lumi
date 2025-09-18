package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.material.tags.BlockTags;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBoneMeal;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.block.data.BlockColor;
import cn.nukkit.utils.Utils;

/**
 * @author Angelic47
 * Nukkit Project
 */
public class BlockTallGrass extends BlockFlowable {

    public BlockTallGrass() {
        this(1);
    }

    public BlockTallGrass(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return TALL_GRASS;
    }

    @Override
    public String getName() {
        String[] names = new String[]{
                "Grass",
                "Grass",
                "Fern",
                "Fern"
        };
        return names[this.getDamage() & 0x03];
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean canBeReplaced() {
        return true;
    }

    @Override
    public int getBurnChance() {
        return 60;
    }

    @Override
    public int getBurnAbility() {
        return 100;
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
            if (this.down().isTransparent()) {
                this.getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
        }
        return 0;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (item instanceof ItemBoneMeal) {
            Block up = this.up();

            if (up.getId() == AIR) {
                int meta = switch (this.getDamage()) {
                    case 0, 1 -> BlockDoublePlant.TALL_GRASS;
                    case 2, 3 -> BlockDoublePlant.LARGE_FERN;
                    default -> -1;
                };

                if (meta != -1) {
                    if (player != null && !player.isCreative()) {
                        item.count--;
                    }

                    this.level.addParticle(new BoneMealParticle(this));
                    this.level.setBlock(this, get(DOUBLE_PLANT, meta), true, false);
                    this.level.setBlock(up, get(DOUBLE_PLANT, meta ^ BlockDoublePlant.TOP_HALF_BITMASK), true);
                }
            }

            return true;
        }

        return false;
    }


    @Override
    public Item[] getDrops(Item item) {
        if (item.isShears()) {
            return new Item[] {
                    Item.get(Item.TALL_GRASS, this.getDamage() == 0 ? 1 : this.getDamage(), 1)
            };
        }

        if (Utils.random.nextInt(10) == 0) {
            return new Item[] {
                    Item.get(Item.WHEAT_SEEDS)
            };
        } else {
            return Item.EMPTY_ARRAY;
        }
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_SHEARS;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.FOLIAGE_BLOCK_COLOR;
    }
}
