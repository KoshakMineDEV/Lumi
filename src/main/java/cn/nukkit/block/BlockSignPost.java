package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntitySign;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.block.data.BlockColor;
import cn.nukkit.block.data.Faceable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nukkit Project Team
 */
public class BlockSignPost extends BlockSignBase implements Faceable, BlockEntityHolder<BlockEntitySign> {

    public BlockSignPost() {
        this(0);
    }

    public BlockSignPost(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return SIGN_POST;
    }

    @NotNull
    @Override
    public Class<? extends BlockEntitySign> getBlockEntityClass() {
        return BlockEntitySign.class;
    }

    @NotNull
    @Override
    public String getBlockEntityType() {
        return BlockEntity.SIGN;
    }

    @Override
    public double getHardness() {
        return 1;
    }

    @Override
    public double getResistance() {
        return 5;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public String getName() {
        return "Sign Post";
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }

    protected int getPostId() {
        return getId();
    }

    public int getWallId() {
        return WALL_SIGN;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        if (face != BlockFace.DOWN) {
            CompoundTag nbt = new CompoundTag()
                    .putString("id", BlockEntity.SIGN)
                    .putInt("x", (int) block.x)
                    .putInt("y", (int) block.y)
                    .putInt("z", (int) block.z);

            if (face == BlockFace.UP) {
                setDamage((int) Math.floor(((player.yaw + 180) * 16 / 360) + 0.5) & 0x0f);
                getLevel().setBlock(block, Block.get(getPostId(), getDamage()), true);
            } else {
                setDamage(face.getIndex());
                getLevel().setBlock(block, Block.get(getWallId(), getDamage()), true);
            }

            if (player != null) {
                nbt.putString("Creator", player.getUniqueId().toString()); //已不再使用，保留是防止有插件用
            }

            if (item.hasCustomBlockData()) {
                for (Tag aTag : item.getCustomBlockData().getAllTags()) {
                    nbt.put(aTag.getName(), aTag);
                }
            }

            BlockEntity blockEntity = BlockEntity.createBlockEntity(BlockEntity.SIGN, this.level.getChunk(block.getChunkX(), block.getChunkZ()), nbt);
            player.openSignEditor(blockEntity, true);
            return true;
        }

        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (down().getId() == Block.AIR) {
                this.getLevel().useBreakOn(this, null, null, true);

                return Level.BLOCK_UPDATE_NORMAL;
            }
        }

        return 0;
    }

    @Override
    public Item toItem() {
        return Item.get(ItemNamespaceId.OAK_SIGN);
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.AIR_BLOCK_COLOR;
    }

    @Override
    public BlockFace getBlockFace() {
        return BlockFace.fromIndex(this.getDamage() & 0x07);
    }
}
