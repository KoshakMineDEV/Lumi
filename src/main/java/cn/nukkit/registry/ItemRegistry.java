package cn.nukkit.registry;

import cn.nukkit.Server;
import cn.nukkit.item.*;
import cn.nukkit.item.customitem.CustomItem;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.material.CustomItemType;
import cn.nukkit.item.material.ItemTypes;
import cn.nukkit.utils.OK;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ItemRegistry implements ItemNamespaceId, IRegistry<String, Item, Supplier<Item>> {

    private static final Object2ObjectOpenHashMap<String, Supplier<Item>> NAMESPACED_ID_ITEM = new Object2ObjectOpenHashMap<>();
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
        register(BONE_MEAL, ItemBoneMeal::new);
        register(BREEZE_ROD, ItemBreezeRod::new);
        register(BREWER_POTTERY_SHERD, ItemBrewerPotterySherd::new);
        register(BROWN_DYE, ItemDyeBrown::new);
        register(BROWN_EGG, ItemBrownEgg::new);
        register(BRUSH, ItemBrush::new);
        register(BURN_POTTERY_SHERD, ItemBurnPotterySherd::new);
        register(CHERRY_DOOR, ItemDoorCherry::new);
        register(CHERRY_SIGN, ItemCherrySign::new);
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
        register(CRIMSON_SIGN, ItemCrimsonSign::new);
        register(CYAN_DYE, ItemDyeCyan::new);
        register(DANGER_POTTERY_SHERD, ItemDangerPotterySherd::new);
        register(DARK_OAK_SIGN, ItemDarkOakSign::new);
        register(DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemDuneArmorTrimSmithingTemplate::new);
        register(ECHO_SHARD, ItemEchoShard::new);
        register(EXPLORER_POTTERY_SHERD, ItemExplorerPotterySherd::new);
        register(EYE_ARMOR_TRIM_SMITHING_TEMPLATE, ItemEyeArmorTrimSmithingTemplate::new);
        register(FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, ItemSmithingTemplateArmorTrimFlow::new);
        register(FLOW_BANNER_PATTERN, ItemBannerPatternFlow::new);
        register(FLOW_POTTERY_SHERD, ItemFlowPotterySherd::new);
        register(FRIEND_POTTERY_SHERD, ItemFriendPotterySherd::new);
        register(GLOW_INK_SAC, ItemGlowInkSac::new);
        register(GOAT_HORN, ItemGoatHorn::new);
        register(GRAY_DYE, ItemDyeGray::new);
        register(GREEN_DYE, ItemDyeGreen::new);
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
    }

    @Override
    public void register(String id, Supplier<Item> value) {
        NAMESPACED_ID_ITEM.put(id, value);
    }

    @Override
    public Item get(String id) {
        Supplier<Item> supplier = NAMESPACED_ID_ITEM.get(id);
        if (supplier == null) {
            return Item.AIR_ITEM;
        }
        return supplier.get();
    }

    public Supplier<Item> getSupplier(String id) {
        return NAMESPACED_ID_ITEM.get(id);
    }

    public boolean isItemRegistered(String id) {
        return NAMESPACED_ID_ITEM.containsKey(id);
    }

    public OK<?> registerCustom(@NotNull List<Class<? extends CustomItem>> itemClassList) {
        for (Class<? extends CustomItem> itemClass : itemClassList) {
            OK<?> result = registerCustom(itemClass);
            if (!result.ok()) {
                return result;
            }
        }
        return new OK<>(true);
    }

    public OK<?> registerCustom(@NotNull Class<? extends CustomItem> clazz) {
        return registerCustom(clazz, true);
    }

    public OK<?> registerCustom(@NotNull Class<? extends CustomItem> clazz, boolean addCreativeItem) {
        if (!Server.getInstance().getSettings().features().enableExperimentMode()) {
            Server.getInstance().getLogger().warning("The server does not have the experiment mode feature enabled. Unable to register the custom item!");
            return new OK<>(false, "The server does not have the experiment mode feature enabled. Unable to register the custom item!");
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
            return new OK<>(false, e);
        }

        if (CUSTOM_ITEMS.containsKey(customItem.getNamespaceId())) {
            return new OK<>(false, "The custom item with the namespace ID \"" + customItem.getNamespaceId() + "\" is already registered!");
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

        return new OK<Void>(true);
    }

    private void registerCustom(CustomItem item, int protocol, boolean addCreativeItem) {
        if (RuntimeItems.getMapping(protocol).registerCustomItem(item) && addCreativeItem) {
            Registries.CREATIVE_ITEM.register(protocol, (Item) item);
        }
    }

    public void addToCustom(String namespace, Item item) {
        CUSTOM_ITEMS.put(namespace, () -> item);
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

    public Map<String, Supplier<? extends Item>> getCustomItems() {
        return Collections.unmodifiableMap(CUSTOM_ITEMS);
    }

    public Map<String, CustomItemDefinition> getCustomItemDefinition() {
        return Collections.unmodifiableMap(CUSTOM_ITEM_DEFINITIONS);
    }

    @Override
    public void trim() {
        NAMESPACED_ID_ITEM.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        NAMESPACED_ID_ITEM.clear();
        CUSTOM_ITEM_DEFINITIONS.clear();
        init();
    }
}
