package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatueWeathered extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatueWeathered() {
        this(0);
    }

    public BlockCopperGolemStatueWeathered(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Weathered Copper Golem Statue";
    }

    @Override
    public int getId() {
        return WEATHERED_COPPER_GOLEM_STATUE;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.WEATHERED;
    }
}
