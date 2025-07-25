package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.properties.enums.OxidizationLevel;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockCopperDoorBase extends BlockDoor implements Oxidizable, Waxable {

    public BlockCopperDoorBase() {
        this(0);
    }

    public BlockCopperDoorBase(int meta) {
        super(meta);
    }

    @Override
    public double getHardness() {
        return 3;
    }

    @Override
    public double getResistance() {
        return 3;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_STONE;
    }

    @Override
    public boolean onActivate(@NotNull Item item, Player player) {
       if (player != null) {
           if (player.isSneaking()) {
               if (Waxable.super.onActivate(item, player) || Oxidizable.super.onActivate(item, player)) {
                   return true;
               }
           }
       }
        return super.onActivate(item, player);
    }

    @Override
    public void playOpenSound() {
        this.level.addSound(this, Sound.OPEN_DOOR_COPPER);
    }

    @Override
    public void playCloseSound() {
        this.level.addSound(this, Sound.CLOSE_DOOR_COPPER);
    }

    @Override
    public Block getStateWithOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        return Block.get(getCopperId(isWaxed(), oxidizationLevel));
    }

    @Override
    public boolean setOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        if (getOxidizationLevel().equals(oxidizationLevel)) {
            return true;
        }
        return getValidLevel().setBlock(this, Block.get(getCopperId(isWaxed(), oxidizationLevel)));
    }

    @Override
    public boolean setWaxed(boolean waxed) {
        if (isWaxed() == waxed) {
            return true;
        }
        return getValidLevel().setBlock(this, Block.get(getCopperId(waxed, getOxidizationLevel())));
    }

    @Override
    public boolean isWaxed() {
        return false;
    }

    protected int getCopperId(boolean waxed, @Nullable OxidizationLevel oxidizationLevel) {
        if (oxidizationLevel == null) {
            return getId();
        }
        return switch (oxidizationLevel) {
            case UNAFFECTED -> waxed ? WAXED_COPPER_DOOR : COPPER_DOOR;
            case EXPOSED -> waxed ? WAXED_EXPOSED_COPPER_DOOR : EXPOSED_COPPER_DOOR;
            case WEATHERED -> waxed ? WAXED_WEATHERED_COPPER_DOOR : WEATHERED_COPPER_DOOR;
            case OXIDIZED -> waxed ? WAXED_OXIDIZED_COPPER_DOOR : OXIDIZED_COPPER_DOOR;
        };
    }
}
