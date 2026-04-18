package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatueExposed extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatueExposed() {
        this(0);
    }

    public BlockCopperGolemStatueExposed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Exposed Copper Golem Statue";
    }

    @Override
    public int getId() {
        return EXPOSED_COPPER_GOLEM_STATUE;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.EXPOSED;
    }
}
