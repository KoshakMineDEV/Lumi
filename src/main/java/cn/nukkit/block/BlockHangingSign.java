package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntityHangingSign;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.CompassRoseDirection;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import org.jetbrains.annotations.NotNull;

public abstract class BlockHangingSign extends BlockSignBase implements BlockEntityHolder<BlockEntityHangingSign> {
    // Corrected bit masks (using 16-bit representation for clarity)
    private static final int ATTACHED_MASK  = 0b0000_0000_0000_0001; // Bit 0 (1 bit)
    private static final int FACING_MASK    = 0b0000_0000_0000_1110; // Bits 1-3 (3 bits)
    private static final int DIRECTION_MASK = 0b0000_0000_1111_0000; // Bits 4-7 (4 bits)
    private static final int HANGING_MASK   = 0b0000_0001_0000_0000; // Bit 8 (1 bit)

    public BlockHangingSign() {
        super(0);
    }

    public BlockHangingSign(int meta) {
        super(meta);
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (player != null && !player.isSneaking() && target instanceof BlockHangingSign) {
            return false;
        }

        if (face == BlockFace.UP) {
            BlockFace blockFace = checkGroundBlock();
            if (blockFace == null) {
                return false;
            }
            face = blockFace;
        }
        if (target instanceof BlockHangingSign && face != BlockFace.DOWN) {
            return false;
        }

        Block layer0 = level.getBlock(this, 0);
        Block layer1 = level.getBlock(this, 1);

        CompoundTag nbt = new CompoundTag()
                .putString("id", BlockEntity.HANGING_SIGN)
                .putInt("x", (int) block.x)
                .putInt("y", (int) block.y)
                .putInt("z", (int) block.z);

        if (face == BlockFace.DOWN) {
            this.setPropertyValue(HANGING_MASK, 1);
            CompassRoseDirection direction = CompassRoseDirection.from(
                    (int) Math.floor((((player != null ? player.yaw : 0) + 180) * 16 / 360) + 0.5) & 0x0f
            );
            if ((player != null && player.isSneaking()) || target instanceof BlockThin || target instanceof BlockChain || target instanceof BlockHangingSign) {
                this.setPropertyValue(ATTACHED_MASK, 1);
                this.setPropertyValue(DIRECTION_MASK, direction.getIndex());
                getLevel().setBlock(block, this, true);
            } else {
                this.setPropertyValue(FACING_MASK, direction.getClosestBlockFace().getIndex());
                getLevel().setBlock(block, this, true);
            }
        } else {
            this.setPropertyValue(FACING_MASK, face.rotateY().getIndex());
            getLevel().setBlock(block, this, true);
        }
        if (item.hasCustomBlockData()) {
            for (Tag aTag : item.getCustomBlockData().getAllTags()) {
                nbt.put(aTag.getName(), aTag);
            }
        }

        try {
            BlockEntity blockEntity = BlockEntity.createBlockEntity(BlockEntity.HANGING_SIGN, this.level.getChunk(block.getChunkX(), block.getChunkZ()), nbt);
            if (player != null) {
                player.openSignEditor(blockEntity, true);
            }
            return true;
        } catch (Exception e) {
            level.setBlock(layer0, 0, layer0, true);
            level.setBlock(layer1, 0, layer1, true);
            return false;
        }
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (isHanging()) {
                if (up().isAir()) {
                    getLevel().useBreakOn(this);
                    return Level.BLOCK_UPDATE_NORMAL;
                }
            } else {
                if (checkGroundBlock() == null) {
                    getLevel().useBreakOn(this);
                    return Level.BLOCK_UPDATE_NORMAL;
                }
            }
        }
        return 0;
    }

    private BlockFace checkGroundBlock() {
        if (getSide(BlockFace.NORTH, 1).canBePlaced()) return BlockFace.NORTH;
        if (getSide(BlockFace.SOUTH, 1).canBePlaced()) return BlockFace.SOUTH;
        if (getSide(BlockFace.WEST, 1).canBePlaced()) return BlockFace.WEST;
        if (getSide(BlockFace.EAST, 1).canBePlaced()) return BlockFace.EAST;
        return null;
    }

    public CompassRoseDirection getSignDirection() {
        if (isHanging() && isAttached()) {
            return CompassRoseDirection.from(getDirection());
        } else {
            return CompassRoseDirection.from(getFacing());
        }
    }

    public int getDirection() {
        return getPropertyValue(DIRECTION_MASK);
    }

    public int getFacing() {
        return getPropertyValue(FACING_MASK);
    }

    public boolean isHanging() {
        return getPropertyValue(HANGING_MASK) != 0;
    }

    public boolean isAttached() {
        return getPropertyValue(ATTACHED_MASK) != 0;
    }

    public int getPropertyValue(int mask) {
        int data = getDamage();
        if(mask == DIRECTION_MASK) return (data & DIRECTION_MASK) >>> 4;
        return (data & mask) >> 4;
    }

    public void setPropertyValue(int mask, int value) {
        int data = getDamage();
        int shift = Integer.numberOfTrailingZeros(mask);
        int maxValue = mask >>> shift;
        int clampedValue = value & maxValue;
        setDamage((data & ~mask) | (clampedValue << shift));
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0);
    }

    @Override
    public @NotNull Class<? extends BlockEntityHangingSign> getBlockEntityClass() {
        return BlockEntityHangingSign.class;
    }

    @Override
    public @NotNull String getBlockEntityType() {
        return BlockEntity.HANGING_SIGN;
    }

    @Override
    public abstract int getId();
}
