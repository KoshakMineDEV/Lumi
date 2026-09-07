package cn.nukkit.entity;

import cn.nukkit.block.Block;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * Base class for creatures whose motion is integrated by the server each tick.
 * Physics runs independently of AI behaviors.
 */
public abstract class EntityPhysical extends EntityCreature {

    private static final double AIR_DRAG = 0.91;
    private static final double GROUND_CONTACT_PROBE = -0.00001;
    private static final double MOTION_EPSILON = 0.00001;

    private double movementBlockFriction = Block.DEFAULT_FRICTION_FACTOR;
    private double horizontalMovementDragOverride = Double.NaN;
    private int fallingTick;

    public EntityPhysical(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public AxisAlignedBB getAABB() {
        return this.getBoundingBox();
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

    public final void addTmpMoveMotion(Vector3 motion) {
        this.addTmpMoveMotion(motion.x, motion.y, motion.z);
    }

    public final void addTmpMoveMotionXZ(Vector3 motion) {
        this.addTmpMoveMotionXZ(motion.x, motion.z);
    }

    /**
     * Returns the block friction sampled for the current movement tick.
     * Acceleration and post-movement damping use the same sample.
     */
    public final double getMovementBlockFriction() {
        return this.movementBlockFriction;
    }

    /**
     * Requests an environment-specific horizontal drag for the current physics tick.
     * If several blocks request a drag, the strongest damping wins.
     *
     * @param drag velocity multiplier in the range {@code [0, 1]}
     */
    @Override
    public void requestHorizontalMovementDrag(double drag) {
        if (!Double.isFinite(drag) || drag < 0 || drag > 1) {
            throw new IllegalArgumentException("Horizontal movement drag must be finite and in the range [0, 1]");
        }

        this.horizontalMovementDragOverride = selectHorizontalMovementDrag(
                this.horizontalMovementDragOverride,
                drag
        );
    }

    static double selectHorizontalMovementDrag(double currentDrag, double requestedDrag) {
        return Double.isNaN(currentDrag) || requestedDrag < currentDrag ? requestedDrag : currentDrag;
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
                .getTickCachedBlock(blockX, blockY, blockZ, 0, false)
                .getFrictionFactor();
    }

    protected static double getHorizontalMovementDrag(boolean grounded, double blockFriction) {
        return grounded ? blockFriction * AIR_DRAG : AIR_DRAG;
    }

    static double resolveHorizontalMovementDrag(boolean grounded, double blockFriction, double override) {
        return Double.isNaN(override) ? getHorizontalMovementDrag(grounded, blockFriction) : override;
    }

    static double movementYWithGroundContactProbe(boolean grounded, double motionY) {
        return grounded && motionY == 0 ? GROUND_CONTACT_PROBE : motionY;
    }

    protected static double getVerticalMovementDrag(float drag) {
        return 1.0 - drag;
    }

    public boolean isFalling() {
        return this.getGravity() > 0 && !this.onGround && this.y < this.highestPosition;
    }

    public int getFallingTick() {
        return this.fallingTick;
    }

    @Override
    public void resetFallDistance() {
        this.fallingTick = 0;
        super.resetFallDistance();
    }

    @Override
    public boolean isSubmerged() {
        return isSubmerged(true);
    }

    /**
     * Prepares motion after friction has been sampled and before movement.
     * Subclasses can apply input or run their movement controllers here.
     */
    protected void prepareMotion(int tickDiff) {
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        this.horizontalMovementDragOverride = Double.NaN;
        boolean updated = super.entityBaseTick(tickDiff);
        if (!this.isImmobile()) {
            boolean groundedForMovement = this.onGround;
            this.prepareMovementBlockFriction(groundedForMovement);
            this.prepareMotion(tickDiff);
            this.move(
                    this.motionX,
                    movementYWithGroundContactProbe(groundedForMovement, this.motionY),
                    this.motionZ
            );

            float gravity = this.getGravity();
            if (gravity <= 0) {
                this.resetFallDistance();
            } else if (!this.onGround) {
                if (this.y > this.highestPosition) {
                    this.highestPosition = this.y;
                    this.fallingTick = 0;
                } else if (this.y < this.highestPosition) {
                    this.fallingTick++;
                }
            } else {
                this.fallingTick = 0;
            }

            if (this.onGround && this.motionY < 0) {
                this.motionY = 0;
            } else if (!this.onGround && gravity > 0) {
                this.motionY -= gravity;
            }

            this.motionY *= getVerticalMovementDrag(this.getDrag());

            double horizontalDrag = resolveHorizontalMovementDrag(
                    groundedForMovement,
                    this.movementBlockFriction,
                    this.horizontalMovementDragOverride
            );
            this.motionX *= horizontalDrag;
            this.motionZ *= horizontalDrag;

            if (Math.abs(this.motionX) < MOTION_EPSILON) {
                this.motionX = 0;
            }
            if (Math.abs(this.motionY) < MOTION_EPSILON) {
                this.motionY = 0;
            }
            if (Math.abs(this.motionZ) < MOTION_EPSILON) {
                this.motionZ = 0;
            }

            updated = true;
        }
        return updated;
    }
}
