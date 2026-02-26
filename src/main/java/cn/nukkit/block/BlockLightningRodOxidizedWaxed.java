package cn.nukkit.block;

import cn.nukkit.block.properties.enums.OxidizationLevel;
import org.jetbrains.annotations.NotNull;

public class BlockLightningRodOxidizedWaxed extends BlockLightningRodBase {
    public BlockLightningRodOxidizedWaxed() {
        this(0);
    }

    public BlockLightningRodOxidizedWaxed(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Waxed Oxidized Lightning Rod";
    }

    @Override
    public int getId() {
        return WAXED_OXIDIZED_LIGHTNING_ROD;
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