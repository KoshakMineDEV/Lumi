package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntityChiseledBookshelf;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.*;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.block.data.BlockColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BlockChiseledBookshelf extends BlockSolidMeta implements BlockEntityHolder<BlockEntityChiseledBookshelf> {
    public static final int BOOKS_MASK = 0b00111111;    // 6 bits (0-5) for books_stored (0-63)
    public static final int DIRECTION_MASK = 0b11000000; // 2 bits (6-7) for direction (0-3)

    public BlockChiseledBookshelf() {
        this(0);
    }

    public BlockChiseledBookshelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Chiseled Bookshelf";
    }

    @Override
    public double getHardness() {
        return 1.5D;
    }

    @Override
    public double getResistance() {
        return 7.5D;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public int getBurnChance() {
        return 30;
    }

    @Override
    public int getBurnAbility() {
        return 20;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.WOOD_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        return Item.EMPTY_ARRAY;
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, @Nullable Player player) {
        if (player != null) {
            setBlockFace(player.getHorizontalFacing().getOpposite());
        } else {
            setBlockFace(BlockFace.SOUTH);
        }
        CompoundTag nbt = new CompoundTag();
        if (item.hasCustomName()) {
            nbt.putString("CustomName", item.getCustomName());
        }
        if (item.hasCustomBlockData()) {
            Map<String, Tag> customData = item.getCustomBlockData().getTags();
            for (Map.Entry<String, Tag> tag : customData.entrySet()) {
                nbt.put(tag.getKey(), tag.getValue());
            }
        }
        return BlockEntityHolder.setBlockAndCreateEntity(this, true, true, nbt) != null;
    }

    @Override
    public int onTouch(@NotNull Vector3 vector, @NotNull Item item, @NotNull BlockFace face, float fx, float fy, float fz, @Nullable Player player, PlayerInteractEvent.@NotNull Action action) {
        if(action== PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
            BlockFace blockFace = getBlockFace();
            assert player != null;
            if (player.getHorizontalFacing().getOpposite() == blockFace) {
                /*
                 * south z==1  The lower left corner is the origin
                 * east  x==1  The lower right corner is the origin
                 * west  x==0  The lower left corner is the origin
                 * north z==0  The lower right corner is the origin
                 */
                Vector2 clickPos = switch (blockFace) {
                    case NORTH -> new Vector2(1 - fx, fy);
                    case SOUTH -> new Vector2(fx, fy);
                    case WEST -> new Vector2(fz, fy);
                    case EAST -> new Vector2(1 - fz, fy);
                    default -> throw new IllegalArgumentException(blockFace.toString());
                };
                int index = getRegion(clickPos);
                BlockEntityChiseledBookshelf blockEntity = this.getBlockEntity();
                if (blockEntity != null) {
                    if (blockEntity.hasBook(index)) {
                        Item book = blockEntity.removeBook(index);
                        player.getInventory().addItem(book);
                    } else if (item instanceof ItemBook || item instanceof ItemBookEnchanted || item instanceof ItemBookWritable) {
                        Item itemClone = item.clone();
                        if (!player.isCreative()) {
                            itemClone.setCount(itemClone.getCount() - 1);
                            player.getInventory().setItemInHand(itemClone);
                        }
                        itemClone.setCount(1);
                        blockEntity.setBook(itemClone, index);
                    }
                    this.setProperty(BOOKS_MASK, blockEntity.getBooksStoredBit());
                    this.getLevel().setBlock(this, this, true);
                }
            }
            return 1;
        }
        return 0;
    }

    public void setBlockFace(BlockFace blockFace) {
        setProperty(DIRECTION_MASK, blockFace.getHorizontalIndex());
    }

    public BlockFace getBlockFace() {
        return BlockFace.fromHorizontalIndex(this.getProperty(DIRECTION_MASK));
    }

    public void setProperty(int mask, int value) {
        int data = getDamage();
        int shift = Integer.numberOfTrailingZeros(mask);
        int maxValue = (mask >>> shift);
        int clampedValue = value & maxValue;
        setDamage((data & ~mask) | (clampedValue << shift));
    }

    public int getProperty(int mask) {
        int data = getDamage();
        int shift = Integer.numberOfTrailingZeros(mask);
        return (data & mask) >>> shift;
    }

    private int getRegion(Vector2 clickPos) {
        if (clickPos.getX() - 0.333333 < 0) {
            return clickPos.getY() - 0.5 < 0 ? 3 : 0;
        } else if (clickPos.getX() - 0.666666 < 0) {
            return clickPos.getY() - 0.5 < 0 ? 4 : 1;
        } else {
            return clickPos.getY() - 0.5 < 0 ? 5 : 2;
        }
    }

    @Override
    public int getId() {
        return CHISELED_BOOKSHELF;
    }

    @Override
    public @NotNull Class<? extends BlockEntityChiseledBookshelf> getBlockEntityClass() {
        return BlockEntityChiseledBookshelf.class;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0);
    }

    @Override
    public @NotNull String getBlockEntityType() {
        return BlockEntity.CHISELED_BOOKSHELF;
    }
}
