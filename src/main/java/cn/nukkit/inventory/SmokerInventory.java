package cn.nukkit.inventory;

import cn.nukkit.blockentity.impl.BlockEntitySmoker;

public class SmokerInventory extends FurnaceInventory {

    public SmokerInventory(BlockEntitySmoker smoker) {
        super(smoker, InventoryType.SMOKER);
    }

    @Override
    public BlockEntitySmoker getHolder() {
        return (BlockEntitySmoker) this.holder;
    }
}
