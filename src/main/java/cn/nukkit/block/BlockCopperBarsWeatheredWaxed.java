package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperBarsWeatheredWaxed extends BlockCopperBarsBase {
    public BlockCopperBarsWeatheredWaxed() {
        this(0);
    }

    public BlockCopperBarsWeatheredWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Weathered Copper Bars";
    }

    @Override
    public int getId() {
        return WAXED_WEATHERED_COPPER_BARS;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.WEATHERED;
    }

    @Override
    public boolean isWaxed() {
        return true;
    }
}
