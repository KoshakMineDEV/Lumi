package cn.nukkit.entity;

import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroup;
import cn.nukkit.entity.ai.memory.MemoryStorage;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * Base class for living entities controlled by the Lumi AI system.
 */
public abstract class EntityIntelligent extends EntityPhysical {

    private BehaviorGroup behaviorGroup;

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

    public long getRuntimeId() {
        return super.getId();
    }

    public long getTick() {
        return this.ticksLived;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        var result = super.attack(source);
        var storage = getMemoryStorage();
        if (storage != null) {
            //TODO: uncomment: storage.put(MemoryTypes.BE_ATTACKED_EVENT, source);
            storage.put(MemoryTypes.LAST_BE_ATTACKED_TIME, getTick());
        }
        return result;
    }

    @Override
    protected void prepareMotion(int tickDiff) {
        super.prepareMotion(tickDiff);
        if (this.behaviorGroup != null) {
            this.behaviorGroup.tick();
        }
    }
}
