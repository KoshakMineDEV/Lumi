package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.EntityIntelligent;

/**
 * Applies three-dimensional movement toward the current route waypoint.
 * Used by flying and swimming entities.
 */
public class SpaceMoveController implements Controller {

    private static final double MIN_DISTANCE_SQUARED = 2.5000003e-7;
    private static final float DEFAULT_MAX_TURN = 90.0f;

    private final float maxTurn;

    public SpaceMoveController() {
        this(DEFAULT_MAX_TURN);
    }

    public SpaceMoveController(float maxTurn) {
        if (!Float.isFinite(maxTurn) || maxTurn < 0) {
            throw new IllegalArgumentException("maxTurn must be finite and non-negative");
        }
        this.maxTurn = maxTurn;
    }

    @Override
    public boolean control(EntityIntelligent entity) {
        if (!entity.hasMoveDirection() || entity.shouldUpdateMoveDirection()) {
            return false;
        }

        var direction = entity.getMoveDirectionEnd();
        if (direction == null) {
            return false;
        }

        float speed = entity.getMovementSpeed();
        if (!(speed > 0)) {
            return false;
        }

        double dx = direction.x - entity.x;
        double dy = direction.y - entity.y;
        double dz = direction.z - entity.z;
        double distanceSquared = squaredLength(dx, dy, dz);
        if (!(distanceSquared > MIN_DISTANCE_SQUARED)) {
            needNewDirection(entity);
            return false;
        }

        double distance = Math.sqrt(distanceSquared);
        double factor = movementAcceleration(speed) / distance;
        entity.addTmpMoveMotion(dx * factor, dy * factor, dz * factor);

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double desiredYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double desiredPitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance));
        entity.yaw = rotateTowards(entity.yaw, desiredYaw, 90.0);
        entity.headYaw = entity.yaw;
        entity.pitch = rotateTowards(entity.pitch, desiredPitch, this.maxTurn);
        return true;
    }

    static double movementAcceleration(float speed) {
        return speed * WalkController.AIR_ACCELERATION;
    }

    static double squaredLength(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    static double rotateTowards(double current, double target, double maximumChange) {
        double delta = wrapDegrees(target - current);
        delta = Math.max(-maximumChange, Math.min(maximumChange, delta));
        return current + delta;
    }

    static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360.0;
        if (wrapped >= 180.0) {
            wrapped -= 360.0;
        }
        if (wrapped < -180.0) {
            wrapped += 360.0;
        }
        return wrapped;
    }

    protected void needNewDirection(EntityIntelligent entity) {
        entity.setShouldUpdateMoveDirection(true);
    }
}
