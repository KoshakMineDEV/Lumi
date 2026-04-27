package cn.nukkit.blockentity.impl;

import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class BlockEntityTrialSpawner extends BlockEntitySpawnable {
    public static final String TAG_X = "x";
    public static final String TAG_Y = "y";
    public static final String TAG_Z = "z";
    public static final String TAG_SPAWN_DATA = "spawn_data";
    public static final String TAG_TYPE_ID = "TypeId";
    public static final String TAG_WEIGHT = "Weight";

    public BlockEntityTrialSpawner(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return getDefaultCompound(this, BlockEntityID.TRIAL_SPAWNER)
                .putCompound(TAG_SPAWN_DATA, new CompoundTag()
                        .putString(TAG_TYPE_ID, "minecraft:pig")
                        .putInt(TAG_WEIGHT, 1)
                )
                .putInt(TAG_X, (int) this.x)
                .putInt(TAG_Y, (int) this.y)
                .putInt(TAG_Z, (int) this.z);
    }
}
