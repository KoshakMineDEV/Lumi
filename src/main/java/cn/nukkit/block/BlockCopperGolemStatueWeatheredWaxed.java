package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatueWeatheredWaxed extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatueWeatheredWaxed() {
        this(0);
    }

    public BlockCopperGolemStatueWeatheredWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Weathered Copper Golem Statue";
    }

    @Override
    public int getId() {
        return WAXED_WEATHERED_COPPER_GOLEM_STATUE;
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
