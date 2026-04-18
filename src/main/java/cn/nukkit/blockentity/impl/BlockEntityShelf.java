package cn.nukkit.blockentity.impl;

import cn.nukkit.block.BlockShelf;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.BlockEntitySpawnableContainer;
import cn.nukkit.inventory.ShelfInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;

public class BlockEntityShelf extends BlockEntitySpawnableContainer {

    public BlockEntityShelf(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        this.inventory = new ShelfInventory(this);

        if (!this.namedTag.contains("Items") || !(this.namedTag.get("Items") instanceof ListTag)) {
            this.namedTag.putList("Items", new ListTag<CompoundTag>());
        }

        ListTag<CompoundTag> itemsTag = this.namedTag.getList("Items", CompoundTag.class);
        for (int i = 0; i < itemsTag.size(); i++) {
            this.inventory.setItem(i, NBTIO.getItemHelper(itemsTag.get(i)));
        }
        this.level.updateComparatorOutputLevel(this);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putList("Items", new ListTag<CompoundTag>());
        for (int index = 0; index < this.getSize(); index++) {
            this.setItem(index, this.inventory.getItem(index));
        }
    }

    public int getSize() {
        return getInventory().getSize();
    }

    @Override
    public boolean isBlockEntityValid() {
        return this.getBlock() instanceof BlockShelf;
    }

    @Override
    public void setDirty() {
        this.saveNBT();
        this.spawnToAll();
        super.setDirty();
    }

    @Override
    public CompoundTag getSpawnCompound() {
        CompoundTag tag = getDefaultCompound(this, BlockEntityID.SHELF);
        ListTag<CompoundTag> items = new ListTag<>("items");
        for(int i = 0; i < getSize(); i++) {
            Item item = this.inventory.getItem(i);
            items.add(NBTIO.putItemHelper(item, i));
        }
        tag.put("Items", items);
        return tag;
    }

    @Override
    public void onBreak() {
        for (Item content : getInventory().getContents().values()) {
            level.dropItem(this, content);
        }
        this.getInventory().clearAll();
    }

    @Override
    public String getName() {
        return "Shelf";
    }


    @Override
    public ShelfInventory getInventory() {
        return (ShelfInventory) this.inventory;
    }
}