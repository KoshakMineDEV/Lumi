package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.data.Faceable;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.block.properties.enums.OxidizationLevel;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.impl.BlockEntityCopperGolemStatue;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static cn.nukkit.blockentity.impl.BlockEntityCopperGolemStatue.CopperPose;

/**
 * @author keksdev
 */
public abstract class BlockCopperGolemStatueBase extends BlockTransparentMeta implements Oxidizable, Waxable, Faceable, BlockEntityHolder<BlockEntityCopperGolemStatue>, BlockPropertiesHelper {
    private static final BlockProperties PROPERTIES = new BlockProperties(VanillaProperties.CARDINAL_DIRECTION);

    public BlockCopperGolemStatueBase() {
        this(0);
    }

    public BlockCopperGolemStatueBase(int meta) {
        super(meta);
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
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public int onTouch(@NotNull Vector3 vector, @NotNull Item item, @NotNull BlockFace blockFace, float fx, float fy, float fz, @org.jetbrains.annotations.Nullable Player player, PlayerInteractEvent.Action action) {
        if (action ==  PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (item.isAxe() && !isWaxed()) {
                OxidizationLevel oxidizationLevel = getOxidizationLevel();
                if (OxidizationLevel.UNAFFECTED.equals(oxidizationLevel)) {
                    //TODO: Spawn copper golem entity after implementing it
                    return 1;
                }
            }
            if (player != null && player.getInventory().getItemInHand().isNull()) {
                BlockEntityCopperGolemStatue blockEntity = this.getOrCreateBlockEntity();
                CopperPose[] poses = CopperPose.values();
                blockEntity.setPose(poses[(blockEntity.getPose().ordinal() + 1) % poses.length]);
                blockEntity.spawnToAll();
                return 1;
            } else {
                boolean activated = Waxable.super.onActivate(item, player)
                        || Oxidizable.super.onActivate(item, player);
                return activated ? 1 : 0;
            }
        }
        return 0;
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, Player player) {
        setBlockFace(player != null ? player.getDirection().getOpposite() : BlockFace.SOUTH);
        this.getLevel().setBlock(this, this, true);
        return true;
    }

    @Override
    public int onUpdate(int type) {
        return Oxidizable.super.onUpdate(type);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public Block getStateWithOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        return Block.get(getCopperId(isWaxed(), oxidizationLevel), this.getDamage());
    }

    @Override
    public boolean setOxidizationLevel(@NotNull OxidizationLevel oxidizationLevel) {
        if (getOxidizationLevel().equals(oxidizationLevel)) {
            return true;
        }
        return getValidLevel().setBlock(this, Block.get(getCopperId(isWaxed(), oxidizationLevel), this.getDamage()));
    }

    @Override
    public boolean setWaxed(boolean waxed) {
        if (isWaxed() == waxed) {
            return true;
        }
        return getValidLevel().setBlock(this, Block.get(getCopperId(waxed, getOxidizationLevel()), this.getDamage()));
    }

    @Override
    public boolean isWaxed() {
        return false;
    }

    private int getCopperId(boolean waxed, @Nullable OxidizationLevel oxidizationLevel) {
        if (oxidizationLevel == null) {
            return getId();
        }
        return switch (oxidizationLevel) {
            case UNAFFECTED -> waxed ? WAXED_COPPER_GOLEM_STATUE : COPPER_GOLEM_STATUE;
            case EXPOSED -> waxed ? WAXED_EXPOSED_COPPER_GOLEM_STATUE : EXPOSED_COPPER_GOLEM_STATUE;
            case WEATHERED -> waxed ? WAXED_WEATHERED_COPPER_GOLEM_STATUE : WEATHERED_COPPER_GOLEM_STATUE;
            case OXIDIZED -> waxed ? WAXED_OXIDIZED_COPPER_GOLEM_STATUE : OXIDIZED_COPPER_GOLEM_STATUE;
        };
    }

    @Override
    public void setBlockFace(BlockFace face) {
        this.setPropertyValue(VanillaProperties.CARDINAL_DIRECTION, face);
    }

    @Override
    public BlockFace getBlockFace() {
        return getPropertyValue(VanillaProperties.CARDINAL_DIRECTION);
    }

    @Override
    public @NotNull Class<? extends BlockEntityCopperGolemStatue> getBlockEntityClass() {
        return BlockEntityCopperGolemStatue.class;
    }

    @Override
    public @NotNull String getBlockEntityType() {
        return BlockEntityID.COPPER_GOLEM_STATUE;
    }
}