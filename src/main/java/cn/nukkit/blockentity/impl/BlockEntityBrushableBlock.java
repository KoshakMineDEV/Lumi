package cn.nukkit.blockentity.impl;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityNameable;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

/**
 * @author Koshak_Mine, glorydark
 */
public class BlockEntityBrushableBlock extends BlockEntitySpawnable implements BlockEntityNameable {

    public static final int REQUIRED_BRUSHES_TO_BREAK = 20;

    private Item item;

    private byte brushDirection;

    private int brushCount = 0;

    public BlockEntityBrushableBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public BlockFace getBrushDirection() {
        return BlockFace.fromIndex(this.brushDirection);
    }

    public void setBrushDirection(BlockFace face) {
        this.brushDirection = (byte) face.getIndex();
        this.namedTag.putByte("brush_direction", this.brushDirection);
        this.spawnToAll();
    }

    public int getBrushCount() {
        return brushCount;
    }

    public void setBrushCount(int brushCount) {
        this.brushCount = brushCount;
        this.namedTag.putInt("brush_count", brushCount);
        this.spawnToAll();
    }

    public void setItem(Item item) {
        this.item = item;
        this.namedTag.putCompound("item", NBTIO.putItemHelper(this.item, true));
    }

    public Item getItem() {
        return item;
    }

    @Override
    protected void initBlockEntity() {
        if (this.namedTag.contains("item")) {
            this.item = NBTIO.getItemHelper(this.namedTag.getCompound("item"));
        } else {
            this.item = Item.AIR_ITEM.clone();
        }

        super.initBlockEntity();
    }

    @Override
    public String getName() {
        return this.hasName() ? this.namedTag.getString("CustomName") : "Brushable Block";
    }

    @Override
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            this.namedTag.remove("CustomName");
            return;
        }
        this.namedTag.putString("CustomName", name);
    }

    @Override
    public boolean hasName() {
        return this.namedTag.contains("CustomName");
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString("id", BlockEntity.BRUSHABLE_BLOCK)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z)
                .putCompound("item", NBTIO.putItemHelper(this.item, true))
                .putList("type", namedTag.getList(getBlock().toItem().getNamespaceId()))
                .putInt("brush_count", brushCount / 2)
                .putByte("brush_direction", brushDirection)
                .putInt("LootTableSeed", Utils.random.nextInt())
                .putString("LootTable", "");
    }

    public int getCompletionState() {
        //TODO: Make it match vanilla time with better code style
        if (brushCount >= 3) {
            if (brushCount >= 7) {
                if (brushCount >= 14) {
                    return 3;
                }
                return 2;
            }
            return 1;
        } else {
            return 0;
        }
    }
}