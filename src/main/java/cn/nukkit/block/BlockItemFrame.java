package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntityItemFrame;
import cn.nukkit.event.block.ItemFrameUseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemItemFrame;
import cn.nukkit.level.Level;
import cn.nukkit.level.sound.ItemFrameItemAddedSound;
import cn.nukkit.level.sound.ItemFrameItemRotated;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.block.data.Faceable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Pub4Game on 03.07.2016.
 */
public class BlockItemFrame extends BlockTransparentMeta implements Faceable, BlockEntityHolder<BlockEntityItemFrame> {

    private static final int FACING_MASK = 0b0000_0000_0000_0111;
    private static final int MAP_BIT_MASK = 0b0000_0000_0000_1000;
    private static final int PHOTO_BIT_MASK = 0b0000_0000_0001_0000;

    public BlockItemFrame() {
        this(0);
    }

    public BlockItemFrame(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return ITEM_FRAME_BLOCK;
    }

    @Override
    public String getName() {
        return "Item Frame";
    }

    @NotNull
    @Override
    public Class<? extends BlockEntityItemFrame> getBlockEntityClass() {
        return BlockEntityItemFrame.class;
    }

    @NotNull
    @Override
    public String getBlockEntityType() {
        return BlockEntity.ITEM_FRAME;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!this.getSide(getFacing().getOpposite()).isSolid()) {
                this.level.useBreakOn(this, null, null, true);
                return type;
            }
        }

        return 0;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
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
    public int onTouch(@NotNull Vector3 vector, @NotNull Item item, @NotNull BlockFace face, float fx, float fy, float fz, @Nullable Player player, PlayerInteractEvent.Action action) {
        this.onUpdate(Level.BLOCK_UPDATE_TOUCH);
        if (player != null && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            BlockEntity itemFrame = this.level.getBlockEntity(this);
            if (itemFrame instanceof BlockEntityItemFrame && ((BlockEntityItemFrame) itemFrame).dropItem(player)) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        BlockEntity blockEntity = this.getLevel().getBlockEntity(this);
        BlockEntityItemFrame itemFrame = (BlockEntityItemFrame) blockEntity;
        if (itemFrame.getItem().getId() == Item.AIR) {
            Item itemToFrame = item.clone();
            ItemFrameUseEvent event = new ItemFrameUseEvent(player, this, itemFrame, itemToFrame, ItemFrameUseEvent.Action.PUT);
            this.getLevel().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            if (player != null && player.isSurvival()) {
                item.setCount(item.getCount() - 1);
                player.getInventory().setItemInHand(item);
            }
            itemToFrame.setCount(1);
            itemFrame.setItem(itemToFrame);
            if (itemToFrame.getId() == ItemID.MAP) {
                setStoringMap(true);
                this.getLevel().setBlock(this, this, true);
            }
            this.getLevel().addSound(new ItemFrameItemAddedSound(this));
        } else {
            ItemFrameUseEvent event = new ItemFrameUseEvent(player, this, itemFrame, null, ItemFrameUseEvent.Action.ROTATION);
            this.getLevel().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            itemFrame.setItemRotation((itemFrame.getItemRotation() + 1) % 8);
            if (isStoringMap()) {
                setStoringMap(false);
                this.getLevel().setBlock(this, this, true);
            }
            this.getLevel().addSound(new ItemFrameItemRotated(this));
        }
        return true;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (target.isSolid() && (!block.isSolid() || block.canBeReplaced())) {
            this.setBlockFace(face);
            this.getLevel().setBlock(block, this, true, true);
            CompoundTag nbt = new CompoundTag()
                    .putString("id", BlockEntity.ITEM_FRAME)
                    .putInt("x", (int) block.x)
                    .putInt("y", (int) block.y)
                    .putInt("z", (int) block.z)
                    .putByte("ItemRotation", 0)
                    .putFloat("ItemDropChance", 1.0f);
            if (item.hasCustomBlockData()) {
                for (Tag aTag : item.getCustomBlockData().getAllTags()) {
                    nbt.put(aTag.getName(), aTag);
                }
            }
            BlockEntity.createBlockEntity(BlockEntity.ITEM_FRAME, this.getChunk(), nbt);
            this.getLevel().addLevelEvent(this, LevelEventPacket.EVENT_SOUND_ITEM_FRAME_PLACED);
            return true;
        }
        return false;
    }

    @Override
    public boolean onBreak(Item item) {
        this.getLevel().setBlock(this, Block.get(BlockID.AIR), true, true);
        this.getLevel().addLevelEvent(this, LevelEventPacket.EVENT_SOUND_ITEM_FRAME_REMOVED);
        return true;
    }

    @Override
    public Item[] getDrops(Item item) {
        BlockEntity blockEntity = this.getLevel().getBlockEntity(this);
        BlockEntityItemFrame itemFrame = (BlockEntityItemFrame) blockEntity;
        if (itemFrame != null && ThreadLocalRandom.current().nextFloat() <= itemFrame.getItemDropChance()) {
            return new Item[]{
                    toItem(), itemFrame.getItem().clone()
            };
        } else {
            return new Item[]{
                    toItem()
            };
        }
    }

    @Override
    public Item toItem() {
        return new ItemItemFrame();
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride() {
        BlockEntity blockEntity = this.level.getBlockEntity(this);

        if (blockEntity instanceof BlockEntityItemFrame) {
            return ((BlockEntityItemFrame) blockEntity).getAnalogOutput();
        }

        return super.getComparatorInputOverride();
    }

    public BlockFace getFacing() {
        return BlockFace.fromIndex(getProperty(FACING_MASK));
    }

    @Override
    public void setBlockFace(@NotNull BlockFace face) {
        this.setProperty(FACING_MASK, face.getIndex());
    }

    public boolean isStoringMap() {
        return (getProperty(MAP_BIT_MASK)) != 0;
    }

    public void setStoringMap(boolean map) {
        setProperty(MAP_BIT_MASK, map ? 0 : 1);
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

    @Override
    public double getHardness() {
        return 0.25;
    }

    @Override
    public BlockFace getBlockFace() {
        return this.getFacing().getOpposite();
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
