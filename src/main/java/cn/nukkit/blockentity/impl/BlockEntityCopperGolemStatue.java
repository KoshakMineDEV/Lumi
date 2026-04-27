package cn.nukkit.blockentity.impl;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author Buddelbubi
 * @see <a href="https://github.com/GeyserMC/Geyser/blob/master/core/src/main/java/org/geysermc/geyser/translator/level/block/entity/CopperBlockEntityTranslator.java#L35">NBT Info</a>
 */
public class BlockEntityCopperGolemStatue extends BlockEntitySpawnable {
    public BlockEntityCopperGolemStatue(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        super.initBlockEntity();
        if(this.namedTag.containsInt("Pose")) {
            this.setPose(CopperPose.STANDING);
        }
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return BlockEntity.getDefaultCompound(this, BlockEntityID.COPPER_GOLEM_STATUE)
                .putBoolean("isMovable", false)
                .putInt("Pose", this.namedTag.getInt("Pose")
                );
    }

    public void setPose(CopperPose pose) {
        this.namedTag.putInt("Pose", pose.ordinal());
    }

    public CopperPose getPose() {
        return CopperPose.values()[this.namedTag.getInt("Pose")];
    }

    public enum CopperPose {
        STANDING,
        SITTING,
        RUNNING,
        STAR
    }

}