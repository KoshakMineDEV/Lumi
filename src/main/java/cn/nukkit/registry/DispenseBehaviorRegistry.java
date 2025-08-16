package cn.nukkit.registry;

import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.tags.BlockInternalTags;
import cn.nukkit.block.material.tags.BlockTag;
import cn.nukkit.dispenser.DispenseBehavior;
import cn.nukkit.dispenser.impl.*;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.material.ItemType;
import cn.nukkit.item.material.tags.ItemTag;
import cn.nukkit.item.material.tags.ItemTags;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

public class DispenseBehaviorRegistry implements IRegistry<String, DispenseBehavior, DispenseBehavior> {

    private static final Object2ObjectOpenHashMap<String, DispenseBehavior> BEHAVIORS = new Object2ObjectOpenHashMap<>();
    private static final DispenseBehavior DEFAULT_BEHAVIOR = new DefaultDispenseBehavior();
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(ItemNamespaceId.OAK_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.SPRUCE_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.BIRCH_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.JUNGLE_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.ACACIA_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.DARK_OAK_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.MANGROVE_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.CHERRY_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.PALE_OAK_BOAT, new BoatDispenseBehavior());
        register(ItemNamespaceId.BAMBOO_RAFT, new BoatDispenseBehavior());

        register(ItemNamespaceId.BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.MILK_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.WATER_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.LAVA_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.COD_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.SALMON_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.TROPICAL_FISH_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.PUFFERFISH_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.AXOLOTL_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.POWDER_SNOW_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.TADPOLE_BUCKET, new BucketDispenseBehavior());
        register(ItemNamespaceId.BONE_MEAL, new BoneMealDispenseBehavior());

        registerByItemTag(ItemTags.SPAWN_EGG, new SpawnEggDispenseBehavior());
        register(ItemNamespaceId.FIREWORK_ROCKET, new FireworksDispenseBehavior());
        register(ItemNamespaceId.FLINT_AND_STEEL, new FlintAndSteelDispenseBehavior());
        register(ItemNamespaceId.UNDYED_SHULKER_BOX, new UndyedShulkerBoxDispenseBehavior());
        registerByBlockTag(BlockInternalTags.DYED_SHULKER_BOX, new ShulkerBoxDispenseBehavior());

        register(ItemNamespaceId.TNT, new TNTDispenseBehavior());
        register(ItemNamespaceId.FIRE_CHARGE, new FireChargeDispenseBehavior());
        register(ItemNamespaceId.SHEARS, new ShearsDispenseBehaviour());
        register(ItemNamespaceId.POTION, new PotionDispenseBehaviour());
        register(ItemNamespaceId.ARROW, new ArrowDispenseBehavior());
        register(ItemNamespaceId.EGG, new EggDispenseBehavior());
        register(ItemNamespaceId.SNOWBALL, new SnowballDispenseBehavior());
        register(ItemNamespaceId.EXPERIENCE_BOTTLE, new ThrownExpBottleDispenseBehavior());
        register(ItemNamespaceId.SPLASH_POTION, new ThrownPotionDispenseBehavior());
        register(ItemNamespaceId.LINGERING_POTION, new LingeringPotionDispenseBehavior());
        register(ItemNamespaceId.TRIDENT, new ThrownPotionDispenseBehavior());

        register(ItemNamespaceId.ACACIA_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.DARK_OAK_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.BIRCH_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.JUNGLE_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.MANGROVE_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.SPRUCE_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.OAK_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.CHERRY_CHEST_BOAT, new ChestBoatDispenseBehavior());
        register(ItemNamespaceId.BAMBOO_CHEST_RAFT, new ChestBoatDispenseBehavior());

        register(ItemNamespaceId.MINECART, new MinecartDispenseBehavior());
        register(ItemNamespaceId.CHEST_MINECART, new MinecartDispenseBehavior());
        register(ItemNamespaceId.HOPPER_MINECART, new MinecartDispenseBehavior());
        register(ItemNamespaceId.TNT_MINECART, new MinecartDispenseBehavior());
    }

    @Override
    public void register(String id, DispenseBehavior behavior) {
        BEHAVIORS.put(id, behavior);
    }

    public void registerByItemTag(ItemTag tag, DispenseBehavior behavior) {
        for (ItemType type : tag.getItemTypes()) {
            register(type.getIdentifier(), behavior);
        }
    }

    public void registerByBlockTag(BlockTag tag, DispenseBehavior behavior) {
        for (BlockType type : tag.getBlockTypes()) {
            register(type.getIdentifier(), behavior);
        }
    }

    @Override
    public DispenseBehavior get(String id) {
        return BEHAVIORS.getOrDefault(id, DEFAULT_BEHAVIOR);
    }

    public DispenseBehavior getDefault() {
        return DEFAULT_BEHAVIOR;
    }

    @Override
    public void trim() {
        BEHAVIORS.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        BEHAVIORS.clear();
        init();
    }
}
