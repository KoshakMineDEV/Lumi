package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.EntityIntelligent;

/**
 * Ground walking movement controller. Walks the entity toward its current
 * move direction.
 *
 * @author daoge_cmd
 */
public class WalkController implements Controller {

    protected static final int JUMP_COOL_DOWN = 10;
    protected static final double AIR_ACCELERATION = 0.02;
    protected static final double GROUND_ACCELERATION_NUMERATOR = 0.21600002;
    protected static final double DEFAULT_BLOCK_FRICTION = 0.6;

    protected int currentJumpCoolDown = JUMP_COOL_DOWN;

    @Override
    public boolean control(EntityIntelligent entity) {
        currentJumpCoolDown++;

        if (!entity.hasMoveDirection()) {
            return false;
        }

        var end = entity.getMoveDirectionEnd();
        if (end == null) {
            return false;
        }

        float speed = entity.getMovementSpeed();

        double dx = end.x - entity.x;
        double dz = end.z - entity.z;
        double horizontalDistSq = dx * dx + dz * dz;
        double horizontalDist = Math.sqrt(horizontalDistSq);

        // If the entity is already at (or extremely close to) the waypoint,
        // skip this tick — updateRoute() will advance on the next tick
        if (horizontalDist < 0.01) {
            return false;
        }

        // Match Java Edition's MoveControl + LivingEntity.travel pipeline.
        // MoveControl sets both speed and forward input to movementSpeed, so
        // ground acceleration is speed squared. Existing motion is preserved;
        // matching block/air drag is applied after movement by EntityIntelligent.
        if (speed <= 0) {
            return false;
        }

        double acceleration = movementAcceleration(
                speed,
                entity.isOnGround(),
                entity.getMovementBlockFriction()
        );
        double factor = acceleration / horizontalDist;
        double accelerationX = dx * factor;
        double accelerationZ = dz * factor;

        // Jump logic:
        // Condition A: target waypoint is above step height and entity is horizontally close
        // Condition B: the next horizontal movement would collide while chasing a higher waypoint
        // Condition C: entity is stuck inside a block's collision shape at feet level
        double motionY = 0;
        if (entity.isOnGround() && currentJumpCoolDown >= JUMP_COOL_DOWN) {
            double dy = end.y - entity.y;
            double entityWidth = entity.getAABB().getMaxX() - entity.getAABB().getMinX();
            // The wider threshold is required because the
            // pathfinder includes diagonal neighbors, where horizontal distance can reach ~√2
            boolean shouldJump = dy > 0.6
                    && horizontalDistSq < Math.max(2.25, entityWidth);

            if (!shouldJump) {
                shouldJump = dy > 0 && collidesBlocks(entity, accelerationX, accelerationZ);
            }

            if (!shouldJump) {
                int blockX = (int) Math.floor(entity.x);
                int blockY = (int) Math.floor(entity.y);
                int blockZ = (int) Math.floor(entity.z);
                var block = entity.getLevel().getBlock(blockX, blockY, blockZ);
                var blockBox = block.getCollisionBoundingBox();
                if (blockBox != null) {
                    shouldJump = entity.y < blockBox.getMaxY();
                }
            }

            if (shouldJump) {
                motionY = 0.42;
                currentJumpCoolDown = 0;
            }
        }

        entity.addTmpMoveMotionXZ(accelerationX, accelerationZ);
        if (motionY != 0) {
            entity.addTmpMoveMotion(0, motionY, 0);
        }

        return true;
    }

    static double movementAcceleration(float speed, boolean grounded, double blockFriction) {
        if (!grounded) {
            return speed * AIR_ACCELERATION;
        }

        double frictionInfluence = blockFriction > DEFAULT_BLOCK_FRICTION
                ? GROUND_ACCELERATION_NUMERATOR
                    / (blockFriction * blockFriction * blockFriction)
                : 1.0;
        return speed * speed * frictionInfluence;
    }

    protected boolean collidesBlocks(EntityIntelligent entity, double dx, double dz) {
        var aabb = entity.getAABB().getOffsetBoundingBox(dx, 0, dz);
        return entity.getLevel().hasCollision(entity.getLivingEntity(), aabb, false);
    }
}
