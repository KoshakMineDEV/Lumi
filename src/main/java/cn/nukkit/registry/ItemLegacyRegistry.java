package cn.nukkit.registry;

import cn.nukkit.block.BlockID;
import cn.nukkit.item.*;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.concurrent.atomic.AtomicBoolean;

//TODO: remove legacy items at all
public class ItemLegacyRegistry implements IRegistry<Integer, Class<?>, Class<?>>, BlockID, ItemID {
    private static final Int2ObjectOpenHashMap<Class<?>> LEGACY_ITEMS = new Int2ObjectOpenHashMap<>();
    public static int HIGHEST_LEGACY_ITEM_ID = 0;

    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;

        for (int i = 0; i < 256; ++i) {
            if (Registries.BLOCK.getClass(i) != null) {
                register(i, Registries.BLOCK.getClass(i));
            }
        }

        register(LADDER, ItemLadder.class); //65
        register(RAIL, ItemRail.class); //66
        register(CACTUS, ItemCactus.class); //81
        register(IRON_SHOVEL, ItemShovelIron.class); //256
        register(IRON_PICKAXE, ItemPickaxeIron.class); //257
        register(IRON_AXE, ItemAxeIron.class); //258
        register(FLINT_AND_STEEL, ItemFlintSteel.class); //259
        register(APPLE, ItemApple.class); //260
        register(BOW, ItemBow.class); //261
        register(ARROW, ItemArrow.class); //262
        register(DIAMOND, ItemDiamond.class); //264
        register(IRON_INGOT, ItemIngotIron.class); //265
        register(GOLD_INGOT, ItemIngotGold.class); //266
        register(IRON_SWORD, ItemSwordIron.class); //267
        register(WOODEN_SWORD, ItemSwordWood.class); //268
        register(WOODEN_SHOVEL, ItemShovelWood.class); //269
        register(WOODEN_PICKAXE, ItemPickaxeWood.class); //270
        register(WOODEN_AXE, ItemAxeWood.class); //271
        register(STONE_SWORD, ItemSwordStone.class); //272
        register(STONE_SHOVEL, ItemShovelStone.class); //273
        register(STONE_PICKAXE, ItemPickaxeStone.class); //274
        register(STONE_AXE, ItemAxeStone.class); //275
        register(DIAMOND_SWORD, ItemSwordDiamond.class); //276
        register(DIAMOND_SHOVEL, ItemShovelDiamond.class); //277
        register(DIAMOND_PICKAXE, ItemPickaxeDiamond.class); //278
        register(DIAMOND_AXE, ItemAxeDiamond.class); //279
        register(STICK, ItemStick.class); //280
        register(MUSHROOM_STEW, ItemMushroomStew.class); //282
        register(GOLD_SWORD, ItemSwordGold.class); //283
        register(GOLD_SHOVEL, ItemShovelGold.class); //284
        register(GOLD_PICKAXE, ItemPickaxeGold.class); //285
        register(GOLD_AXE, ItemAxeGold.class); //286
        register(WOODEN_HOE, ItemHoeWood.class); //290
        register(STONE_HOE, ItemHoeStone.class); //291
        register(IRON_HOE, ItemHoeIron.class); //292
        register(DIAMOND_HOE, ItemHoeDiamond.class); //293
        register(GOLD_HOE, ItemHoeGold.class); //294
        register(WHEAT_SEEDS, ItemSeedsWheat.class); //295
        register(WHEAT, ItemWheat.class); //296
        register(BREAD, ItemBread.class); //297
        register(LEATHER_CAP, ItemHelmetLeather.class); //298
        register(LEATHER_TUNIC, ItemChestplateLeather.class); //299
        register(LEATHER_PANTS, ItemLeggingsLeather.class); //300
        register(LEATHER_BOOTS, ItemBootsLeather.class); //301
        register(CHAIN_HELMET, ItemHelmetChain.class); //302
        register(CHAIN_CHESTPLATE, ItemChestplateChain.class); //303
        register(CHAIN_LEGGINGS, ItemLeggingsChain.class); //304
        register(CHAIN_BOOTS, ItemBootsChain.class); //305
        register(IRON_HELMET, ItemHelmetIron.class); //306
        register(IRON_CHESTPLATE, ItemChestplateIron.class); //307
        register(IRON_LEGGINGS, ItemLeggingsIron.class); //308
        register(IRON_BOOTS, ItemBootsIron.class); //309
        register(DIAMOND_HELMET, ItemHelmetDiamond.class); //310
        register(DIAMOND_CHESTPLATE, ItemChestplateDiamond.class); //311
        register(DIAMOND_LEGGINGS, ItemLeggingsDiamond.class); //312
        register(DIAMOND_BOOTS, ItemBootsDiamond.class); //313
        register(GOLD_HELMET, ItemHelmetGold.class); //314
        register(GOLD_CHESTPLATE, ItemChestplateGold.class); //315
        register(GOLD_LEGGINGS, ItemLeggingsGold.class); //316
        register(GOLD_BOOTS, ItemBootsGold.class); //317
        register(FLINT, ItemFlint.class); //318
        register(RAW_PORKCHOP, ItemPorkchopRaw.class); //319
        register(COOKED_PORKCHOP, ItemPorkchopCooked.class); //320
        register(PAINTING, ItemPainting.class); //321
        register(GOLDEN_APPLE, ItemAppleGold.class); //322
        register(WOODEN_DOOR, ItemDoorWood.class); //324
        register(BUCKET, ItemBucket.class); //325
        register(MINECART, ItemMinecart.class); //328
        register(SADDLE, ItemSaddle.class); //329
        register(IRON_DOOR, ItemDoorIron.class); //330
        register(REDSTONE, ItemRedstone.class); //331
        register(SNOWBALL, ItemSnowball.class); //332
        register(BOAT, ItemBoat.class); //333
        register(LEATHER, ItemLeather.class); //334
        register(KELP, ItemKelp.class); //335
        register(BRICK, ItemBrick.class); //336
        register(CLAY, ItemClay.class); //337
        register(SUGARCANE, ItemSugarcane.class); //338
        register(PAPER, ItemPaper.class); //339
        register(BOOK, ItemBook.class); //340
        register(SLIMEBALL, ItemSlimeball.class); //341
        register(MINECART_WITH_CHEST, ItemMinecartChest.class); //342
        register(EGG, ItemEgg.class); //344
        register(COMPASS, ItemCompass.class); //345
        register(FISHING_ROD, ItemFishingRod.class); //346
        register(CLOCK, ItemClock.class); //347
        register(GLOWSTONE_DUST, ItemGlowstoneDust.class); //348
        register(RAW_FISH, ItemFish.class); //349
        register(COOKED_FISH, ItemFishCooked.class); //350
        register(CAKE, ItemCake.class); //354
        register(BED, ItemBed.class); //355
        register(REPEATER, ItemRedstoneRepeater.class); //356
        register(COOKIE, ItemCookie.class); //357
        register(MAP, ItemMap.class); //358
        register(SHEARS, ItemShears.class); //359
        register(MELON, ItemMelon.class); //360
        register(PUMPKIN_SEEDS, ItemSeedsPumpkin.class); //361
        register(MELON_SEEDS, ItemSeedsMelon.class); //362
        register(RAW_BEEF, ItemBeefRaw.class); //363
        register(STEAK, ItemSteak.class); //364
        register(RAW_CHICKEN, ItemChickenRaw.class); //365
        register(COOKED_CHICKEN, ItemChickenCooked.class); //366
        register(ROTTEN_FLESH, ItemRottenFlesh.class); //367
        register(ENDER_PEARL, ItemEnderPearl.class); //368
        register(BLAZE_ROD, ItemBlazeRod.class); //369
        register(GHAST_TEAR, ItemGhastTear.class); //370
        register(GOLD_NUGGET, ItemNuggetGold.class); //371
        register(NETHER_WART, ItemNetherWart.class); //372
        register(POTION, ItemPotion.class); //373
        register(GLASS_BOTTLE, ItemGlassBottle.class); //374
        register(SPIDER_EYE, ItemSpiderEye.class); //375
        register(FERMENTED_SPIDER_EYE, ItemSpiderEyeFermented.class); //376
        register(BLAZE_POWDER, ItemBlazePowder.class); //377
        register(MAGMA_CREAM, ItemMagmaCream.class); //378
        register(BREWING_STAND, ItemBrewingStand.class); //379
        register(CAULDRON, ItemCauldron.class); //380
        register(ENDER_EYE, ItemEnderEye.class); //381
        register(GLISTERING_MELON, ItemMelonGlistering.class); //382
        register(SPAWN_EGG, ItemSpawnEgg.class); //383
        register(EXPERIENCE_BOTTLE, ItemExpBottle.class); //384
        register(FIRE_CHARGE, ItemFireCharge.class); //385
        register(BOOK_AND_QUILL, ItemBookAndQuill.class); //386
        register(WRITTEN_BOOK, ItemBookWritten.class); //387
        register(EMERALD, ItemEmerald.class); //388
        register(ITEM_FRAME, ItemItemFrame.class); //389
        register(FLOWER_POT, ItemFlowerPot.class); //390
        register(CARROT, ItemCarrot.class); //391
        register(POTATO, ItemPotato.class); //392
        register(BAKED_POTATO, ItemPotatoBaked.class); //393
        register(POISONOUS_POTATO, ItemPotatoPoisonous.class); //394
        register(EMPTY_MAP, ItemEmptyMap.class); //395
        register(GOLDEN_CARROT, ItemCarrotGolden.class); //396
        register(CARROT_ON_A_STICK, ItemCarrotOnAStick.class); //398
        register(NETHER_STAR, ItemNetherStar.class); //399
        register(PUMPKIN_PIE, ItemPumpkinPie.class); //400
        register(FIREWORKS, ItemFirework.class); //401
        register(FIREWORKSCHARGE, ItemFireworkStar.class); //402
        register(ENCHANTED_BOOK, ItemBookEnchanted.class); //403
        register(COMPARATOR, ItemRedstoneComparator.class); //404
        register(NETHER_BRICK, ItemNetherBrick.class); //405
        register(QUARTZ, ItemQuartz.class); //406
        register(MINECART_WITH_TNT, ItemMinecartTNT.class); //407
        register(MINECART_WITH_HOPPER, ItemMinecartHopper.class); //408
        register(PRISMARINE_SHARD, ItemPrismarineShard.class); //409
        register(HOPPER, ItemHopper.class);
        register(RAW_RABBIT, ItemRabbitRaw.class); //411
        register(COOKED_RABBIT, ItemRabbitCooked.class); //412
        register(RABBIT_STEW, ItemRabbitStew.class); //413
        register(RABBIT_FOOT, ItemRabbitFoot.class); //414
        register(RABBIT_HIDE, ItemRabbitHide.class); //415
        register(LEATHER_HORSE_ARMOR, ItemHorseArmorLeather.class); //416
        register(IRON_HORSE_ARMOR, ItemHorseArmorIron.class); //417
        register(GOLD_HORSE_ARMOR, ItemHorseArmorGold.class); //418
        register(DIAMOND_HORSE_ARMOR, ItemHorseArmorDiamond.class); //419
        register(LEAD, ItemLead.class); //420
        register(NAME_TAG, ItemNameTag.class); //421
        register(PRISMARINE_CRYSTALS, ItemPrismarineCrystals.class); //422
        register(RAW_MUTTON, ItemMuttonRaw.class); //423
        register(COOKED_MUTTON, ItemMuttonCooked.class); //424
        register(END_CRYSTAL, ItemEndCrystal.class); //426
        register(SPRUCE_DOOR, ItemDoorSpruce.class); //427
        register(BIRCH_DOOR, ItemDoorBirch.class); //428
        register(JUNGLE_DOOR, ItemDoorJungle.class); //429
        register(ACACIA_DOOR, ItemDoorAcacia.class); //430
        register(DARK_OAK_DOOR, ItemDoorDarkOak.class); //431
        register(CHORUS_FRUIT, ItemChorusFruit.class); //432
        register(POPPED_CHORUS_FRUIT, ItemChorusFruitPopped.class); //433
        register(BANNER_PATTERN, ItemBannerPattern.class); //434
        register(DRAGON_BREATH, ItemDragonBreath.class); //437
        register(SPLASH_POTION, ItemPotionSplash.class); //438
        register(LINGERING_POTION, ItemPotionLingering.class); //441
        register(ELYTRA, ItemElytra.class); //444
        register(SHULKER_SHELL, ItemShulkerShell.class); //445
        register(BANNER, ItemBanner.class); //446
        register(TOTEM, ItemTotem.class); //450
        register(IRON_NUGGET, ItemNuggetIron.class); //452
        register(TRIDENT, ItemTrident.class); //455
        register(BEETROOT, ItemBeetroot.class); //457
        register(BEETROOT_SEEDS, ItemSeedsBeetroot.class); //458
        register(BEETROOT_SOUP, ItemBeetrootSoup.class); //459
        register(RAW_SALMON, ItemSalmon.class); //460
        register(CLOWNFISH, ItemClownfish.class); //461
        register(PUFFERFISH, ItemPufferfish.class); //462
        register(COOKED_SALMON, ItemSalmonCooked.class); //463
        register(DRIED_KELP, ItemDriedKelp.class); //464
        register(NAUTILUS_SHELL, ItemNautilusShell.class); //465
        register(GOLDEN_APPLE_ENCHANTED, ItemAppleGoldEnchanted.class); //466
        register(HEART_OF_THE_SEA, ItemHeartOfTheSea.class); //467
        register(SCUTE, ItemScute.class); //468
        register(TURTLE_SHELL, ItemTurtleShell.class); //469
        register(PHANTOM_MEMBRANE, ItemPhantomMembrane.class); //470
        register(CROSSBOW, ItemCrossbow.class); //471
        register(SWEET_BERRIES, ItemSweetBerries.class); //477
        register(RECORD_11, ItemRecord11.class); //510
        register(RECORD_CAT, ItemRecordCat.class); //501
        register(RECORD_13, ItemRecord13.class); //500
        register(RECORD_BLOCKS, ItemRecordBlocks.class); //502
        register(RECORD_CHIRP, ItemRecordChirp.class); //503
        register(RECORD_FAR, ItemRecordFar.class); //504
        register(RECORD_WARD, ItemRecordWard.class); //509
        register(RECORD_MALL, ItemRecordMall.class); //505
        register(RECORD_MELLOHI, ItemRecordMellohi.class); //506
        register(RECORD_STAL, ItemRecordStal.class); //507
        register(RECORD_STRAD, ItemRecordStrad.class); //508
        register(RECORD_WAIT, ItemRecordWait.class); //511
        register(SHIELD, ItemShield.class); //513
        register(RECORD_5, ItemRecord5.class); //636
        register(DISC_FRAGMENT_5, ItemDiscFragment5.class); //637
        register(OAK_CHEST_BOAT, ItemChestBoatOak.class); //638
        register(BIRCH_CHEST_BOAT, ItemChestBoatBirch.class); //639
        register(JUNGLE_CHEST_BOAT, ItemChestBoatJungle.class); //640
        register(SPRUCE_CHEST_BOAT, ItemChestBoatSpruce.class); //641
        register(ACACIA_CHEST_BOAT, ItemChestBoatAcacia.class); //642
        register(DARK_OAK_CHEST_BOAT, ItemChestBoatDarkOak.class); //643
        register(MANGROVE_CHEST_BOAT, ItemChestBoatMangrove.class); //644
        register(BAMBOO_CHEST_RAFT, ItemChestRaftBamboo.class); //648
        register(CHERRY_CHEST_BOAT, ItemChestBoatCherry.class); //649
        register(PALE_OAK_CHEST_BOAT, ItemChestBoatPaleOak.class); //650
        register(GLOW_BERRIES, ItemGlowBerries.class); //654
        register(RECORD_RELIC, ItemRecordRelic.class); //701
        register(CAMPFIRE, ItemCampfire.class); //720
        register(SUSPICIOUS_STEW, ItemSuspiciousStew.class); //734
        register(HONEYCOMB, ItemHoneycomb.class); //736
        register(HONEY_BOTTLE, ItemHoneyBottle.class); //737
        register(LODESTONE_COMPASS, ItemLodestoneCompass.class); //741
        register(NETHERITE_INGOT, ItemIngotNetherite.class); //742
        register(NETHERITE_SWORD, ItemSwordNetherite.class); //743
        register(NETHERITE_SHOVEL, ItemShovelNetherite.class); //744
        register(NETHERITE_PICKAXE, ItemPickaxeNetherite.class); //745
        register(NETHERITE_AXE, ItemAxeNetherite.class); //746
        register(NETHERITE_HOE, ItemHoeNetherite.class); //747
        register(NETHERITE_HELMET, ItemHelmetNetherite.class); //748
        register(NETHERITE_CHESTPLATE, ItemChestplateNetherite.class); //749
        register(NETHERITE_LEGGINGS, ItemLeggingsNetherite.class); //750
        register(NETHERITE_BOOTS, ItemBootsNetherite.class); //751
        register(NETHERITE_SCRAP, ItemScrapNetherite.class); //752
        register(CRIMSON_DOOR, ItemDoorCrimson.class); //755
        register(WARPED_DOOR, ItemDoorWarped.class); //756
        register(WARPED_FUNGUS_ON_A_STICK, ItemWarpedFungusOnAStick.class); //757
        register(CHAIN, ItemChain.class); //758
        register(RECORD_PIGSTEP, ItemRecordPigstep.class); //759
        register(NETHER_SPROUTS, ItemNetherSprouts.class); //760
        register(SPYGLASS, ItemSpyglass.class); //772
        register(RECORD_OTHERSIDE, ItemRecordOtherside.class); //773
        register(SOUL_CAMPFIRE, ItemCampfireSoul.class); //801
        register(GLOW_ITEM_FRAME, ItemItemFrameGlow.class); //850

        // Add vanilla items to NAMESPACED_ID_ITEM
        RuntimeItemMapping mapping = RuntimeItems.getMapping(ProtocolInfo.CURRENT_PROTOCOL);
        for (Object2IntMap.Entry<String> entity : mapping.getName2RuntimeId().object2IntEntrySet()) {
            try {
                RuntimeItemMapping.LegacyEntry legacyEntry = mapping.fromRuntime(entity.getIntValue());
                int id = legacyEntry.getLegacyId();
                int damage = 0;
                if (legacyEntry.isHasDamage()) {
                    damage = legacyEntry.getDamage();
                }
                Item item = Item.get(id, damage);
                if (item.getId() != 0 && !Registries.ITEM.isItemRegistered(entity.getKey())) {
                    Registries.ITEM.register(entity.getKey(), item::clone);
                }
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void register(Integer key, Class<?> value) {
        LEGACY_ITEMS.put(key, value);
        if (key > HIGHEST_LEGACY_ITEM_ID) HIGHEST_LEGACY_ITEM_ID = key;
    }

    @Override
    public Class<?> get(Integer key) {
        return LEGACY_ITEMS.get(key);
    }

    @Override
    public void trim() {
        LEGACY_ITEMS.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        LEGACY_ITEMS.clear();
        init();
    }
}
