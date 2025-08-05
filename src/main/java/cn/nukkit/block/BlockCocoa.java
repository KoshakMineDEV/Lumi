package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDye;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.utils.DyeColor;
import cn.nukkit.utils.Faceable;
import cn.nukkit.utils.Utils;

/**
 * Created by CreeperFace on 27.10.2016.
 */
public class BlockCocoa extends BlockTransparentMeta implements Faceable {

    protected static final AxisAlignedBB[] EAST = new AxisAlignedBB[]{new SimpleAxisAlignedBB(0.6875D, 0.4375D, 0.375D, 0.9375D, 0.75D, 0.625D), new SimpleAxisAlignedBB(0.5625D, 0.3125D, 0.3125D, 0.9375D, 0.75D, 0.6875D), new SimpleAxisAlignedBB(0.5625D, 0.3125D, 0.3125D, 0.9375D, 0.75D, 0.6875D)};
    protected static final AxisAlignedBB[] WEST = new AxisAlignedBB[]{new SimpleAxisAlignedBB(0.0625D, 0.4375D, 0.375D, 0.3125D, 0.75D, 0.625D), new SimpleAxisAlignedBB(0.0625D, 0.3125D, 0.3125D, 0.4375D, 0.75D, 0.6875D), new SimpleAxisAlignedBB(0.0625D, 0.3125D, 0.3125D, 0.4375D, 0.75D, 0.6875D)};
    protected static final AxisAlignedBB[] NORTH = new AxisAlignedBB[]{new SimpleAxisAlignedBB(0.375D, 0.4375D, 0.0625D, 0.625D, 0.75D, 0.3125D), new SimpleAxisAlignedBB(0.3125D, 0.3125D, 0.0625D, 0.6875D, 0.75D, 0.4375D), new SimpleAxisAlignedBB(0.3125D, 0.3125D, 0.0625D, 0.6875D, 0.75D, 0.4375D)};
    protected static final AxisAlignedBB[] SOUTH = new AxisAlignedBB[]{new SimpleAxisAlignedBB(0.375D, 0.4375D, 0.6875D, 0.625D, 0.75D, 0.9375D), new SimpleAxisAlignedBB(0.3125D, 0.3125D, 0.5625D, 0.6875D, 0.75D, 0.9375D), new SimpleAxisAlignedBB(0.3125D, 0.3125D, 0.5625D, 0.6875D, 0.75D, 0.9375D)};

    private static final short[] faces = new short[]{
            0,
            0,
            0,
            2,
            3,
            1,
    };

    private static final short[] faces2 = new short[]{
            3, 4, 2, 5, 3, 4, 2, 5, 3, 4, 2, 5
    };

    public BlockCocoa() {
        this(0);
    }

    public BlockCocoa(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return COCOA;
    }

    @Override
    public String getName() {
        return "Cocoa";
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        if (this.boundingBox == null) {
            this.boundingBox = recalculateBoundingBox();
        }
        return this.boundingBox;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        int damage = this.getDamage();
        if (damage > 11) {
            this.setDamage(11);
        }

        AxisAlignedBB[] aabb = switch (this.getDamage()) {
            case 1, 5, 9 -> EAST;
            case 2, 6, 10 -> SOUTH;
            case 3, 7, 11 -> WEST;
            default -> NORTH;
        };

        return aabb[(this.getDamage() >> 2)].getOffsetBoundingBox(x, y, z);
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (target.hasBlockTag(BlockInternalTags.JUNGLE)) {
            if (face != BlockFace.DOWN && face != BlockFace.UP) {
                this.setDamage(faces[face.getIndex()]);
                this.level.setBlock(block, this, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            Block side = this.getSide(BlockFace.fromIndex(faces2[Math.min(this.getDamage(), 11)]));

            if (!side.hasBlockTag(BlockInternalTags.JUNGLE)) {
                this.getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (Utils.random.nextInt(2) == 1) {
                if (this.getDamage() >> 2 < 2) {
                    BlockCocoa block = (BlockCocoa) this.clone();
                    block.setDamage(block.getDamage() + 4);
                    BlockGrowEvent event = new BlockGrowEvent(this, block);

                    if (event.call()) {
                        this.getLevel().setBlock(this, event.getNewState(), true, true);
                    } else {
                        return Level.BLOCK_UPDATE_RANDOM;
                    }
                }
            } else {
                return Level.BLOCK_UPDATE_RANDOM;
            }
        }

        return 0;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (item.getId() == Item.DYE && item.getDamage() == 0x0f) {
            Block block = this.clone();
            if (this.getDamage() >> 2 < 2) {
                block.setDamage(block.getDamage() + 4);
                BlockGrowEvent event = new BlockGrowEvent(this, block);

                if (!event.call()) {
                    return false;
                }

                this.getLevel().setBlock(this, event.getNewState(), true, true);
                this.level.addParticle(new BoneMealParticle(this));

                if (player != null && !player.isCreative()) {
                    item.count--;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public double getResistance() {
        return 15;
    }

    @Override
    public double getHardness() {
        return 0.2;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.FLOW_INTO_BLOCK;
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
    public Item toItem() {
        return new ItemDye(DyeColor.BROWN.getDyeData());
    }

    @Override
    public Item[] getDrops(Item item) {
        if (this.getDamage() >= 8) {
            return new Item[]{new ItemDye(3, Utils.rand(2, 3))};
        } else {
            return new Item[]{new ItemDye(3, 1)};
        }
    }

    @Override
    public BlockFace getBlockFace() {
        return BlockFace.fromHorizontalIndex(this.getDamage() & 0x7);
    }
}
