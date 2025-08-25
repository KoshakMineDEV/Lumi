package cn.nukkit.blockentity.impl;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.nbt.tag.CompoundTag;

public class BlockEntityTarget extends BlockEntity {

    public BlockEntityTarget(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public int getActivePower() {
        return NukkitMath.clamp(namedTag.getInt("activePower"), 0, 15);
    }

    public void setActivePower(int power) {
        namedTag.putInt("activePower", power);
    }
}