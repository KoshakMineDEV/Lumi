package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockWaxedCopperGrate extends BlockCopperGrateBase {
    @Override
    public String getName() {
        return "Waxed Copper Grate";
    }

    @Override
    public int getId() {
        return WAXED_COPPER_GRATE;
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
