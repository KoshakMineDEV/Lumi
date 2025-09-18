package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntityFlowerPot;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFlowerPot;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nukkit Project Team
 */
public class BlockFlowerPot extends BlockFlowable implements BlockEntityHolder<BlockEntityFlowerPot> {

    public BlockFlowerPot() {
        this(0);
    }

    public BlockFlowerPot(int meta) {
        super(meta);
    }

    protected static boolean canPlaceIntoFlowerPot(Block block) {
        return block.hasBlockTag(BlockInternalTags.POTTABLE);
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public String getName() {
        return "Flower Pot";
    }

    @Override
    public int getId() {
        return FLOWER_POT_BLOCK;
    }

    @NotNull
    @Override
    public Class<? extends BlockEntityFlowerPot> getBlockEntityClass() {
        return BlockEntityFlowerPot.class;
    }

    @NotNull
    @Override
    public String getBlockEntityType() {
        return BlockEntity.FLOWER_POT;
    }

    private static boolean isSupportValid(Block block) {
        return block.isSolid() || block.isNarrowSurface() || Block.canStayOnFullSolid(block);
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!isSupportValid(down())) {
                level.useBreakOn(this, null, null, true);
                return type;
            }
        }
        return 0;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (!isSupportValid(down())) return false;
        CompoundTag nbt = new CompoundTag()
                .putString("id", BlockEntity.FLOWER_POT)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z)
                .putShort("item", 0)
                .putInt("data", 0);
        if (item.hasCustomBlockData()) {
            for (Tag aTag : item.getCustomBlockData().getAllTags()) {
                nbt.put(aTag.getName(), aTag);
            }
        }
        BlockEntity.createBlockEntity(BlockEntity.FLOWER_POT, this.level.getChunk(block.getChunkX(), block.getChunkZ()), nbt);

        this.getLevel().setBlock(block, this, true, true);
        return true;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item) {
        return this.onActivate(item, null);
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        BlockEntity blockEntity = getLevel().getBlockEntity(this);
        if (!(blockEntity instanceof BlockEntityFlowerPot)) return false;

        String itemId = blockEntity.namedTag.getCompound("PlantBlock").getString("name");
        if (itemId.equals(ItemNamespaceId.AIR)) {
            if (canPlaceIntoFlowerPot(item.getBlock())) {
                blockEntity.namedTag.getCompound("PlantBlock").putString("name", item.getNamespaceId());

                this.setDamage(1);
                this.getLevel().setBlock(this, this, true);
                ((BlockEntityFlowerPot) blockEntity).spawnToAll();

                if (!player.isCreative()) {
                    item.setCount(item.getCount() - 1);
                    player.getInventory().setItemInHand(item.getCount() > 0 ? item : Item.get(Item.AIR));
                }
                return true;
            }
            return false;
        } else {
            for (Item drop : player.getInventory().addItem(Item.get(itemId))) {
                player.dropItem(drop);
            }

            blockEntity.namedTag.getCompound("PlantBlock").putString("name", ItemNamespaceId.AIR);
            this.setDamage(0);
            this.level.setBlock(this, this, true);
            ((BlockEntityFlowerPot) blockEntity).spawnToAll();
            return true;
        }
    }

    @Override
    public Item[] getDrops(Item item) {
        BlockEntity blockEntity = getLevel().getBlockEntity(this);
        String itemId = blockEntity.namedTag.getCompound("PlantBlock").getString("name");
        if (!itemId.equals(ItemNamespaceId.AIR)) {
            return new Item[]{Item.get(ItemNamespaceId.FLOWER_POT), Item.get(itemId)};
        }
        
        return new Item[]{Item.get(ItemNamespaceId.FLOWER_POT)};
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        return new SimpleAxisAlignedBB(this.x + 0.3125, this.y, this.z + 0.3125, this.x + 0.6875, this.y + 0.375, this.z + 0.6875);
    }

    @Override
    public boolean canPassThrough() {
        return false;
    }

    @Override
    public Item toItem() {
        return new ItemFlowerPot();
    }
}
