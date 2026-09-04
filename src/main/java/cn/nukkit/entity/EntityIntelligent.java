package cn.nukkit.entity;

import cn.nukkit.block.Block;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroup;
import cn.nukkit.entity.ai.memory.MemoryStorage;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * Base class for living entities controlled by the Lumi AI system.
 */
public abstract class EntityIntelligent extends EntityCreature {

    private static final double AIR_DRAG = 0.91;

    private BehaviorGroup behaviorGroup;
    private double movementBlockFriction = Block.DEFAULT_FRICTION_FACTOR;

    public EntityIntelligent(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public BehaviorGroup getBehaviorGroup() {
        return this.behaviorGroup;
    }

    public void setBehaviorGroup(BehaviorGroup behaviorGroup) {
        this.behaviorGroup = behaviorGroup;
        if (behaviorGroup != null) {
            behaviorGroup.setEntity(this);
            behaviorGroup.getMemoryStorage().put(MemoryTypes.MOVEMENT_SPEED, super.getMovementSpeed());
        }
    }

    public MemoryStorage getMemoryStorage() {
        return getBehaviorGroup().getMemoryStorage();
    }

    public float getMovementSpeed() {
        if (this.behaviorGroup == null) {
            return super.getMovementSpeed();
        }
        return this.behaviorGroup.getMemoryStorage().get(MemoryTypes.MOVEMENT_SPEED);
    }

    public void setBaseMovementSpeed(float speed) {
        if (this.behaviorGroup != null) {
            this.behaviorGroup.getMemoryStorage().put(MemoryTypes.MOVEMENT_SPEED, speed);
        }

        super.setBaseMovementSpeed(speed);
    }

    public Vector3 getLookTarget() {
        return getMemoryStorage().get(MemoryTypes.LOOK_TARGET);
    }

    public void setLookTarget(Vector3 target) {
        getMemoryStorage().put(MemoryTypes.LOOK_TARGET, target);
    }

    public Vector3 getMoveTarget() {
        return getMemoryStorage().get(MemoryTypes.MOVE_TARGET);
    }

    public void setMoveTarget(Vector3 target) {
        getMemoryStorage().put(MemoryTypes.MOVE_TARGET, target);
    }

    public Vector3 getMoveDirectionStart() {
        return getMemoryStorage().get(MemoryTypes.MOVE_DIRECTION_START);
    }

    public void setMoveDirectionStart(Vector3 start) {
        getMemoryStorage().put(MemoryTypes.MOVE_DIRECTION_START, start);
    }

    public Vector3 getMoveDirectionEnd() {
        return getMemoryStorage().get(MemoryTypes.MOVE_DIRECTION_END);
    }

    public void setMoveDirectionEnd(Vector3 end) {
        getMemoryStorage().put(MemoryTypes.MOVE_DIRECTION_END, end);
    }

    public boolean hasMoveDirection() {
        return getMoveDirectionStart() != null && getMoveDirectionEnd() != null;
    }

    public boolean shouldUpdateMoveDirection() {
        return getMemoryStorage().get(MemoryTypes.SHOULD_UPDATE_MOVE_DIRECTION);
    }

    public void setShouldUpdateMoveDirection(boolean shouldUpdate) {
        getMemoryStorage().put(MemoryTypes.SHOULD_UPDATE_MOVE_DIRECTION, shouldUpdate);
    }

    public boolean isPitchEnabled() {
        return getMemoryStorage().get(MemoryTypes.ENABLE_PITCH);
    }

    public void setPitchEnabled(boolean enabled) {
        getMemoryStorage().put(MemoryTypes.ENABLE_PITCH, enabled);
    }

    public EntityLiving getLivingEntity() {
        return this;
    }

    public AxisAlignedBB getAABB() {
        return this.getBoundingBox();
    }

    public long getRuntimeId() {
        return super.getId();
    }

    public long getTick() {
        return this.ticksLived;
    }

    public final void addTmpMoveMotion(double x, double y, double z) {
        this.motionX += x;
        this.motionY += y;
        this.motionZ += z;
    }

    public final void addTmpMoveMotionXZ(double x, double z) {
        this.motionX += x;
        this.motionZ += z;
    }

    /**
     * Returns the friction of the block sampled for the current AI movement
     * tick. The value is prepared once before controllers run and reused by
     * both acceleration and post-movement damping.
     */
    public final double getMovementBlockFriction() {
        return this.movementBlockFriction;
    }

    protected void prepareMovementBlockFriction(boolean grounded) {
        if (!grounded) {
            this.movementBlockFriction = 1.0;
            return;
        }

        int blockX = NukkitMath.floorDouble(this.x);
        int blockY = NukkitMath.floorDouble(this.y - 0.500001);
        int blockZ = NukkitMath.floorDouble(this.z);
        this.movementBlockFriction = this.level
                .getBlock(this.chunk, blockX, blockY, blockZ, 0, false)
                .getFrictionFactor();
    }

    protected static double getHorizontalMovementDrag(boolean grounded, double blockFriction) {
        return grounded ? blockFriction * AIR_DRAG : AIR_DRAG;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean updated = super.entityBaseTick(tickDiff);
        if (this.behaviorGroup != null && !this.isImmobile()) {
            boolean groundedForMovement = this.onGround;
            this.prepareMovementBlockFriction(groundedForMovement);
            this.behaviorGroup.tick();
            this.move(this.motionX, this.motionY, this.motionZ);
            if (this.onGround && this.motionY < 0) {
                this.motionY = 0;
            } else if (!this.onGround) {
                this.motionY -= this.getGravity();
            }
            double horizontalDrag = getHorizontalMovementDrag(
                    groundedForMovement,
                    this.movementBlockFriction
            );
            this.motionX *= horizontalDrag;
            this.motionZ *= horizontalDrag;

            updateMovement();
            updated = true;
        }
        return updated;
    }
}
