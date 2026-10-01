package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemString;
import cn.nukkit.level.Level;
import cn.nukkit.level.vibration.VanillaVibrationTypes;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;

/**
 * @author CreeperFace
 */
public class BlockTripWire extends BlockFlowable implements BlockPropertiesHelper {

    private static final BlockProperties PROPERTIES = new BlockProperties(
            VanillaProperties.POWERED_BIT, VanillaProperties.SUSPENDED_BIT,
            VanillaProperties.ATTACHED_BIT, VanillaProperties.DISARMED_BIT,
            VanillaProperties.CONNECTION_WEST, VanillaProperties.CONNECTION_SOUTH,
            VanillaProperties.CONNECTION_NORTH, VanillaProperties.CONNECTION_EAST);

    public BlockTripWire(int meta) {
        super(meta);
    }

    public BlockTripWire() {
        this(0);
    }

    @Override
    public int getId() {
        return TRIPWIRE;
    }

    @Override
    public String getName() {
        return "Tripwire";
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Override
    public Item toItem() {
        return new ItemString();
    }

    public boolean isPowered() {
        return this.getBooleanValue(VanillaProperties.POWERED_BIT);
    }

    public boolean isSuspended() {
        return this.getBooleanValue(VanillaProperties.SUSPENDED_BIT);
    }

    public boolean isAttached() {
        return this.getBooleanValue(VanillaProperties.ATTACHED_BIT);
    }

    public boolean isDisarmed() {
        return this.getBooleanValue(VanillaProperties.DISARMED_BIT);
    }

    public void setPowered(boolean value) {
        this.setBooleanValue(VanillaProperties.POWERED_BIT, value);
    }

    public void setSuspended(boolean value) {
        this.setBooleanValue(VanillaProperties.SUSPENDED_BIT, value);
    }

    public void setAttached(boolean value) {
        this.setBooleanValue(VanillaProperties.ATTACHED_BIT, value);
    }

    public void setDisarmed(boolean value) {
        this.setBooleanValue(VanillaProperties.DISARMED_BIT, value);
    }

    public boolean updateConnections() {
        int previous = this.getDamage();
        this.setBooleanValue(VanillaProperties.CONNECTION_WEST, this.canConnect(this.west(), BlockFace.WEST));
        this.setBooleanValue(VanillaProperties.CONNECTION_SOUTH, this.canConnect(this.south(), BlockFace.SOUTH));
        this.setBooleanValue(VanillaProperties.CONNECTION_NORTH, this.canConnect(this.north(), BlockFace.NORTH));
        this.setBooleanValue(VanillaProperties.CONNECTION_EAST, this.canConnect(this.east(), BlockFace.EAST));
        return this.getDamage() != previous;
    }

    private boolean canConnect(Block block, BlockFace face) {
        return block instanceof BlockTripWire
                || block instanceof BlockTripWireHook hook && hook.getFacing() == face.getOpposite();
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (!entity.doesTriggerPressurePlate()) {
            return;
        }

        boolean powered = this.isPowered();

        if (!powered) {
            this.setPowered(true);
            this.level.setBlock(this, this, true, true);
            this.updateHook(false);

            this.level.scheduleUpdate(this, 10);
        }
    }

    public void updateHook(boolean scheduleUpdate) {
        for (BlockFace side : new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST}) {
            for (int i = 1; i < 42; ++i) {
                Block block = this.getSide(side, i);

                if (block instanceof BlockTripWireHook) {
                    BlockTripWireHook hook = (BlockTripWireHook) block;

                    if (hook.getFacing() == side.getOpposite()) {
                        hook.calculateState(false, true, i, this);
                    }

                    /*if (scheduleUpdate) {
                        this.level.scheduleUpdate(hook, 10);
                    }*/
                    break;
                }

                if (block.getId() != Block.TRIPWIRE) {
                    break;
                }
            }
        }
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (this.updateConnections()) {
                this.level.setBlock(this, this, true);
            }
            return type;
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!isPowered()) {
                return type;
            }

            boolean found = false;
            Entity[] e = this.level.getCollidingEntities(this.getCollisionBoundingBox());
            for (Entity entity : e) {
                if (!entity.doesTriggerPressurePlate()) {
                    continue;
                }

                found = true;
            }

            if (found) {
                this.level.scheduleUpdate(this, 10);
            } else {
                this.setPowered(false);
                this.level.setBlock(this, this, true, true);
                this.updateHook(false);
            }
            return type;
        }

        return 0;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        this.updateConnections();
        this.getLevel().setBlock(this, this, true, true);
        this.updateHook(false);

        return true;
    }

    @Override
    public boolean onBreak(Item item) {
        if (item.getId() == Item.SHEARS) {
            this.setDisarmed(true);
            this.level.setBlock(this, this, true, true);
            this.updateHook(false);
            this.getLevel().setBlock(this, Block.get(BlockID.AIR), true, true);
            var pos = this.add(0.5, 0.5, 0.5);
            this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, pos, isPowered() ? VanillaVibrationTypes.BLOCK_ACTIVATE : VanillaVibrationTypes.BLOCK_DEACTIVATE));
        } else {
            this.setPowered(true);
            this.getLevel().setBlock(this, Block.get(BlockID.AIR), true, true);
            this.updateHook(true);
        }

        return true;
    }

    @Override
    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return new SimpleAxisAlignedBB(this.x, this.y, this.z, this.x + 1, this.y + 0.5, this.z + 1);
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.FLOW_INTO_BLOCK;
    }

    @Override
    public boolean canBeFlowedInto() {
        return false;
    }
}
