package cn.nukkit.blockentity.impl;

import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockSculkSensor;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.level.vibration.VibrationListener;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author Kevims KCodeYT
 */

public class BlockEntitySculkSensor extends BlockEntity implements VibrationListener {

    protected long lastActiveTime = getLevel().getCurrentTick();
    protected VibrationEvent lastVibrationEvent;

    protected int power = 0;

    protected int comparatorPower = 0;

    protected boolean waitForVibration = false;

    public BlockEntitySculkSensor(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        this.level.getVibrationManager().addListener(this);
    }

    @Override
    public void onBreak() {
        this.level.getVibrationManager().removeListener(this);
    }

    @Override
    public void close() {
        this.level.getVibrationManager().removeListener(this);
        super.close();
    }

    @Override
    public boolean isBlockEntityValid() {
        return getLevelBlock().getId() == BlockID.SCULK_SENSOR;
    }

    @Override
    public Position getListenerVector() {
        return this.getLocation().setLevel(this.level).floor().add(0.5f, 0.5f, 0.5f);
    }

    @Override
    public boolean onVibrationOccur(VibrationEvent event) {
        if (this.isBlockEntityValid() && !(this.level.getBlock(event.source()) instanceof BlockSculkSensor)) {
            boolean canBeActive = (getLevel().getCurrentTick() - lastActiveTime) > 40 && !waitForVibration;
            if (canBeActive) waitForVibration = true;
            return canBeActive;
        } else {
            return false;
        }
    }

    @Override
    public void onVibrationArrive(VibrationEvent event) {
        if (this.level != null && this.isBlockEntityValid()) {
            this.lastVibrationEvent = event;
            this.updateLastActiveTime();
            waitForVibration = false;

            calPower();

            var block = (BlockSculkSensor) this.getBlock();
            block.setPhase(1);
            block.updateAroundRedstone();
            level.scheduleUpdate(block, 41);
        }
    }

    public VibrationEvent getLastVibrationEvent() {
        return this.lastVibrationEvent;
    }

    public long getLastActiveTime() {
        return this.lastActiveTime;
    }

    public int getPower() {
        return power;
    }

    public int getComparatorPower() {
        return comparatorPower;
    }

    @Override
    public double getListenRange() {
        return 8;
    }

    protected void updateLastActiveTime() {
        this.lastActiveTime = getLevel().getCurrentTick();
    }

    public void calPower() {
        var event = this.getLastVibrationEvent();
        if ((this.level.getCurrentTick() - this.getLastActiveTime()) >= 40 || event == null) {
            power = 0;
            comparatorPower = 0;
            return;
        }
        comparatorPower = event.type().getFrequency();
        power = Math.max(1, 15 - (int) Math.floor(event.source().distance(this.add(0.5, 0.5, 0.5)) * 1.875));
    }
}