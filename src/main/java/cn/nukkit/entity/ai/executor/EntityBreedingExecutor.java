package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

import static cn.nukkit.entity.ai.executor.EntityControlHelper.*;

/**
 * Generic breeding executor. Finds the nearest in-love entity of the same type,
 * moves both toward each other, and spawns a baby after the breeding duration.
 *
 * @author daoge_cmd
 */
public class EntityBreedingExecutor implements BehaviorExecutor {

    protected static final int FINDING_RANGE_SQUARED = 256; // 16 blocks

    protected final int duration;
   
    protected final float speed;

    protected int tickCounter;
    protected Entity spouse;
    protected boolean isInitiator;
    protected Vector3 lastTargetPos;

    public EntityBreedingExecutor(int duration, float speed) {
        this.duration = duration;
        this.speed = speed;
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        tickCounter = 0;
        spouse = null;
        isInitiator = false;
        lastTargetPos = null;
        entity.setBaseMovementSpeed(speed);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        tickCounter++;

        if (spouse == null || !spouse.isAlive()) {
            spouse = null;
            // Check if another entity's executor already paired with us
            var spouseId = entity.getMemoryStorage().get(MemoryTypes.ENTITY_SPOUSE);
            if (spouseId != null) {
                var resolved = entity.getLevel().getEntity(spouseId);
                if (resolved != null && resolved.isAlive()) {
                    spouse = resolved;
                }
            }

            if (spouse == null) {
                spouse = findSpouse(entity);
                if (spouse == null) {
                    return true; // keep searching
                }
                isInitiator = true;
                if (spouse instanceof EntityIntelligent spouseIntelligent) {
                    // Atomically claim the spouse — if another entity already claimed it, retry next tick
                    if (!spouseIntelligent.getMemoryStorage().putIfAbsent(MemoryTypes.ENTITY_SPOUSE, entity.getRuntimeId())) {
                        spouse = null;
                        isInitiator = false;
                        return true;
                    }
                }
                entity.getMemoryStorage().put(MemoryTypes.ENTITY_SPOUSE, spouse.getId());
            }
        }

        // Move toward spouse, re-path only when target moved >1 block
        var spouseLoc = spouse.getLocation();
        var targetPos = new Vector3(spouseLoc.x, spouseLoc.y, spouseLoc.z);
        if (lastTargetPos == null || lastTargetPos.distanceSquared(targetPos) > 1.0) {
            setRouteTarget(entity, targetPos);
            lastTargetPos = targetPos;
        }

        setLookTarget(entity, new Vector3(
                spouseLoc.x, spouseLoc.y + spouse.getEyeHeight(), spouseLoc.z
        ));

        // Only the initiator checks for breeding completion and spawns the baby
        if (isInitiator) {
            double distSq = entity.getLocation().distanceSquared(spouseLoc);
            if (distSq < 4.0 && tickCounter >= duration) {
                spawnBaby(entity);
                entity.getMemoryStorage().put(MemoryTypes.IS_IN_LOVE, false);
                if (spouse instanceof EntityIntelligent spouseIntelligent) {
                    spouseIntelligent.getMemoryStorage().put(MemoryTypes.IS_IN_LOVE, false);
                }
                return false;
            }
        }

        return tickCounter < duration + 60; // timeout after duration + buffer
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        clearEntityState(entity);
        if (spouse instanceof EntityIntelligent spouseIntelligent) {
            clearEntityState(spouseIntelligent);
        }
        spouse = null;
        isInitiator = false;
        lastTargetPos = null;
    }

    @Override
    public void onInterrupt(EntityIntelligent entity) {
        onStop(entity);
    }

    protected void clearEntityState(EntityIntelligent entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        entity.getMemoryStorage().clear(MemoryTypes.ENTITY_SPOUSE);
    }

    protected Entity findSpouse(EntityIntelligent entity) {
        Entity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (var candidate : entity.getLevel().getEntities()) {
            if (candidate == entity.getLivingEntity()) continue;
            if (candidate.getNetworkId() != entity.getLivingEntity().getNetworkId()) continue;
            if (!(candidate instanceof EntityIntelligent candidateIntelligent)) continue;
            if (!candidateIntelligent.getMemoryStorage().get(MemoryTypes.IS_IN_LOVE)) continue;
            // Skip babies
            if (candidate instanceof EntityAgeable ageable && ageable.isBaby()) continue;
            // Skip already paired entities
            if (candidateIntelligent.getMemoryStorage().get(MemoryTypes.ENTITY_SPOUSE) != null) continue;

            double distSq = entity.getLocation().distanceSquared(candidate.getLocation());
            if (distSq > FINDING_RANGE_SQUARED) continue;
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }

        return nearest;
    }

    protected void spawnBaby(EntityIntelligent entity) {
        var loc = entity.getLocation();
        var baby = Entity.createEntity(entity.getLivingEntity().getNetworkId(),
                new Position(loc.x, loc.y, loc.z, entity.getLevel()));
        if (baby == null) {
            return;
        }
        if (baby instanceof EntityAgeable ageableBaby) {
            ageableBaby.setBaby(true);
        }
        // Prevent baby from breeding immediately
        if (baby instanceof EntityIntelligent babyIntelligent) {
            babyIntelligent.getMemoryStorage().put(MemoryTypes.LAST_IN_LOVE_TIME, entity.getTick());
        }
        baby.spawnToAll();
    }
}
