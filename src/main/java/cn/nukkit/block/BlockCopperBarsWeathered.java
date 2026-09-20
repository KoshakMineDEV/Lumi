package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperBarsWeathered extends BlockCopperBarsBase {
    public BlockCopperBarsWeathered() {
        this(0);
    }

    public BlockCopperBarsWeathered(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Weathered Copper Bars";
    }

    @Override
    public int getId() {
        return WEATHERED_COPPER_BARS;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.WEATHERED;
    }
}
