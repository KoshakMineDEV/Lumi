package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatue extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatue() {
        this(0);
    }

    public BlockCopperGolemStatue(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Copper Golem Statue";
    }

    @Override
    public int getId() {
        return COPPER_GOLEM_STATUE;
    }

    @Override
    public @NotNull OxidizationLevel getOxidizationLevel() {
        return OxidizationLevel.UNAFFECTED;
    }
}
