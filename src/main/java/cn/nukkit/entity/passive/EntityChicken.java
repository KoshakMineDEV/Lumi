package cn.nukkit.entity.passive;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.behavior.BehaviorImpl;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroupImpl;
import cn.nukkit.entity.ai.controller.FluctuateController;
import cn.nukkit.entity.ai.controller.LookController;
import cn.nukkit.entity.ai.controller.WalkController;
import cn.nukkit.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator;
import cn.nukkit.entity.ai.evaluator.PassByTimeEvaluator;
import cn.nukkit.entity.ai.evaluator.ProbabilityEvaluator;
import cn.nukkit.entity.ai.evaluator.RandomSoundEvaluator;
import cn.nukkit.entity.ai.executor.FlatRandomRoamExecutor;
import cn.nukkit.entity.ai.executor.FollowEntityExecutor;
import cn.nukkit.entity.ai.executor.LookAtEntityExecutor;
import cn.nukkit.entity.ai.executor.PlaySoundExecutor;
import cn.nukkit.entity.ai.memory.MemoryTypes;
import cn.nukkit.entity.ai.route.finder.FlatAStarRouteFinder;
import cn.nukkit.entity.ai.route.posevaluator.WalkingPosEvaluator;
import cn.nukkit.entity.ai.sensor.NearestFeedingPlayerSensor;
import cn.nukkit.entity.ai.sensor.NearestPlayerSensor;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static cn.nukkit.entity.ai.evaluator.LogicHelper.all;

public class EntityChicken extends EntityIntelligent implements EntityClimateVariant {

    public static final int NETWORK_ID = 10;

    public EntityChicken(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setBaseMovementSpeed(0.25F);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.4f;
    }

    @Override
    public float getHeight() {
        return 0.7f;
    }

    @Override
    public float getDrag() {
        return 0.2f;
    }

    @Override
    public float getGravity() {
        //Should be lower but that breaks jumping
        return 0.08f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(4);
        super.initEntity();

        if (namedTag.contains("variant")) {
            setVariant(Variant.get(namedTag.getString("variant")));
        } else {
            setVariant(getBiomeVariant(getLevel().getBiomeId(getFloorX(), getFloorZ())));
        }

        setBehaviorGroup(BehaviorGroupImpl.builder()
                .sensor(new NearestFeedingPlayerSensor(8, 5, item -> item.getNamespaceId().equals(ItemNamespaceId.WHEAT_SEEDS)))
                .sensor(new NearestPlayerSensor(8, 0, 20))
                .behavior(BehaviorImpl.builder()
                        .executor(new PlaySoundExecutor(Sound.MOB_CHICKEN_SAY))
                        .evaluator(new RandomSoundEvaluator())
                        .priority(7)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new FlatRandomRoamExecutor(0.325f, 12, 40, true, 100, true, 10))
                        .evaluator(new PassByTimeEvaluator(MemoryTypes.LAST_BE_ATTACKED_TIME, 0, 100))
                        .priority(4)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new FollowEntityExecutor(MemoryTypes.NEAREST_FEEDING_PLAYER, 0.25f, 64, 2.25))
                        .evaluator(new MemoryCheckNotEmptyEvaluator(MemoryTypes.NEAREST_FEEDING_PLAYER))
                        .priority(3)
                        .build())
                .behavior(BehaviorImpl.builder()
                        .executor(new LookAtEntityExecutor(MemoryTypes.NEAREST_PLAYER, 100))
                        .evaluator(all(
                                new MemoryCheckNotEmptyEvaluator(MemoryTypes.NEAREST_PLAYER),
                                new ProbabilityEvaluator(2, 5)
                        ))
                        .priority(2)
                        .period(100)
                        .build())
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

        recalculateBoundingBox();

        this.noFallDamage = true;

        this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_CAN_CLIMB, true);
        this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_WALKER, true);
        this.setDataFlag(Entity.DATA_FLAGS2, Entity.DATA_FLAG_BREATHING, true);
        this.setDataFlag(Entity.DATA_FLAGS2, Entity.DATA_FLAG_HAS_COLLISION, true);
        this.setDataFlag(Entity.DATA_FLAGS2, Entity.DATA_FLAG_GRAVITY, true);
    }

    private Item getEgg() {
        if (Server.getInstance().getSettings().features().enableNewChickenEggsLaying()) {
            if (getVariant() == Variant.COLD) return Item.get(ItemNamespaceId.BLUE_EGG);
            if (getVariant() == Variant.WARM) return Item.get(ItemNamespaceId.BROWN_EGG);
        }
        return Item.get(Item.EGG, 0, 1);
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(ItemNamespaceId.FEATHER, 0, Utils.rand(0, 2)));
        drops.add(Item.get(this.isOnFire() ? Item.COOKED_CHICKEN : Item.RAW_CHICKEN, 0, 1));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        if (ev.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return super.attack(ev);
        }

        return false;
    }
}
