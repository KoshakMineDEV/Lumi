package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperBarsOxidizedWaxed extends BlockCopperBarsBase {
    public BlockCopperBarsOxidizedWaxed() {
        this(0);
    }

    public BlockCopperBarsOxidizedWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Oxidized Copper Bars";
    }

    @Override
    public int getId() {
        return WAXED_OXIDIZED_COPPER_BARS;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.OXIDIZED;
    }

    @Override
    public boolean isWaxed() {
        return true;
    }
}
