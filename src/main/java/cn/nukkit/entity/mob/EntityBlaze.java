package cn.nukkit.entity.mob;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorImpl;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroupImpl;
import cn.nukkit.entity.ai.controller.LookController;
import cn.nukkit.entity.ai.controller.WalkController;
import cn.nukkit.entity.ai.evaluator.DistanceEvaluator;
import cn.nukkit.entity.ai.evaluator.EntityCheckEvaluator;
import cn.nukkit.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator;
import cn.nukkit.entity.ai.evaluator.ProbabilityEvaluator;
import cn.nukkit.entity.ai.evaluator.RandomSoundEvaluator;
import cn.nukkit.entity.ai.executor.*;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.ai.route.finder.FlatAStarRouteFinder;
import cn.nukkit.entity.ai.route.posevaluator.WalkingPosEvaluator;
import cn.nukkit.entity.ai.sensor.NearestPlayerSensor;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.entity.ai.evaluator.LogicHelper.all;
import static cn.nukkit.entity.ai.evaluator.LogicHelper.no;

public class EntityBlaze extends EntityIntelligent {

    public static final int NETWORK_ID = 43;
    private static final float MOVEMENT_SPEED = 0.23f;
    private static final double ATTACK_TARGET_RANGE_SQUARED = 48 * 48;
    private static final double RANGED_COMBAT_DISTANCE_SQUARED = 15 * 15;
    private static final double MELEE_START_DISTANCE = 1;
    private static final double MELEE_TRACKING_DISTANCE = 3;
    private static final double MELEE_ATTACK_DISTANCE = Math.sqrt(2.5);
    private static final EntityCheckEvaluator ATTACK_TARGET_CHECK =
            new EntityCheckEvaluator(MemoryTypes.ATTACK_TARGET);
    private static final double FALLING_DAMPING = 0.6;
    private static final double VERTICAL_TARGET_SPEED = 0.3;
    private static final double VERTICAL_TARGET_APPROACH = 0.3;
    private static final int HEIGHT_OFFSET_CHANGE_INTERVAL = 100;
    private static final double HEIGHT_OFFSET_MEAN = 0.5;
    private static final double HEIGHT_OFFSET_DEVIATION = 6.891;

    private float allowedHeightOffset = (float) HEIGHT_OFFSET_MEAN;
    private int nextHeightOffsetChangeTick;

    public EntityBlaze(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.8f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        this.setBaseMovementSpeed(MOVEMENT_SPEED);

        super.initEntity();

        setBehaviorGroup(BehaviorGroupImpl.builder()
                .sensor(new NearestPlayerSensor(8, 0, 10))
                .sensor(new NearestPlayerSensor(
                        MemoryTypes.ATTACK_TARGET,
                        48,
                        0,
                        10,
                        player -> player.isSurvival() || player.isAdventure()
                ))
                .coreBehavior(BehaviorImpl.builder()
                        .executor(new PlaySoundExecutor(Sound.MOB_BLAZE_BREATHE))
                        .evaluator(new RandomSoundEvaluator())
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new MeleeAttackExecutor(
                                MemoryTypes.ATTACK_TARGET,
                                0.3f,
                                MELEE_TRACKING_DISTANCE,
                                false,
                                30,
                                MELEE_ATTACK_DISTANCE,
                                MELEE_START_DISTANCE
                        ))
                        .evaluator(all(
                                new EntityCheckEvaluator(MemoryTypes.ATTACK_TARGET),
                                new DistanceEvaluator(MemoryTypes.ATTACK_TARGET, MELEE_START_DISTANCE)
                        ))
                        .priority(4)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new FollowEntityExecutor(
                                MemoryTypes.ATTACK_TARGET,
                                MOVEMENT_SPEED,
                                ATTACK_TARGET_RANGE_SQUARED,
                                RANGED_COMBAT_DISTANCE_SQUARED
                        ))
                        .evaluator(new EntityCheckEvaluator(MemoryTypes.ATTACK_TARGET))
                        .priority(3)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new BlazeShootExecutor(
                                MemoryTypes.ATTACK_TARGET, 0.3f, 15, false, 100, 40, false
                        ))
                        .evaluator(new EntityCheckEvaluator(MemoryTypes.ATTACK_TARGET))
                        .priority(3)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new LookAtEntityExecutor(MemoryTypes.NEAREST_PLAYER, 40, 79, 8))
                        .evaluator(all(
                                new MemoryCheckNotEmptyEvaluator(MemoryTypes.NEAREST_PLAYER),
                                new ProbabilityEvaluator(2, 100)
                        ))
                        .priority(2)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new FlatRandomRoamExecutor(
                                MOVEMENT_SPEED, 10, 120, false, -1, true, 10
                        ))
                        .evaluator(no(new EntityCheckEvaluator(MemoryTypes.ATTACK_TARGET)))
                        .priority(1)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new RandomLookAroundExecutor())
                        .evaluator(new ProbabilityEvaluator(2, 100))
                        .priority(1)
                        .build())
                .controller(new WalkController())
                .controller(new LookController(true, true))
                .routeFinder(new FlatAStarRouteFinder(new WalkingPosEvaluator(false)))
                .build()
        );

        this.fireProof = true;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_FIRE_IMMUNE, true);
    }

    @Override
    protected void prepareMotion(int tickDiff) {
        // Blaze.aiStep applies slow falling before the regular mob AI and travel pipeline.
        this.motionY = dampFallingMotion(this.onGround, this.motionY);
        super.prepareMotion(tickDiff);

        nextHeightOffsetChangeTick -= tickDiff;
        if (nextHeightOffsetChangeTick <= 0) {
            nextHeightOffsetChangeTick = HEIGHT_OFFSET_CHANGE_INTERVAL;
            ThreadLocalRandom random = ThreadLocalRandom.current();
            allowedHeightOffset = (float) triangularHeightOffset(
                    random.nextDouble(),
                    random.nextDouble()
            );
        }

        if (ATTACK_TARGET_CHECK.evaluate(this)) {
            Entity target = getMemoryStorage().get(MemoryTypes.ATTACK_TARGET);
            if (target.getEyeY() > this.getEyeY() + allowedHeightOffset) {
                this.motionY = approachVerticalTarget(this.motionY);
            }
        }
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean updated = super.entityBaseTick(tickDiff);
        if (!this.isAlive()) {
            return updated;
        }

        boolean wet = this.isInsideOfWater()
                || this.getLevel().isRaining() && this.canSeeSky();
        if (wet) {
            updated |= this.attack(new EntityDamageEvent(
                    this,
                    EntityDamageEvent.DamageCause.DROWNING,
                    1
            ));
        }
        return updated;
    }

    static double dampFallingMotion(boolean onGround, double motionY) {
        return !onGround && motionY < 0 ? motionY * FALLING_DAMPING : motionY;
    }

    static double approachVerticalTarget(double motionY) {
        return motionY + (VERTICAL_TARGET_SPEED - motionY) * VERTICAL_TARGET_APPROACH;
    }

    static double triangularHeightOffset(double firstRandom, double secondRandom) {
        return HEIGHT_OFFSET_MEAN
                + HEIGHT_OFFSET_DEVIATION * (firstRandom - secondRandom);
    }
}
