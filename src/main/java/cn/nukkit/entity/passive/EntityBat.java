package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorImpl;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroupImpl;
import cn.nukkit.entity.ai.controller.LookController;
import cn.nukkit.entity.ai.controller.SpaceMoveController;
import cn.nukkit.entity.ai.executor.SpaceRandomRoamExecutor;
import cn.nukkit.entity.ai.route.finder.SpaceAStarRouteFinder;
import cn.nukkit.entity.ai.route.posevaluator.FlyingPosEvaluator;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityBat extends EntityIntelligent {
    public static final int NETWORK_ID = 19;
    private static final float FLYING_DRAG = 0.4f;

    public EntityBat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 0.9f;
    }

    @Override
    public float getGravity() {
        return 0;
    }

    @Override
    protected float getDrag() {
        return FLYING_DRAG;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(6);
        super.initEntity();

        setBehaviorGroup(BehaviorGroupImpl.builder()
                .behavior(BehaviorImpl.builder()
                        .executor(new SpaceRandomRoamExecutor(1.1f, 12, 3, 20, false, -1, true, 10))
                        .evaluator(entity -> true)
                        .build())
                .controller(new SpaceMoveController())
                .controller(new LookController(true, true))
                .routeFinder(new SpaceAStarRouteFinder(new FlyingPosEvaluator()))
                .build());
    }
}
