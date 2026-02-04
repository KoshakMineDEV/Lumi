package cn.nukkit.registry;

import cn.nukkit.Server;
import cn.nukkit.item.*;
import cn.nukkit.item.customitem.CustomItem;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.material.CustomItemType;
import cn.nukkit.item.material.ItemTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ItemRegistry implements ItemNamespaceId, IRegistry<String, Item, Supplier<Item>> {

    private static final Object2ObjectOpenHashMap<String, Supplier<Item>> NAMESPACE_ID_ITEMS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, Supplier<Item>> CUSTOM_ITEMS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, CustomItemDefinition> CUSTOM_ITEM_DEFINITIONS = new Object2ObjectOpenHashMap<>();

    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(ACACIA_SIGN, ItemAcaciaSign::new);
        register(AMETHYST_SHARD, ItemAmethystShard::new);
        register(ANGLER_POTTERY_SHERD, ItemAnglerPotterySherd::new);
        register(ARCHER_POTTERY_SHERD, ItemArcherPotterySherd::new);
        register(ARMOR_STAND, ItemArmorStand::new);
        register(ARMS_UP_POTTERY_SHERD, ItemArmsUpPotterySherd::new);
        register(BAMBOO_DOOR, ItemDoorBamboo::new);
        register(BAMBOO_SIGN, ItemBambooSign::new);
        register(BIRCH_SIGN, ItemBirchSign::new);
        register(BLACK_DYE, ItemDyeBlack::new);
        register(BLADE_POTTERY_SHERD, ItemBladePotterySherd::new);
        register(BLUE_DYE, ItemDyeBlue::new);
        register(BLUE_EGG, ItemBlueEgg::new);
        register(BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSmithingTemplateArmorTrimBolt::new);
        register(BONE, ItemBone::new);
        register(BONE_MEAL, ItemBoneMeal::new);
        register(BOWL, ItemBowl::new);
        register(BREEZE_ROD, ItemBreezeRod::new);
        register(BREWER_POTTERY_SHERD, ItemBrewerPotterySherd::new);
        register(BROWN_DYE, ItemDyeBrown::new);
        register(BROWN_EGG, ItemBrownEgg::new);
        register(BRUSH, ItemBrush::new);
        register(BURN_POTTERY_SHERD, ItemBurnPotterySherd::new);
        register(CHARCOAL, ItemCharcoal::new);
        register(CHERRY_DOOR, ItemDoorCherry::new);
        register(CHERRY_SIGN, ItemCherrySign::new);
        register(COAL, ItemCoal::new);
        register(COAST_ARMOR_TRIM_SMITHING_TEMPLATE, ItemCoastArmorTrimSmithingTemplate::new);
        register(COCOA_BEANS, ItemCocoaBeans::new);
        register(COPPER_AXE, ItemAxeCopper::new);
        register(COPPER_BOOTS, ItemBootsCopper::new);
        register(COPPER_CHESTPLATE, ItemChestplateCopper::new);
        register(COPPER_HELMET, ItemHelmetCopper::new);
        register(COPPER_HOE, ItemHoeCopper::new);
        register(COPPER_INGOT, ItemIngotCopper::new);
        register(COPPER_LEGGINGS, ItemLeggingsCopper::new);
        register(COPPER_NUGGET, ItemNuggetCopper::new);
        register(COPPER_PICKAXE, ItemPickaxeCopper::new);
        register(COPPER_SHOVEL, ItemShovelCopper::new);
        register(COPPER_SWORD, ItemSwordCopper::new);
        register(COPPER_HORSE_ARMOR, ItemHorseArmorCopper::new);
        register(CRIMSON_SIGN, ItemCrimsonSign::new);
        register(CYAN_DYE, ItemDyeCyan::new);
        register(DANGER_POTTERY_SHERD, ItemDangerPotterySherd::new);
        register(DARK_OAK_SIGN, ItemDarkOakSign::new);
        register(DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemDuneArmorTrimSmithingTemplate::new);
        register(ECHO_SHARD, ItemEchoShard::new);
        register(EXPLORER_POTTERY_SHERD, ItemExplorerPotterySherd::new);
        register(EYE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemEyeArmorTrimSmithingTemplate::new);
        register(FEATHER, ItemFeather::new);
        register(FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSmithingTemplateArmorTrimFlow::new);
        register(FLOW_BANNER_PATTERN, ItemBannerPatternFlow::new);
        register(FLOW_POTTERY_SHERD, ItemFlowPotterySherd::new);
        register(FRIEND_POTTERY_SHERD, ItemFriendPotterySherd::new);
        register(GLOW_INK_SAC, ItemGlowInkSac::new);
        register(GOAT_HORN, ItemGoatHorn::new);
        register(GRAY_DYE, ItemDyeGray::new);
        register(GREEN_DYE, ItemDyeGreen::new);
        register(GUNPOWDER, ItemGunpowder::new);
        register(GUSTER_BANNER_PATTERN, ItemBannerPatternGuster::new);
        register(GUSTER_POTTERY_SHERD, ItemGusterPotterySherd::new);
        register(HEARTBREAK_POTTERY_SHERD, ItemHeartbreakPotterySherd::new);
        register(HEART_POTTERY_SHERD, ItemHeartPotterySherd::new);
        register(HOST_ARMOR_TRIM_SMITHING_TEMPLATE, ItemHostArmorTrimSmithingTemplate::new);
        register(HOWL_POTTERY_SHERD, ItemHowlPotterySherd::new);
        register(INK_SAC, ItemInkSac::new);
        register(JUNGLE_SIGN, ItemJungleSign::new);
        register(LAPIS_LAZULI, ItemLapisLazuli::new);
        register(LIGHT_BLUE_DYE, ItemDyeLightBlue::new);
        register(LIGHT_GRAY_DYE, ItemDyeLightGray::new);
        register(LIME_DYE, ItemDyeLime::new);
        register(MACE, ItemMace::new);
        register(MAGENTA_DYE, ItemDyeMagenta::new);
        register(MANGROVE_DOOR, ItemDoorMangrove::new);
        register(MANGROVE_SIGN, ItemMangroveSign::new);
        register(MINER_POTTERY_SHERD, ItemMinerPotterySherd::new);
        register(MOURNER_POTTERY_SHERD, ItemMournerPotterySherd::new);
        register(MUSIC_DISC_CREATOR, ItemRecordCreator::new);
        register(MUSIC_DISC_CREATOR_MUSIC_BOX, ItemRecordCreatorMusicBox::new);
        register(MUSIC_DISC_LAVA_CHICKEN, ItemRecordLavaChicken::new);
        register(MUSIC_DISC_PRECIPICE, ItemRecordPrecipice::new);
        register(MUSIC_DISC_TEARS, ItemRecordTears::new);
        register(NETHERITE_UPGRADE_SMITHING_TEMPLATE, ItemNetheriteUpgradeSmithingTemplate::new);
        register(OAK_SIGN, ItemOakSign::new);
        register(OMINOUS_BOTTLE, ItemOminousBottle::new);
        register(OMINOUS_TRIAL_KEY, ItemTrialKeyOminous::new);
        register(ORANGE_DYE, ItemDyeOrange::new);
        register(PINK_DYE, ItemDyePink::new);
        register(PITCHER_POD, ItemPitcherPod::new);
        register(PLENTY_POTTERY_SHERD, ItemPlentyPotterySherd::new);
        register(PRIZE_POTTERY_SHERD, ItemPrizePotterySherd::new);
        register(PURPLE_DYE, ItemDyePurple::new);
        register(RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRaiserArmorTrimSmithingTemplate::new);
        register(RAW_COPPER, ItemRawCopper::new);
        register(RAW_GOLD, ItemRawGold::new);
        register(RAW_IRON, ItemRawIron::new);
        register(RECOVERY_COMPASS, ItemRecoveryCompass::new);
        register(RED_DYE, ItemDyeRed::new);
        register(RESIN_BRICK, ItemResinBrick::new);
        register(RIB_ARMOR_TRIM_SMITHING_TEMPLATE, ItemRibArmorTrimSmithingTemplate::new);
        register(SCRAPE_POTTERY_SHERD, ItemScrapePotterySherd::new);
        register(SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSentryArmorTrimSmithingTemplate::new);
        register(SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, ItemShaperArmorTrimSmithingTemplate::new);
        register(SHEAF_POTTERY_SHERD, ItemSheafPotterySherd::new);
        register(SHELTER_POTTERY_SHERD, ItemShelterPotterySherd::new);
        register(SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSilenceArmorTrimSmithingTemplate::new);
        register(SKULL_POTTERY_SHERD, ItemSkullPotterySherd::new);
        register(SNORT_POTTERY_SHERD, ItemSnortPotterySherd::new);
        register(SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSnoutArmorTrimSmithingTemplate::new);
        register(SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSpireArmorTrimSmithingTemplate::new);
        register(SPRUCE_SIGN, ItemSpruceSign::new);
        register(STRING, ItemString::new);
        register(SUGAR, ItemSugar::new);
        register(TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemTideArmorTrimSmithingTemplate::new);
        register(TORCHFLOWER_SEEDS, ItemTorchflowerSeeds::new);
        register(TRIAL_KEY, ItemTrialKey::new);
        register(VEX_ARMOR_TRIM_SMITHING_TEMPLATE, ItemVexArmorTrimSmithingTemplate::new);
        register(WARD_ARMOR_TRIM_SMITHING_TEMPLATE, ItemWardArmorTrimSmithingTemplate::new);
        register(WARPED_SIGN, ItemWarpedSign::new);
        register(WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, ItemWayfinderArmorTrimSmithingTemplate::new);
        register(WHITE_DYE, ItemDyeWhite::new);
        register(WILD_ARMOR_TRIM_SMITHING_TEMPLATE, ItemWildArmorTrimSmithingTemplate::new);
        register(WIND_CHARGE, ItemWindCharge::new);
        register(YELLOW_DYE, ItemDyeYellow::new);
        register(PALE_OAK_DOOR, ItemDoorPaleOak::new);
        register(PALE_OAK_SIGN, ItemPaleOakSign::new);
        register(WOODEN_SPEAR, ItemWoodenSpear::new);
        register(STONE_SPEAR, ItemStoneSpear::new);
        register(COPPER_SPEAR, ItemCopperSpear::new);
        register(IRON_SPEAR, ItemIronSpear::new);
        register(GOLDEN_SPEAR, ItemGoldenSpear::new);
        register(DIAMOND_SPEAR, ItemDiamondSpear::new);
        register(NETHERITE_SPEAR, ItemNetheriteSpear::new);
        register(IRON_NAUTILUS_ARMOR, ItemIronNautilusArmor::new);
        register(COPPER_NAUTILUS_ARMOR, ItemCopperNautilusArmor::new);
        register(GOLDEN_NAUTILUS_ARMOR, ItemGoldenNautilusArmor::new);
        register(DIAMOND_NAUTILUS_ARMOR, ItemDiamondNautilusArmor::new);
        register(NETHERITE_NAUTILUS_ARMOR, ItemNetheriteNautilusArmor::new);
        register(BUNDLE, ItemBundle::new);
        register(BLACK_BUNDLE, ItemBlackBundle::new);
        register(BLUE_BUNDLE, ItemBlueBundle::new);
        register(BROWN_BUNDLE, ItemBrownBundle::new);
        register(CYAN_BUNDLE, ItemCyanBundle::new);
        register(GRAY_BUNDLE, ItemGrayBundle::new);
        register(GREEN_BUNDLE, ItemGreenBundle::new);
        register(LIGHT_BLUE_BUNDLE, ItemLightBlueBundle::new);
        register(LIGHT_GRAY_BUNDLE, ItemLightGrayBundle::new);
        register(LIME_BUNDLE, ItemLimeBundle::new);
        register(MAGENTA_BUNDLE, ItemMagentaBundle::new);
        register(ORANGE_BUNDLE, ItemOrangeBundle::new);
        register(PINK_BUNDLE, ItemPinkBundle::new);
        register(PURPLE_BUNDLE, ItemPurpleBundle::new);
        register(RED_BUNDLE, ItemRedBundle::new);
        register(WHITE_BUNDLE, ItemWhiteBundle::new);
        register(YELLOW_BUNDLE, ItemYellowBundle::new);
        register(NETHERITE_HORSE_ARMOR, ItemHorseArmorNetherite::new);
        register(WOLF_ARMOR, ItemWolfArmor::new);
        register(WHITE_HARNESS, ItemWhiteHarness::new);
        register(ORANGE_HARNESS, ItemOrangeHarness::new);
        register(MAGENTA_HARNESS, ItemMagentaHarness::new);
        register(LIGHT_BLUE_HARNESS, ItemLightBlueHarness::new);
        register(YELLOW_HARNESS, ItemYellowHarness::new);
        register(LIME_HARNESS, ItemLimeHarness::new);
        register(PINK_HARNESS, ItemPinkHarness::new);
        register(GRAY_HARNESS, ItemGrayHarness::new);
        register(LIGHT_GRAY_HARNESS, ItemLightGrayHarness::new);
        register(CYAN_HARNESS, ItemCyanHarness::new);
        register(PURPLE_HARNESS, ItemPurpleHarness::new);
        register(BLUE_HARNESS, ItemBlueHarness::new);
        register(BROWN_HARNESS, ItemBrownHarness::new);
        register(GREEN_HARNESS, ItemGreenHarness::new);
        register(RED_HARNESS, ItemRedHarness::new);
        register(BLACK_HARNESS, ItemBlackHarness::new);
    }

    @Override
    public void register(String id, Supplier<Item> value) {
        NAMESPACE_ID_ITEMS.put(id, value);
    }

    public void registerCustom(@NotNull List<Class<? extends CustomItem>> itemClassList) {
        for (Class<? extends CustomItem> itemClass : itemClassList) {
            registerCustom(itemClass);
        }
    }

    public void registerCustom(@NotNull Class<? extends CustomItem> clazz) {
        registerCustom(clazz, true);
    }

    public void registerCustom(@NotNull Class<? extends CustomItem> clazz, boolean addCreativeItem) {
        if (!Server.getInstance().getSettings().features().enableExperimentMode()) {
            throw new RegisterException("The server does not have the experiment mode feature enabled. Unable to register the custom item!");
        }

        CustomItem customItem;
        Supplier<Item> supplier;

        try {
            var method = clazz.getDeclaredConstructor();
            method.setAccessible(true);
            customItem = method.newInstance();
            supplier = () -> {
                try {
                    return (Item) method.newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new UnsupportedOperationException(e);
                }
            };
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RegisterException(e);
        }

        if (CUSTOM_ITEMS.containsKey(customItem.getNamespaceId())) {
            throw new RegisterException("The custom item with the namespace ID '" + customItem.getNamespaceId() + "' is already registered!");
        }

        CUSTOM_ITEMS.put(customItem.getNamespaceId(), supplier);
        CUSTOM_ITEM_DEFINITIONS.put(customItem.getNamespaceId(), customItem.getDefinition());
        register(customItem.getNamespaceId(), supplier);

        // Registering custom items for all creative item protocols
        CreativeItemRegistry.CREATIVE_ITEMS_PROTOCOLS.forEach(protocol -> {
            registerCustom(customItem, protocol, addCreativeItem);
        });

        // Registering custom item type
        ItemTypes.register(new CustomItemType(customItem));
    }

    private void registerCustom(CustomItem item, int protocol, boolean addCreativeItem) {
        if (RuntimeItems.getMapping(protocol).registerCustomItem(item) && addCreativeItem) {
            Registries.CREATIVE_ITEM.register(protocol, (Item) item);
        }
    }

    public void addToCustom(String namespace, Item item) {
        CUSTOM_ITEMS.put(namespace, item::clone);
        register(namespace, item::clone);
    }

    public void deleteCustom(String namespaceId) {
        if (CUSTOM_ITEMS.containsKey(namespaceId)) {
            Item customItem = Item.get(namespaceId);
            CUSTOM_ITEMS.remove(namespaceId);
            CUSTOM_ITEM_DEFINITIONS.remove(namespaceId);

            CreativeItemRegistry.CREATIVE_ITEMS_PROTOCOLS.forEach(protocol -> {
                deleteCustom(customItem, protocol);
            });
        }
    }

    private void deleteCustom(Item item, int protocol) {
        RuntimeItems.getMapping(protocol).deleteCustomItem((CustomItem) item);
        Registries.CREATIVE_ITEM.remove(protocol, item);
    }

    @Override
    public Item get(String id) {
        Supplier<Item> supplier = getSupplier(id);
        if (supplier == null) {
            return Item.AIR_ITEM.clone();
        }
        return supplier.get();
    }

    public Supplier<Item> getSupplier(String id) {
        return NAMESPACE_ID_ITEMS.get(id);
    }

    public Map<String, Supplier<Item>> getNamespaceIdItems() {
        return Collections.unmodifiableMap(NAMESPACE_ID_ITEMS);
    }

    public Map<String, Supplier<? extends Item>> getCustomItems() {
        return Collections.unmodifiableMap(CUSTOM_ITEMS);
    }

    public Map<String, CustomItemDefinition> getCustomItemDefinition() {
        return Collections.unmodifiableMap(CUSTOM_ITEM_DEFINITIONS);
    }

    public boolean isItemRegistered(String id) {
        return NAMESPACE_ID_ITEMS.containsKey(id);
    }

    @Override
    public void trim() {
        NAMESPACE_ID_ITEMS.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        NAMESPACE_ID_ITEMS.clear();
        CUSTOM_ITEM_DEFINITIONS.clear();
        init();
    }
}
