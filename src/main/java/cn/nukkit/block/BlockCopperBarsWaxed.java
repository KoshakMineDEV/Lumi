package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperBarsWaxed extends BlockCopperBarsBase {
    public BlockCopperBarsWaxed() {
        this(0);
    }

    public BlockCopperBarsWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Copper Bars";
    }

    @Override
    public int getId() {
        return WAXED_COPPER_BARS;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.UNAFFECTED;
    }

    @Override
    public boolean isWaxed() {
        return true;
    }
}
