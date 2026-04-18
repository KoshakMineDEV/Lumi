package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatueOxidizedWaxed extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatueOxidizedWaxed() {
        this(0);
    }

    public BlockCopperGolemStatueOxidizedWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Oxidized Copper Golem Statue";
    }

    @Override
    public int getId() {
        return WAXED_OXIDIZED_COPPER_GOLEM_STATUE;
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
