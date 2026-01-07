package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.BooleanBlockProperty;
import cn.nukkit.block.customblock.properties.IntBlockProperty;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntityCommandBlock;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockCommandBlock extends BlockSolidMeta implements BlockEntityHolder<BlockEntityCommandBlock>, BlockPropertiesHelper {

    private static final BooleanBlockProperty CONDITIONAL_BIT = new BooleanBlockProperty("conditional_bit", false);
    private static final IntBlockProperty FACING_DIRECTION = new IntBlockProperty("facing_direction", false, 5);

    private static final BlockProperties PROPERTIES = new BlockProperties(CONDITIONAL_BIT, FACING_DIRECTION);

    public BlockCommandBlock() {
        super(0);
    }

    public BlockCommandBlock(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return COMMAND_BLOCK;
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @NotNull
    @Override
    public Class<? extends BlockEntityCommandBlock> getBlockEntityClass() {
        return BlockEntityCommandBlock.class;
    }

    @NotNull
    @Override
    public String getBlockEntityType() {
        return BlockEntity.COMMAND_BLOCK;
    }

    @Override
    public double getHardness() {
        return -1;
    }

    @Override
    public double getResistance() {
        return 18000000;
    }

    @Override
    public String getName() {
        return "Command Block";
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public boolean isBreakable(Item item) {
        return false;
    }

    public boolean canBreakWithHand(Player player) {
        return player != null && player.isCreative();
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (player != null && !player.isCreative()) {
            return false;
        }

        if (player != null) {
            if (Math.abs(player.getFloorX() - this.x) < 2 && Math.abs(player.getFloorZ() - this.z) < 2) {
                double y = player.y + player.getEyeHeight();
                if (y - this.y > 2) {
                    this.setFacingDirection(BlockFace.UP);
                } else if (this.y - y > 0) {
                    this.setFacingDirection(BlockFace.DOWN);
                } else {
                    this.setFacingDirection(player.getHorizontalFacing().getOpposite());
                }
            } else {
                this.setFacingDirection(player.getHorizontalFacing().getOpposite());
            }
        } else {
            this.setFacingDirection(BlockFace.DOWN);
        }

        boolean blockSuccess = this.getLevel().setBlock(this, this, true, true);

        if (blockSuccess) {
            CompoundTag nbt = new CompoundTag();

            if (item.hasCustomBlockData()) {
                Map<String, Tag> customData = item.getCustomBlockData().getTags();
                for (Map.Entry<String, Tag> tag : customData.entrySet()) {
                    nbt.put(tag.getKey(), tag.getValue());
                }
            }

            BlockEntityCommandBlock blockEntity = (BlockEntityCommandBlock) BlockEntity.createBlockEntity(
                    BlockEntity.COMMAND_BLOCK,
                    this.getChunk(),
                    nbt.putString("id", BlockEntity.COMMAND_BLOCK)
                            .putInt("x", (int) this.x)
                            .putInt("y", (int) this.y)
                            .putInt("z", (int) this.z)
            );

            return blockEntity != null;
        }

        return false;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (player != null) {
            if (!Server.getInstance().getSettings().features().enableCommandBlocks()) {
                return false;
            }

            if (player.isSneaking() && !(item.isTool() || item.getId() == 0)) {
                return false;
            }

            BlockEntityCommandBlock tile = this.getOrCreateBlockEntity();
            tile.spawnTo(player);
            player.addWindow(tile.getInventory());
        }
        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
            BlockEntityCommandBlock tile = this.getBlockEntity();
            if (tile == null) {
                return super.onUpdate(type);
            }
            if (this.level.isBlockPowered(this)) {
                if (!tile.isPowered()) {
                    tile.setPowered();
                    tile.trigger();
                }
            } else {
                tile.setPowered(false);
            }
        }
        return super.onUpdate(type);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride() {
        BlockEntityCommandBlock blockEntity = this.getBlockEntity();
        if (blockEntity != null) {
            return Math.min(blockEntity.getSuccessCount(), 15);
        }
        return 0;
    }

    public void setConditionalBit(boolean value) {
        this.setPropertyValue(CONDITIONAL_BIT, value);
    }

    public boolean getConditionalBit() {
        return this.getPropertyValue(CONDITIONAL_BIT);
    }

    public void setFacingDirection(BlockFace face) {
        this.setPropertyValue(FACING_DIRECTION, face.getIndex());
    }

    public BlockFace getFacingDirection() {
        return BlockFace.values()[this.getPropertyValue(FACING_DIRECTION)];
    }

}