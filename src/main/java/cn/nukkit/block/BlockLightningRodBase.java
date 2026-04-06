package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.BooleanBlockProperty;
import cn.nukkit.block.data.Faceable;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.block.properties.enums.OxidizationLevel;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.math.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockLightningRodBase extends BlockTransparentMeta implements Faceable, BlockPropertiesHelper, Oxidizable, Waxable {

    private static final BooleanBlockProperty POWERED_BIT = new BooleanBlockProperty("powered_bit", false);
    private static final BlockProperties PROPERTIES = new BlockProperties(VanillaProperties.FACING_DIRECTION, POWERED_BIT);

    public BlockLightningRodBase() {
        this(0);
    }

    public BlockLightningRodBase(int meta) {
        super(meta);
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public double getHardness() {
        return 3;
    }

    @Override
    public double getResistance() {
        return 6;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public double getMinX() {
        return this.x + 0.4;
    }

    @Override
    public double getMinZ() {
        return this.z + 0.4;
    }

    @Override
    public double getMaxX() {
        return this.x + 0.6;
    }

    @Override
    public double getMaxZ() {
        return this.z + 0.6;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        this.setBlockFace(face);
        this.getLevel().setBlock(this, this, true, true);
        return true;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_STONE) {
            return new Item[]{
                    toItem()
            };
        } else {
            return Item.EMPTY_ARRAY;
        }
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0, 1);
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public BlockFace getBlockFace() {
        return getPropertyValue(VanillaProperties.FACING_DIRECTION);
    }

    @Override
    public void setBlockFace(BlockFace face) {
        setPropertyValue(VanillaProperties.FACING_DIRECTION, face);
    }

    @Override
    public int onUpdate(int type) {
        return Oxidizable.super.onUpdate(type);
    }

    @Override
    public boolean onActivate(@NotNull Item item, Player player) {
        return Waxable.super.onActivate(item, player)
                || Oxidizable.super.onActivate(item, player);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public Block getStateWithOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        BlockLightningRodBase rod = (BlockLightningRodBase) Block.get((getCopperId(isWaxed(), oxidizationLevel)));
        rod.setDamage(getDamage());
        return rod;
    }

    @Override
    public boolean setOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        if (getOxidizationLevel().equals(oxidizationLevel)) {
            return true;
        }

        BlockLightningRodBase rod = (BlockLightningRodBase) Block.get((getCopperId(isWaxed(), oxidizationLevel)));
        rod.setDamage(getDamage());

        return getValidLevel().setBlock(this, rod);
    }

    @Override
    public boolean setWaxed(boolean waxed) {
        if (isWaxed() == waxed) {
            return true;
        }

        BlockLightningRodBase rod = (BlockLightningRodBase) Block.get((getCopperId(waxed, getOxidizationLevel())));
        rod.setDamage(getDamage());

        return getValidLevel().setBlock(this, rod);
    }

    protected int getCopperId(boolean waxed, @Nullable OxidizationLevel oxidizationLevel) {
        if (oxidizationLevel == null) {
            return getId();
        }
        switch (oxidizationLevel) {
            case UNAFFECTED:
                return waxed? WAXED_LIGHTNING_ROD : LIGHTNING_ROD;
            case EXPOSED:
                return waxed? WAXED_EXPOSED_LIGHTNING_ROD : EXPOSED_LIGHTNING_ROD;
            case WEATHERED:
                return waxed? WAXED_WEATHERED_LIGHTNING_ROD : WEATHERED_LIGHTNING_ROD;
            case OXIDIZED:
                return waxed? WAXED_OXIDIZED_LIGHTNING_ROD : OXIDIZED_LIGHTNING_ROD;
            default:
                return getId();
        }
    }

    @Override
    public boolean isWaxed() {
        return false;
    }
}