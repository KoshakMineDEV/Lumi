package cn.nukkit.registry;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Registries {
    public static final CreativeItemRegistry CREATIVE_ITEM = new CreativeItemRegistry();
    public static final ItemRegistry ITEM = new ItemRegistry();
    public static final ItemLegacyRegistry ITEM_LEGACY = new ItemLegacyRegistry();
    public static final BlockRegistry BLOCK = new BlockRegistry();
    public static final BlockToItemRegistry BLOCK_TO_ITEM = new BlockToItemRegistry();
    public static final EnchantmentRegistry ENCHANTMENT = new EnchantmentRegistry();
    public static final EffectRegistry EFFECT = new EffectRegistry();
    public static final FuelRegistry FUEL = new FuelRegistry();
    public static final PotionRegistry POTION = new PotionRegistry();
    public static final DispenseBehaviorRegistry DISPENSE_BEHAVIOR = new DispenseBehaviorRegistry();
    public static final BlockEntityRegistry BLOCK_ENTITY = new BlockEntityRegistry();
    public static final EntityRegistry ENTITY = new EntityRegistry();
}
