package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatueOxidized extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatueOxidized() {
        this(0);
    }

    public BlockCopperGolemStatueOxidized(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Oxidized Copper Golem Statue";
    }

    @Override
    public int getId() {
        return OXIDIZED_COPPER_GOLEM_STATUE;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.OXIDIZED;
    }
}
