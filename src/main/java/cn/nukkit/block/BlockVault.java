package cn.nukkit.block;

import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.impl.BlockEntityVault;
import cn.nukkit.item.Item;
import org.jetbrains.annotations.NotNull;

public class BlockVault extends BlockSolidMeta implements BlockEntityHolder<BlockEntityVault> {
    public BlockVault() {
        this(0);
    }

    public BlockVault(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public int getId() {
        return VAULT;
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
    public @NotNull Class<? extends BlockEntityVault> getBlockEntityClass() {
        return BlockEntityVault.class;
    }

    @Override
    public @NotNull String getBlockEntityType() {
        return BlockEntityID.VAULT;
    }
}
