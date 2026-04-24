package cn.nukkit.block;

import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.impl.BlockEntityTrialSpawner;
import cn.nukkit.item.Item;
import org.jetbrains.annotations.NotNull;

public class BlockTrialSpawner extends BlockSolidMeta implements BlockEntityHolder<BlockEntityTrialSpawner> {
    public BlockTrialSpawner() {
        this(0);
    }

    public BlockTrialSpawner(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Trial Spawner";
    }

    @Override
    public int getId() {
        return TRIAL_SPAWNER;
    }

    @Override
    public double getHardness() {
        return 50;
    }

    @Override
    public double getResistance() {
        return 50;
    }

    @Override
    public Item[] getDrops(Item item) {
        return Item.EMPTY_ARRAY;
    }

    @Override
    public @NotNull Class<? extends BlockEntityTrialSpawner> getBlockEntityClass() {
        return BlockEntityTrialSpawner.class;
    }

    @Override
    public @NotNull String getBlockEntityType() {
        return BlockEntityID.TRIAL_SPAWNER;
    }
}
