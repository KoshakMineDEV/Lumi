package cn.nukkit.entity.projectile;

import cn.nukkit.Difficulty;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityEffectUpdateEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EntityShulkerBullet extends EntityProjectile {

    public static final int NETWORK_ID = 76;

    private static final double SEGMENT_SPEED = 0.15;
    private static final double TARGET_DELTA_ACCELERATION = 1.025;
    private static final double MOTION_INTERPOLATION = 0.2;
    private static final double LOST_TARGET_GRAVITY = 0.04;
    private static final int TARGET_RESOLVE_INTERVAL = 20;

    private Entity finalTarget;
    private UUID finalTargetUniqueId;
    private BlockFace currentMoveDirection;
    private int flightSteps;
    private int targetResolveCooldown;
    private Entity tickTarget;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getDrag() {
        return 0;
    }

    @Override
    public float getWidth() {
        return 0.3125f;
    }

    @Override
    public float getLength() {
        return 0.3125f;
    }

    @Override
    public float getGravity() {
        return (float) LOST_TARGET_GRAVITY;
    }

    @Override
    public float getHeight() {
        return 0.3125f;
    }

    public EntityShulkerBullet(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityShulkerBullet(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    public EntityShulkerBullet(FullChunk chunk, CompoundTag nbt, Entity shootingEntity,
                               Entity target, BlockFace.Axis invalidStartAxis) {
        super(chunk, nbt, shootingEntity);
        if (shootingEntity != null) {
            this.setPosition(new Vector3(
                    (shootingEntity.boundingBox.getMinX() + shootingEntity.boundingBox.getMaxX()) * 0.5,
                    (shootingEntity.boundingBox.getMinY() + shootingEntity.boundingBox.getMaxY()) * 0.5,
                    (shootingEntity.boundingBox.getMinZ() + shootingEntity.boundingBox.getMaxZ()) * 0.5
            ));
        }
        this.currentMoveDirection = BlockFace.UP;
        this.setTarget(target, invalidStartAxis);
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.flightSteps = this.namedTag.getInt("Steps");
        this.targetDeltaX = this.namedTag.getDouble("TXD");
        this.targetDeltaY = this.namedTag.getDouble("TYD");
        this.targetDeltaZ = this.namedTag.getDouble("TZD");

        if (this.namedTag.contains("Dir")) {
            int directionIndex = this.namedTag.getByte("Dir");
            this.currentMoveDirection = directionIndex >= 0 && directionIndex < BlockFace.values().length
                    ? BlockFace.fromIndex(directionIndex)
                    : null;
        }
        if (this.namedTag.containsString("Target")) {
            try {
                this.finalTargetUniqueId = UUID.fromString(this.namedTag.getString("Target"));
            } catch (IllegalArgumentException ignored) {
                this.finalTargetUniqueId = null;
            }
        }
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (this.server.getDifficulty() == Difficulty.PEACEFUL) {
            this.close();
            return false;
        }

        Entity target = this.resolveTarget();
        this.tickTarget = target;
        boolean updated;
        try {
            updated = super.onUpdate(currentTick);
        } finally {
            this.tickTarget = null;
        }
        if (this.closed) {
            return false;
        }

        if (target != null) {
            if (this.flightSteps > 0 && --this.flightSteps == 0) {
                this.selectNextMoveDirection(
                        this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis(),
                        target
                );
            }

            if (this.currentMoveDirection != null) {
                int currentX = this.getFloorX();
                int currentY = this.getFloorY();
                int currentZ = this.getFloorZ();
                BlockFace.Axis axis = this.currentMoveDirection.getAxis();
                int nextX = currentX + this.currentMoveDirection.getXOffset();
                int nextY = currentY + this.currentMoveDirection.getYOffset();
                int nextZ = currentZ + this.currentMoveDirection.getZOffset();

                if (!this.level.getBlock(nextX, nextY, nextZ, false).canPassThrough()
                        || isAlignedWithTarget(axis, currentX, currentY, currentZ, target)) {
                    this.selectNextMoveDirection(axis, target);
                }
            }
        }
        return updated || !this.closed;
    }

    @Override
    protected void updateMotion() {
        Entity target = this.tickTarget;
        if (!isUsableTarget(target)) {
            this.motionY -= LOST_TARGET_GRAVITY;
            return;
        }

        this.targetDeltaX = accelerateTargetDelta(this.targetDeltaX);
        this.targetDeltaY = accelerateTargetDelta(this.targetDeltaY);
        this.targetDeltaZ = accelerateTargetDelta(this.targetDeltaZ);
        this.motionX = interpolateMotion(this.motionX, this.targetDeltaX);
        this.motionY = interpolateMotion(this.motionY, this.targetDeltaY);
        this.motionZ = interpolateMotion(this.motionZ, this.targetDeltaZ);
    }

    @Override
    public void updateRotation() {
        double horizontal = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        if (horizontal * horizontal + this.motionY * this.motionY < 1.0e-12) {
            return;
        }

        double desiredYaw = Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
        double desiredPitch = -Math.toDegrees(Math.atan2(this.motionY, horizontal));
        this.yaw = lerpRotation(this.yaw, desiredYaw, 0.5);
        this.headYaw = this.yaw;
        this.pitch = lerpRotation(this.pitch, desiredPitch, 0.5);
    }

    @Override
    protected double getBaseDamage() {
        return 4;
    }

    @Override
    public int getResultDamage() {
        return 4;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (this.closed || !source.call()) {
            return false;
        }

        this.setLastDamageCause(source);
        this.level.addSoundToViewers(this, Sound.MOB_SHULKER_BULLET_HIT);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 15; i++) {
            this.level.addParticle(new CriticalParticle(new Vector3(
                    this.x + random.nextDouble(-0.2, 0.2),
                    this.y + random.nextDouble(-0.2, 0.2),
                    this.z + random.nextDouble(-0.2, 0.2)
            )));
        }
        this.close();
        return true;
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        super.onCollideWithEntity(entity);
        if (this.hadCollision && entity instanceof EntityLiving livingEntity) {
            livingEntity.addEffect(
                    Effect.get(EffectType.LEVITATION).setDuration(200),
                    EntityEffectUpdateEvent.Cause.ATTACK
            );
        }
    }

    @Override
    protected void onHitGround(Vector3 moveTarget) {
        super.onHitGround(moveTarget);
        this.level.addParticle(new ExplodeParticle(this));
        this.level.addParticle(new ExplodeParticle(this));
        this.level.addSoundToViewers(this, Sound.MOB_SHULKER_BULLET_HIT);
        this.close();
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        UUID targetUniqueId = this.finalTarget != null
                ? this.finalTarget.getUniqueId()
                : this.finalTargetUniqueId;
        if (targetUniqueId == null) {
            this.namedTag.remove("Target");
        } else {
            this.namedTag.putString("Target", targetUniqueId.toString());
        }
        this.namedTag.putByte("Dir", this.currentMoveDirection == null
                ? -1
                : this.currentMoveDirection.getIndex());
        this.namedTag.putInt("Steps", this.flightSteps);
        this.namedTag.putDouble("TXD", this.targetDeltaX);
        this.namedTag.putDouble("TYD", this.targetDeltaY);
        this.namedTag.putDouble("TZD", this.targetDeltaZ);
    }

    public Entity getTarget() {
        return this.finalTarget;
    }

    public void setTarget(Entity target) {
        this.setTarget(target, null);
    }

    public void setTarget(Entity target, BlockFace.Axis invalidStartAxis) {
        this.finalTarget = target;
        this.finalTargetUniqueId = target == null ? null : target.getUniqueId();
        this.targetResolveCooldown = 0;
        this.selectNextMoveDirection(invalidStartAxis, target);
    }

    private Entity resolveTarget() {
        if (this.finalTarget != null
                && !this.finalTarget.isClosed()
                && this.finalTarget.getLevel() == this.level
                && (this.finalTargetUniqueId == null
                || this.finalTargetUniqueId.equals(this.finalTarget.getUniqueId()))) {
            return this.finalTarget;
        }
        this.finalTarget = null;

        if (this.finalTargetUniqueId == null) {
            return null;
        }
        if (this.targetResolveCooldown > 0) {
            this.targetResolveCooldown--;
            return null;
        }
        this.targetResolveCooldown = TARGET_RESOLVE_INTERVAL - 1;
        for (Entity candidate : this.level.getEntities()) {
            if (!candidate.isClosed() && this.finalTargetUniqueId.equals(candidate.getUniqueId())) {
                this.finalTarget = candidate;
                if (this.flightSteps <= 0) {
                    this.selectNextMoveDirection(
                            this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis(),
                            candidate
                    );
                }
                return candidate;
            }
        }
        return null;
    }

    private void selectNextMoveDirection(BlockFace.Axis invalidAxis, Entity target) {
        double targetHalfHeight = target == null
                ? 0.5
                : (target.boundingBox.getMaxY() - target.boundingBox.getMinY()) * 0.5;
        int targetBlockX = target == null ? this.getFloorX() : NukkitMath.floorDouble(target.x);
        int targetBlockY = target == null
                ? this.getFloorY() - 1
                : NukkitMath.floorDouble(target.y + targetHalfHeight);
        int targetBlockZ = target == null ? this.getFloorZ() : NukkitMath.floorDouble(target.z);

        double targetX = targetBlockX + 0.5;
        double targetY = targetBlockY + targetHalfHeight;
        double targetZ = targetBlockZ + 0.5;
        BlockFace selection = null;
        double blockCenterX = targetBlockX + 0.5 - this.x;
        double blockCenterY = targetBlockY + 0.5 - this.y;
        double blockCenterZ = targetBlockZ + 0.5 - this.z;
        boolean targetIsFar = blockCenterX * blockCenterX
                + blockCenterY * blockCenterY
                + blockCenterZ * blockCenterZ >= 4.0;

        if (targetIsFar) {
            int currentX = this.getFloorX();
            int currentY = this.getFloorY();
            int currentZ = this.getFloorZ();
            BlockFace xCandidate = null;
            BlockFace yCandidate = null;
            BlockFace zCandidate = null;
            int candidateCount = 0;

            if (invalidAxis != BlockFace.Axis.X) {
                BlockFace candidate = currentX < targetBlockX ? BlockFace.EAST
                        : currentX > targetBlockX ? BlockFace.WEST : null;
                if (candidate != null && isEmpty(currentX, currentY, currentZ, candidate)) {
                    xCandidate = candidate;
                    candidateCount++;
                }
            }
            if (invalidAxis != BlockFace.Axis.Y) {
                BlockFace candidate = currentY < targetBlockY ? BlockFace.UP
                        : currentY > targetBlockY ? BlockFace.DOWN : null;
                if (candidate != null && isEmpty(currentX, currentY, currentZ, candidate)) {
                    yCandidate = candidate;
                    candidateCount++;
                }
            }
            if (invalidAxis != BlockFace.Axis.Z) {
                BlockFace candidate = currentZ < targetBlockZ ? BlockFace.SOUTH
                        : currentZ > targetBlockZ ? BlockFace.NORTH : null;
                if (candidate != null && isEmpty(currentX, currentY, currentZ, candidate)) {
                    zCandidate = candidate;
                    candidateCount++;
                }
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            selection = BlockFace.random(random);
            if (candidateCount == 0) {
                for (int attempts = 5;
                     !isEmpty(currentX, currentY, currentZ, selection) && attempts > 0;
                     attempts--) {
                    selection = BlockFace.random(random);
                }
            } else {
                int selectedCandidate = random.nextInt(candidateCount);
                if (xCandidate != null && selectedCandidate-- == 0) {
                    selection = xCandidate;
                } else if (yCandidate != null && selectedCandidate-- == 0) {
                    selection = yCandidate;
                } else {
                    selection = zCandidate;
                }
            }

            targetX = this.x + selection.getXOffset();
            targetY = this.y + selection.getYOffset();
            targetZ = this.z + selection.getZOffset();
        }

        this.currentMoveDirection = selection;
        setTargetDelta(targetX - this.x, targetY - this.y, targetZ - this.z);
        this.flightSteps = 10 + ThreadLocalRandom.current().nextInt(5) * 10;
    }

    private void setTargetDelta(double x, double y, double z) {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) {
            this.targetDeltaX = 0;
            this.targetDeltaY = 0;
            this.targetDeltaZ = 0;
            return;
        }
        double scale = SEGMENT_SPEED / length;
        this.targetDeltaX = x * scale;
        this.targetDeltaY = y * scale;
        this.targetDeltaZ = z * scale;
    }

    private boolean isEmpty(int x, int y, int z, BlockFace direction) {
        return this.level.getBlock(
                x + direction.getXOffset(),
                y + direction.getYOffset(),
                z + direction.getZOffset(),
                false
        ).isAir();
    }

    private static boolean isAlignedWithTarget(BlockFace.Axis axis,
                                               int x, int y, int z, Entity target) {
        return switch (axis) {
            case X -> x == target.getFloorX();
            case Y -> y == target.getFloorY();
            case Z -> z == target.getFloorZ();
        };
    }

    private static boolean isUsableTarget(Entity target) {
        return target != null
                && target.isAlive()
                && (!(target instanceof Player player) || !player.isSpectator());
    }

    static double lerpRotation(double current, double target, double amount) {
        double delta = (target - current) % 360.0;
        if (delta >= 180.0) {
            delta -= 360.0;
        }
        if (delta < -180.0) {
            delta += 360.0;
        }
        return current + delta * amount;
    }

    static double accelerateTargetDelta(double targetDelta) {
        return NukkitMath.clamp(targetDelta * TARGET_DELTA_ACCELERATION, -1.0, 1.0);
    }

    static double interpolateMotion(double motion, double targetDelta) {
        return motion + (targetDelta - motion) * MOTION_INTERPOLATION;
    }
}
