package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorImpl;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroupImpl;
import cn.nukkit.entity.ai.controller.FluctuateController;
import cn.nukkit.entity.ai.controller.LookController;
import cn.nukkit.entity.ai.controller.WalkController;
import cn.nukkit.entity.ai.executor.FlatRandomRoamExecutor;
import cn.nukkit.entity.ai.route.finder.FlatAStarRouteFinder;
import cn.nukkit.entity.ai.route.posevaluator.WalkingPosEvaluator;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntityCow extends EntityIntelligent implements EntityClimateVariant {
    public static final int NETWORK_ID = 11;

    public EntityCow(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 1.4f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);
        super.initEntity();

        if (namedTag.contains("variant")) {
            setVariant(Variant.get(namedTag.getString("variant")));
        } else {
            setVariant(getBiomeVariant(getLevel().getBiomeId(getFloorX(), getFloorZ())));
        }

        //TODO: complete behavior
        setBehaviorGroup(BehaviorGroupImpl.builder()
                .behavior(BehaviorImpl.builder()
                        .executor(new FlatRandomRoamExecutor(0.25f, 12, 120, false, -1, true, 10))
                        .evaluator(entity -> true)
                        .priority(1)
                        .build())
                .controller(new WalkController())
                .controller(new LookController(true, true))
                .controller(new FluctuateController())
                .routeFinder(new FlatAStarRouteFinder(new WalkingPosEvaluator()))
                .build()
        );
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        drops.add(Item.get(Item.LEATHER, 0, Utils.rand(0, 2)));
        drops.add(Item.get(this.isOnFire() ? Item.STEAK : Item.RAW_BEEF, 0, Utils.rand(1, 3)));

        return drops.toArray(Item.EMPTY_ARRAY);
    }
}
