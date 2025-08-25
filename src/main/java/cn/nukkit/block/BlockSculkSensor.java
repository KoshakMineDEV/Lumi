package cn.nukkit.block;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.impl.BlockEntitySculkSensor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.block.data.BlockColor;

public class BlockSculkSensor extends BlockTransparentMeta implements BlockEntityHolder<BlockEntitySculkSensor>, RedstoneComponent {

    public BlockSculkSensor() {
        super(0);
    }

    public BlockSculkSensor(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return SCULK_SENSOR;
    }

    @Override
    public String getName() {
        return "Sculk Sensor";
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_HOE;
    }

    @Override
    public double getHardness() {
        return 0.5;
    }

    @Override
    public double getResistance() {
        return 1.5;
    }

    @Override
    public int getLightLevel() {
        return 1;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BLACK_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.hasEnchantment(Enchantment.ID_SILK_TOUCH)) {
            return new Item[]{
                    this.toItem()
            };
        }
        return Item.EMPTY_ARRAY;
    }

    @Override
    public Class<? extends BlockEntitySculkSensor> getBlockEntityClass() {
        return BlockEntitySculkSensor.class;
    }

    @Override
    public String getBlockEntityType() {
        return BlockEntity.SCULK_SENSOR;
    }

    @Override
    public int onUpdate(int type) {
        getOrCreateBlockEntity();
        if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            this.getBlockEntity().calPower();
            this.setPhase(0);
            updateAroundRedstone();
            return type;
        }
        return 0;
    }

    @Override
    public int getStrongPower(BlockFace side) {
        return super.getStrongPower(side);
    }

    @Override
    public int getWeakPower(BlockFace face) {
        var blockEntity = this.getOrCreateBlockEntity();
        if (this.getSide(face.getOpposite()) instanceof BlockRedstoneComparator) {
            return blockEntity.getComparatorPower();
        } else {
            return blockEntity.getPower();
        }
    }


    public void setPhase(int phase) {
        if (phase == 1) this.level.addSound(this.add(0.5, 0.5, 0.5), Sound.POWER_ON_SCULK_SENSOR);
        else this.level.addSound(this.add(0.5, 0.5, 0.5), Sound.POWER_OFF_SCULK_SENSOR);
        this.setDamage(phase);
        this.level.setBlock(this, this, true, false);
    }
}