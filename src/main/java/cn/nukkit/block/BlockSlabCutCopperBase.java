package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.properties.enums.OxidizationLevel;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.block.data.BlockColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockSlabCutCopperBase extends BlockSlab implements Oxidizable, Waxable {
    public BlockSlabCutCopperBase(int meta, int doubleSlab) {
        super(meta, doubleSlab);
    }

    @Override
    public double getHardness() {
        return 3;
    }

    @Override
    public double getResistance() {
        return 6;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_STONE;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.ORANGE_BLOCK_COLOR;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public int onUpdate(int type) {
        return Oxidizable.super.onUpdate(type);
    }

    @Override
    public boolean onActivate(@NotNull Item item, Player player) {
        return Waxable.super.onActivate(item, player)
                || Oxidizable.super.onActivate(item, player);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean isWaxed() {
        return false;
    }

    @Override
    public Block getStateWithOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        BlockSlabCutCopperBase slab = (BlockSlabCutCopperBase) Block.get((getCopperId(isWaxed(), oxidizationLevel)));
        slab.setDamage(getDamage());
        return slab;
    }

    @Override
    public boolean setOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        if (getOxidizationLevel().equals(oxidizationLevel)) {
            return true;
        }

        BlockSlabCutCopperBase slab = (BlockSlabCutCopperBase) Block.get((getCopperId(isWaxed(), oxidizationLevel)));
        slab.setDamage(getDamage());

        return getValidLevel().setBlock(this, slab);
    }

    @Override
    public boolean setWaxed(boolean waxed) {
        if (isWaxed() == waxed) {
            return true;
        }

        BlockSlabCutCopperBase slab = (BlockSlabCutCopperBase) Block.get((getCopperId(waxed, getOxidizationLevel())));
        slab.setDamage(getDamage());

        return getValidLevel().setBlock(this, slab);
    }

    protected int getCopperId(boolean waxed, @Nullable OxidizationLevel oxidizationLevel) {
        if (oxidizationLevel == null) {
            return getId();
        }
        switch (oxidizationLevel) {
            case UNAFFECTED:
                return waxed? WAXED_CUT_COPPER_SLAB : CUT_COPPER_SLAB;
            case EXPOSED:
                return waxed? WAXED_EXPOSED_CUT_COPPER_SLAB : EXPOSED_CUT_COPPER_SLAB;
            case WEATHERED:
                return waxed? WAXED_WEATHERED_CUT_COPPER_SLAB : WEATHERED_CUT_COPPER_SLAB;
            case OXIDIZED:
                return waxed? WAXED_OXIDIZED_CUT_COPPER_SLAB : OXIDIZED_CUT_COPPER_SLAB;
            default:
                return getId();
        }
    }

    @Override
    public String getName() {
        return getOxidizationLevel().name().toLowerCase() + "Slab";
    }
}
