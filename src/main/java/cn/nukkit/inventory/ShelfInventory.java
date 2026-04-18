package cn.nukkit.inventory;

import cn.nukkit.blockentity.impl.BlockEntityShelf;

public class ShelfInventory extends ContainerInventory {

    public ShelfInventory(InventoryHolder holder) {
        super(holder, InventoryType.SHELF);
    }

    @Override
    public BlockEntityShelf getHolder() {
        return (BlockEntityShelf) super.getHolder();
    }

    @Override
    public boolean canCauseVibration() {
        return true;
    }
}