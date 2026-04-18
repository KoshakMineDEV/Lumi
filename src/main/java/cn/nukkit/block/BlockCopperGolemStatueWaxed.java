package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockCopperGolemStatueWaxed extends BlockCopperGolemStatueBase {
    public BlockCopperGolemStatueWaxed() {
        this(0);
    }

    public BlockCopperGolemStatueWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Copper Golem Statue";
    }

    @Override
    public int getId() {
        return WAXED_COPPER_GOLEM_STATUE;
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
