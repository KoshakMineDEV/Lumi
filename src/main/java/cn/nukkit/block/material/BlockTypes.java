package cn.nukkit.block.material;

import cn.nukkit.item.material.ItemTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;

/**
 * This class is generated automatically, do not change it manually.
 */
public final class BlockTypes {
    private static final Int2ObjectMap<BlockType> RUNTIME_TO_TYPE = new Int2ObjectOpenHashMap<>();

    private static final Object2ObjectMap<String, BlockType> ID_TO_TYPE = new Object2ObjectOpenHashMap<>();

    public static final BlockType ACACIA_BUTTON = register("minecraft:acacia_button", -140);

    public static final BlockType ACACIA_CHEST_BOAT = register("minecraft:acacia_chest_boat", 679);

    public static final BlockType ACACIA_DOUBLE_SLAB = register("minecraft:acacia_double_slab", -812);

    public static final BlockType ACACIA_FENCE = register("minecraft:acacia_fence", -575);

    public static final BlockType ACACIA_FENCE_GATE = register("minecraft:acacia_fence_gate", 187);

    public static final BlockType ACACIA_HANGING_SIGN = register("minecraft:acacia_hanging_sign", -504);

    public static final BlockType ACACIA_LEAVES = register("minecraft:acacia_leaves", 161);

    public static final BlockType ACACIA_LOG = register("minecraft:acacia_log", 162);

    public static final BlockType ACACIA_PLANKS = register("minecraft:acacia_planks", -742);

    public static final BlockType ACACIA_PRESSURE_PLATE = register("minecraft:acacia_pressure_plate", -150);

    public static final BlockType ACACIA_SAPLING = register("minecraft:acacia_sapling", -828);

    public static final BlockType ACACIA_SIGN = register("minecraft:acacia_sign", 612);

    public static final BlockType ACACIA_SLAB = register("minecraft:acacia_slab", -807);

    public static final BlockType ACACIA_STAIRS = register("minecraft:acacia_stairs", 163);

    public static final BlockType ACACIA_STANDING_SIGN = register("minecraft:acacia_standing_sign", -190);

    public static final BlockType ACACIA_TRAPDOOR = register("minecraft:acacia_trapdoor", -145);

    public static final BlockType ACACIA_WALL_SIGN = register("minecraft:acacia_wall_sign", -191);

    public static final BlockType ACACIA_WOOD = register("minecraft:acacia_wood", -817);

    public static final BlockType ACTIVATOR_RAIL = register("minecraft:activator_rail", 126);

    public static final BlockType AIR = register("minecraft:air", -158);

    public static final BlockType ALLIUM = register("minecraft:allium", -831);

    public static final BlockType ALLOW = register("minecraft:allow", 210);

    public static final BlockType AMETHYST_BLOCK = register("minecraft:amethyst_block", -327);

    public static final BlockType AMETHYST_CLUSTER = register("minecraft:amethyst_cluster", -329);

    public static final BlockType AMETHYST_SHARD = register("minecraft:amethyst_shard", 661);

    public static final BlockType ANCIENT_DEBRIS = register("minecraft:ancient_debris", -271);

    public static final BlockType ANDESITE = register("minecraft:andesite", -594);

    public static final BlockType ANDESITE_DOUBLE_SLAB = register("minecraft:andesite_double_slab", -920);

    public static final BlockType ANDESITE_SLAB = register("minecraft:andesite_slab", -893);

    public static final BlockType ANDESITE_STAIRS = register("minecraft:andesite_stairs", -171);

    public static final BlockType ANDESITE_WALL = register("minecraft:andesite_wall", -974);

    public static final BlockType ANVIL = register("minecraft:anvil", 145);

    public static final BlockType APPLE = register("minecraft:apple", 278);

    public static final BlockType ARMOR_STAND = register("minecraft:armor_stand", 585);

    public static final BlockType ARROW = register("minecraft:arrow", 325);

    public static final BlockType AZALEA = register("minecraft:azalea", -337);

    public static final BlockType AZALEA_LEAVES = register("minecraft:azalea_leaves", -324);

    public static final BlockType AZALEA_LEAVES_FLOWERED = register("minecraft:azalea_leaves_flowered", -325);

    public static final BlockType AZURE_BLUET = register("minecraft:azure_bluet", -832);

    public static final BlockType BAKED_POTATO = register("minecraft:baked_potato", 303);

    public static final BlockType BALLOON = register("minecraft:balloon", 635);

    public static final BlockType BAMBOO = register("minecraft:bamboo", -163);

    public static final BlockType BAMBOO_BLOCK = register("minecraft:bamboo_block", -527);

    public static final BlockType BAMBOO_BUTTON = register("minecraft:bamboo_button", -511);

    public static final BlockType BAMBOO_CHEST_RAFT = register("minecraft:bamboo_chest_raft", 691);

    public static final BlockType BAMBOO_DOOR = register("minecraft:bamboo_door", -517);

    public static final BlockType BAMBOO_DOUBLE_SLAB = register("minecraft:bamboo_double_slab", -521);

    public static final BlockType BAMBOO_FENCE = register("minecraft:bamboo_fence", -515);

    public static final BlockType BAMBOO_FENCE_GATE = register("minecraft:bamboo_fence_gate", -516);

    public static final BlockType BAMBOO_HANGING_SIGN = register("minecraft:bamboo_hanging_sign", -522);

    public static final BlockType BAMBOO_MOSAIC = register("minecraft:bamboo_mosaic", -509);

    public static final BlockType BAMBOO_MOSAIC_DOUBLE_SLAB = register("minecraft:bamboo_mosaic_double_slab", -525);

    public static final BlockType BAMBOO_MOSAIC_SLAB = register("minecraft:bamboo_mosaic_slab", -524);

    public static final BlockType BAMBOO_MOSAIC_STAIRS = register("minecraft:bamboo_mosaic_stairs", -523);

    public static final BlockType BAMBOO_PLANKS = register("minecraft:bamboo_planks", -510);

    public static final BlockType BAMBOO_PRESSURE_PLATE = register("minecraft:bamboo_pressure_plate", -514);

    public static final BlockType BAMBOO_SAPLING = register("minecraft:bamboo_sapling", -164);

    public static final BlockType BAMBOO_SIGN = register("minecraft:bamboo_sign", 689);

    public static final BlockType BAMBOO_SLAB = register("minecraft:bamboo_slab", -513);

    public static final BlockType BAMBOO_STAIRS = register("minecraft:bamboo_stairs", -512);

    public static final BlockType BAMBOO_STANDING_SIGN = register("minecraft:bamboo_standing_sign", -518);

    public static final BlockType BAMBOO_TRAPDOOR = register("minecraft:bamboo_trapdoor", -520);

    public static final BlockType BAMBOO_WALL_SIGN = register("minecraft:bamboo_wall_sign", -519);

    public static final BlockType BANNER = register("minecraft:banner", 600);

    public static final BlockType BANNER_PATTERN = register("minecraft:banner_pattern", 825);

    public static final BlockType BARREL = register("minecraft:barrel", -203);

    public static final BlockType BARRIER = register("minecraft:barrier", -161);

    public static final BlockType BASALT = register("minecraft:basalt", -234);

    public static final BlockType BEACON = register("minecraft:beacon", 138);

    public static final BlockType BEDROCK = register("minecraft:bedrock", 7);

    public static final BlockType BEE_NEST = register("minecraft:bee_nest", -218);

    public static final BlockType BEEF = register("minecraft:beef", 295);

    public static final BlockType BEEHIVE = register("minecraft:beehive", -219);

    public static final BlockType BEETROOT_SEEDS = register("minecraft:beetroot_seeds", 317);

    public static final BlockType BEETROOT_SOUP = register("minecraft:beetroot_soup", 308);

    public static final BlockType BELL = register("minecraft:bell", -206);

    public static final BlockType BIG_DRIPLEAF = register("minecraft:big_dripleaf", -323);

    public static final BlockType BIRCH_BUTTON = register("minecraft:birch_button", -141);

    public static final BlockType BIRCH_CHEST_BOAT = register("minecraft:birch_chest_boat", 676);

    public static final BlockType BIRCH_DOUBLE_SLAB = register("minecraft:birch_double_slab", -810);

    public static final BlockType BIRCH_FENCE = register("minecraft:birch_fence", -576);

    public static final BlockType BIRCH_FENCE_GATE = register("minecraft:birch_fence_gate", 184);

    public static final BlockType BIRCH_HANGING_SIGN = register("minecraft:birch_hanging_sign", -502);

    public static final BlockType BIRCH_LEAVES = register("minecraft:birch_leaves", -801);

    public static final BlockType BIRCH_LOG = register("minecraft:birch_log", -570);

    public static final BlockType BIRCH_PLANKS = register("minecraft:birch_planks", -740);

    public static final BlockType BIRCH_PRESSURE_PLATE = register("minecraft:birch_pressure_plate", -151);

    public static final BlockType BIRCH_SAPLING = register("minecraft:birch_sapling", -826);

    public static final BlockType BIRCH_SIGN = register("minecraft:birch_sign", 610);

    public static final BlockType BIRCH_SLAB = register("minecraft:birch_slab", -805);

    public static final BlockType BIRCH_STAIRS = register("minecraft:birch_stairs", 135);

    public static final BlockType BIRCH_STANDING_SIGN = register("minecraft:birch_standing_sign", -186);

    public static final BlockType BIRCH_TRAPDOOR = register("minecraft:birch_trapdoor", -146);

    public static final BlockType BIRCH_WALL_SIGN = register("minecraft:birch_wall_sign", -187);

    public static final BlockType BIRCH_WOOD = register("minecraft:birch_wood", -815);

    public static final BlockType BLACK_CANDLE = register("minecraft:black_candle", -428);

    public static final BlockType BLACK_CANDLE_CAKE = register("minecraft:black_candle_cake", -445);

    public static final BlockType BLACK_CARPET = register("minecraft:black_carpet", -611);

    public static final BlockType BLACK_CONCRETE = register("minecraft:black_concrete", -642);

    public static final BlockType BLACK_CONCRETE_POWDER = register("minecraft:black_concrete_powder", -723);

    public static final BlockType BLACK_GLAZED_TERRACOTTA = register("minecraft:black_glazed_terracotta", 235);

    public static final BlockType BLACK_SHULKER_BOX = register("minecraft:black_shulker_box", -627);

    public static final BlockType BLACK_STAINED_GLASS = register("minecraft:black_stained_glass", -687);

    public static final BlockType BLACK_STAINED_GLASS_PANE = register("minecraft:black_stained_glass_pane", -657);

    public static final BlockType BLACK_TERRACOTTA = register("minecraft:black_terracotta", -738);

    public static final BlockType BLACK_WOOL = register("minecraft:black_wool", -554);

    public static final BlockType BLACKSTONE = register("minecraft:blackstone", -273);

    public static final BlockType BLACKSTONE_DOUBLE_SLAB = register("minecraft:blackstone_double_slab", -283);

    public static final BlockType BLACKSTONE_SLAB = register("minecraft:blackstone_slab", -282);

    public static final BlockType BLACKSTONE_STAIRS = register("minecraft:blackstone_stairs", -276);

    public static final BlockType BLACKSTONE_WALL = register("minecraft:blackstone_wall", -277);

    public static final BlockType BLAST_FURNACE = register("minecraft:blast_furnace", -196);

    public static final BlockType BLAZE_POWDER = register("minecraft:blaze_powder", 456);

    public static final BlockType BLAZE_ROD = register("minecraft:blaze_rod", 449);

    public static final BlockType BLEACH = register("minecraft:bleach", 633);

    public static final BlockType BLUE_CANDLE = register("minecraft:blue_candle", -424);

    public static final BlockType BLUE_CANDLE_CAKE = register("minecraft:blue_candle_cake", -441);

    public static final BlockType BLUE_CARPET = register("minecraft:blue_carpet", -607);

    public static final BlockType BLUE_CONCRETE = register("minecraft:blue_concrete", -638);

    public static final BlockType BLUE_CONCRETE_POWDER = register("minecraft:blue_concrete_powder", -719);

    public static final BlockType BLUE_GLAZED_TERRACOTTA = register("minecraft:blue_glazed_terracotta", 231);

    public static final BlockType BLUE_ICE = register("minecraft:blue_ice", -11);

    public static final BlockType BLUE_ORCHID = register("minecraft:blue_orchid", -830);

    public static final BlockType BLUE_SHULKER_BOX = register("minecraft:blue_shulker_box", -623);

    public static final BlockType BLUE_STAINED_GLASS = register("minecraft:blue_stained_glass", -683);

    public static final BlockType BLUE_STAINED_GLASS_PANE = register("minecraft:blue_stained_glass_pane", -653);

    public static final BlockType BLUE_TERRACOTTA = register("minecraft:blue_terracotta", -734);

    public static final BlockType BLUE_WOOL = register("minecraft:blue_wool", -563);

    public static final BlockType BOAT = register("minecraft:boat", 823);

    public static final BlockType BONE = register("minecraft:bone", 441);

    public static final BlockType BONE_BLOCK = register("minecraft:bone_block", 216);

    public static final BlockType BOOK = register("minecraft:book", 413);

    public static final BlockType BOOKSHELF = register("minecraft:bookshelf", 47);

    public static final BlockType BORDER_BLOCK = register("minecraft:border_block", 212);

    public static final BlockType BOW = register("minecraft:bow", 324);

    public static final BlockType BOWL = register("minecraft:bowl", 346);

    public static final BlockType BRAIN_CORAL = register("minecraft:brain_coral", -581);

    public static final BlockType BRAIN_CORAL_BLOCK = register("minecraft:brain_coral_block", -849);

    public static final BlockType BRAIN_CORAL_FAN = register("minecraft:brain_coral_fan", -840);

    public static final BlockType BRAIN_CORAL_WALL_FAN = register("minecraft:brain_coral_wall_fan", -904);

    public static final BlockType BREAD = register("minecraft:bread", 283);

    public static final BlockType BREWING_STAND = register("minecraft:brewing_stand", 458);

    public static final BlockType BRICK = register("minecraft:brick", 409);

    public static final BlockType BRICK_BLOCK = register("minecraft:brick_block", 45);

    public static final BlockType BRICK_DOUBLE_SLAB = register("minecraft:brick_double_slab", -880);

    public static final BlockType BRICK_SLAB = register("minecraft:brick_slab", -874);

    public static final BlockType BRICK_STAIRS = register("minecraft:brick_stairs", 108);

    public static final BlockType BRICK_WALL = register("minecraft:brick_wall", -976);

    public static final BlockType BROWN_CANDLE = register("minecraft:brown_candle", -425);

    public static final BlockType BROWN_CANDLE_CAKE = register("minecraft:brown_candle_cake", -442);

    public static final BlockType BROWN_CARPET = register("minecraft:brown_carpet", -608);

    public static final BlockType BROWN_CONCRETE = register("minecraft:brown_concrete", -639);

    public static final BlockType BROWN_CONCRETE_POWDER = register("minecraft:brown_concrete_powder", -720);

    public static final BlockType BROWN_GLAZED_TERRACOTTA = register("minecraft:brown_glazed_terracotta", 232);

    public static final BlockType BROWN_MUSHROOM = register("minecraft:brown_mushroom", 39);

    public static final BlockType BROWN_MUSHROOM_BLOCK = register("minecraft:brown_mushroom_block", 99);

    public static final BlockType BROWN_SHULKER_BOX = register("minecraft:brown_shulker_box", -624);

    public static final BlockType BROWN_STAINED_GLASS = register("minecraft:brown_stained_glass", -684);

    public static final BlockType BROWN_STAINED_GLASS_PANE = register("minecraft:brown_stained_glass_pane", -654);

    public static final BlockType BROWN_TERRACOTTA = register("minecraft:brown_terracotta", -735);

    public static final BlockType BROWN_WOOL = register("minecraft:brown_wool", -555);

    public static final BlockType BUBBLE_COLUMN = register("minecraft:bubble_column", -160);

    public static final BlockType BUBBLE_CORAL = register("minecraft:bubble_coral", -582);

    public static final BlockType BUBBLE_CORAL_BLOCK = register("minecraft:bubble_coral_block", -850);

    public static final BlockType BUBBLE_CORAL_FAN = register("minecraft:bubble_coral_fan", -841);

    public static final BlockType BUBBLE_CORAL_WALL_FAN = register("minecraft:bubble_coral_wall_fan", -136);

    public static final BlockType BUCKET = register("minecraft:bucket", 385);

    public static final BlockType BUDDING_AMETHYST = register("minecraft:budding_amethyst", -328);

    public static final BlockType BUSH = register("minecraft:bush", -1023);

    public static final BlockType CACTUS = register("minecraft:cactus", 81);

    public static final BlockType CACTUS_FLOWER = register("minecraft:cactus_flower", -1030);

    public static final BlockType CALCITE = register("minecraft:calcite", -326);

    public static final BlockType CALIBRATED_SCULK_SENSOR = register("minecraft:calibrated_sculk_sensor", -580);

    public static final BlockType CANDLE = register("minecraft:candle", -412);

    public static final BlockType CANDLE_CAKE = register("minecraft:candle_cake", -429);

    public static final BlockType CARPET = register("minecraft:carpet", 780);

    public static final BlockType CARROT = register("minecraft:carrot", 301);

    public static final BlockType CARROT_ON_A_STICK = register("minecraft:carrot_on_a_stick", 550);

    public static final BlockType CARROTS = register("minecraft:carrots", 141);

    public static final BlockType CARTOGRAPHY_TABLE = register("minecraft:cartography_table", -200);

    public static final BlockType CARVED_PUMPKIN = register("minecraft:carved_pumpkin", -155);

    public static final BlockType CAVE_VINES = register("minecraft:cave_vines", -322);

    public static final BlockType CAVE_VINES_BODY_WITH_BERRIES = register("minecraft:cave_vines_body_with_berries", -375);

    public static final BlockType CAVE_VINES_HEAD_WITH_BERRIES = register("minecraft:cave_vines_head_with_berries", -376);

    public static final BlockType CHAIN_COMMAND_BLOCK = register("minecraft:chain_command_block", 189);

    public static final BlockType CHAINMAIL_BOOTS = register("minecraft:chainmail_boots", 367);

    public static final BlockType CHAINMAIL_CHESTPLATE = register("minecraft:chainmail_chestplate", 365);

    public static final BlockType CHAINMAIL_HELMET = register("minecraft:chainmail_helmet", 364);

    public static final BlockType CHAINMAIL_LEGGINGS = register("minecraft:chainmail_leggings", 366);

    public static final BlockType CHALKBOARD = register("minecraft:chalkboard", 230);

    public static final BlockType CHEMICAL_HEAT = register("minecraft:chemical_heat", 192);

    public static final BlockType CHEMISTRY_TABLE = register("minecraft:chemistry_table", 817);

    public static final BlockType CHERRY_BUTTON = register("minecraft:cherry_button", -530);

    public static final BlockType CHERRY_CHEST_BOAT = register("minecraft:cherry_chest_boat", 687);

    public static final BlockType CHERRY_DOOR = register("minecraft:cherry_door", -531);

    public static final BlockType CHERRY_DOUBLE_SLAB = register("minecraft:cherry_double_slab", -540);

    public static final BlockType CHERRY_FENCE = register("minecraft:cherry_fence", -532);

    public static final BlockType CHERRY_FENCE_GATE = register("minecraft:cherry_fence_gate", -533);

    public static final BlockType CHERRY_HANGING_SIGN = register("minecraft:cherry_hanging_sign", -534);

    public static final BlockType CHERRY_LEAVES = register("minecraft:cherry_leaves", -548);

    public static final BlockType CHERRY_LOG = register("minecraft:cherry_log", -536);

    public static final BlockType CHERRY_PLANKS = register("minecraft:cherry_planks", -537);

    public static final BlockType CHERRY_PRESSURE_PLATE = register("minecraft:cherry_pressure_plate", -538);

    public static final BlockType CHERRY_SAPLING = register("minecraft:cherry_sapling", -547);

    public static final BlockType CHERRY_SIGN = register("minecraft:cherry_sign", 688);

    public static final BlockType CHERRY_SLAB = register("minecraft:cherry_slab", -539);

    public static final BlockType CHERRY_STAIRS = register("minecraft:cherry_stairs", -541);

    public static final BlockType CHERRY_STANDING_SIGN = register("minecraft:cherry_standing_sign", -542);

    public static final BlockType CHERRY_TRAPDOOR = register("minecraft:cherry_trapdoor", -543);

    public static final BlockType CHERRY_WALL_SIGN = register("minecraft:cherry_wall_sign", -544);

    public static final BlockType CHERRY_WOOD = register("minecraft:cherry_wood", -546);

    public static final BlockType CHEST = register("minecraft:chest", 54);

    public static final BlockType CHEST_BOAT = register("minecraft:chest_boat", 682);

    public static final BlockType CHEST_MINECART = register("minecraft:chest_minecart", 415);

    public static final BlockType CHICKEN = register("minecraft:chicken", 297);

    public static final BlockType CHIPPED_ANVIL = register("minecraft:chipped_anvil", -959);

    public static final BlockType CHISELED_BOOKSHELF = register("minecraft:chiseled_bookshelf", -526);

    public static final BlockType CHISELED_COPPER = register("minecraft:chiseled_copper", -760);

    public static final BlockType CHISELED_DEEPSLATE = register("minecraft:chiseled_deepslate", -395);

    public static final BlockType CHISELED_NETHER_BRICKS = register("minecraft:chiseled_nether_bricks", -302);

    public static final BlockType CHISELED_POLISHED_BLACKSTONE = register("minecraft:chiseled_polished_blackstone", -279);

    public static final BlockType CHISELED_QUARTZ_BLOCK = register("minecraft:chiseled_quartz_block", -953);

    public static final BlockType CHISELED_RED_SANDSTONE = register("minecraft:chiseled_red_sandstone", -956);

    public static final BlockType CHISELED_RESIN_BRICKS = register("minecraft:chiseled_resin_bricks", -1020);

    public static final BlockType CHISELED_SANDSTONE = register("minecraft:chiseled_sandstone", -944);

    public static final BlockType CHISELED_STONE_BRICKS = register("minecraft:chiseled_stone_bricks", -870);

    public static final BlockType CHISELED_TUFF = register("minecraft:chiseled_tuff", -753);

    public static final BlockType CHISELED_TUFF_BRICKS = register("minecraft:chiseled_tuff_bricks", -759);

    public static final BlockType CHORUS_FLOWER = register("minecraft:chorus_flower", 200);

    public static final BlockType CHORUS_FRUIT = register("minecraft:chorus_fruit", 591);

    public static final BlockType CHORUS_PLANT = register("minecraft:chorus_plant", 240);

    public static final BlockType CLAY = register("minecraft:clay", 82);

    public static final BlockType CLAY_BALL = register("minecraft:clay_ball", 410);

    public static final BlockType CLIENT_REQUEST_PLACEHOLDER_BLOCK = register("minecraft:client_request_placeholder_block", -465);

    public static final BlockType CLOCK = register("minecraft:clock", 419);

    public static final BlockType CLOSED_EYEBLOSSOM = register("minecraft:closed_eyeblossom", -1019);

    public static final BlockType COAL = register("minecraft:coal", 326);

    public static final BlockType COAL_BLOCK = register("minecraft:coal_block", 173);

    public static final BlockType COAL_ORE = register("minecraft:coal_ore", 16);

    public static final BlockType COARSE_DIRT = register("minecraft:coarse_dirt", -962);

    public static final BlockType COBBLED_DEEPSLATE = register("minecraft:cobbled_deepslate", -379);

    public static final BlockType COBBLED_DEEPSLATE_DOUBLE_SLAB = register("minecraft:cobbled_deepslate_double_slab", -396);

    public static final BlockType COBBLED_DEEPSLATE_SLAB = register("minecraft:cobbled_deepslate_slab", -380);

    public static final BlockType COBBLED_DEEPSLATE_STAIRS = register("minecraft:cobbled_deepslate_stairs", -381);

    public static final BlockType COBBLED_DEEPSLATE_WALL = register("minecraft:cobbled_deepslate_wall", -382);

    public static final BlockType COBBLESTONE = register("minecraft:cobblestone", 4);

    public static final BlockType COBBLESTONE_DOUBLE_SLAB = register("minecraft:cobblestone_double_slab", -879);

    public static final BlockType COBBLESTONE_SLAB = register("minecraft:cobblestone_slab", -873);

    public static final BlockType COBBLESTONE_WALL = register("minecraft:cobblestone_wall", 139);

    public static final BlockType COCOA = register("minecraft:cocoa", 127);

    public static final BlockType COD = register("minecraft:cod", 286);

    public static final BlockType COLORED_TORCH_BLUE = register("minecraft:colored_torch_blue", 204);

    public static final BlockType COLORED_TORCH_BP = register("minecraft:colored_torch_bp", 821);

    public static final BlockType COLORED_TORCH_GREEN = register("minecraft:colored_torch_green", -963);

    public static final BlockType COLORED_TORCH_PURPLE = register("minecraft:colored_torch_purple", -964);

    public static final BlockType COLORED_TORCH_RED = register("minecraft:colored_torch_red", 202);

    public static final BlockType COLORED_TORCH_RG = register("minecraft:colored_torch_rg", 820);

    public static final BlockType COMMAND_BLOCK = register("minecraft:command_block", 137);

    public static final BlockType COMMAND_BLOCK_MINECART = register("minecraft:command_block_minecart", 596);

    public static final BlockType COMPARATOR = register("minecraft:comparator", 555);

    public static final BlockType COMPASS = register("minecraft:compass", 417);

    public static final BlockType COMPOSTER = register("minecraft:composter", -213);

    public static final BlockType COMPOUND = register("minecraft:compound", 631);

    public static final BlockType COMPOUND_CREATOR = register("minecraft:compound_creator", 238);

    public static final BlockType CONCRETE = register("minecraft:concrete", 806);

    public static final BlockType CONCRETE_POWDER = register("minecraft:concrete_powder", 807);

    public static final BlockType CONDUIT = register("minecraft:conduit", -157);

    public static final BlockType COOKED_BEEF = register("minecraft:cooked_beef", 296);

    public static final BlockType COOKED_CHICKEN = register("minecraft:cooked_chicken", 298);

    public static final BlockType COOKED_COD = register("minecraft:cooked_cod", 290);

    public static final BlockType COOKED_MUTTON = register("minecraft:cooked_mutton", 584);

    public static final BlockType COOKED_PORKCHOP = register("minecraft:cooked_porkchop", 285);

    public static final BlockType COOKED_RABBIT = register("minecraft:cooked_rabbit", 311);

    public static final BlockType COOKED_SALMON = register("minecraft:cooked_salmon", 291);

    public static final BlockType COOKIE = register("minecraft:cookie", 293);

    public static final BlockType COPPER_BLOCK = register("minecraft:copper_block", -340);

    public static final BlockType COPPER_BULB = register("minecraft:copper_bulb", -776);

    public static final BlockType COPPER_DOOR = register("minecraft:copper_door", -784);

    public static final BlockType COPPER_GRATE = register("minecraft:copper_grate", -768);

    public static final BlockType COPPER_INGOT = register("minecraft:copper_ingot", 538);

    public static final BlockType COPPER_ORE = register("minecraft:copper_ore", -311);

    public static final BlockType COPPER_TRAPDOOR = register("minecraft:copper_trapdoor", -792);

    public static final BlockType CORAL = register("minecraft:coral", 802);

    public static final BlockType CORAL_BLOCK = register("minecraft:coral_block", 784);

    public static final BlockType CORAL_FAN = register("minecraft:coral_fan", 793);

    public static final BlockType CORAL_FAN_DEAD = register("minecraft:coral_fan_dead", 794);

    public static final BlockType CORNFLOWER = register("minecraft:cornflower", -838);

    public static final BlockType CRACKED_DEEPSLATE_BRICKS = register("minecraft:cracked_deepslate_bricks", -410);

    public static final BlockType CRACKED_DEEPSLATE_TILES = register("minecraft:cracked_deepslate_tiles", -409);

    public static final BlockType CRACKED_NETHER_BRICKS = register("minecraft:cracked_nether_bricks", -303);

    public static final BlockType CRACKED_POLISHED_BLACKSTONE_BRICKS = register("minecraft:cracked_polished_blackstone_bricks", -280);

    public static final BlockType CRACKED_STONE_BRICKS = register("minecraft:cracked_stone_bricks", -869);

    public static final BlockType CRAFTER = register("minecraft:crafter", -313);

    public static final BlockType CRAFTING_TABLE = register("minecraft:crafting_table", 58);

    public static final BlockType CREAKING_HEART = register("minecraft:creaking_heart", -1012);

    public static final BlockType CREEPER_BANNER_PATTERN = register("minecraft:creeper_banner_pattern", 615);

    public static final BlockType CREEPER_HEAD = register("minecraft:creeper_head", -968);

    public static final BlockType CRIMSON_BUTTON = register("minecraft:crimson_button", -260);

    public static final BlockType CRIMSON_DOUBLE_SLAB = register("minecraft:crimson_double_slab", -266);

    public static final BlockType CRIMSON_FENCE = register("minecraft:crimson_fence", -256);

    public static final BlockType CRIMSON_FENCE_GATE = register("minecraft:crimson_fence_gate", -258);

    public static final BlockType CRIMSON_FUNGUS = register("minecraft:crimson_fungus", -228);

    public static final BlockType CRIMSON_HANGING_SIGN = register("minecraft:crimson_hanging_sign", -506);

    public static final BlockType CRIMSON_HYPHAE = register("minecraft:crimson_hyphae", -299);

    public static final BlockType CRIMSON_NYLIUM = register("minecraft:crimson_nylium", -232);

    public static final BlockType CRIMSON_PLANKS = register("minecraft:crimson_planks", -242);

    public static final BlockType CRIMSON_PRESSURE_PLATE = register("minecraft:crimson_pressure_plate", -262);

    public static final BlockType CRIMSON_ROOTS = register("minecraft:crimson_roots", -223);

    public static final BlockType CRIMSON_SIGN = register("minecraft:crimson_sign", 651);

    public static final BlockType CRIMSON_SLAB = register("minecraft:crimson_slab", -264);

    public static final BlockType CRIMSON_STAIRS = register("minecraft:crimson_stairs", -254);

    public static final BlockType CRIMSON_STANDING_SIGN = register("minecraft:crimson_standing_sign", -250);

    public static final BlockType CRIMSON_STEM = register("minecraft:crimson_stem", -225);

    public static final BlockType CRIMSON_TRAPDOOR = register("minecraft:crimson_trapdoor", -246);

    public static final BlockType CRIMSON_WALL_SIGN = register("minecraft:crimson_wall_sign", -252);

    public static final BlockType CROSSBOW = register("minecraft:crossbow", 608);

    public static final BlockType CRYING_OBSIDIAN = register("minecraft:crying_obsidian", -289);

    public static final BlockType CUT_COPPER = register("minecraft:cut_copper", -347);

    public static final BlockType CUT_COPPER_SLAB = register("minecraft:cut_copper_slab", -361);

    public static final BlockType CUT_COPPER_STAIRS = register("minecraft:cut_copper_stairs", -354);

    public static final BlockType CUT_RED_SANDSTONE = register("minecraft:cut_red_sandstone", -957);

    public static final BlockType CUT_RED_SANDSTONE_DOUBLE_SLAB = register("minecraft:cut_red_sandstone_double_slab", -928);

    public static final BlockType CUT_RED_SANDSTONE_SLAB = register("minecraft:cut_red_sandstone_slab", -901);

    public static final BlockType CUT_SANDSTONE = register("minecraft:cut_sandstone", -945);

    public static final BlockType CUT_SANDSTONE_DOUBLE_SLAB = register("minecraft:cut_sandstone_double_slab", -927);

    public static final BlockType CUT_SANDSTONE_SLAB = register("minecraft:cut_sandstone_slab", -900);

    public static final BlockType CYAN_CANDLE = register("minecraft:cyan_candle", -422);

    public static final BlockType CYAN_CANDLE_CAKE = register("minecraft:cyan_candle_cake", -439);

    public static final BlockType CYAN_CARPET = register("minecraft:cyan_carpet", -605);

    public static final BlockType CYAN_CONCRETE = register("minecraft:cyan_concrete", -636);

    public static final BlockType CYAN_CONCRETE_POWDER = register("minecraft:cyan_concrete_powder", -717);

    public static final BlockType CYAN_GLAZED_TERRACOTTA = register("minecraft:cyan_glazed_terracotta", 229);

    public static final BlockType CYAN_SHULKER_BOX = register("minecraft:cyan_shulker_box", -621);

    public static final BlockType CYAN_STAINED_GLASS = register("minecraft:cyan_stained_glass", -681);

    public static final BlockType CYAN_STAINED_GLASS_PANE = register("minecraft:cyan_stained_glass_pane", -651);

    public static final BlockType CYAN_TERRACOTTA = register("minecraft:cyan_terracotta", -732);

    public static final BlockType CYAN_WOOL = register("minecraft:cyan_wool", -561);

    public static final BlockType DAMAGED_ANVIL = register("minecraft:damaged_anvil", -960);

    public static final BlockType DANDELION = register("minecraft:dandelion", 37);

    public static final BlockType DARK_OAK_BUTTON = register("minecraft:dark_oak_button", -142);

    public static final BlockType DARK_OAK_CHEST_BOAT = register("minecraft:dark_oak_chest_boat", 680);

    public static final BlockType DARK_OAK_DOUBLE_SLAB = register("minecraft:dark_oak_double_slab", -813);

    public static final BlockType DARK_OAK_FENCE = register("minecraft:dark_oak_fence", -577);

    public static final BlockType DARK_OAK_FENCE_GATE = register("minecraft:dark_oak_fence_gate", 186);

    public static final BlockType DARK_OAK_HANGING_SIGN = register("minecraft:dark_oak_hanging_sign", -505);

    public static final BlockType DARK_OAK_LEAVES = register("minecraft:dark_oak_leaves", -803);

    public static final BlockType DARK_OAK_LOG = register("minecraft:dark_oak_log", -572);

    public static final BlockType DARK_OAK_PLANKS = register("minecraft:dark_oak_planks", -743);

    public static final BlockType DARK_OAK_PRESSURE_PLATE = register("minecraft:dark_oak_pressure_plate", -152);

    public static final BlockType DARK_OAK_SAPLING = register("minecraft:dark_oak_sapling", -829);

    public static final BlockType DARK_OAK_SIGN = register("minecraft:dark_oak_sign", 613);

    public static final BlockType DARK_OAK_SLAB = register("minecraft:dark_oak_slab", -808);

    public static final BlockType DARK_OAK_STAIRS = register("minecraft:dark_oak_stairs", 164);

    public static final BlockType DARK_OAK_TRAPDOOR = register("minecraft:dark_oak_trapdoor", -147);

    public static final BlockType DARK_OAK_WOOD = register("minecraft:dark_oak_wood", -818);

    public static final BlockType DARK_PRISMARINE = register("minecraft:dark_prismarine", -947);

    public static final BlockType DARK_PRISMARINE_DOUBLE_SLAB = register("minecraft:dark_prismarine_double_slab", -913);

    public static final BlockType DARK_PRISMARINE_SLAB = register("minecraft:dark_prismarine_slab", -886);

    public static final BlockType DARK_PRISMARINE_STAIRS = register("minecraft:dark_prismarine_stairs", -3);

    public static final BlockType DARKOAK_STANDING_SIGN = register("minecraft:darkoak_standing_sign", -192);

    public static final BlockType DARKOAK_WALL_SIGN = register("minecraft:darkoak_wall_sign", -193);

    public static final BlockType DAYLIGHT_DETECTOR = register("minecraft:daylight_detector", 151);

    public static final BlockType DAYLIGHT_DETECTOR_INVERTED = register("minecraft:daylight_detector_inverted", 178);

    public static final BlockType DEAD_BRAIN_CORAL = register("minecraft:dead_brain_coral", -586);

    public static final BlockType DEAD_BRAIN_CORAL_BLOCK = register("minecraft:dead_brain_coral_block", -854);

    public static final BlockType DEAD_BRAIN_CORAL_FAN = register("minecraft:dead_brain_coral_fan", -844);

    public static final BlockType DEAD_BRAIN_CORAL_WALL_FAN = register("minecraft:dead_brain_coral_wall_fan", -906);

    public static final BlockType DEAD_BUBBLE_CORAL = register("minecraft:dead_bubble_coral", -587);

    public static final BlockType DEAD_BUBBLE_CORAL_BLOCK = register("minecraft:dead_bubble_coral_block", -855);

    public static final BlockType DEAD_BUBBLE_CORAL_FAN = register("minecraft:dead_bubble_coral_fan", -845);

    public static final BlockType DEAD_BUBBLE_CORAL_WALL_FAN = register("minecraft:dead_bubble_coral_wall_fan", -908);

    public static final BlockType DEAD_FIRE_CORAL = register("minecraft:dead_fire_coral", -588);

    public static final BlockType DEAD_FIRE_CORAL_BLOCK = register("minecraft:dead_fire_coral_block", -856);

    public static final BlockType DEAD_FIRE_CORAL_FAN = register("minecraft:dead_fire_coral_fan", -846);

    public static final BlockType DEAD_FIRE_CORAL_WALL_FAN = register("minecraft:dead_fire_coral_wall_fan", -909);

    public static final BlockType DEAD_HORN_CORAL = register("minecraft:dead_horn_coral", -589);

    public static final BlockType DEAD_HORN_CORAL_BLOCK = register("minecraft:dead_horn_coral_block", -857);

    public static final BlockType DEAD_HORN_CORAL_FAN = register("minecraft:dead_horn_coral_fan", -847);

    public static final BlockType DEAD_HORN_CORAL_WALL_FAN = register("minecraft:dead_horn_coral_wall_fan", -910);

    public static final BlockType DEAD_TUBE_CORAL = register("minecraft:dead_tube_coral", -585);

    public static final BlockType DEAD_TUBE_CORAL_BLOCK = register("minecraft:dead_tube_coral_block", -853);

    public static final BlockType DEAD_TUBE_CORAL_FAN = register("minecraft:dead_tube_coral_fan", -134);

    public static final BlockType DEAD_TUBE_CORAL_WALL_FAN = register("minecraft:dead_tube_coral_wall_fan", -905);

    public static final BlockType DEADBUSH = register("minecraft:deadbush", 32);

    public static final BlockType DECORATED_POT = register("minecraft:decorated_pot", -551);

    public static final BlockType DEEPSLATE = register("minecraft:deepslate", -378);

    public static final BlockType DEEPSLATE_BRICK_DOUBLE_SLAB = register("minecraft:deepslate_brick_double_slab", -399);

    public static final BlockType DEEPSLATE_BRICK_SLAB = register("minecraft:deepslate_brick_slab", -392);

    public static final BlockType DEEPSLATE_BRICK_STAIRS = register("minecraft:deepslate_brick_stairs", -393);

    public static final BlockType DEEPSLATE_BRICK_WALL = register("minecraft:deepslate_brick_wall", -394);

    public static final BlockType DEEPSLATE_BRICKS = register("minecraft:deepslate_bricks", -391);

    public static final BlockType DEEPSLATE_COAL_ORE = register("minecraft:deepslate_coal_ore", -406);

    public static final BlockType DEEPSLATE_COPPER_ORE = register("minecraft:deepslate_copper_ore", -408);

    public static final BlockType DEEPSLATE_DIAMOND_ORE = register("minecraft:deepslate_diamond_ore", -405);

    public static final BlockType DEEPSLATE_EMERALD_ORE = register("minecraft:deepslate_emerald_ore", -407);

    public static final BlockType DEEPSLATE_GOLD_ORE = register("minecraft:deepslate_gold_ore", -402);

    public static final BlockType DEEPSLATE_IRON_ORE = register("minecraft:deepslate_iron_ore", -401);

    public static final BlockType DEEPSLATE_LAPIS_ORE = register("minecraft:deepslate_lapis_ore", -400);

    public static final BlockType DEEPSLATE_REDSTONE_ORE = register("minecraft:deepslate_redstone_ore", -403);

    public static final BlockType DEEPSLATE_TILE_DOUBLE_SLAB = register("minecraft:deepslate_tile_double_slab", -398);

    public static final BlockType DEEPSLATE_TILE_SLAB = register("minecraft:deepslate_tile_slab", -388);

    public static final BlockType DEEPSLATE_TILE_STAIRS = register("minecraft:deepslate_tile_stairs", -389);

    public static final BlockType DEEPSLATE_TILE_WALL = register("minecraft:deepslate_tile_wall", -390);

    public static final BlockType DEEPSLATE_TILES = register("minecraft:deepslate_tiles", -387);

    public static final BlockType DENY = register("minecraft:deny", 211);

    public static final BlockType DEPRECATED_ANVIL = register("minecraft:deprecated_anvil", -961);

    public static final BlockType DEPRECATED_PURPUR_BLOCK_1 = register("minecraft:deprecated_purpur_block_1", -950);

    public static final BlockType DEPRECATED_PURPUR_BLOCK_2 = register("minecraft:deprecated_purpur_block_2", -952);

    public static final BlockType DETECTOR_RAIL = register("minecraft:detector_rail", 28);

    public static final BlockType DIAMOND = register("minecraft:diamond", 328);

    public static final BlockType DIAMOND_AXE = register("minecraft:diamond_axe", 343);

    public static final BlockType DIAMOND_BLOCK = register("minecraft:diamond_block", 57);

    public static final BlockType DIAMOND_BOOTS = register("minecraft:diamond_boots", 375);

    public static final BlockType DIAMOND_CHESTPLATE = register("minecraft:diamond_chestplate", 373);

    public static final BlockType DIAMOND_HELMET = register("minecraft:diamond_helmet", 372);

    public static final BlockType DIAMOND_HOE = register("minecraft:diamond_hoe", 357);

    public static final BlockType DIAMOND_HORSE_ARMOR = register("minecraft:diamond_horse_armor", 566);

    public static final BlockType DIAMOND_LEGGINGS = register("minecraft:diamond_leggings", 374);

    public static final BlockType DIAMOND_ORE = register("minecraft:diamond_ore", 56);

    public static final BlockType DIAMOND_PICKAXE = register("minecraft:diamond_pickaxe", 342);

    public static final BlockType DIAMOND_SHOVEL = register("minecraft:diamond_shovel", 341);

    public static final BlockType DIAMOND_SWORD = register("minecraft:diamond_sword", 340);

    public static final BlockType DIORITE = register("minecraft:diorite", -592);

    public static final BlockType DIORITE_DOUBLE_SLAB = register("minecraft:diorite_double_slab", -921);

    public static final BlockType DIORITE_SLAB = register("minecraft:diorite_slab", -894);

    public static final BlockType DIORITE_STAIRS = register("minecraft:diorite_stairs", -170);

    public static final BlockType DIORITE_WALL = register("minecraft:diorite_wall", -973);

    public static final BlockType DIRT = register("minecraft:dirt", 3);

    public static final BlockType DIRT_WITH_ROOTS = register("minecraft:dirt_with_roots", -318);

    public static final BlockType DISC_FRAGMENT_5 = register("minecraft:disc_fragment_5", 674);

    public static final BlockType DISPENSER = register("minecraft:dispenser", 23);

    public static final BlockType DOUBLE_CUT_COPPER_SLAB = register("minecraft:double_cut_copper_slab", -368);

    public static final BlockType DOUBLE_PLANT = register("minecraft:double_plant", 800);

    public static final BlockType DOUBLE_STONE_BLOCK_SLAB = register("minecraft:double_stone_block_slab", 789);

    public static final BlockType DOUBLE_STONE_BLOCK_SLAB2 = register("minecraft:double_stone_block_slab2", 790);

    public static final BlockType DOUBLE_STONE_BLOCK_SLAB3 = register("minecraft:double_stone_block_slab3", 791);

    public static final BlockType DOUBLE_STONE_BLOCK_SLAB4 = register("minecraft:double_stone_block_slab4", 792);

    public static final BlockType DRAGON_BREATH = register("minecraft:dragon_breath", 593);

    public static final BlockType DRAGON_EGG = register("minecraft:dragon_egg", 122);

    public static final BlockType DRAGON_HEAD = register("minecraft:dragon_head", -969);

    public static final BlockType DRIED_KELP = register("minecraft:dried_kelp", 292);

    public static final BlockType DRIED_KELP_BLOCK = register("minecraft:dried_kelp_block", -139);

    public static final BlockType DRIPSTONE_BLOCK = register("minecraft:dripstone_block", -317);

    public static final BlockType DROPPER = register("minecraft:dropper", 125);

    public static final BlockType DYE = register("minecraft:dye", 824);

    public static final BlockType ECHO_SHARD = register("minecraft:echo_shard", 684);

    public static final BlockType EGG = register("minecraft:egg", 416);

    public static final BlockType ELEMENT_0 = register("minecraft:element_0", 36);

    public static final BlockType ELEMENT_1 = register("minecraft:element_1", -12);

    public static final BlockType ELEMENT_10 = register("minecraft:element_10", -21);

    public static final BlockType ELEMENT_100 = register("minecraft:element_100", -111);

    public static final BlockType ELEMENT_101 = register("minecraft:element_101", -112);

    public static final BlockType ELEMENT_102 = register("minecraft:element_102", -113);

    public static final BlockType ELEMENT_103 = register("minecraft:element_103", -114);

    public static final BlockType ELEMENT_104 = register("minecraft:element_104", -115);

    public static final BlockType ELEMENT_105 = register("minecraft:element_105", -116);

    public static final BlockType ELEMENT_106 = register("minecraft:element_106", -117);

    public static final BlockType ELEMENT_107 = register("minecraft:element_107", -118);

    public static final BlockType ELEMENT_108 = register("minecraft:element_108", -119);

    public static final BlockType ELEMENT_109 = register("minecraft:element_109", -120);

    public static final BlockType ELEMENT_11 = register("minecraft:element_11", -22);

    public static final BlockType ELEMENT_110 = register("minecraft:element_110", -121);

    public static final BlockType ELEMENT_111 = register("minecraft:element_111", -122);

    public static final BlockType ELEMENT_112 = register("minecraft:element_112", -123);

    public static final BlockType ELEMENT_113 = register("minecraft:element_113", -124);

    public static final BlockType ELEMENT_114 = register("minecraft:element_114", -125);

    public static final BlockType ELEMENT_115 = register("minecraft:element_115", -126);

    public static final BlockType ELEMENT_116 = register("minecraft:element_116", -127);

    public static final BlockType ELEMENT_117 = register("minecraft:element_117", -128);

    public static final BlockType ELEMENT_118 = register("minecraft:element_118", -129);

    public static final BlockType ELEMENT_12 = register("minecraft:element_12", -23);

    public static final BlockType ELEMENT_13 = register("minecraft:element_13", -24);

    public static final BlockType ELEMENT_14 = register("minecraft:element_14", -25);

    public static final BlockType ELEMENT_15 = register("minecraft:element_15", -26);

    public static final BlockType ELEMENT_16 = register("minecraft:element_16", -27);

    public static final BlockType ELEMENT_17 = register("minecraft:element_17", -28);

    public static final BlockType ELEMENT_18 = register("minecraft:element_18", -29);

    public static final BlockType ELEMENT_19 = register("minecraft:element_19", -30);

    public static final BlockType ELEMENT_2 = register("minecraft:element_2", -13);

    public static final BlockType ELEMENT_20 = register("minecraft:element_20", -31);

    public static final BlockType ELEMENT_21 = register("minecraft:element_21", -32);

    public static final BlockType ELEMENT_22 = register("minecraft:element_22", -33);

    public static final BlockType ELEMENT_23 = register("minecraft:element_23", -34);

    public static final BlockType ELEMENT_24 = register("minecraft:element_24", -35);

    public static final BlockType ELEMENT_25 = register("minecraft:element_25", -36);

    public static final BlockType ELEMENT_26 = register("minecraft:element_26", -37);

    public static final BlockType ELEMENT_27 = register("minecraft:element_27", -38);

    public static final BlockType ELEMENT_28 = register("minecraft:element_28", -39);

    public static final BlockType ELEMENT_29 = register("minecraft:element_29", -40);

    public static final BlockType ELEMENT_3 = register("minecraft:element_3", -14);

    public static final BlockType ELEMENT_30 = register("minecraft:element_30", -41);

    public static final BlockType ELEMENT_31 = register("minecraft:element_31", -42);

    public static final BlockType ELEMENT_32 = register("minecraft:element_32", -43);

    public static final BlockType ELEMENT_33 = register("minecraft:element_33", -44);

    public static final BlockType ELEMENT_34 = register("minecraft:element_34", -45);

    public static final BlockType ELEMENT_35 = register("minecraft:element_35", -46);

    public static final BlockType ELEMENT_36 = register("minecraft:element_36", -47);

    public static final BlockType ELEMENT_37 = register("minecraft:element_37", -48);

    public static final BlockType ELEMENT_38 = register("minecraft:element_38", -49);

    public static final BlockType ELEMENT_39 = register("minecraft:element_39", -50);

    public static final BlockType ELEMENT_4 = register("minecraft:element_4", -15);

    public static final BlockType ELEMENT_40 = register("minecraft:element_40", -51);

    public static final BlockType ELEMENT_41 = register("minecraft:element_41", -52);

    public static final BlockType ELEMENT_42 = register("minecraft:element_42", -53);

    public static final BlockType ELEMENT_43 = register("minecraft:element_43", -54);

    public static final BlockType ELEMENT_44 = register("minecraft:element_44", -55);

    public static final BlockType ELEMENT_45 = register("minecraft:element_45", -56);

    public static final BlockType ELEMENT_46 = register("minecraft:element_46", -57);

    public static final BlockType ELEMENT_47 = register("minecraft:element_47", -58);

    public static final BlockType ELEMENT_48 = register("minecraft:element_48", -59);

    public static final BlockType ELEMENT_49 = register("minecraft:element_49", -60);

    public static final BlockType ELEMENT_5 = register("minecraft:element_5", -16);

    public static final BlockType ELEMENT_50 = register("minecraft:element_50", -61);

    public static final BlockType ELEMENT_51 = register("minecraft:element_51", -62);

    public static final BlockType ELEMENT_52 = register("minecraft:element_52", -63);

    public static final BlockType ELEMENT_53 = register("minecraft:element_53", -64);

    public static final BlockType ELEMENT_54 = register("minecraft:element_54", -65);

    public static final BlockType ELEMENT_55 = register("minecraft:element_55", -66);

    public static final BlockType ELEMENT_56 = register("minecraft:element_56", -67);

    public static final BlockType ELEMENT_57 = register("minecraft:element_57", -68);

    public static final BlockType ELEMENT_58 = register("minecraft:element_58", -69);

    public static final BlockType ELEMENT_59 = register("minecraft:element_59", -70);

    public static final BlockType ELEMENT_6 = register("minecraft:element_6", -17);

    public static final BlockType ELEMENT_60 = register("minecraft:element_60", -71);

    public static final BlockType ELEMENT_61 = register("minecraft:element_61", -72);

    public static final BlockType ELEMENT_62 = register("minecraft:element_62", -73);

    public static final BlockType ELEMENT_63 = register("minecraft:element_63", -74);

    public static final BlockType ELEMENT_64 = register("minecraft:element_64", -75);

    public static final BlockType ELEMENT_65 = register("minecraft:element_65", -76);

    public static final BlockType ELEMENT_66 = register("minecraft:element_66", -77);

    public static final BlockType ELEMENT_67 = register("minecraft:element_67", -78);

    public static final BlockType ELEMENT_68 = register("minecraft:element_68", -79);

    public static final BlockType ELEMENT_69 = register("minecraft:element_69", -80);

    public static final BlockType ELEMENT_7 = register("minecraft:element_7", -18);

    public static final BlockType ELEMENT_70 = register("minecraft:element_70", -81);

    public static final BlockType ELEMENT_71 = register("minecraft:element_71", -82);

    public static final BlockType ELEMENT_72 = register("minecraft:element_72", -83);

    public static final BlockType ELEMENT_73 = register("minecraft:element_73", -84);

    public static final BlockType ELEMENT_74 = register("minecraft:element_74", -85);

    public static final BlockType ELEMENT_75 = register("minecraft:element_75", -86);

    public static final BlockType ELEMENT_76 = register("minecraft:element_76", -87);

    public static final BlockType ELEMENT_77 = register("minecraft:element_77", -88);

    public static final BlockType ELEMENT_78 = register("minecraft:element_78", -89);

    public static final BlockType ELEMENT_79 = register("minecraft:element_79", -90);

    public static final BlockType ELEMENT_8 = register("minecraft:element_8", -19);

    public static final BlockType ELEMENT_80 = register("minecraft:element_80", -91);

    public static final BlockType ELEMENT_81 = register("minecraft:element_81", -92);

    public static final BlockType ELEMENT_82 = register("minecraft:element_82", -93);

    public static final BlockType ELEMENT_83 = register("minecraft:element_83", -94);

    public static final BlockType ELEMENT_84 = register("minecraft:element_84", -95);

    public static final BlockType ELEMENT_85 = register("minecraft:element_85", -96);

    public static final BlockType ELEMENT_86 = register("minecraft:element_86", -97);

    public static final BlockType ELEMENT_87 = register("minecraft:element_87", -98);

    public static final BlockType ELEMENT_88 = register("minecraft:element_88", -99);

    public static final BlockType ELEMENT_89 = register("minecraft:element_89", -100);

    public static final BlockType ELEMENT_9 = register("minecraft:element_9", -20);

    public static final BlockType ELEMENT_90 = register("minecraft:element_90", -101);

    public static final BlockType ELEMENT_91 = register("minecraft:element_91", -102);

    public static final BlockType ELEMENT_92 = register("minecraft:element_92", -103);

    public static final BlockType ELEMENT_93 = register("minecraft:element_93", -104);

    public static final BlockType ELEMENT_94 = register("minecraft:element_94", -105);

    public static final BlockType ELEMENT_95 = register("minecraft:element_95", -106);

    public static final BlockType ELEMENT_96 = register("minecraft:element_96", -107);

    public static final BlockType ELEMENT_97 = register("minecraft:element_97", -108);

    public static final BlockType ELEMENT_98 = register("minecraft:element_98", -109);

    public static final BlockType ELEMENT_99 = register("minecraft:element_99", -110);

    public static final BlockType ELEMENT_CONSTRUCTOR = register("minecraft:element_constructor", -987);

    public static final BlockType ELYTRA = register("minecraft:elytra", 597);

    public static final BlockType EMERALD = register("minecraft:emerald", 546);

    public static final BlockType EMERALD_BLOCK = register("minecraft:emerald_block", 133);

    public static final BlockType EMERALD_ORE = register("minecraft:emerald_ore", 129);

    public static final BlockType EMPTY_MAP = register("minecraft:empty_map", 549);

    public static final BlockType ENCHANTED_BOOK = register("minecraft:enchanted_book", 554);

    public static final BlockType ENCHANTED_GOLDEN_APPLE = register("minecraft:enchanted_golden_apple", 281);

    public static final BlockType ENCHANTING_TABLE = register("minecraft:enchanting_table", 116);

    public static final BlockType END_BRICK_STAIRS = register("minecraft:end_brick_stairs", -178);

    public static final BlockType END_BRICKS = register("minecraft:end_bricks", 206);

    public static final BlockType END_CRYSTAL = register("minecraft:end_crystal", 827);

    public static final BlockType END_GATEWAY = register("minecraft:end_gateway", 209);

    public static final BlockType END_PORTAL = register("minecraft:end_portal", 119);

    public static final BlockType END_PORTAL_FRAME = register("minecraft:end_portal_frame", 120);

    public static final BlockType END_ROD = register("minecraft:end_rod", 208);

    public static final BlockType END_STONE = register("minecraft:end_stone", 121);

    public static final BlockType END_STONE_BRICK_DOUBLE_SLAB = register("minecraft:end_stone_brick_double_slab", -167);

    public static final BlockType END_STONE_BRICK_SLAB = register("minecraft:end_stone_brick_slab", -162);

    public static final BlockType END_STONE_BRICK_WALL = register("minecraft:end_stone_brick_wall", -980);

    public static final BlockType ENDER_CHEST = register("minecraft:ender_chest", 130);

    public static final BlockType ENDER_EYE = register("minecraft:ender_eye", 460);

    public static final BlockType ENDER_PEARL = register("minecraft:ender_pearl", 448);

    public static final BlockType EXPERIENCE_BOTTLE = register("minecraft:experience_bottle", 542);

    public static final BlockType EXPOSED_CHISELED_COPPER = register("minecraft:exposed_chiseled_copper", -761);

    public static final BlockType EXPOSED_COPPER = register("minecraft:exposed_copper", -341);

    public static final BlockType EXPOSED_COPPER_BULB = register("minecraft:exposed_copper_bulb", -777);

    public static final BlockType EXPOSED_COPPER_DOOR = register("minecraft:exposed_copper_door", -785);

    public static final BlockType EXPOSED_COPPER_GRATE = register("minecraft:exposed_copper_grate", -769);

    public static final BlockType EXPOSED_COPPER_TRAPDOOR = register("minecraft:exposed_copper_trapdoor", -793);

    public static final BlockType EXPOSED_CUT_COPPER = register("minecraft:exposed_cut_copper", -348);

    public static final BlockType EXPOSED_CUT_COPPER_SLAB = register("minecraft:exposed_cut_copper_slab", -362);

    public static final BlockType EXPOSED_CUT_COPPER_STAIRS = register("minecraft:exposed_cut_copper_stairs", -355);

    public static final BlockType EXPOSED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:exposed_double_cut_copper_slab", -369);

    public static final BlockType FARMLAND = register("minecraft:farmland", 60);

    public static final BlockType FEATHER = register("minecraft:feather", 352);

    public static final BlockType FENCE = register("minecraft:fence", 782);

    public static final BlockType FENCE_GATE = register("minecraft:fence_gate", 107);

    public static final BlockType FERMENTED_SPIDER_EYE = register("minecraft:fermented_spider_eye", 455);

    public static final BlockType FERN = register("minecraft:fern", -848);

    public static final BlockType FILLED_MAP = register("minecraft:filled_map", 446);

    public static final BlockType FIRE = register("minecraft:fire", 51);

    public static final BlockType FIRE_CHARGE = register("minecraft:fire_charge", 543);

    public static final BlockType FIRE_CORAL = register("minecraft:fire_coral", -583);

    public static final BlockType FIRE_CORAL_BLOCK = register("minecraft:fire_coral_block", -851);

    public static final BlockType FIRE_CORAL_FAN = register("minecraft:fire_coral_fan", -842);

    public static final BlockType FIRE_CORAL_WALL_FAN = register("minecraft:fire_coral_wall_fan", -907);

    public static final BlockType FIREFLY_BUSH = register("minecraft:firefly_bush", -1025);

    public static final BlockType FIREWORK_ROCKET = register("minecraft:firework_rocket", 552);

    public static final BlockType FIREWORK_STAR = register("minecraft:firework_star", 553);

    public static final BlockType FISHING_ROD = register("minecraft:fishing_rod", 418);

    public static final BlockType FLETCHING_TABLE = register("minecraft:fletching_table", -201);

    public static final BlockType FLINT = register("minecraft:flint", 381);

    public static final BlockType FLINT_AND_STEEL = register("minecraft:flint_and_steel", 323);

    public static final BlockType FLOWERING_AZALEA = register("minecraft:flowering_azalea", -338);

    public static final BlockType FLOWING_LAVA = register("minecraft:flowing_lava", 10);

    public static final BlockType FLOWING_WATER = register("minecraft:flowing_water", 8);

    public static final BlockType FROG_SPAWN = register("minecraft:frog_spawn", -468);

    public static final BlockType FROSTED_ICE = register("minecraft:frosted_ice", 207);

    public static final BlockType FURNACE = register("minecraft:furnace", 61);

    public static final BlockType GHAST_TEAR = register("minecraft:ghast_tear", 451);

    public static final BlockType GILDED_BLACKSTONE = register("minecraft:gilded_blackstone", -281);

    public static final BlockType GLASS = register("minecraft:glass", 20);

    public static final BlockType GLASS_BOTTLE = register("minecraft:glass_bottle", 454);

    public static final BlockType GLASS_PANE = register("minecraft:glass_pane", 102);

    public static final BlockType GLISTERING_MELON_SLICE = register("minecraft:glistering_melon_slice", 461);

    public static final BlockType GLOW_BERRIES = register("minecraft:glow_berries", 828);

    public static final BlockType GLOW_LICHEN = register("minecraft:glow_lichen", -411);

    public static final BlockType GLOW_STICK = register("minecraft:glow_stick", 638);

    public static final BlockType GLOWINGOBSIDIAN = register("minecraft:glowingobsidian", 246);

    public static final BlockType GLOWSTONE = register("minecraft:glowstone", 89);

    public static final BlockType GLOWSTONE_DUST = register("minecraft:glowstone_dust", 420);

    public static final BlockType GOLD_BLOCK = register("minecraft:gold_block", 41);

    public static final BlockType GOLD_INGOT = register("minecraft:gold_ingot", 330);

    public static final BlockType GOLD_NUGGET = register("minecraft:gold_nugget", 452);

    public static final BlockType GOLD_ORE = register("minecraft:gold_ore", 14);

    public static final BlockType GOLDEN_APPLE = register("minecraft:golden_apple", 280);

    public static final BlockType GOLDEN_AXE = register("minecraft:golden_axe", 350);

    public static final BlockType GOLDEN_BOOTS = register("minecraft:golden_boots", 379);

    public static final BlockType GOLDEN_CARROT = register("minecraft:golden_carrot", 305);

    public static final BlockType GOLDEN_CHESTPLATE = register("minecraft:golden_chestplate", 377);

    public static final BlockType GOLDEN_HELMET = register("minecraft:golden_helmet", 376);

    public static final BlockType GOLDEN_HOE = register("minecraft:golden_hoe", 358);

    public static final BlockType GOLDEN_HORSE_ARMOR = register("minecraft:golden_horse_armor", 565);

    public static final BlockType GOLDEN_LEGGINGS = register("minecraft:golden_leggings", 378);

    public static final BlockType GOLDEN_PICKAXE = register("minecraft:golden_pickaxe", 349);

    public static final BlockType GOLDEN_RAIL = register("minecraft:golden_rail", 27);

    public static final BlockType GOLDEN_SHOVEL = register("minecraft:golden_shovel", 348);

    public static final BlockType GOLDEN_SWORD = register("minecraft:golden_sword", 347);

    public static final BlockType GRANITE = register("minecraft:granite", -590);

    public static final BlockType GRANITE_DOUBLE_SLAB = register("minecraft:granite_double_slab", -923);

    public static final BlockType GRANITE_SLAB = register("minecraft:granite_slab", -896);

    public static final BlockType GRANITE_STAIRS = register("minecraft:granite_stairs", -169);

    public static final BlockType GRANITE_WALL = register("minecraft:granite_wall", -972);

    public static final BlockType GRASS_BLOCK = register("minecraft:grass_block", 2);

    public static final BlockType GRASS_PATH = register("minecraft:grass_path", 198);

    public static final BlockType GRAVEL = register("minecraft:gravel", 13);

    public static final BlockType GRAY_CANDLE = register("minecraft:gray_candle", -420);

    public static final BlockType GRAY_CANDLE_CAKE = register("minecraft:gray_candle_cake", -437);

    public static final BlockType GRAY_CARPET = register("minecraft:gray_carpet", -603);

    public static final BlockType GRAY_CONCRETE = register("minecraft:gray_concrete", -634);

    public static final BlockType GRAY_CONCRETE_POWDER = register("minecraft:gray_concrete_powder", -715);

    public static final BlockType GRAY_GLAZED_TERRACOTTA = register("minecraft:gray_glazed_terracotta", 227);

    public static final BlockType GRAY_SHULKER_BOX = register("minecraft:gray_shulker_box", -619);

    public static final BlockType GRAY_STAINED_GLASS = register("minecraft:gray_stained_glass", -679);

    public static final BlockType GRAY_STAINED_GLASS_PANE = register("minecraft:gray_stained_glass_pane", -649);

    public static final BlockType GRAY_TERRACOTTA = register("minecraft:gray_terracotta", -730);

    public static final BlockType GRAY_WOOL = register("minecraft:gray_wool", -553);

    public static final BlockType GREEN_CANDLE = register("minecraft:green_candle", -426);

    public static final BlockType GREEN_CANDLE_CAKE = register("minecraft:green_candle_cake", -443);

    public static final BlockType GREEN_CARPET = register("minecraft:green_carpet", -609);

    public static final BlockType GREEN_CONCRETE = register("minecraft:green_concrete", -640);

    public static final BlockType GREEN_CONCRETE_POWDER = register("minecraft:green_concrete_powder", -721);

    public static final BlockType GREEN_GLAZED_TERRACOTTA = register("minecraft:green_glazed_terracotta", 233);

    public static final BlockType GREEN_SHULKER_BOX = register("minecraft:green_shulker_box", -625);

    public static final BlockType GREEN_STAINED_GLASS = register("minecraft:green_stained_glass", -685);

    public static final BlockType GREEN_STAINED_GLASS_PANE = register("minecraft:green_stained_glass_pane", -655);

    public static final BlockType GREEN_TERRACOTTA = register("minecraft:green_terracotta", -736);

    public static final BlockType GREEN_WOOL = register("minecraft:green_wool", -560);

    public static final BlockType GRINDSTONE = register("minecraft:grindstone", -195);

    public static final BlockType GUNPOWDER = register("minecraft:gunpowder", 353);

    public static final BlockType HANGING_ROOTS = register("minecraft:hanging_roots", -319);

    public static final BlockType HARD_BLACK_STAINED_GLASS = register("minecraft:hard_black_stained_glass", -702);

    public static final BlockType HARD_BLACK_STAINED_GLASS_PANE = register("minecraft:hard_black_stained_glass_pane", -672);

    public static final BlockType HARD_BLUE_STAINED_GLASS = register("minecraft:hard_blue_stained_glass", -698);

    public static final BlockType HARD_BLUE_STAINED_GLASS_PANE = register("minecraft:hard_blue_stained_glass_pane", -668);

    public static final BlockType HARD_BROWN_STAINED_GLASS = register("minecraft:hard_brown_stained_glass", -699);

    public static final BlockType HARD_BROWN_STAINED_GLASS_PANE = register("minecraft:hard_brown_stained_glass_pane", -669);

    public static final BlockType HARD_CYAN_STAINED_GLASS = register("minecraft:hard_cyan_stained_glass", -696);

    public static final BlockType HARD_CYAN_STAINED_GLASS_PANE = register("minecraft:hard_cyan_stained_glass_pane", -666);

    public static final BlockType HARD_GLASS = register("minecraft:hard_glass", 253);

    public static final BlockType HARD_GLASS_PANE = register("minecraft:hard_glass_pane", 190);

    public static final BlockType HARD_GRAY_STAINED_GLASS = register("minecraft:hard_gray_stained_glass", -694);

    public static final BlockType HARD_GRAY_STAINED_GLASS_PANE = register("minecraft:hard_gray_stained_glass_pane", -664);

    public static final BlockType HARD_GREEN_STAINED_GLASS = register("minecraft:hard_green_stained_glass", -700);

    public static final BlockType HARD_GREEN_STAINED_GLASS_PANE = register("minecraft:hard_green_stained_glass_pane", -670);

    public static final BlockType HARD_LIGHT_BLUE_STAINED_GLASS = register("minecraft:hard_light_blue_stained_glass", -690);

    public static final BlockType HARD_LIGHT_BLUE_STAINED_GLASS_PANE = register("minecraft:hard_light_blue_stained_glass_pane", -660);

    public static final BlockType HARD_LIGHT_GRAY_STAINED_GLASS = register("minecraft:hard_light_gray_stained_glass", -695);

    public static final BlockType HARD_LIGHT_GRAY_STAINED_GLASS_PANE = register("minecraft:hard_light_gray_stained_glass_pane", -665);

    public static final BlockType HARD_LIME_STAINED_GLASS = register("minecraft:hard_lime_stained_glass", -692);

    public static final BlockType HARD_LIME_STAINED_GLASS_PANE = register("minecraft:hard_lime_stained_glass_pane", -662);

    public static final BlockType HARD_MAGENTA_STAINED_GLASS = register("minecraft:hard_magenta_stained_glass", -689);

    public static final BlockType HARD_MAGENTA_STAINED_GLASS_PANE = register("minecraft:hard_magenta_stained_glass_pane", -659);

    public static final BlockType HARD_ORANGE_STAINED_GLASS = register("minecraft:hard_orange_stained_glass", -688);

    public static final BlockType HARD_ORANGE_STAINED_GLASS_PANE = register("minecraft:hard_orange_stained_glass_pane", -658);

    public static final BlockType HARD_PINK_STAINED_GLASS = register("minecraft:hard_pink_stained_glass", -693);

    public static final BlockType HARD_PINK_STAINED_GLASS_PANE = register("minecraft:hard_pink_stained_glass_pane", -663);

    public static final BlockType HARD_PURPLE_STAINED_GLASS = register("minecraft:hard_purple_stained_glass", -697);

    public static final BlockType HARD_PURPLE_STAINED_GLASS_PANE = register("minecraft:hard_purple_stained_glass_pane", -667);

    public static final BlockType HARD_RED_STAINED_GLASS = register("minecraft:hard_red_stained_glass", -701);

    public static final BlockType HARD_RED_STAINED_GLASS_PANE = register("minecraft:hard_red_stained_glass_pane", -671);

    public static final BlockType HARD_STAINED_GLASS = register("minecraft:hard_stained_glass", 818);

    public static final BlockType HARD_STAINED_GLASS_PANE = register("minecraft:hard_stained_glass_pane", 819);

    public static final BlockType HARD_WHITE_STAINED_GLASS = register("minecraft:hard_white_stained_glass", 254);

    public static final BlockType HARD_WHITE_STAINED_GLASS_PANE = register("minecraft:hard_white_stained_glass_pane", 191);

    public static final BlockType HARD_YELLOW_STAINED_GLASS = register("minecraft:hard_yellow_stained_glass", -691);

    public static final BlockType HARD_YELLOW_STAINED_GLASS_PANE = register("minecraft:hard_yellow_stained_glass_pane", -661);

    public static final BlockType HARDENED_CLAY = register("minecraft:hardened_clay", 172);

    public static final BlockType HAY_BLOCK = register("minecraft:hay_block", 170);

    public static final BlockType HEART_OF_THE_SEA = register("minecraft:heart_of_the_sea", 604);

    public static final BlockType HEAVY_CORE = register("minecraft:heavy_core", -316);

    public static final BlockType HEAVY_WEIGHTED_PRESSURE_PLATE = register("minecraft:heavy_weighted_pressure_plate", 148);

    public static final BlockType HONEY_BLOCK = register("minecraft:honey_block", -220);

    public static final BlockType HONEY_BOTTLE = register("minecraft:honey_bottle", 627);

    public static final BlockType HONEYCOMB = register("minecraft:honeycomb", 626);

    public static final BlockType HONEYCOMB_BLOCK = register("minecraft:honeycomb_block", -221);

    public static final BlockType HOPPER_MINECART = register("minecraft:hopper_minecart", 559);

    public static final BlockType HORN_CORAL = register("minecraft:horn_coral", -584);

    public static final BlockType HORN_CORAL_BLOCK = register("minecraft:horn_coral_block", -852);

    public static final BlockType HORN_CORAL_FAN = register("minecraft:horn_coral_fan", -843);

    public static final BlockType HORN_CORAL_WALL_FAN = register("minecraft:horn_coral_wall_fan", -137);

    public static final BlockType ICE = register("minecraft:ice", 79);

    public static final BlockType ICE_BOMB = register("minecraft:ice_bomb", 632);

    public static final BlockType INFESTED_CHISELED_STONE_BRICKS = register("minecraft:infested_chiseled_stone_bricks", -862);

    public static final BlockType INFESTED_COBBLESTONE = register("minecraft:infested_cobblestone", -858);

    public static final BlockType INFESTED_CRACKED_STONE_BRICKS = register("minecraft:infested_cracked_stone_bricks", -861);

    public static final BlockType INFESTED_DEEPSLATE = register("minecraft:infested_deepslate", -454);

    public static final BlockType INFESTED_MOSSY_STONE_BRICKS = register("minecraft:infested_mossy_stone_bricks", -860);

    public static final BlockType INFESTED_STONE = register("minecraft:infested_stone", 97);

    public static final BlockType INFESTED_STONE_BRICKS = register("minecraft:infested_stone_bricks", -859);

    public static final BlockType INFO_UPDATE = register("minecraft:info_update", 248);

    public static final BlockType INFO_UPDATE2 = register("minecraft:info_update2", 249);

    public static final BlockType INK_SAC = register("minecraft:ink_sac", 439);

    public static final BlockType INVISIBLE_BEDROCK = register("minecraft:invisible_bedrock", 95);

    public static final BlockType IRON_AXE = register("minecraft:iron_axe", 322);

    public static final BlockType IRON_BARS = register("minecraft:iron_bars", 101);

    public static final BlockType IRON_BLOCK = register("minecraft:iron_block", 42);

    public static final BlockType IRON_BOOTS = register("minecraft:iron_boots", 371);

    public static final BlockType IRON_CHESTPLATE = register("minecraft:iron_chestplate", 369);

    public static final BlockType IRON_HELMET = register("minecraft:iron_helmet", 368);

    public static final BlockType IRON_HOE = register("minecraft:iron_hoe", 356);

    public static final BlockType IRON_HORSE_ARMOR = register("minecraft:iron_horse_armor", 564);

    public static final BlockType IRON_INGOT = register("minecraft:iron_ingot", 329);

    public static final BlockType IRON_LEGGINGS = register("minecraft:iron_leggings", 370);

    public static final BlockType IRON_NUGGET = register("minecraft:iron_nugget", 602);

    public static final BlockType IRON_ORE = register("minecraft:iron_ore", 15);

    public static final BlockType IRON_PICKAXE = register("minecraft:iron_pickaxe", 321);

    public static final BlockType IRON_SHOVEL = register("minecraft:iron_shovel", 320);

    public static final BlockType IRON_SWORD = register("minecraft:iron_sword", 331);

    public static final BlockType IRON_TRAPDOOR = register("minecraft:iron_trapdoor", 167);

    public static final BlockType ACACIA_DOOR = register("minecraft:item.acacia_door", 196);

    public static final BlockType BED = register("minecraft:item.bed", 26);

    public static final BlockType BEETROOT = register("minecraft:item.beetroot", 244);

    public static final BlockType BIRCH_DOOR = register("minecraft:item.birch_door", 194);

    public static final BlockType CAKE = register("minecraft:item.cake", 92);

    public static final BlockType CAMERA = register("minecraft:item.camera", 242);

    public static final BlockType CAMPFIRE = register("minecraft:item.campfire", -209);

    public static final BlockType CAULDRON = register("minecraft:item.cauldron", 118);

    public static final BlockType CHAIN = register("minecraft:item.chain", -286);

    public static final BlockType CRIMSON_DOOR = register("minecraft:item.crimson_door", -244);

    public static final BlockType DARK_OAK_DOOR = register("minecraft:item.dark_oak_door", 197);

    public static final BlockType FLOWER_POT = register("minecraft:item.flower_pot", 140);

    public static final BlockType FRAME = register("minecraft:item.frame", 199);

    public static final BlockType GLOW_FRAME = register("minecraft:item.glow_frame", -339);

    public static final BlockType HOPPER = register("minecraft:item.hopper", 154);

    public static final BlockType IRON_DOOR = register("minecraft:item.iron_door", 71);

    public static final BlockType JUNGLE_DOOR = register("minecraft:item.jungle_door", 195);

    public static final BlockType KELP = register("minecraft:item.kelp", -138);

    public static final BlockType MANGROVE_DOOR = register("minecraft:item.mangrove_door", -493);

    public static final BlockType NETHER_SPROUTS = register("minecraft:item.nether_sprouts", -238);

    public static final BlockType NETHER_WART = register("minecraft:item.nether_wart", 115);

    public static final BlockType REEDS = register("minecraft:item.reeds", 83);

    public static final BlockType SOUL_CAMPFIRE = register("minecraft:item.soul_campfire", -290);

    public static final BlockType SPRUCE_DOOR = register("minecraft:item.spruce_door", 193);

    public static final BlockType WARPED_DOOR = register("minecraft:item.warped_door", -245);

    public static final BlockType WHEAT = register("minecraft:item.wheat", 59);

    public static final BlockType WOODEN_DOOR = register("minecraft:item.wooden_door", 64);

    public static final BlockType JIGSAW = register("minecraft:jigsaw", -211);

    public static final BlockType JUKEBOX = register("minecraft:jukebox", 84);

    public static final BlockType JUNGLE_BUTTON = register("minecraft:jungle_button", -143);

    public static final BlockType JUNGLE_CHEST_BOAT = register("minecraft:jungle_chest_boat", 677);

    public static final BlockType JUNGLE_DOUBLE_SLAB = register("minecraft:jungle_double_slab", -811);

    public static final BlockType JUNGLE_FENCE = register("minecraft:jungle_fence", -578);

    public static final BlockType JUNGLE_FENCE_GATE = register("minecraft:jungle_fence_gate", 185);

    public static final BlockType JUNGLE_HANGING_SIGN = register("minecraft:jungle_hanging_sign", -503);

    public static final BlockType JUNGLE_LEAVES = register("minecraft:jungle_leaves", -802);

    public static final BlockType JUNGLE_LOG = register("minecraft:jungle_log", -571);

    public static final BlockType JUNGLE_PLANKS = register("minecraft:jungle_planks", -741);

    public static final BlockType JUNGLE_PRESSURE_PLATE = register("minecraft:jungle_pressure_plate", -153);

    public static final BlockType JUNGLE_SAPLING = register("minecraft:jungle_sapling", -827);

    public static final BlockType JUNGLE_SIGN = register("minecraft:jungle_sign", 611);

    public static final BlockType JUNGLE_SLAB = register("minecraft:jungle_slab", -806);

    public static final BlockType JUNGLE_STAIRS = register("minecraft:jungle_stairs", 136);

    public static final BlockType JUNGLE_STANDING_SIGN = register("minecraft:jungle_standing_sign", -188);

    public static final BlockType JUNGLE_TRAPDOOR = register("minecraft:jungle_trapdoor", -148);

    public static final BlockType JUNGLE_WALL_SIGN = register("minecraft:jungle_wall_sign", -189);

    public static final BlockType JUNGLE_WOOD = register("minecraft:jungle_wood", -816);

    public static final BlockType LAB_TABLE = register("minecraft:lab_table", -988);

    public static final BlockType LADDER = register("minecraft:ladder", 65);

    public static final BlockType LANTERN = register("minecraft:lantern", -208);

    public static final BlockType LAPIS_BLOCK = register("minecraft:lapis_block", 22);

    public static final BlockType LAPIS_ORE = register("minecraft:lapis_ore", 21);

    public static final BlockType LARGE_AMETHYST_BUD = register("minecraft:large_amethyst_bud", -330);

    public static final BlockType LARGE_FERN = register("minecraft:large_fern", -865);

    public static final BlockType LAVA = register("minecraft:lava", 11);

    public static final BlockType LEAD = register("minecraft:lead", 580);

    public static final BlockType LEAF_LITTER = register("minecraft:leaf_litter", -1026);

    public static final BlockType LEATHER = register("minecraft:leather", 407);

    public static final BlockType LEATHER_BOOTS = register("minecraft:leather_boots", 363);

    public static final BlockType LEATHER_CHESTPLATE = register("minecraft:leather_chestplate", 361);

    public static final BlockType LEATHER_HELMET = register("minecraft:leather_helmet", 360);

    public static final BlockType LEATHER_HORSE_ARMOR = register("minecraft:leather_horse_armor", 563);

    public static final BlockType LEATHER_LEGGINGS = register("minecraft:leather_leggings", 362);

    public static final BlockType LEAVES = register("minecraft:leaves", 796);

    public static final BlockType LEAVES2 = register("minecraft:leaves2", 797);

    public static final BlockType LECTERN = register("minecraft:lectern", -194);

    public static final BlockType LEVER = register("minecraft:lever", 69);

    public static final BlockType LIGHT_BLOCK = register("minecraft:light_block", 822);

    public static final BlockType LIGHT_BLOCK_0 = register("minecraft:light_block_0", -215);

    public static final BlockType LIGHT_BLOCK_1 = register("minecraft:light_block_1", -929);

    public static final BlockType LIGHT_BLOCK_10 = register("minecraft:light_block_10", -938);

    public static final BlockType LIGHT_BLOCK_11 = register("minecraft:light_block_11", -939);

    public static final BlockType LIGHT_BLOCK_12 = register("minecraft:light_block_12", -940);

    public static final BlockType LIGHT_BLOCK_13 = register("minecraft:light_block_13", -941);

    public static final BlockType LIGHT_BLOCK_14 = register("minecraft:light_block_14", -942);

    public static final BlockType LIGHT_BLOCK_15 = register("minecraft:light_block_15", -943);

    public static final BlockType LIGHT_BLOCK_2 = register("minecraft:light_block_2", -930);

    public static final BlockType LIGHT_BLOCK_3 = register("minecraft:light_block_3", -931);

    public static final BlockType LIGHT_BLOCK_4 = register("minecraft:light_block_4", -932);

    public static final BlockType LIGHT_BLOCK_5 = register("minecraft:light_block_5", -933);

    public static final BlockType LIGHT_BLOCK_6 = register("minecraft:light_block_6", -934);

    public static final BlockType LIGHT_BLOCK_7 = register("minecraft:light_block_7", -935);

    public static final BlockType LIGHT_BLOCK_8 = register("minecraft:light_block_8", -936);

    public static final BlockType LIGHT_BLOCK_9 = register("minecraft:light_block_9", -937);

    public static final BlockType LIGHT_BLUE_CANDLE = register("minecraft:light_blue_candle", -416);

    public static final BlockType LIGHT_BLUE_CANDLE_CAKE = register("minecraft:light_blue_candle_cake", -433);

    public static final BlockType LIGHT_BLUE_CARPET = register("minecraft:light_blue_carpet", -599);

    public static final BlockType LIGHT_BLUE_CONCRETE = register("minecraft:light_blue_concrete", -630);

    public static final BlockType LIGHT_BLUE_CONCRETE_POWDER = register("minecraft:light_blue_concrete_powder", -711);

    public static final BlockType LIGHT_BLUE_GLAZED_TERRACOTTA = register("minecraft:light_blue_glazed_terracotta", 223);

    public static final BlockType LIGHT_BLUE_SHULKER_BOX = register("minecraft:light_blue_shulker_box", -615);

    public static final BlockType LIGHT_BLUE_STAINED_GLASS = register("minecraft:light_blue_stained_glass", -675);

    public static final BlockType LIGHT_BLUE_STAINED_GLASS_PANE = register("minecraft:light_blue_stained_glass_pane", -645);

    public static final BlockType LIGHT_BLUE_TERRACOTTA = register("minecraft:light_blue_terracotta", -726);

    public static final BlockType LIGHT_BLUE_WOOL = register("minecraft:light_blue_wool", -562);

    public static final BlockType LIGHT_GRAY_CANDLE = register("minecraft:light_gray_candle", -421);

    public static final BlockType LIGHT_GRAY_CANDLE_CAKE = register("minecraft:light_gray_candle_cake", -438);

    public static final BlockType LIGHT_GRAY_CARPET = register("minecraft:light_gray_carpet", -604);

    public static final BlockType LIGHT_GRAY_CONCRETE = register("minecraft:light_gray_concrete", -635);

    public static final BlockType LIGHT_GRAY_CONCRETE_POWDER = register("minecraft:light_gray_concrete_powder", -716);

    public static final BlockType LIGHT_GRAY_SHULKER_BOX = register("minecraft:light_gray_shulker_box", -620);

    public static final BlockType LIGHT_GRAY_STAINED_GLASS = register("minecraft:light_gray_stained_glass", -680);

    public static final BlockType LIGHT_GRAY_STAINED_GLASS_PANE = register("minecraft:light_gray_stained_glass_pane", -650);

    public static final BlockType LIGHT_GRAY_TERRACOTTA = register("minecraft:light_gray_terracotta", -731);

    public static final BlockType LIGHT_GRAY_WOOL = register("minecraft:light_gray_wool", -552);

    public static final BlockType LIGHT_WEIGHTED_PRESSURE_PLATE = register("minecraft:light_weighted_pressure_plate", 147);

    public static final BlockType LIGHTNING_ROD = register("minecraft:lightning_rod", -312);

    public static final BlockType LILAC = register("minecraft:lilac", -863);

    public static final BlockType LILY_OF_THE_VALLEY = register("minecraft:lily_of_the_valley", -839);

    public static final BlockType LIME_CANDLE = register("minecraft:lime_candle", -418);

    public static final BlockType LIME_CANDLE_CAKE = register("minecraft:lime_candle_cake", -435);

    public static final BlockType LIME_CARPET = register("minecraft:lime_carpet", -601);

    public static final BlockType LIME_CONCRETE = register("minecraft:lime_concrete", -632);

    public static final BlockType LIME_CONCRETE_POWDER = register("minecraft:lime_concrete_powder", -713);

    public static final BlockType LIME_GLAZED_TERRACOTTA = register("minecraft:lime_glazed_terracotta", 225);

    public static final BlockType LIME_SHULKER_BOX = register("minecraft:lime_shulker_box", -617);

    public static final BlockType LIME_STAINED_GLASS = register("minecraft:lime_stained_glass", -677);

    public static final BlockType LIME_STAINED_GLASS_PANE = register("minecraft:lime_stained_glass_pane", -647);

    public static final BlockType LIME_TERRACOTTA = register("minecraft:lime_terracotta", -728);

    public static final BlockType LIME_WOOL = register("minecraft:lime_wool", -559);

    public static final BlockType LINGERING_POTION = register("minecraft:lingering_potion", 595);

    public static final BlockType LIT_BLAST_FURNACE = register("minecraft:lit_blast_furnace", -214);

    public static final BlockType LIT_DEEPSLATE_REDSTONE_ORE = register("minecraft:lit_deepslate_redstone_ore", -404);

    public static final BlockType LIT_FURNACE = register("minecraft:lit_furnace", 62);

    public static final BlockType LIT_PUMPKIN = register("minecraft:lit_pumpkin", 91);

    public static final BlockType LIT_REDSTONE_LAMP = register("minecraft:lit_redstone_lamp", 124);

    public static final BlockType LIT_REDSTONE_ORE = register("minecraft:lit_redstone_ore", 74);

    public static final BlockType LIT_SMOKER = register("minecraft:lit_smoker", -199);

    public static final BlockType LODESTONE = register("minecraft:lodestone", -222);

    public static final BlockType LODESTONE_COMPASS = register("minecraft:lodestone_compass", 639);

    public static final BlockType LOG = register("minecraft:log", 781);

    public static final BlockType LOG2 = register("minecraft:log2", 804);

    public static final BlockType LOOM = register("minecraft:loom", -204);

    public static final BlockType MAGENTA_CANDLE = register("minecraft:magenta_candle", -415);

    public static final BlockType MAGENTA_CANDLE_CAKE = register("minecraft:magenta_candle_cake", -432);

    public static final BlockType MAGENTA_CARPET = register("minecraft:magenta_carpet", -598);

    public static final BlockType MAGENTA_CONCRETE = register("minecraft:magenta_concrete", -629);

    public static final BlockType MAGENTA_CONCRETE_POWDER = register("minecraft:magenta_concrete_powder", -710);

    public static final BlockType MAGENTA_GLAZED_TERRACOTTA = register("minecraft:magenta_glazed_terracotta", 222);

    public static final BlockType MAGENTA_SHULKER_BOX = register("minecraft:magenta_shulker_box", -614);

    public static final BlockType MAGENTA_STAINED_GLASS = register("minecraft:magenta_stained_glass", -674);

    public static final BlockType MAGENTA_STAINED_GLASS_PANE = register("minecraft:magenta_stained_glass_pane", -644);

    public static final BlockType MAGENTA_TERRACOTTA = register("minecraft:magenta_terracotta", -725);

    public static final BlockType MAGENTA_WOOL = register("minecraft:magenta_wool", -565);

    public static final BlockType MAGMA = register("minecraft:magma", 213);

    public static final BlockType MAGMA_CREAM = register("minecraft:magma_cream", 457);

    public static final BlockType MANGROVE_BUTTON = register("minecraft:mangrove_button", -487);

    public static final BlockType MANGROVE_CHEST_BOAT = register("minecraft:mangrove_chest_boat", 681);

    public static final BlockType MANGROVE_DOUBLE_SLAB = register("minecraft:mangrove_double_slab", -499);

    public static final BlockType MANGROVE_FENCE = register("minecraft:mangrove_fence", -491);

    public static final BlockType MANGROVE_FENCE_GATE = register("minecraft:mangrove_fence_gate", -492);

    public static final BlockType MANGROVE_HANGING_SIGN = register("minecraft:mangrove_hanging_sign", -508);

    public static final BlockType MANGROVE_LEAVES = register("minecraft:mangrove_leaves", -472);

    public static final BlockType MANGROVE_LOG = register("minecraft:mangrove_log", -484);

    public static final BlockType MANGROVE_PLANKS = register("minecraft:mangrove_planks", -486);

    public static final BlockType MANGROVE_PRESSURE_PLATE = register("minecraft:mangrove_pressure_plate", -490);

    public static final BlockType MANGROVE_PROPAGULE = register("minecraft:mangrove_propagule", -474);

    public static final BlockType MANGROVE_ROOTS = register("minecraft:mangrove_roots", -482);

    public static final BlockType MANGROVE_SIGN = register("minecraft:mangrove_sign", 671);

    public static final BlockType MANGROVE_SLAB = register("minecraft:mangrove_slab", -489);

    public static final BlockType MANGROVE_STAIRS = register("minecraft:mangrove_stairs", -488);

    public static final BlockType MANGROVE_STANDING_SIGN = register("minecraft:mangrove_standing_sign", -494);

    public static final BlockType MANGROVE_TRAPDOOR = register("minecraft:mangrove_trapdoor", -496);

    public static final BlockType MANGROVE_WALL_SIGN = register("minecraft:mangrove_wall_sign", -495);

    public static final BlockType MANGROVE_WOOD = register("minecraft:mangrove_wood", -497);

    public static final BlockType MATERIAL_REDUCER = register("minecraft:material_reducer", -986);

    public static final BlockType MEDICINE = register("minecraft:medicine", 636);

    public static final BlockType MEDIUM_AMETHYST_BUD = register("minecraft:medium_amethyst_bud", -331);

    public static final BlockType MELON_BLOCK = register("minecraft:melon_block", 103);

    public static final BlockType MELON_SEEDS = register("minecraft:melon_seeds", 315);

    public static final BlockType MELON_SLICE = register("minecraft:melon_slice", 294);

    public static final BlockType MELON_STEM = register("minecraft:melon_stem", 105);

    public static final BlockType MINECART = register("minecraft:minecart", 395);

    public static final BlockType MOB_SPAWNER = register("minecraft:mob_spawner", 52);

    public static final BlockType MONSTER_EGG = register("minecraft:monster_egg", 805);

    public static final BlockType MOSS_BLOCK = register("minecraft:moss_block", -320);

    public static final BlockType MOSS_CARPET = register("minecraft:moss_carpet", -335);

    public static final BlockType MOSSY_COBBLESTONE = register("minecraft:mossy_cobblestone", 48);

    public static final BlockType MOSSY_COBBLESTONE_DOUBLE_SLAB = register("minecraft:mossy_cobblestone_double_slab", -915);

    public static final BlockType MOSSY_COBBLESTONE_SLAB = register("minecraft:mossy_cobblestone_slab", -888);

    public static final BlockType MOSSY_COBBLESTONE_STAIRS = register("minecraft:mossy_cobblestone_stairs", -179);

    public static final BlockType MOSSY_COBBLESTONE_WALL = register("minecraft:mossy_cobblestone_wall", -971);

    public static final BlockType MOSSY_STONE_BRICK_DOUBLE_SLAB = register("minecraft:mossy_stone_brick_double_slab", -168);

    public static final BlockType MOSSY_STONE_BRICK_SLAB = register("minecraft:mossy_stone_brick_slab", -166);

    public static final BlockType MOSSY_STONE_BRICK_STAIRS = register("minecraft:mossy_stone_brick_stairs", -175);

    public static final BlockType MOSSY_STONE_BRICK_WALL = register("minecraft:mossy_stone_brick_wall", -978);

    public static final BlockType MOSSY_STONE_BRICKS = register("minecraft:mossy_stone_bricks", -868);

    public static final BlockType MOVING_BLOCK = register("minecraft:moving_block", 250);

    public static final BlockType MUD = register("minecraft:mud", -473);

    public static final BlockType MUD_BRICK_DOUBLE_SLAB = register("minecraft:mud_brick_double_slab", -479);

    public static final BlockType MUD_BRICK_SLAB = register("minecraft:mud_brick_slab", -478);

    public static final BlockType MUD_BRICK_STAIRS = register("minecraft:mud_brick_stairs", -480);

    public static final BlockType MUD_BRICK_WALL = register("minecraft:mud_brick_wall", -481);

    public static final BlockType MUD_BRICKS = register("minecraft:mud_bricks", -475);

    public static final BlockType MUDDY_MANGROVE_ROOTS = register("minecraft:muddy_mangrove_roots", -483);

    public static final BlockType MUSHROOM_STEM = register("minecraft:mushroom_stem", -1008);

    public static final BlockType MUSHROOM_STEW = register("minecraft:mushroom_stew", 282);

    public static final BlockType MUSIC_DISC_11 = register("minecraft:music_disc_11", 577);

    public static final BlockType MUSIC_DISC_13 = register("minecraft:music_disc_13", 567);

    public static final BlockType MUSIC_DISC_5 = register("minecraft:music_disc_5", 673);

    public static final BlockType MUSIC_DISC_BLOCKS = register("minecraft:music_disc_blocks", 569);

    public static final BlockType MUSIC_DISC_CAT = register("minecraft:music_disc_cat", 568);

    public static final BlockType MUSIC_DISC_CHIRP = register("minecraft:music_disc_chirp", 570);

    public static final BlockType MUSIC_DISC_FAR = register("minecraft:music_disc_far", 571);

    public static final BlockType MUSIC_DISC_MALL = register("minecraft:music_disc_mall", 572);

    public static final BlockType MUSIC_DISC_MELLOHI = register("minecraft:music_disc_mellohi", 573);

    public static final BlockType MUSIC_DISC_OTHERSIDE = register("minecraft:music_disc_otherside", 663);

    public static final BlockType MUSIC_DISC_PIGSTEP = register("minecraft:music_disc_pigstep", 657);

    public static final BlockType MUSIC_DISC_RELIC = register("minecraft:music_disc_relic", 736);

    public static final BlockType MUSIC_DISC_STAL = register("minecraft:music_disc_stal", 574);

    public static final BlockType MUSIC_DISC_STRAD = register("minecraft:music_disc_strad", 575);

    public static final BlockType MUSIC_DISC_WAIT = register("minecraft:music_disc_wait", 578);

    public static final BlockType MUSIC_DISC_WARD = register("minecraft:music_disc_ward", 576);

    public static final BlockType MUTTON = register("minecraft:mutton", 583);

    public static final BlockType MYCELIUM = register("minecraft:mycelium", 110);

    public static final BlockType NAME_TAG = register("minecraft:name_tag", 581);

    public static final BlockType NAUTILUS_SHELL = register("minecraft:nautilus_shell", 603);

    public static final BlockType NETHER_BRICK = register("minecraft:nether_brick", 112);

    public static final BlockType NETHER_BRICK_DOUBLE_SLAB = register("minecraft:nether_brick_double_slab", -883);

    public static final BlockType NETHER_BRICK_FENCE = register("minecraft:nether_brick_fence", 113);

    public static final BlockType NETHER_BRICK_SLAB = register("minecraft:nether_brick_slab", -877);

    public static final BlockType NETHER_BRICK_STAIRS = register("minecraft:nether_brick_stairs", 114);

    public static final BlockType NETHER_BRICK_WALL = register("minecraft:nether_brick_wall", -979);

    public static final BlockType NETHER_GOLD_ORE = register("minecraft:nether_gold_ore", -288);

    public static final BlockType NETHER_STAR = register("minecraft:nether_star", 551);

    public static final BlockType NETHER_WART_BLOCK = register("minecraft:nether_wart_block", 214);

    public static final BlockType NETHERBRICK = register("minecraft:netherbrick", 556);

    public static final BlockType NETHERITE_AXE = register("minecraft:netherite_axe", 643);

    public static final BlockType NETHERITE_BLOCK = register("minecraft:netherite_block", -270);

    public static final BlockType NETHERITE_BOOTS = register("minecraft:netherite_boots", 649);

    public static final BlockType NETHERITE_CHESTPLATE = register("minecraft:netherite_chestplate", 647);

    public static final BlockType NETHERITE_HELMET = register("minecraft:netherite_helmet", 646);

    public static final BlockType NETHERITE_HOE = register("minecraft:netherite_hoe", 644);

    public static final BlockType NETHERITE_INGOT = register("minecraft:netherite_ingot", 645);

    public static final BlockType NETHERITE_LEGGINGS = register("minecraft:netherite_leggings", 648);

    public static final BlockType NETHERITE_PICKAXE = register("minecraft:netherite_pickaxe", 642);

    public static final BlockType NETHERITE_SCRAP = register("minecraft:netherite_scrap", 650);

    public static final BlockType NETHERITE_SHOVEL = register("minecraft:netherite_shovel", 641);

    public static final BlockType NETHERITE_SWORD = register("minecraft:netherite_sword", 640);

    public static final BlockType NETHERRACK = register("minecraft:netherrack", 87);

    public static final BlockType NETHERREACTOR = register("minecraft:netherreactor", 247);

    public static final BlockType NORMAL_STONE_DOUBLE_SLAB = register("minecraft:normal_stone_double_slab", -926);

    public static final BlockType NORMAL_STONE_SLAB = register("minecraft:normal_stone_slab", -899);

    public static final BlockType NORMAL_STONE_STAIRS = register("minecraft:normal_stone_stairs", -180);

    public static final BlockType NOTEBLOCK = register("minecraft:noteblock", 25);

    public static final BlockType OAK_BOAT = register("minecraft:oak_boat", 401);

    public static final BlockType OAK_CHEST_BOAT = register("minecraft:oak_chest_boat", 675);

    public static final BlockType OAK_DOUBLE_SLAB = register("minecraft:oak_double_slab", 157);

    public static final BlockType OAK_FENCE = register("minecraft:oak_fence", 85);

    public static final BlockType OAK_HANGING_SIGN = register("minecraft:oak_hanging_sign", -500);

    public static final BlockType OAK_LEAVES = register("minecraft:oak_leaves", 18);

    public static final BlockType OAK_LOG = register("minecraft:oak_log", 17);

    public static final BlockType OAK_PLANKS = register("minecraft:oak_planks", 5);

    public static final BlockType OAK_SAPLING = register("minecraft:oak_sapling", 6);

    public static final BlockType OAK_SIGN = register("minecraft:oak_sign", 383);

    public static final BlockType OAK_SLAB = register("minecraft:oak_slab", 158);

    public static final BlockType OAK_STAIRS = register("minecraft:oak_stairs", 53);

    public static final BlockType OAK_WOOD = register("minecraft:oak_wood", -212);

    public static final BlockType OBSERVER = register("minecraft:observer", 251);

    public static final BlockType OBSIDIAN = register("minecraft:obsidian", 49);

    public static final BlockType OCHRE_FROGLIGHT = register("minecraft:ochre_froglight", -471);

    public static final BlockType OPEN_EYEBLOSSOM = register("minecraft:open_eyeblossom", -1018);

    public static final BlockType ORANGE_CANDLE = register("minecraft:orange_candle", -414);

    public static final BlockType ORANGE_CANDLE_CAKE = register("minecraft:orange_candle_cake", -431);

    public static final BlockType ORANGE_CARPET = register("minecraft:orange_carpet", -597);

    public static final BlockType ORANGE_CONCRETE = register("minecraft:orange_concrete", -628);

    public static final BlockType ORANGE_CONCRETE_POWDER = register("minecraft:orange_concrete_powder", -709);

    public static final BlockType ORANGE_GLAZED_TERRACOTTA = register("minecraft:orange_glazed_terracotta", 221);

    public static final BlockType ORANGE_SHULKER_BOX = register("minecraft:orange_shulker_box", -613);

    public static final BlockType ORANGE_STAINED_GLASS = register("minecraft:orange_stained_glass", -673);

    public static final BlockType ORANGE_STAINED_GLASS_PANE = register("minecraft:orange_stained_glass_pane", -643);

    public static final BlockType ORANGE_TERRACOTTA = register("minecraft:orange_terracotta", -724);

    public static final BlockType ORANGE_TULIP = register("minecraft:orange_tulip", -834);

    public static final BlockType ORANGE_WOOL = register("minecraft:orange_wool", -557);

    public static final BlockType OXEYE_DAISY = register("minecraft:oxeye_daisy", -837);

    public static final BlockType OXIDIZED_CHISELED_COPPER = register("minecraft:oxidized_chiseled_copper", -763);

    public static final BlockType OXIDIZED_COPPER = register("minecraft:oxidized_copper", -343);

    public static final BlockType OXIDIZED_COPPER_BULB = register("minecraft:oxidized_copper_bulb", -779);

    public static final BlockType OXIDIZED_COPPER_DOOR = register("minecraft:oxidized_copper_door", -787);

    public static final BlockType OXIDIZED_COPPER_GRATE = register("minecraft:oxidized_copper_grate", -771);

    public static final BlockType OXIDIZED_COPPER_TRAPDOOR = register("minecraft:oxidized_copper_trapdoor", -795);

    public static final BlockType OXIDIZED_CUT_COPPER = register("minecraft:oxidized_cut_copper", -350);

    public static final BlockType OXIDIZED_CUT_COPPER_SLAB = register("minecraft:oxidized_cut_copper_slab", -364);

    public static final BlockType OXIDIZED_CUT_COPPER_STAIRS = register("minecraft:oxidized_cut_copper_stairs", -357);

    public static final BlockType OXIDIZED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:oxidized_double_cut_copper_slab", -371);

    public static final BlockType PACKED_ICE = register("minecraft:packed_ice", 174);

    public static final BlockType PACKED_MUD = register("minecraft:packed_mud", -477);

    public static final BlockType PAINTING = register("minecraft:painting", 382);

    public static final BlockType PALE_HANGING_MOSS = register("minecraft:pale_hanging_moss", -1011);

    public static final BlockType PALE_MOSS_BLOCK = register("minecraft:pale_moss_block", -1009);

    public static final BlockType PALE_MOSS_CARPET = register("minecraft:pale_moss_carpet", -1010);

    public static final BlockType PALE_OAK_BUTTON = register("minecraft:pale_oak_button", -989);

    public static final BlockType PALE_OAK_CHEST_BOAT = register("minecraft:pale_oak_chest_boat", 745);

    public static final BlockType PALE_OAK_DOOR = register("minecraft:pale_oak_door", -990);

    public static final BlockType PALE_OAK_DOUBLE_SLAB = register("minecraft:pale_oak_double_slab", -999);

    public static final BlockType PALE_OAK_FENCE = register("minecraft:pale_oak_fence", -991);

    public static final BlockType PALE_OAK_FENCE_GATE = register("minecraft:pale_oak_fence_gate", -992);

    public static final BlockType PALE_OAK_HANGING_SIGN = register("minecraft:pale_oak_hanging_sign", -993);

    public static final BlockType PALE_OAK_LEAVES = register("minecraft:pale_oak_leaves", -1007);

    public static final BlockType PALE_OAK_LOG = register("minecraft:pale_oak_log", -995);

    public static final BlockType PALE_OAK_PLANKS = register("minecraft:pale_oak_planks", -996);

    public static final BlockType PALE_OAK_PRESSURE_PLATE = register("minecraft:pale_oak_pressure_plate", -997);

    public static final BlockType PALE_OAK_SAPLING = register("minecraft:pale_oak_sapling", -1006);

    public static final BlockType PALE_OAK_SLAB = register("minecraft:pale_oak_slab", -998);

    public static final BlockType PALE_OAK_STAIRS = register("minecraft:pale_oak_stairs", -1000);

    public static final BlockType PALE_OAK_STANDING_SIGN = register("minecraft:pale_oak_standing_sign", -1001);

    public static final BlockType PALE_OAK_TRAPDOOR = register("minecraft:pale_oak_trapdoor", -1002);

    public static final BlockType PALE_OAK_WALL_SIGN = register("minecraft:pale_oak_wall_sign", -1003);

    public static final BlockType PALE_OAK_WOOD = register("minecraft:pale_oak_wood", -1005);

    public static final BlockType PAPER = register("minecraft:paper", 412);

    public static final BlockType PEARLESCENT_FROGLIGHT = register("minecraft:pearlescent_froglight", -469);

    public static final BlockType PEONY = register("minecraft:peony", -867);

    public static final BlockType PETRIFIED_OAK_DOUBLE_SLAB = register("minecraft:petrified_oak_double_slab", -903);

    public static final BlockType PETRIFIED_OAK_SLAB = register("minecraft:petrified_oak_slab", -902);

    public static final BlockType PHANTOM_MEMBRANE = register("minecraft:phantom_membrane", 607);

    public static final BlockType PIGLIN_HEAD = register("minecraft:piglin_head", -970);

    public static final BlockType PINK_CANDLE = register("minecraft:pink_candle", -419);

    public static final BlockType PINK_CANDLE_CAKE = register("minecraft:pink_candle_cake", -436);

    public static final BlockType PINK_CARPET = register("minecraft:pink_carpet", -602);

    public static final BlockType PINK_CONCRETE = register("minecraft:pink_concrete", -633);

    public static final BlockType PINK_CONCRETE_POWDER = register("minecraft:pink_concrete_powder", -714);

    public static final BlockType PINK_GLAZED_TERRACOTTA = register("minecraft:pink_glazed_terracotta", 226);

    public static final BlockType PINK_PETALS = register("minecraft:pink_petals", -549);

    public static final BlockType PINK_SHULKER_BOX = register("minecraft:pink_shulker_box", -618);

    public static final BlockType PINK_STAINED_GLASS = register("minecraft:pink_stained_glass", -678);

    public static final BlockType PINK_STAINED_GLASS_PANE = register("minecraft:pink_stained_glass_pane", -648);

    public static final BlockType PINK_TERRACOTTA = register("minecraft:pink_terracotta", -729);

    public static final BlockType PINK_TULIP = register("minecraft:pink_tulip", -836);

    public static final BlockType PINK_WOOL = register("minecraft:pink_wool", -566);

    public static final BlockType PISTON = register("minecraft:piston", 33);

    public static final BlockType PISTON_ARM_COLLISION = register("minecraft:piston_arm_collision", 34);

    public static final BlockType PITCHER_CROP = register("minecraft:pitcher_crop", -574);

    public static final BlockType PITCHER_PLANT = register("minecraft:pitcher_plant", -612);

    public static final BlockType PLANKS = register("minecraft:planks", 801);

    public static final BlockType PLAYER_HEAD = register("minecraft:player_head", -967);

    public static final BlockType PODZOL = register("minecraft:podzol", 243);

    public static final BlockType POINTED_DRIPSTONE = register("minecraft:pointed_dripstone", -308);

    public static final BlockType POISONOUS_POTATO = register("minecraft:poisonous_potato", 304);

    public static final BlockType POLISHED_ANDESITE = register("minecraft:polished_andesite", -595);

    public static final BlockType POLISHED_ANDESITE_DOUBLE_SLAB = register("minecraft:polished_andesite_double_slab", -919);

    public static final BlockType POLISHED_ANDESITE_SLAB = register("minecraft:polished_andesite_slab", -892);

    public static final BlockType POLISHED_ANDESITE_STAIRS = register("minecraft:polished_andesite_stairs", -174);

    public static final BlockType POLISHED_BASALT = register("minecraft:polished_basalt", -235);

    public static final BlockType POLISHED_BLACKSTONE = register("minecraft:polished_blackstone", -291);

    public static final BlockType POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB = register("minecraft:polished_blackstone_brick_double_slab", -285);

    public static final BlockType POLISHED_BLACKSTONE_BRICK_SLAB = register("minecraft:polished_blackstone_brick_slab", -284);

    public static final BlockType POLISHED_BLACKSTONE_BRICK_STAIRS = register("minecraft:polished_blackstone_brick_stairs", -275);

    public static final BlockType POLISHED_BLACKSTONE_BRICK_WALL = register("minecraft:polished_blackstone_brick_wall", -278);

    public static final BlockType POLISHED_BLACKSTONE_BRICKS = register("minecraft:polished_blackstone_bricks", -274);

    public static final BlockType POLISHED_BLACKSTONE_BUTTON = register("minecraft:polished_blackstone_button", -296);

    public static final BlockType POLISHED_BLACKSTONE_DOUBLE_SLAB = register("minecraft:polished_blackstone_double_slab", -294);

    public static final BlockType POLISHED_BLACKSTONE_PRESSURE_PLATE = register("minecraft:polished_blackstone_pressure_plate", -295);

    public static final BlockType POLISHED_BLACKSTONE_SLAB = register("minecraft:polished_blackstone_slab", -293);

    public static final BlockType POLISHED_BLACKSTONE_STAIRS = register("minecraft:polished_blackstone_stairs", -292);

    public static final BlockType POLISHED_BLACKSTONE_WALL = register("minecraft:polished_blackstone_wall", -297);

    public static final BlockType POLISHED_DEEPSLATE = register("minecraft:polished_deepslate", -383);

    public static final BlockType POLISHED_DEEPSLATE_DOUBLE_SLAB = register("minecraft:polished_deepslate_double_slab", -397);

    public static final BlockType POLISHED_DEEPSLATE_SLAB = register("minecraft:polished_deepslate_slab", -384);

    public static final BlockType POLISHED_DEEPSLATE_STAIRS = register("minecraft:polished_deepslate_stairs", -385);

    public static final BlockType POLISHED_DEEPSLATE_WALL = register("minecraft:polished_deepslate_wall", -386);

    public static final BlockType POLISHED_DIORITE = register("minecraft:polished_diorite", -593);

    public static final BlockType POLISHED_DIORITE_DOUBLE_SLAB = register("minecraft:polished_diorite_double_slab", -922);

    public static final BlockType POLISHED_DIORITE_SLAB = register("minecraft:polished_diorite_slab", -895);

    public static final BlockType POLISHED_DIORITE_STAIRS = register("minecraft:polished_diorite_stairs", -173);

    public static final BlockType POLISHED_GRANITE = register("minecraft:polished_granite", -591);

    public static final BlockType POLISHED_GRANITE_DOUBLE_SLAB = register("minecraft:polished_granite_double_slab", -924);

    public static final BlockType POLISHED_GRANITE_SLAB = register("minecraft:polished_granite_slab", -897);

    public static final BlockType POLISHED_GRANITE_STAIRS = register("minecraft:polished_granite_stairs", -172);

    public static final BlockType POLISHED_TUFF = register("minecraft:polished_tuff", -748);

    public static final BlockType POLISHED_TUFF_DOUBLE_SLAB = register("minecraft:polished_tuff_double_slab", -750);

    public static final BlockType POLISHED_TUFF_SLAB = register("minecraft:polished_tuff_slab", -749);

    public static final BlockType POLISHED_TUFF_STAIRS = register("minecraft:polished_tuff_stairs", -751);

    public static final BlockType POLISHED_TUFF_WALL = register("minecraft:polished_tuff_wall", -752);

    public static final BlockType POPPED_CHORUS_FRUIT = register("minecraft:popped_chorus_fruit", 592);

    public static final BlockType POPPY = register("minecraft:poppy", 38);

    public static final BlockType PORKCHOP = register("minecraft:porkchop", 284);

    public static final BlockType PORTAL = register("minecraft:portal", 90);

    public static final BlockType POTATO = register("minecraft:potato", 302);

    public static final BlockType POTATOES = register("minecraft:potatoes", 142);

    public static final BlockType POTION = register("minecraft:potion", 453);

    public static final BlockType POWDER_SNOW = register("minecraft:powder_snow", -306);

    public static final BlockType POWERED_COMPARATOR = register("minecraft:powered_comparator", 150);

    public static final BlockType POWERED_REPEATER = register("minecraft:powered_repeater", 94);

    public static final BlockType PRISMARINE = register("minecraft:prismarine", 168);

    public static final BlockType PRISMARINE_BRICK_DOUBLE_SLAB = register("minecraft:prismarine_brick_double_slab", -914);

    public static final BlockType PRISMARINE_BRICK_SLAB = register("minecraft:prismarine_brick_slab", -887);

    public static final BlockType PRISMARINE_BRICKS = register("minecraft:prismarine_bricks", -948);

    public static final BlockType PRISMARINE_BRICKS_STAIRS = register("minecraft:prismarine_bricks_stairs", -4);

    public static final BlockType PRISMARINE_CRYSTALS = register("minecraft:prismarine_crystals", 582);

    public static final BlockType PRISMARINE_DOUBLE_SLAB = register("minecraft:prismarine_double_slab", -912);

    public static final BlockType PRISMARINE_SHARD = register("minecraft:prismarine_shard", 598);

    public static final BlockType PRISMARINE_SLAB = register("minecraft:prismarine_slab", -885);

    public static final BlockType PRISMARINE_STAIRS = register("minecraft:prismarine_stairs", -2);

    public static final BlockType PRISMARINE_WALL = register("minecraft:prismarine_wall", -981);

    public static final BlockType PUFFERFISH = register("minecraft:pufferfish", 289);

    public static final BlockType PUMPKIN = register("minecraft:pumpkin", 86);

    public static final BlockType PUMPKIN_PIE = register("minecraft:pumpkin_pie", 306);

    public static final BlockType PUMPKIN_SEEDS = register("minecraft:pumpkin_seeds", 314);

    public static final BlockType PUMPKIN_STEM = register("minecraft:pumpkin_stem", 104);

    public static final BlockType PURPLE_CANDLE = register("minecraft:purple_candle", -423);

    public static final BlockType PURPLE_CANDLE_CAKE = register("minecraft:purple_candle_cake", -440);

    public static final BlockType PURPLE_CARPET = register("minecraft:purple_carpet", -606);

    public static final BlockType PURPLE_CONCRETE = register("minecraft:purple_concrete", -637);

    public static final BlockType PURPLE_CONCRETE_POWDER = register("minecraft:purple_concrete_powder", -718);

    public static final BlockType PURPLE_GLAZED_TERRACOTTA = register("minecraft:purple_glazed_terracotta", 219);

    public static final BlockType PURPLE_SHULKER_BOX = register("minecraft:purple_shulker_box", -622);

    public static final BlockType PURPLE_STAINED_GLASS = register("minecraft:purple_stained_glass", -682);

    public static final BlockType PURPLE_STAINED_GLASS_PANE = register("minecraft:purple_stained_glass_pane", -652);

    public static final BlockType PURPLE_TERRACOTTA = register("minecraft:purple_terracotta", -733);

    public static final BlockType PURPLE_WOOL = register("minecraft:purple_wool", -564);

    public static final BlockType PURPUR_BLOCK = register("minecraft:purpur_block", 201);

    public static final BlockType PURPUR_DOUBLE_SLAB = register("minecraft:purpur_double_slab", -911);

    public static final BlockType PURPUR_PILLAR = register("minecraft:purpur_pillar", -951);

    public static final BlockType PURPUR_SLAB = register("minecraft:purpur_slab", -884);

    public static final BlockType PURPUR_STAIRS = register("minecraft:purpur_stairs", 203);

    public static final BlockType QUARTZ = register("minecraft:quartz", 557);

    public static final BlockType QUARTZ_BLOCK = register("minecraft:quartz_block", 155);

    public static final BlockType QUARTZ_BRICKS = register("minecraft:quartz_bricks", -304);

    public static final BlockType QUARTZ_DOUBLE_SLAB = register("minecraft:quartz_double_slab", -882);

    public static final BlockType QUARTZ_ORE = register("minecraft:quartz_ore", 153);

    public static final BlockType QUARTZ_PILLAR = register("minecraft:quartz_pillar", -954);

    public static final BlockType QUARTZ_SLAB = register("minecraft:quartz_slab", -876);

    public static final BlockType QUARTZ_STAIRS = register("minecraft:quartz_stairs", 156);

    public static final BlockType RABBIT = register("minecraft:rabbit", 310);

    public static final BlockType RABBIT_FOOT = register("minecraft:rabbit_foot", 561);

    public static final BlockType RABBIT_HIDE = register("minecraft:rabbit_hide", 562);

    public static final BlockType RABBIT_STEW = register("minecraft:rabbit_stew", 312);

    public static final BlockType RAIL = register("minecraft:rail", 66);

    public static final BlockType RAPID_FERTILIZER = register("minecraft:rapid_fertilizer", 634);

    public static final BlockType RAW_COPPER = register("minecraft:raw_copper", 541);

    public static final BlockType RAW_COPPER_BLOCK = register("minecraft:raw_copper_block", -452);

    public static final BlockType RAW_GOLD_BLOCK = register("minecraft:raw_gold_block", -453);

    public static final BlockType RAW_IRON_BLOCK = register("minecraft:raw_iron_block", -451);

    public static final BlockType RECOVERY_COMPASS = register("minecraft:recovery_compass", 683);

    public static final BlockType RED_CANDLE = register("minecraft:red_candle", -427);

    public static final BlockType RED_CANDLE_CAKE = register("minecraft:red_candle_cake", -444);

    public static final BlockType RED_CARPET = register("minecraft:red_carpet", -610);

    public static final BlockType RED_CONCRETE = register("minecraft:red_concrete", -641);

    public static final BlockType RED_CONCRETE_POWDER = register("minecraft:red_concrete_powder", -722);

    public static final BlockType RED_FLOWER = register("minecraft:red_flower", 799);

    public static final BlockType RED_GLAZED_TERRACOTTA = register("minecraft:red_glazed_terracotta", 234);

    public static final BlockType RED_MUSHROOM = register("minecraft:red_mushroom", 40);

    public static final BlockType RED_MUSHROOM_BLOCK = register("minecraft:red_mushroom_block", 100);

    public static final BlockType RED_NETHER_BRICK = register("minecraft:red_nether_brick", 215);

    public static final BlockType RED_NETHER_BRICK_DOUBLE_SLAB = register("minecraft:red_nether_brick_double_slab", -917);

    public static final BlockType RED_NETHER_BRICK_SLAB = register("minecraft:red_nether_brick_slab", -890);

    public static final BlockType RED_NETHER_BRICK_STAIRS = register("minecraft:red_nether_brick_stairs", -184);

    public static final BlockType RED_NETHER_BRICK_WALL = register("minecraft:red_nether_brick_wall", -983);

    public static final BlockType RED_SAND = register("minecraft:red_sand", -949);

    public static final BlockType RED_SANDSTONE = register("minecraft:red_sandstone", 179);

    public static final BlockType RED_SANDSTONE_DOUBLE_SLAB = register("minecraft:red_sandstone_double_slab", 181);

    public static final BlockType RED_SANDSTONE_SLAB = register("minecraft:red_sandstone_slab", 182);

    public static final BlockType RED_SANDSTONE_STAIRS = register("minecraft:red_sandstone_stairs", 180);

    public static final BlockType RED_SANDSTONE_WALL = register("minecraft:red_sandstone_wall", -982);

    public static final BlockType RED_SHULKER_BOX = register("minecraft:red_shulker_box", -626);

    public static final BlockType RED_STAINED_GLASS = register("minecraft:red_stained_glass", -686);

    public static final BlockType RED_STAINED_GLASS_PANE = register("minecraft:red_stained_glass_pane", -656);

    public static final BlockType RED_TERRACOTTA = register("minecraft:red_terracotta", -737);

    public static final BlockType RED_TULIP = register("minecraft:red_tulip", -833);

    public static final BlockType RED_WOOL = register("minecraft:red_wool", -556);

    public static final BlockType REDSTONE = register("minecraft:redstone", 398);

    public static final BlockType REDSTONE_BLOCK = register("minecraft:redstone_block", 152);

    public static final BlockType REDSTONE_LAMP = register("minecraft:redstone_lamp", 123);

    public static final BlockType REDSTONE_ORE = register("minecraft:redstone_ore", 73);

    public static final BlockType REDSTONE_TORCH = register("minecraft:redstone_torch", 76);

    public static final BlockType REDSTONE_WIRE = register("minecraft:redstone_wire", 55);

    public static final BlockType REINFORCED_DEEPSLATE = register("minecraft:reinforced_deepslate", -466);

    public static final BlockType REPEATER = register("minecraft:repeater", 445);

    public static final BlockType REPEATING_COMMAND_BLOCK = register("minecraft:repeating_command_block", 188);

    public static final BlockType RESERVED6 = register("minecraft:reserved6", 255);

    public static final BlockType RESIN_BLOCK = register("minecraft:resin_block", -1021);

    public static final BlockType RESIN_BRICK_DOUBLE_SLAB = register("minecraft:resin_brick_double_slab", -1015);

    public static final BlockType RESIN_BRICK_SLAB = register("minecraft:resin_brick_slab", -1014);

    public static final BlockType RESIN_BRICK_STAIRS = register("minecraft:resin_brick_stairs", -1016);

    public static final BlockType RESIN_BRICK_WALL = register("minecraft:resin_brick_wall", -1017);

    public static final BlockType RESIN_BRICKS = register("minecraft:resin_bricks", -1013);

    public static final BlockType RESIN_CLUMP = register("minecraft:resin_clump", -1022);

    public static final BlockType RESPAWN_ANCHOR = register("minecraft:respawn_anchor", -272);

    public static final BlockType ROSE_BUSH = register("minecraft:rose_bush", -866);

    public static final BlockType ROTTEN_FLESH = register("minecraft:rotten_flesh", 299);

    public static final BlockType SADDLE = register("minecraft:saddle", 396);

    public static final BlockType SALMON = register("minecraft:salmon", 287);

    public static final BlockType SAND = register("minecraft:sand", 12);

    public static final BlockType SANDSTONE = register("minecraft:sandstone", 24);

    public static final BlockType SANDSTONE_DOUBLE_SLAB = register("minecraft:sandstone_double_slab", -878);

    public static final BlockType SANDSTONE_SLAB = register("minecraft:sandstone_slab", -872);

    public static final BlockType SANDSTONE_STAIRS = register("minecraft:sandstone_stairs", 128);

    public static final BlockType SANDSTONE_WALL = register("minecraft:sandstone_wall", -975);

    public static final BlockType SAPLING = register("minecraft:sapling", 795);

    public static final BlockType SCAFFOLDING = register("minecraft:scaffolding", -165);

    public static final BlockType SCULK = register("minecraft:sculk", -458);

    public static final BlockType SCULK_CATALYST = register("minecraft:sculk_catalyst", -460);

    public static final BlockType SCULK_SENSOR = register("minecraft:sculk_sensor", -307);

    public static final BlockType SCULK_SHRIEKER = register("minecraft:sculk_shrieker", -461);

    public static final BlockType SCULK_VEIN = register("minecraft:sculk_vein", -459);

    public static final BlockType SEA_LANTERN = register("minecraft:sea_lantern", 169);

    public static final BlockType SEA_PICKLE = register("minecraft:sea_pickle", -156);

    public static final BlockType SEAGRASS = register("minecraft:seagrass", -130);

    public static final BlockType SHEARS = register("minecraft:shears", 447);

    public static final BlockType SHIELD = register("minecraft:shield", 380);

    public static final BlockType SHORT_DRY_GRASS = register("minecraft:short_dry_grass", -1028);

    public static final BlockType SHORT_GRASS = register("minecraft:short_grass", 31);

    public static final BlockType SHROOMLIGHT = register("minecraft:shroomlight", -230);

    public static final BlockType SHULKER_BOX = register("minecraft:shulker_box", 810);

    public static final BlockType SHULKER_SHELL = register("minecraft:shulker_shell", 599);

    public static final BlockType SILVER_GLAZED_TERRACOTTA = register("minecraft:silver_glazed_terracotta", 228);

    public static final BlockType SKELETON_SKULL = register("minecraft:skeleton_skull", 144);

    public static final BlockType SKULL = register("minecraft:skull", 737);

    public static final BlockType SLIME = register("minecraft:slime", 165);

    public static final BlockType SLIME_BALL = register("minecraft:slime_ball", 414);

    public static final BlockType SMALL_AMETHYST_BUD = register("minecraft:small_amethyst_bud", -332);

    public static final BlockType SMALL_DRIPLEAF_BLOCK = register("minecraft:small_dripleaf_block", -336);

    public static final BlockType SMITHING_TABLE = register("minecraft:smithing_table", -202);

    public static final BlockType SMOKER = register("minecraft:smoker", -198);

    public static final BlockType SMOOTH_BASALT = register("minecraft:smooth_basalt", -377);

    public static final BlockType SMOOTH_QUARTZ = register("minecraft:smooth_quartz", -955);

    public static final BlockType SMOOTH_QUARTZ_DOUBLE_SLAB = register("minecraft:smooth_quartz_double_slab", -925);

    public static final BlockType SMOOTH_QUARTZ_SLAB = register("minecraft:smooth_quartz_slab", -898);

    public static final BlockType SMOOTH_QUARTZ_STAIRS = register("minecraft:smooth_quartz_stairs", -185);

    public static final BlockType SMOOTH_RED_SANDSTONE = register("minecraft:smooth_red_sandstone", -958);

    public static final BlockType SMOOTH_RED_SANDSTONE_DOUBLE_SLAB = register("minecraft:smooth_red_sandstone_double_slab", -918);

    public static final BlockType SMOOTH_RED_SANDSTONE_SLAB = register("minecraft:smooth_red_sandstone_slab", -891);

    public static final BlockType SMOOTH_RED_SANDSTONE_STAIRS = register("minecraft:smooth_red_sandstone_stairs", -176);

    public static final BlockType SMOOTH_SANDSTONE = register("minecraft:smooth_sandstone", -946);

    public static final BlockType SMOOTH_SANDSTONE_DOUBLE_SLAB = register("minecraft:smooth_sandstone_double_slab", -916);

    public static final BlockType SMOOTH_SANDSTONE_SLAB = register("minecraft:smooth_sandstone_slab", -889);

    public static final BlockType SMOOTH_SANDSTONE_STAIRS = register("minecraft:smooth_sandstone_stairs", -177);

    public static final BlockType SMOOTH_STONE = register("minecraft:smooth_stone", -183);

    public static final BlockType SMOOTH_STONE_DOUBLE_SLAB = register("minecraft:smooth_stone_double_slab", 43);

    public static final BlockType SMOOTH_STONE_SLAB = register("minecraft:smooth_stone_slab", 44);

    public static final BlockType SNIFFER_EGG = register("minecraft:sniffer_egg", -596);

    public static final BlockType SNOW = register("minecraft:snow", 80);

    public static final BlockType SNOW_LAYER = register("minecraft:snow_layer", 78);

    public static final BlockType SNOWBALL = register("minecraft:snowball", 399);

    public static final BlockType SOUL_FIRE = register("minecraft:soul_fire", -237);

    public static final BlockType SOUL_LANTERN = register("minecraft:soul_lantern", -269);

    public static final BlockType SOUL_SAND = register("minecraft:soul_sand", 88);

    public static final BlockType SOUL_SOIL = register("minecraft:soul_soil", -236);

    public static final BlockType SOUL_TORCH = register("minecraft:soul_torch", -268);

    public static final BlockType SPARKLER = register("minecraft:sparkler", 637);

    public static final BlockType SPAWN_EGG = register("minecraft:spawn_egg", 826);

    public static final BlockType SPIDER_EYE = register("minecraft:spider_eye", 300);

    public static final BlockType SPLASH_POTION = register("minecraft:splash_potion", 594);

    public static final BlockType SPONGE = register("minecraft:sponge", 19);

    public static final BlockType SPORE_BLOSSOM = register("minecraft:spore_blossom", -321);

    public static final BlockType SPRUCE_BUTTON = register("minecraft:spruce_button", -144);

    public static final BlockType SPRUCE_CHEST_BOAT = register("minecraft:spruce_chest_boat", 678);

    public static final BlockType SPRUCE_DOUBLE_SLAB = register("minecraft:spruce_double_slab", -809);

    public static final BlockType SPRUCE_FENCE = register("minecraft:spruce_fence", -579);

    public static final BlockType SPRUCE_FENCE_GATE = register("minecraft:spruce_fence_gate", 183);

    public static final BlockType SPRUCE_HANGING_SIGN = register("minecraft:spruce_hanging_sign", -501);

    public static final BlockType SPRUCE_LEAVES = register("minecraft:spruce_leaves", -800);

    public static final BlockType SPRUCE_LOG = register("minecraft:spruce_log", -569);

    public static final BlockType SPRUCE_PLANKS = register("minecraft:spruce_planks", -739);

    public static final BlockType SPRUCE_PRESSURE_PLATE = register("minecraft:spruce_pressure_plate", -154);

    public static final BlockType SPRUCE_SAPLING = register("minecraft:spruce_sapling", -825);

    public static final BlockType SPRUCE_SIGN = register("minecraft:spruce_sign", 609);

    public static final BlockType SPRUCE_SLAB = register("minecraft:spruce_slab", -804);

    public static final BlockType SPRUCE_STAIRS = register("minecraft:spruce_stairs", 134);

    public static final BlockType SPRUCE_STANDING_SIGN = register("minecraft:spruce_standing_sign", -181);

    public static final BlockType SPRUCE_TRAPDOOR = register("minecraft:spruce_trapdoor", -149);

    public static final BlockType SPRUCE_WALL_SIGN = register("minecraft:spruce_wall_sign", -182);

    public static final BlockType SPRUCE_WOOD = register("minecraft:spruce_wood", -814);

    public static final BlockType SPYGLASS = register("minecraft:spyglass", 662);

    public static final BlockType STAINED_GLASS = register("minecraft:stained_glass", 808);

    public static final BlockType STAINED_GLASS_PANE = register("minecraft:stained_glass_pane", 809);

    public static final BlockType STAINED_HARDENED_CLAY = register("minecraft:stained_hardened_clay", 738);

    public static final BlockType STANDING_BANNER = register("minecraft:standing_banner", 176);

    public static final BlockType STANDING_SIGN = register("minecraft:standing_sign", 63);

    public static final BlockType STICK = register("minecraft:stick", 345);

    public static final BlockType STICKY_PISTON = register("minecraft:sticky_piston", 29);

    public static final BlockType STICKY_PISTON_ARM_COLLISION = register("minecraft:sticky_piston_arm_collision", -217);

    public static final BlockType STONE = register("minecraft:stone", 1);

    public static final BlockType STONE_AXE = register("minecraft:stone_axe", 339);

    public static final BlockType STONE_BLOCK_SLAB = register("minecraft:stone_block_slab", 785);

    public static final BlockType STONE_BLOCK_SLAB2 = register("minecraft:stone_block_slab2", 786);

    public static final BlockType STONE_BLOCK_SLAB3 = register("minecraft:stone_block_slab3", 787);

    public static final BlockType STONE_BLOCK_SLAB4 = register("minecraft:stone_block_slab4", 788);

    public static final BlockType STONE_BRICK_DOUBLE_SLAB = register("minecraft:stone_brick_double_slab", -881);

    public static final BlockType STONE_BRICK_SLAB = register("minecraft:stone_brick_slab", -875);

    public static final BlockType STONE_BRICK_STAIRS = register("minecraft:stone_brick_stairs", 109);

    public static final BlockType STONE_BRICK_WALL = register("minecraft:stone_brick_wall", -977);

    public static final BlockType STONE_BRICKS = register("minecraft:stone_bricks", 98);

    public static final BlockType STONE_BUTTON = register("minecraft:stone_button", 77);

    public static final BlockType STONE_HOE = register("minecraft:stone_hoe", 355);

    public static final BlockType STONE_PICKAXE = register("minecraft:stone_pickaxe", 338);

    public static final BlockType STONE_PRESSURE_PLATE = register("minecraft:stone_pressure_plate", 70);

    public static final BlockType STONE_SHOVEL = register("minecraft:stone_shovel", 337);

    public static final BlockType STONE_STAIRS = register("minecraft:stone_stairs", 67);

    public static final BlockType STONE_SWORD = register("minecraft:stone_sword", 336);

    public static final BlockType STONEBRICK = register("minecraft:stonebrick", 783);

    public static final BlockType STONECUTTER = register("minecraft:stonecutter", 245);

    public static final BlockType STONECUTTER_BLOCK = register("minecraft:stonecutter_block", -197);

    public static final BlockType STRING = register("minecraft:string", 351);

    public static final BlockType STRIPPED_ACACIA_LOG = register("minecraft:stripped_acacia_log", -8);

    public static final BlockType STRIPPED_ACACIA_WOOD = register("minecraft:stripped_acacia_wood", -823);

    public static final BlockType STRIPPED_BAMBOO_BLOCK = register("minecraft:stripped_bamboo_block", -528);

    public static final BlockType STRIPPED_BIRCH_LOG = register("minecraft:stripped_birch_log", -6);

    public static final BlockType STRIPPED_BIRCH_WOOD = register("minecraft:stripped_birch_wood", -821);

    public static final BlockType STRIPPED_CHERRY_LOG = register("minecraft:stripped_cherry_log", -535);

    public static final BlockType STRIPPED_CHERRY_WOOD = register("minecraft:stripped_cherry_wood", -545);

    public static final BlockType STRIPPED_CRIMSON_HYPHAE = register("minecraft:stripped_crimson_hyphae", -300);

    public static final BlockType STRIPPED_CRIMSON_STEM = register("minecraft:stripped_crimson_stem", -240);

    public static final BlockType STRIPPED_DARK_OAK_LOG = register("minecraft:stripped_dark_oak_log", -9);

    public static final BlockType STRIPPED_DARK_OAK_WOOD = register("minecraft:stripped_dark_oak_wood", -824);

    public static final BlockType STRIPPED_JUNGLE_LOG = register("minecraft:stripped_jungle_log", -7);

    public static final BlockType STRIPPED_JUNGLE_WOOD = register("minecraft:stripped_jungle_wood", -822);

    public static final BlockType STRIPPED_MANGROVE_LOG = register("minecraft:stripped_mangrove_log", -485);

    public static final BlockType STRIPPED_MANGROVE_WOOD = register("minecraft:stripped_mangrove_wood", -498);

    public static final BlockType STRIPPED_OAK_LOG = register("minecraft:stripped_oak_log", -10);

    public static final BlockType STRIPPED_OAK_WOOD = register("minecraft:stripped_oak_wood", -819);

    public static final BlockType STRIPPED_PALE_OAK_LOG = register("minecraft:stripped_pale_oak_log", -994);

    public static final BlockType STRIPPED_PALE_OAK_WOOD = register("minecraft:stripped_pale_oak_wood", -1004);

    public static final BlockType STRIPPED_SPRUCE_LOG = register("minecraft:stripped_spruce_log", -5);

    public static final BlockType STRIPPED_SPRUCE_WOOD = register("minecraft:stripped_spruce_wood", -820);

    public static final BlockType STRIPPED_WARPED_HYPHAE = register("minecraft:stripped_warped_hyphae", -301);

    public static final BlockType STRIPPED_WARPED_STEM = register("minecraft:stripped_warped_stem", -241);

    public static final BlockType STRUCTURE_BLOCK = register("minecraft:structure_block", 252);

    public static final BlockType STRUCTURE_VOID = register("minecraft:structure_void", 217);

    public static final BlockType SUGAR = register("minecraft:sugar", 442);

    public static final BlockType SUGAR_CANE = register("minecraft:sugar_cane", 411);

    public static final BlockType SUNFLOWER = register("minecraft:sunflower", 175);

    public static final BlockType SUSPICIOUS_GRAVEL = register("minecraft:suspicious_gravel", -573);

    public static final BlockType SUSPICIOUS_SAND = register("minecraft:suspicious_sand", -529);

    public static final BlockType SUSPICIOUS_STEW = register("minecraft:suspicious_stew", 625);

    public static final BlockType SWEET_BERRIES = register("minecraft:sweet_berries", 309);

    public static final BlockType SWEET_BERRY_BUSH = register("minecraft:sweet_berry_bush", -207);

    public static final BlockType TALL_DRY_GRASS = register("minecraft:tall_dry_grass", -1029);

    public static final BlockType TALL_GRASS = register("minecraft:tall_grass", -864);

    public static final BlockType TALLGRASS = register("minecraft:tallgrass", 803);

    public static final BlockType TARGET = register("minecraft:target", -239);

    public static final BlockType TINTED_GLASS = register("minecraft:tinted_glass", -334);

    public static final BlockType TNT = register("minecraft:tnt", 46);

    public static final BlockType TNT_MINECART = register("minecraft:tnt_minecart", 558);

    public static final BlockType TORCH = register("minecraft:torch", 50);

    public static final BlockType TORCHFLOWER = register("minecraft:torchflower", -568);

    public static final BlockType TORCHFLOWER_CROP = register("minecraft:torchflower_crop", -567);

    public static final BlockType TOTEM_OF_UNDYING = register("minecraft:totem_of_undying", 601);

    public static final BlockType TRAPDOOR = register("minecraft:trapdoor", 96);

    public static final BlockType TRAPPED_CHEST = register("minecraft:trapped_chest", 146);

    public static final BlockType TRIAL_SPAWNER = register("minecraft:trial_spawner", -315);

    public static final BlockType TRIDENT = register("minecraft:trident", 579);

    public static final BlockType TRIP_WIRE = register("minecraft:trip_wire", 132);

    public static final BlockType TRIPWIRE_HOOK = register("minecraft:tripwire_hook", 131);

    public static final BlockType TROPICAL_FISH = register("minecraft:tropical_fish", 288);

    public static final BlockType TUBE_CORAL = register("minecraft:tube_coral", -131);

    public static final BlockType TUBE_CORAL_BLOCK = register("minecraft:tube_coral_block", -132);

    public static final BlockType TUBE_CORAL_FAN = register("minecraft:tube_coral_fan", -133);

    public static final BlockType TUBE_CORAL_WALL_FAN = register("minecraft:tube_coral_wall_fan", -135);

    public static final BlockType TUFF = register("minecraft:tuff", -333);

    public static final BlockType TUFF_BRICK_DOUBLE_SLAB = register("minecraft:tuff_brick_double_slab", -756);

    public static final BlockType TUFF_BRICK_SLAB = register("minecraft:tuff_brick_slab", -755);

    public static final BlockType TUFF_BRICK_STAIRS = register("minecraft:tuff_brick_stairs", -757);

    public static final BlockType TUFF_BRICK_WALL = register("minecraft:tuff_brick_wall", -758);

    public static final BlockType TUFF_BRICKS = register("minecraft:tuff_bricks", -754);

    public static final BlockType TUFF_DOUBLE_SLAB = register("minecraft:tuff_double_slab", -745);

    public static final BlockType TUFF_SLAB = register("minecraft:tuff_slab", -744);

    public static final BlockType TUFF_STAIRS = register("minecraft:tuff_stairs", -746);

    public static final BlockType TUFF_WALL = register("minecraft:tuff_wall", -747);

    public static final BlockType TURTLE_EGG = register("minecraft:turtle_egg", -159);

    public static final BlockType TURTLE_HELMET = register("minecraft:turtle_helmet", 606);

    public static final BlockType TURTLE_SCUTE = register("minecraft:turtle_scute", 605);

    public static final BlockType TWISTING_VINES = register("minecraft:twisting_vines", -287);

    public static final BlockType UNDERWATER_TNT = register("minecraft:underwater_tnt", -985);

    public static final BlockType UNDERWATER_TORCH = register("minecraft:underwater_torch", 239);

    public static final BlockType UNDYED_SHULKER_BOX = register("minecraft:undyed_shulker_box", 205);

    public static final BlockType UNKNOWN = register("minecraft:unknown", -305);

    public static final BlockType UNLIT_REDSTONE_TORCH = register("minecraft:unlit_redstone_torch", 75);

    public static final BlockType UNPOWERED_COMPARATOR = register("minecraft:unpowered_comparator", 149);

    public static final BlockType UNPOWERED_REPEATER = register("minecraft:unpowered_repeater", 93);

    public static final BlockType VAULT = register("minecraft:vault", -314);

    public static final BlockType VERDANT_FROGLIGHT = register("minecraft:verdant_froglight", -470);

    public static final BlockType VINE = register("minecraft:vine", 106);

    public static final BlockType WALL_BANNER = register("minecraft:wall_banner", 177);

    public static final BlockType WALL_SIGN = register("minecraft:wall_sign", 68);

    public static final BlockType WARPED_BUTTON = register("minecraft:warped_button", -261);

    public static final BlockType WARPED_DOUBLE_SLAB = register("minecraft:warped_double_slab", -267);

    public static final BlockType WARPED_FENCE = register("minecraft:warped_fence", -257);

    public static final BlockType WARPED_FENCE_GATE = register("minecraft:warped_fence_gate", -259);

    public static final BlockType WARPED_FUNGUS = register("minecraft:warped_fungus", -229);

    public static final BlockType WARPED_FUNGUS_ON_A_STICK = register("minecraft:warped_fungus_on_a_stick", 655);

    public static final BlockType WARPED_HANGING_SIGN = register("minecraft:warped_hanging_sign", -507);

    public static final BlockType WARPED_HYPHAE = register("minecraft:warped_hyphae", -298);

    public static final BlockType WARPED_NYLIUM = register("minecraft:warped_nylium", -233);

    public static final BlockType WARPED_PLANKS = register("minecraft:warped_planks", -243);

    public static final BlockType WARPED_PRESSURE_PLATE = register("minecraft:warped_pressure_plate", -263);

    public static final BlockType WARPED_ROOTS = register("minecraft:warped_roots", -224);

    public static final BlockType WARPED_SIGN = register("minecraft:warped_sign", 652);

    public static final BlockType WARPED_SLAB = register("minecraft:warped_slab", -265);

    public static final BlockType WARPED_STAIRS = register("minecraft:warped_stairs", -255);

    public static final BlockType WARPED_STANDING_SIGN = register("minecraft:warped_standing_sign", -251);

    public static final BlockType WARPED_STEM = register("minecraft:warped_stem", -226);

    public static final BlockType WARPED_TRAPDOOR = register("minecraft:warped_trapdoor", -247);

    public static final BlockType WARPED_WALL_SIGN = register("minecraft:warped_wall_sign", -253);

    public static final BlockType WARPED_WART_BLOCK = register("minecraft:warped_wart_block", -227);

    public static final BlockType WATER = register("minecraft:water", 9);

    public static final BlockType WATERLILY = register("minecraft:waterlily", 111);

    public static final BlockType WAXED_CHISELED_COPPER = register("minecraft:waxed_chiseled_copper", -764);

    public static final BlockType WAXED_COPPER = register("minecraft:waxed_copper", -344);

    public static final BlockType WAXED_COPPER_BULB = register("minecraft:waxed_copper_bulb", -780);

    public static final BlockType WAXED_COPPER_DOOR = register("minecraft:waxed_copper_door", -788);

    public static final BlockType WAXED_COPPER_GRATE = register("minecraft:waxed_copper_grate", -772);

    public static final BlockType WAXED_COPPER_TRAPDOOR = register("minecraft:waxed_copper_trapdoor", -796);

    public static final BlockType WAXED_CUT_COPPER = register("minecraft:waxed_cut_copper", -351);

    public static final BlockType WAXED_CUT_COPPER_SLAB = register("minecraft:waxed_cut_copper_slab", -365);

    public static final BlockType WAXED_CUT_COPPER_STAIRS = register("minecraft:waxed_cut_copper_stairs", -358);

    public static final BlockType WAXED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:waxed_double_cut_copper_slab", -372);

    public static final BlockType WAXED_EXPOSED_CHISELED_COPPER = register("minecraft:waxed_exposed_chiseled_copper", -765);

    public static final BlockType WAXED_EXPOSED_COPPER = register("minecraft:waxed_exposed_copper", -345);

    public static final BlockType WAXED_EXPOSED_COPPER_BULB = register("minecraft:waxed_exposed_copper_bulb", -781);

    public static final BlockType WAXED_EXPOSED_COPPER_DOOR = register("minecraft:waxed_exposed_copper_door", -789);

    public static final BlockType WAXED_EXPOSED_COPPER_GRATE = register("minecraft:waxed_exposed_copper_grate", -773);

    public static final BlockType WAXED_EXPOSED_COPPER_TRAPDOOR = register("minecraft:waxed_exposed_copper_trapdoor", -797);

    public static final BlockType WAXED_EXPOSED_CUT_COPPER = register("minecraft:waxed_exposed_cut_copper", -352);

    public static final BlockType WAXED_EXPOSED_CUT_COPPER_SLAB = register("minecraft:waxed_exposed_cut_copper_slab", -366);

    public static final BlockType WAXED_EXPOSED_CUT_COPPER_STAIRS = register("minecraft:waxed_exposed_cut_copper_stairs", -359);

    public static final BlockType WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:waxed_exposed_double_cut_copper_slab", -373);

    public static final BlockType WAXED_OXIDIZED_CHISELED_COPPER = register("minecraft:waxed_oxidized_chiseled_copper", -766);

    public static final BlockType WAXED_OXIDIZED_COPPER = register("minecraft:waxed_oxidized_copper", -446);

    public static final BlockType WAXED_OXIDIZED_COPPER_BULB = register("minecraft:waxed_oxidized_copper_bulb", -783);

    public static final BlockType WAXED_OXIDIZED_COPPER_DOOR = register("minecraft:waxed_oxidized_copper_door", -791);

    public static final BlockType WAXED_OXIDIZED_COPPER_GRATE = register("minecraft:waxed_oxidized_copper_grate", -775);

    public static final BlockType WAXED_OXIDIZED_COPPER_TRAPDOOR = register("minecraft:waxed_oxidized_copper_trapdoor", -799);

    public static final BlockType WAXED_OXIDIZED_CUT_COPPER = register("minecraft:waxed_oxidized_cut_copper", -447);

    public static final BlockType WAXED_OXIDIZED_CUT_COPPER_SLAB = register("minecraft:waxed_oxidized_cut_copper_slab", -449);

    public static final BlockType WAXED_OXIDIZED_CUT_COPPER_STAIRS = register("minecraft:waxed_oxidized_cut_copper_stairs", -448);

    public static final BlockType WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:waxed_oxidized_double_cut_copper_slab", -450);

    public static final BlockType WAXED_WEATHERED_CHISELED_COPPER = register("minecraft:waxed_weathered_chiseled_copper", -767);

    public static final BlockType WAXED_WEATHERED_COPPER = register("minecraft:waxed_weathered_copper", -346);

    public static final BlockType WAXED_WEATHERED_COPPER_BULB = register("minecraft:waxed_weathered_copper_bulb", -782);

    public static final BlockType WAXED_WEATHERED_COPPER_DOOR = register("minecraft:waxed_weathered_copper_door", -790);

    public static final BlockType WAXED_WEATHERED_COPPER_GRATE = register("minecraft:waxed_weathered_copper_grate", -774);

    public static final BlockType WAXED_WEATHERED_COPPER_TRAPDOOR = register("minecraft:waxed_weathered_copper_trapdoor", -798);

    public static final BlockType WAXED_WEATHERED_CUT_COPPER = register("minecraft:waxed_weathered_cut_copper", -353);

    public static final BlockType WAXED_WEATHERED_CUT_COPPER_SLAB = register("minecraft:waxed_weathered_cut_copper_slab", -367);

    public static final BlockType WAXED_WEATHERED_CUT_COPPER_STAIRS = register("minecraft:waxed_weathered_cut_copper_stairs", -360);

    public static final BlockType WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:waxed_weathered_double_cut_copper_slab", -374);

    public static final BlockType WEATHERED_CHISELED_COPPER = register("minecraft:weathered_chiseled_copper", -762);

    public static final BlockType WEATHERED_COPPER = register("minecraft:weathered_copper", -342);

    public static final BlockType WEATHERED_COPPER_BULB = register("minecraft:weathered_copper_bulb", -778);

    public static final BlockType WEATHERED_COPPER_DOOR = register("minecraft:weathered_copper_door", -786);

    public static final BlockType WEATHERED_COPPER_GRATE = register("minecraft:weathered_copper_grate", -770);

    public static final BlockType WEATHERED_COPPER_TRAPDOOR = register("minecraft:weathered_copper_trapdoor", -794);

    public static final BlockType WEATHERED_CUT_COPPER = register("minecraft:weathered_cut_copper", -349);

    public static final BlockType WEATHERED_CUT_COPPER_SLAB = register("minecraft:weathered_cut_copper_slab", -363);

    public static final BlockType WEATHERED_CUT_COPPER_STAIRS = register("minecraft:weathered_cut_copper_stairs", -356);

    public static final BlockType WEATHERED_DOUBLE_CUT_COPPER_SLAB = register("minecraft:weathered_double_cut_copper_slab", -370);

    public static final BlockType WEB = register("minecraft:web", 30);

    public static final BlockType WEEPING_VINES = register("minecraft:weeping_vines", -231);

    public static final BlockType WET_SPONGE = register("minecraft:wet_sponge", -984);

    public static final BlockType WHEAT_SEEDS = register("minecraft:wheat_seeds", 313);

    public static final BlockType WHITE_CANDLE = register("minecraft:white_candle", -413);

    public static final BlockType WHITE_CANDLE_CAKE = register("minecraft:white_candle_cake", -430);

    public static final BlockType WHITE_CARPET = register("minecraft:white_carpet", 171);

    public static final BlockType WHITE_CONCRETE = register("minecraft:white_concrete", 236);

    public static final BlockType WHITE_CONCRETE_POWDER = register("minecraft:white_concrete_powder", 237);

    public static final BlockType WHITE_GLAZED_TERRACOTTA = register("minecraft:white_glazed_terracotta", 220);

    public static final BlockType WHITE_SHULKER_BOX = register("minecraft:white_shulker_box", 218);

    public static final BlockType WHITE_STAINED_GLASS = register("minecraft:white_stained_glass", 241);

    public static final BlockType WHITE_STAINED_GLASS_PANE = register("minecraft:white_stained_glass_pane", 160);

    public static final BlockType WHITE_TERRACOTTA = register("minecraft:white_terracotta", 159);

    public static final BlockType WHITE_TULIP = register("minecraft:white_tulip", -835);

    public static final BlockType WHITE_WOOL = register("minecraft:white_wool", 35);

    public static final BlockType WILDFLOWERS = register("minecraft:wildflowers", -1024);

    public static final BlockType WITHER_ROSE = register("minecraft:wither_rose", -216);

    public static final BlockType WITHER_SKELETON_SKULL = register("minecraft:wither_skeleton_skull", -965);

    public static final BlockType WOOD = register("minecraft:wood", 811);

    public static final BlockType WOODEN_AXE = register("minecraft:wooden_axe", 335);

    public static final BlockType WOODEN_BUTTON = register("minecraft:wooden_button", 143);

    public static final BlockType WOODEN_HOE = register("minecraft:wooden_hoe", 354);

    public static final BlockType WOODEN_PICKAXE = register("minecraft:wooden_pickaxe", 334);

    public static final BlockType WOODEN_PRESSURE_PLATE = register("minecraft:wooden_pressure_plate", 72);

    public static final BlockType WOODEN_SHOVEL = register("minecraft:wooden_shovel", 333);

    public static final BlockType WOODEN_SLAB = register("minecraft:wooden_slab", 798);

    public static final BlockType WOODEN_SWORD = register("minecraft:wooden_sword", 332);

    public static final BlockType WOOL = register("minecraft:wool", 779);

    public static final BlockType WRITABLE_BOOK = register("minecraft:writable_book", 544);

    public static final BlockType WRITTEN_BOOK = register("minecraft:written_book", 545);

    public static final BlockType YELLOW_CANDLE = register("minecraft:yellow_candle", -417);

    public static final BlockType YELLOW_CANDLE_CAKE = register("minecraft:yellow_candle_cake", -434);

    public static final BlockType YELLOW_CARPET = register("minecraft:yellow_carpet", -600);

    public static final BlockType YELLOW_CONCRETE = register("minecraft:yellow_concrete", -631);

    public static final BlockType YELLOW_CONCRETE_POWDER = register("minecraft:yellow_concrete_powder", -712);

    public static final BlockType YELLOW_GLAZED_TERRACOTTA = register("minecraft:yellow_glazed_terracotta", 224);

    public static final BlockType YELLOW_SHULKER_BOX = register("minecraft:yellow_shulker_box", -616);

    public static final BlockType YELLOW_STAINED_GLASS = register("minecraft:yellow_stained_glass", -676);

    public static final BlockType YELLOW_STAINED_GLASS_PANE = register("minecraft:yellow_stained_glass_pane", -646);

    public static final BlockType YELLOW_TERRACOTTA = register("minecraft:yellow_terracotta", -727);

    public static final BlockType YELLOW_WOOL = register("minecraft:yellow_wool", -558);

    public static final BlockType ZOMBIE_HEAD = register("minecraft:zombie_head", -966);

    public static BlockType register(String identifier, int runtimeId) {
        return register(new BlockTypeImpl(identifier, runtimeId));
    }

    public static BlockType register(BlockType blockType) {
        BlockType oldType = ID_TO_TYPE.get(blockType.getIdentifier());
        RUNTIME_TO_TYPE.putIfAbsent(blockType.getRuntimeId(), blockType);
        ID_TO_TYPE.putIfAbsent(blockType.getIdentifier(), blockType);
        ItemTypes.register(blockType.getIdentifier(), blockType.getRuntimeId());
        return oldType != null ? oldType : blockType;
    }

    public static BlockType get(String identifier) {
        return ID_TO_TYPE.get(identifier);
    }

    public static BlockType getFromRuntime(int runtimeId) {
        return RUNTIME_TO_TYPE.get(runtimeId);
    }

    @Data
    private static class BlockTypeImpl implements BlockType {
        private final String identifier;

        private final int runtimeId;
    }
}
