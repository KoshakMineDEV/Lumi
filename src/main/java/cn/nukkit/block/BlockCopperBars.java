package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperBars extends BlockCopperBarsBase {
    public BlockCopperBars() {
        this(0);
    }

    public BlockCopperBars(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Copper Bars";
    }

    @Override
    public int getId() {
        return COPPER_BARS;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.UNAFFECTED;
    }
}
