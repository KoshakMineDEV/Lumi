package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.EntityIntelligent;

/**
 * Controls entity rotation: body yaw from movement direction,
 * head yaw and pitch from the look target.
 * <p>
 * Head yaw is kept independently from body yaw through Nukkit's entity rotation API.
 *
 * @author daoge_cmd
 */
public class LookController implements Controller {

    protected final boolean lookAtTarget;
    protected final boolean lookAtRoute;

    public LookController(boolean lookAtTarget, boolean lookAtRoute) {
        this.lookAtTarget = lookAtTarget;
        this.lookAtRoute = lookAtRoute;
    }

    @Override
    public boolean control(EntityIntelligent entity) {
        var lookTarget = entity.getLookTarget();
        double bodyYaw = entity.yaw;
        double headYaw = entity.headYaw;
        double pitch = entity.pitch;

        if (lookAtRoute && entity.hasMoveDirection()) {
            var end = entity.getMoveDirectionEnd();
            if (end != null) {
                double dx = end.x - entity.x;
                double dy = end.y - entity.y;
                double dz = end.z - entity.z;
                bodyYaw = yaw(dx, dz);
                if (!lookAtTarget) {
                    headYaw = bodyYaw;
                    if (entity.isPitchEnabled()) pitch = pitch(dx, dy, dz);
                }
            }
        }

        if (lookAtTarget && lookTarget != null) {
            // Direction from entity's eyes to the look target
            // (executors already set lookTarget.y at the target's eye height)
            double dx = lookTarget.x - entity.x;
            double dy = lookTarget.y - (entity.y + entity.getEyeHeight());
            double dz = lookTarget.z - entity.z;
            if (entity.isPitchEnabled()) pitch = pitch(dx, dy, dz);
            headYaw = yaw(dx, dz);
        }

        if (!entity.isPitchEnabled()) pitch = 0;

        entity.getLivingEntity().setRotation(bodyYaw, pitch, headYaw);
        return true;
    }

    protected double yaw(double x, double z) {
        return Math.toDegrees(Math.atan2(-x, z));
    }

    protected double pitch(double x, double y, double z) {
        double horizontal = Math.sqrt(x * x + z * z);
        return Math.toDegrees(-Math.atan2(y, horizontal));
    }
}
