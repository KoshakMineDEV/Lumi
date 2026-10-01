package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.properties.enums.OxidizationLevel;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockCopperBarsBase extends BlockThin implements Oxidizable, Waxable {
    public BlockCopperBarsBase() {
        this(0);
    }

    public BlockCopperBarsBase(int meta) {
        super(meta);
    }

    @Override
    public double getHardness() {
        return 5;
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
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public int onUpdate(int type) {
        int connectionUpdate = super.onUpdate(type);
        int oxidationUpdate = Oxidizable.super.onUpdate(type);
        return connectionUpdate != 0 ? connectionUpdate : oxidationUpdate;
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
    public Block getStateWithOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        return Block.get(getCopperId(isWaxed(), oxidizationLevel), this.getDamage());
    }

    @Override
    public boolean setOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        if (getOxidizationLevel().equals(oxidizationLevel)) {
            return true;
        }
        return getValidLevel().setBlock(this, Block.get(getCopperId(isWaxed(), oxidizationLevel)));
    }

    @Override
    public boolean setWaxed(boolean waxed) {
        if (isWaxed() == waxed) {
            return true;
        }
        return getValidLevel().setBlock(this, Block.get(getCopperId(waxed, getOxidizationLevel()), this.getDamage()));
    }

    @Override
    public boolean isWaxed() {
        return false;
    }

    protected int getCopperId(boolean waxed, @Nullable OxidizationLevel oxidizationLevel) {
        if (oxidizationLevel == null) {
            return getId();
        }
        return switch (oxidizationLevel) {
            case UNAFFECTED -> waxed ? WAXED_COPPER_BARS : COPPER_BARS;
            case EXPOSED -> waxed ? WAXED_EXPOSED_COPPER_BARS : EXPOSED_COPPER_BARS;
            case WEATHERED -> waxed ? WAXED_WEATHERED_COPPER_BARS: WEATHERED_COPPER_BARS;
            case OXIDIZED -> waxed ? WAXED_OXIDIZED_COPPER_BARS : OXIDIZED_COPPER_BARS;
        };
    }
}
