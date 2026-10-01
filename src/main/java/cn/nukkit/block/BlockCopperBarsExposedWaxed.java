package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperBarsExposedWaxed extends BlockCopperBarsBase {
    public BlockCopperBarsExposedWaxed() {
        this(0);
    }

    public BlockCopperBarsExposedWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Exposed Copper Bars";
    }

    @Override
    public int getId() {
        return WAXED_EXPOSED_COPPER_BARS;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.EXPOSED;
    }

    @Override
    public boolean isWaxed() {
        return true;
    }
}
