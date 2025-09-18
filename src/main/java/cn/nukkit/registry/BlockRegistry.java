package cn.nukkit.registry;

import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.block.customblock.CustomBlock;
import cn.nukkit.block.customblock.CustomBlockDefinition;
import cn.nukkit.block.customblock.CustomBlockUtil;
import cn.nukkit.block.customblock.VanillaPaletteUpdater;
import cn.nukkit.block.customblock.comparator.HashedPaletteComparator;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.exception.InvalidBlockPropertyMetaException;
import cn.nukkit.block.material.BlockTypes;
import cn.nukkit.block.material.CustomBlockType;
import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.level.BlockPalette;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.format.leveldb.LevelDBConstants;
import cn.nukkit.level.format.leveldb.NukkitLegacyMapper;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class BlockRegistry implements IRegistry<Integer, Block, Class<? extends Block>>, BlockID {

    private static Class<? extends Block>[] LIST = new Class[Block.MAX_BLOCK_ID];
    private static Block[] FULL_LIST = new Block[Block.MAX_BLOCK_ID * (1 << Block.DATA_BITS)];
    private static int[] LIGHT = new int[65536];
    private static int[] LIGHT_FILTER = new int[65536];
    private static boolean[] SOLID = new boolean[65536];
    private static boolean[] TRANSPARENT = new boolean[65536];
    private static boolean[] DIFFUSES_SKY_LIGHT = new boolean[65536];

    private static final List<CustomBlockDefinition> CUSTOM_BLOCK_DEFINITIONS = new ArrayList<>();
    private static final Int2ObjectMap<CustomBlock> ID_TO_CUSTOM_BLOCK = new Int2ObjectOpenHashMap<>();
    private static final ConcurrentHashMap<String, Integer> CUSTOM_BLOCK_ID_MAP = new ConcurrentHashMap<>();
    private static final Map<String, List<CustomBlockUtil.CustomBlockState>> LEGACY_2_CUSTOM_STATE = new HashMap<>();
    private final static SortedMap<String, CustomBlock> HASHED_SORTED_CUSTOM_BLOCK = new TreeMap<>(HashedPaletteComparator.INSTANCE);
    private final static BlockUnknown BLOCK_UNKNOWN = new BlockUnknown(Block.MAX_BLOCK_ID, 0);

    private static int nextBlockId = Block.LOWEST_CUSTOM_BLOCK_ID;
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(AIR, BlockAir.class); //0
        register(STONE, BlockStone.class); //1
        register(GRASS, BlockGrass.class); //2
        register(DIRT, BlockDirt.class); //3
        register(COBBLESTONE, BlockCobblestone.class); //4
        register(OAK_PLANKS, BlockOakPlanks.class); //5
        register(SAPLING, BlockSapling.class); //6
        register(BEDROCK, BlockBedrock.class); //7
        register(WATER, BlockWater.class); //8
        register(STILL_WATER, BlockWaterStill.class); //9
        register(LAVA, BlockLava.class); //10
        register(STILL_LAVA, BlockLavaStill.class); //11
        register(SAND, BlockSand.class); //12
        register(GRAVEL, BlockGravel.class); //13
        register(GOLD_ORE, BlockOreGold.class); //14
        register(IRON_ORE, BlockOreIron.class); //15
        register(COAL_ORE, BlockOreCoal.class); //16
        register(WOOD, BlockWood.class); //17
        register(LEAVES, BlockLeaves.class); //18
        register(SPONGE, BlockSponge.class); //19
        register(GLASS, BlockGlass.class); //20
        register(LAPIS_ORE, BlockOreLapis.class); //21
        register(LAPIS_BLOCK, BlockLapis.class); //22
        register(DISPENSER, BlockDispenser.class); //23
        register(SANDSTONE, BlockSandstone.class); //24
        register(NOTEBLOCK, BlockNoteblock.class); //25
        register(BED_BLOCK, BlockBed.class); //26
        register(POWERED_RAIL, BlockRailPowered.class); //27
        register(DETECTOR_RAIL, BlockRailDetector.class); //28
        register(STICKY_PISTON, BlockPistonSticky.class); //29
        register(COBWEB, BlockCobweb.class); //30
        register(TALL_GRASS, BlockTallGrass.class); //31
        register(DEAD_BUSH, BlockDeadBush.class); //32
        register(PISTON, BlockPiston.class); //33
        register(PISTON_HEAD, BlockPistonHead.class); //34
        register(WOOL, BlockWool.class); //35
        register(DANDELION, BlockDandelion.class); //37
        register(POPPY, BlockPoppy.class); //38
        register(BROWN_MUSHROOM, BlockMushroomBrown.class); //39
        register(RED_MUSHROOM, BlockMushroomRed.class); //40
        register(GOLD_BLOCK, BlockGold.class); //41
        register(IRON_BLOCK, BlockIron.class); //42
        register(DOUBLE_STONE_SLAB, BlockDoubleSlabStone.class); //43
        register(STONE_SLAB, BlockSlabStone.class); //44
        register(BRICKS_BLOCK, BlockBricks.class); //45
        register(TNT, BlockTNT.class); //46
        register(BOOKSHELF, BlockBookshelf.class); //47
        register(MOSS_STONE, BlockMossStone.class); //48
        register(OBSIDIAN, BlockObsidian.class); //49
        register(TORCH, BlockTorch.class); //50
        register(FIRE, BlockFire.class); //51
        register(MONSTER_SPAWNER, BlockMobSpawner.class); //52
        register(WOOD_STAIRS, BlockStairsWood.class); //53
        register(CHEST, BlockChest.class); //54
        register(REDSTONE_WIRE, BlockRedstoneWire.class); //55
        register(DIAMOND_ORE, BlockOreDiamond.class); //56
        register(DIAMOND_BLOCK, BlockDiamond.class); //57
        register(WORKBENCH, BlockCraftingTable.class); //58
        register(WHEAT_BLOCK, BlockWheat.class); //59
        register(FARMLAND, BlockFarmland.class); //60
        register(FURNACE, BlockFurnace.class); //61
        register(BURNING_FURNACE, BlockFurnaceBurning.class); //62
        register(SIGN_POST, BlockSignPost.class); //63
        register(WOOD_DOOR_BLOCK, BlockDoorWood.class); //64
        register(LADDER, BlockLadder.class); //65
        register(RAIL, BlockRail.class); //66
        register(COBBLESTONE_STAIRS, BlockStairsCobblestone.class); //67
        register(WALL_SIGN, BlockWallSign.class); //68
        register(LEVER, BlockLever.class); //69
        register(STONE_PRESSURE_PLATE, BlockPressurePlateStone.class); //70
        register(IRON_DOOR_BLOCK, BlockDoorIron.class); //71
        register(WOODEN_PRESSURE_PLATE, BlockPressurePlateWood.class); //72
        register(REDSTONE_ORE, BlockOreRedstone.class); //73
        register(GLOWING_REDSTONE_ORE, BlockOreRedstoneGlowing.class); //74
        register(UNLIT_REDSTONE_TORCH, BlockRedstoneTorchUnlit.class);
        register(REDSTONE_TORCH, BlockRedstoneTorch.class); //76
        register(STONE_BUTTON, BlockButtonStone.class); //77
        register(SNOW_LAYER, BlockSnowLayer.class); //78
        register(ICE, BlockIce.class); //79
        register(SNOW_BLOCK, BlockSnow.class); //80
        register(CACTUS, BlockCactus.class); //81
        register(CLAY_BLOCK, BlockClay.class); //82
        register(SUGARCANE_BLOCK, BlockSugarcane.class); //83
        register(JUKEBOX, BlockJukebox.class); //84
        register(FENCE, BlockFence.class); //85
        register(PUMPKIN, BlockPumpkin.class); //86
        register(NETHERRACK, BlockNetherrack.class); //87
        register(SOUL_SAND, BlockSoulSand.class); //88
        register(GLOWSTONE_BLOCK, BlockGlowstone.class); //89
        register(NETHER_PORTAL, BlockNetherPortal.class); //90
        register(LIT_PUMPKIN, BlockPumpkinLit.class); //91
        register(CAKE_BLOCK, BlockCake.class); //92
        register(UNPOWERED_REPEATER, BlockRedstoneRepeaterUnpowered.class); //93
        register(POWERED_REPEATER, BlockRedstoneRepeaterPowered.class); //94
        register(INVISIBLE_BEDROCK, BlockBedrockInvisible.class); //95
        register(TRAPDOOR, BlockTrapdoor.class); //96
        register(MONSTER_EGG, BlockMonsterEgg.class); //97
        register(STONE_BRICKS, BlockBricksStone.class); //98
        register(BROWN_MUSHROOM_BLOCK, BlockHugeMushroomBrown.class); //99
        register(RED_MUSHROOM_BLOCK, BlockHugeMushroomRed.class); //100
        register(IRON_BARS, BlockIronBars.class); //101
        register(GLASS_PANE, BlockGlassPane.class); //102
        register(MELON_BLOCK, BlockMelon.class); //103
        register(PUMPKIN_STEM, BlockStemPumpkin.class); //104
        register(MELON_STEM, BlockStemMelon.class); //105
        register(VINE, BlockVine.class); //106
        register(FENCE_GATE, BlockFenceGate.class); //107
        register(BRICK_STAIRS, BlockStairsBrick.class); //108
        register(STONE_BRICK_STAIRS, BlockStairsStoneBrick.class); //109
        register(MYCELIUM, BlockMycelium.class); //110
        register(WATER_LILY, BlockWaterLily.class); //111
        register(NETHER_BRICKS, BlockBricksNether.class); //112
        register(NETHER_BRICK_FENCE, BlockFenceNetherBrick.class); //113
        register(NETHER_BRICKS_STAIRS, BlockStairsNetherBrick.class); //114
        register(NETHER_WART_BLOCK, BlockNetherWart.class); //115
        register(ENCHANTING_TABLE, BlockEnchantingTable.class); //116
        register(BREWING_STAND_BLOCK, BlockBrewingStand.class); //117
        register(CAULDRON_BLOCK, BlockCauldron.class); //118
        register(END_PORTAL, BlockEndPortal.class); //119
        register(END_PORTAL_FRAME, BlockEndPortalFrame.class); //120
        register(END_STONE, BlockEndStone.class); //121
        register(DRAGON_EGG, BlockDragonEgg.class); //122
        register(REDSTONE_LAMP, BlockRedstoneLamp.class); //123
        register(LIT_REDSTONE_LAMP, BlockRedstoneLampLit.class); //124
        register(DROPPER, BlockDropper.class); //125
        register(ACTIVATOR_RAIL, BlockRailActivator.class); //126
        register(COCOA, BlockCocoa.class); //127
        register(SANDSTONE_STAIRS, BlockStairsSandstone.class); //128
        register(EMERALD_ORE, BlockOreEmerald.class); //129
        register(ENDER_CHEST, BlockEnderChest.class); //130
        register(TRIPWIRE_HOOK, BlockTripWireHook.class);
        register(TRIPWIRE, BlockTripWire.class); //132
        register(EMERALD_BLOCK, BlockEmerald.class); //133
        register(SPRUCE_WOOD_STAIRS, BlockStairsSpruce.class); //134
        register(BIRCH_WOOD_STAIRS, BlockStairsBirch.class); //135
        register(JUNGLE_WOOD_STAIRS, BlockStairsJungle.class); //136
        register(COMMAND_BLOCK, BlockCommandBlock.class); //137
        register(BEACON, BlockBeacon.class); //138
        register(COBBLESTONE_WALL, BlockCobblestoneWall.class); //139
        register(FLOWER_POT_BLOCK, BlockFlowerPot.class); //140
        register(CARROT_BLOCK, BlockCarrot.class); //141
        register(POTATO_BLOCK, BlockPotato.class); //142
        register(WOODEN_BUTTON, BlockButtonWooden.class); //143
        register(SKELETON_SKULL_BLOCK, BlockSkullSkeleton.class); //144
        register(ANVIL, BlockAnvil.class); //145
        register(TRAPPED_CHEST, BlockTrappedChest.class); //146
        register(LIGHT_WEIGHTED_PRESSURE_PLATE, BlockWeightedPressurePlateLight.class); //147
        register(HEAVY_WEIGHTED_PRESSURE_PLATE, BlockWeightedPressurePlateHeavy.class); //148
        register(UNPOWERED_COMPARATOR, BlockRedstoneComparatorUnpowered.class); //149
        register(POWERED_COMPARATOR, BlockRedstoneComparatorPowered.class); //149
        register(DAYLIGHT_DETECTOR, BlockDaylightDetector.class); //151
        register(REDSTONE_BLOCK, BlockRedstone.class); //152
        register(QUARTZ_ORE, BlockOreQuartz.class); //153
        register(HOPPER_BLOCK, BlockHopper.class); //154
        register(QUARTZ_BLOCK, BlockQuartz.class); //155
        register(QUARTZ_STAIRS, BlockStairsQuartz.class); //156
        register(DOUBLE_WOOD_SLAB, BlockDoubleSlabWood.class); //157
        register(WOOD_SLAB, BlockSlabWood.class); //158
        register(STAINED_TERRACOTTA, BlockTerracottaStained.class); //159
        register(STAINED_GLASS_PANE, BlockGlassPaneStained.class); //160
        register(LEAVES2, BlockLeaves2.class); //161
        register(WOOD2, BlockWood2.class); //162
        register(ACACIA_WOOD_STAIRS, BlockStairsAcacia.class); //163
        register(DARK_OAK_WOOD_STAIRS, BlockStairsDarkOak.class); //164
        register(SLIME_BLOCK, BlockSlime.class); //165
        register(GLOW_STICK, BlockGlowStick.class); //166
        register(IRON_TRAPDOOR, BlockTrapdoorIron.class); //167
        register(PRISMARINE, BlockPrismarine.class); //168
        register(SEA_LANTERN, BlockSeaLantern.class); //169
        register(HAY_BALE, BlockHayBale.class); //170
        register(CARPET, BlockCarpet.class); //171
        register(TERRACOTTA, BlockTerracotta.class); //172
        register(COAL_BLOCK, BlockCoal.class); //173
        register(PACKED_ICE, BlockIcePacked.class); //174
        register(DOUBLE_PLANT, BlockDoublePlant.class); //175
        register(STANDING_BANNER, BlockBanner.class); //176
        register(WALL_BANNER, BlockWallBanner.class); //177
        register(DAYLIGHT_DETECTOR_INVERTED, BlockDaylightDetectorInverted.class); //178
        register(RED_SANDSTONE, BlockRedSandstone.class); //179
        register(RED_SANDSTONE_STAIRS, BlockStairsRedSandstone.class); //180
        register(DOUBLE_RED_SANDSTONE_SLAB, BlockDoubleSlabRedSandstone.class); //181
        register(RED_SANDSTONE_SLAB, BlockSlabRedSandstone.class); //182
        register(FENCE_GATE_SPRUCE, BlockFenceGateSpruce.class); //183
        register(FENCE_GATE_BIRCH, BlockFenceGateBirch.class); //184
        register(FENCE_GATE_JUNGLE, BlockFenceGateJungle.class); //185
        register(FENCE_GATE_DARK_OAK, BlockFenceGateDarkOak.class); //186
        register(FENCE_GATE_ACACIA, BlockFenceGateAcacia.class); //187
        register(REPEATING_COMMAND_BLOCK, BlockCommandBlockRepeating.class); //188
        register(CHAIN_COMMAND_BLOCK, BlockCommandBlockChain.class); //189
        register(HARD_GLASS_PANE, BlockHardGlassPane.class); //190
        register(HARD_STAINED_GLASS_PANE, BlockHardGlassPaneStained.class); //191
        register(CHEMICAL_HEAT, BlockChemicalHeat.class); //192
        register(SPRUCE_DOOR_BLOCK, BlockDoorSpruce.class); //193
        register(BIRCH_DOOR_BLOCK, BlockDoorBirch.class); //194
        register(JUNGLE_DOOR_BLOCK, BlockDoorJungle.class); //195
        register(ACACIA_DOOR_BLOCK, BlockDoorAcacia.class); //196
        register(DARK_OAK_DOOR_BLOCK, BlockDoorDarkOak.class); //197
        register(GRASS_PATH, BlockGrassPath.class); //198
        register(ITEM_FRAME_BLOCK, BlockItemFrame.class); //199
        register(CHORUS_FLOWER, BlockChorusFlower.class); //200
        register(PURPUR_BLOCK, BlockPurpur.class); //201
        register(COLORED_TORCH_RG, BlockColoredTorchRG.class); //202
        register(PURPUR_STAIRS, BlockStairsPurpur.class); //203
        register(COLORED_TORCH_BP, BlockColoredTorchBP.class); //204
        register(UNDYED_SHULKER_BOX, BlockUndyedShulkerBox.class); //205
        register(END_BRICKS, BlockBricksEndStone.class); //206
        register(FROSTED_ICE, BlockIceFrosted.class); //207
        register(END_ROD, BlockEndRod.class); //208
        register(END_GATEWAY, BlockEndGateway.class); //209
        // 210 Allow in Education Edition
        // 211 Deny in Education Edition
        register(BORDER_BLOCK, BlockBorder.class); //212
        register(MAGMA, BlockMagma.class); //213
        register(BLOCK_NETHER_WART_BLOCK, BlockNetherWartBlock.class); //214
        register(RED_NETHER_BRICK, BlockBricksRedNether.class); //215
        register(BONE_BLOCK, BlockBone.class); //216
        register(STRUCTURE_VOID, BlockStructureVoid.class); //217
        register(SHULKER_BOX, BlockShulkerBox.class); //218
        register(PURPLE_GLAZED_TERRACOTTA, BlockTerracottaGlazedPurple.class); //219
        register(WHITE_GLAZED_TERRACOTTA, BlockTerracottaGlazedWhite.class); //220
        register(ORANGE_GLAZED_TERRACOTTA, BlockTerracottaGlazedOrange.class); //221
        register(MAGENTA_GLAZED_TERRACOTTA, BlockTerracottaGlazedMagenta.class); //222
        register(LIGHT_BLUE_GLAZED_TERRACOTTA, BlockTerracottaGlazedLightBlue.class); //223
        register(YELLOW_GLAZED_TERRACOTTA, BlockTerracottaGlazedYellow.class); //224
        register(LIME_GLAZED_TERRACOTTA, BlockTerracottaGlazedLime.class); //225
        register(PINK_GLAZED_TERRACOTTA, BlockTerracottaGlazedPink.class); //226
        register(GRAY_GLAZED_TERRACOTTA, BlockTerracottaGlazedGray.class); //227
        register(SILVER_GLAZED_TERRACOTTA, BlockTerracottaGlazedSilver.class); //228
        register(CYAN_GLAZED_TERRACOTTA, BlockTerracottaGlazedCyan.class); //229
        // 230 Chalkboard in Education Edition
        register(BLUE_GLAZED_TERRACOTTA, BlockTerracottaGlazedBlue.class); //231
        register(BROWN_GLAZED_TERRACOTTA, BlockTerracottaGlazedBrown.class); //232
        register(GREEN_GLAZED_TERRACOTTA, BlockTerracottaGlazedGreen.class); //233
        register(RED_GLAZED_TERRACOTTA, BlockTerracottaGlazedRed.class); //234
        register(BLACK_GLAZED_TERRACOTTA, BlockTerracottaGlazedBlack.class); //235
        register(CONCRETE, BlockConcrete.class); //236
        register(CONCRETE_POWDER, BlockConcretePowder.class); //237
        register(CHEMISTRY_TABLE, BlockChemistryTable.class); //238
        register(UNDERWATER_TORCH, BlockUnderwaterTorch.class); //239
        register(CHORUS_PLANT, BlockChorusPlant.class); //240
        register(STAINED_GLASS, BlockGlassStained.class); //241
        register(CAMERA_BLOCK, BlockCamera.class); //242
        register(PODZOL, BlockPodzol.class); //243
        register(BEETROOT_BLOCK, BlockBeetroot.class); //244
        register(STONECUTTER, BlockStonecutter.class); //244
        register(GLOWING_OBSIDIAN, BlockObsidianGlowing.class); //246
        register(NETHER_REACTOR, BlockNetherReactor.class); //247
        register(INFO_UPDATE, BlockInfoUpdate.class); //248
        register(INFO_UPDATE2, BlockInfoUpdate2.class); //249
        register(PISTON_EXTENSION, BlockPistonExtension.class); //250
        register(OBSERVER, BlockObserver.class); //251
        register(STRUCTURE_BLOCK, BlockStructureBlock.class); //252
        register(HARD_GLASS, BlockHardGlass.class); //253
        register(HARD_STAINED_GLASS, BlockHardGlassStained.class); //254
        register(RESERVED6, BlockReserved6.class); //255
        // 256 not yet in Minecraft
        register(PRISMARINE_STAIRS, BlockStairsPrismarine.class); //257
        register(DARK_PRISMARINE_STAIRS, BlockStairsDarkPrismarine.class); //258
        register(PRISMARINE_BRICKS_STAIRS, BlockStairsPrismarineBrick.class); //259
        register(STRIPPED_SPRUCE_LOG, BlockWoodStrippedSpruce.class); //260
        register(STRIPPED_BIRCH_LOG, BlockWoodStrippedBirch.class); //261
        register(STRIPPED_JUNGLE_LOG, BlockWoodStrippedJungle.class); //262
        register(STRIPPED_ACACIA_LOG, BlockWoodStrippedAcacia.class); //263
        register(STRIPPED_DARK_OAK_LOG, BlockWoodStrippedDarkOak.class); //264
        register(STRIPPED_OAK_LOG, BlockWoodStrippedOak.class); //265
        register(BLUE_ICE, BlockBlueIce.class); //266

        register(SEAGRASS, BlockSeagrass.class); //385
        register(CORAL, BlockCoral.class); //386
        register(CORAL_BLOCK, BlockCoralBlock.class); //387
        register(CORAL_FAN, BlockCoralFan.class); //388
        register(CORAL_FAN_DEAD, BlockCoralFanDead.class); //389
        register(CORAL_FAN_HANG, BlockCoralFanHang.class); //390
        register(CORAL_FAN_HANG2, BlockCoralFanHang2.class); //391
        register(CORAL_FAN_HANG3, BlockCoralFanHang3.class); //392
        register(BLOCK_KELP, BlockKelp.class); //393
        register(DRIED_KELP_BLOCK, BlockDriedKelpBlock.class); //394
        register(ACACIA_BUTTON, BlockButtonAcacia.class); //395
        register(BIRCH_BUTTON, BlockButtonBirch.class); //396
        register(DARK_OAK_BUTTON, BlockButtonDarkOak.class); //397
        register(JUNGLE_BUTTON, BlockButtonJungle.class); //398
        register(SPRUCE_BUTTON, BlockButtonSpruce.class); //399
        register(ACACIA_TRAPDOOR, BlockTrapdoorAcacia.class); //400
        register(BIRCH_TRAPDOOR, BlockTrapdoorBirch.class); //401
        register(DARK_OAK_TRAPDOOR, BlockTrapdoorDarkOak.class); //402
        register(JUNGLE_TRAPDOOR, BlockTrapdoorJungle.class); //403
        register(SPRUCE_TRAPDOOR, BlockTrapdoorSpruce.class); //404
        register(ACACIA_PRESSURE_PLATE, BlockPressurePlateAcacia.class); //405
        register(BIRCH_PRESSURE_PLATE, BlockPressurePlateBirch.class); //406
        register(DARK_OAK_PRESSURE_PLATE, BlockPressurePlateDarkOak.class); //407
        register(JUNGLE_PRESSURE_PLATE, BlockPressurePlateJungle.class); //408
        register(SPRUCE_PRESSURE_PLATE, BlockPressurePlateSpruce.class); //409
        register(CARVED_PUMPKIN, BlockCarvedPumpkin.class); //410
        register(SEA_PICKLE, BlockSeaPickle.class); //411
        register(CONDUIT, BlockConduit.class); //412
        // 413 not yet in Minecraft
        register(TURTLE_EGG, BlockTurtleEgg.class); //414
        register(BUBBLE_COLUMN, BlockBubbleColumn.class); //415
        register(BARRIER, BlockBarrier.class); //416
        register(STONE_SLAB3, BlockSlabStone3.class); //417
        register(BAMBOO, BlockBamboo.class); //418
        register(BAMBOO_SAPLING, BlockBambooSapling.class); //419
        register(SCAFFOLDING, BlockScaffolding.class); //420
        register(STONE_SLAB4, BlockSlabStone4.class); //421
        register(DOUBLE_STONE_SLAB3, BlockDoubleSlabStone3.class); //422
        register(DOUBLE_STONE_SLAB4, BlockDoubleSlabStone4.class); //423
        register(GRANITE_STAIRS, BlockStairsGranite.class); //424
        register(DIORITE_STAIRS, BlockStairsDiorite.class); //425
        register(ANDESITE_STAIRS, BlockStairsAndesite.class); //426
        register(POLISHED_GRANITE_STAIRS, BlockStairsGranitePolished.class); //427
        register(POLISHED_DIORITE_STAIRS, BlockStairsDioritePolished.class); //428
        register(POLISHED_ANDESITE_STAIRS, BlockStairsAndesitePolished.class); //429
        register(MOSSY_STONE_BRICK_STAIRS, BlockStairsMossyStoneBrick.class); //430
        register(SMOOTH_RED_SANDSTONE_STAIRS, BlockStairsSmoothRedSandstone.class); //431
        register(SMOOTH_SANDSTONE_STAIRS, BlockStairsSmoothSandstone.class); //432
        register(END_BRICK_STAIRS, BlockStairsEndBrick.class); //433
        register(MOSSY_COBBLESTONE_STAIRS, BlockStairsMossyCobblestone.class); //434
        register(NORMAL_STONE_STAIRS, BlockStairsStone.class); //435
        register(SPRUCE_STANDING_SIGN, BlockSpruceSignPost.class); //436
        register(SPRUCE_WALL_SIGN, BlockSpruceWallSign.class); //437
        register(SMOOTH_STONE, BlockSmoothStone.class); //438
        register(RED_NETHER_BRICK_STAIRS, BlockStairsRedNetherBrick.class); //439
        register(SMOOTH_QUARTZ_STAIRS, BlockStairsSmoothQuartz.class); //440
        register(BIRCH_STANDING_SIGN, BlockBirchSignPost.class); //441
        register(BIRCH_WALL_SIGN, BlockBirchWallSign.class); //442
        register(JUNGLE_STANDING_SIGN, BlockJungleSignPost.class); //443
        register(JUNGLE_WALL_SIGN, BlockJungleWallSign.class); //444
        register(ACACIA_STANDING_SIGN, BlockAcaciaSignPost.class); //445
        register(ACACIA_WALL_SIGN, BlockAcaciaWallSign.class); //446
        register(DARKOAK_STANDING_SIGN, BlockDarkOakSignPost.class); //447
        register(DARKOAK_WALL_SIGN, BlockDarkOakWallSign.class); //448
        register(LECTERN, BlockLectern.class); //449
        register(GRINDSTONE, BlockGrindstone.class); //450
        register(BLAST_FURNACE, BlockBlastFurnace.class); //451
        register(STONECUTTER_BLOCK, BlockStonecutterBlock.class); //452
        register(SMOKER, BlockSmoker.class); //453
        register(LIT_SMOKER, BlockSmokerLit.class); //454
        register(CARTOGRAPHY_TABLE, BlockCartographyTable.class); //455
        register(FLETCHING_TABLE, BlockFletchingTable.class); //456
        register(SMITHING_TABLE, BlockSmithingTable.class); //457
        register(BARREL, BlockBarrel.class); //458
        register(LOOM, BlockLoom.class); //459

        register(BELL, BlockBell.class); //461
        register(SWEET_BERRY_BUSH, BlockSweetBerryBush.class); //462
        register(LANTERN, BlockLantern.class); //463
        register(CAMPFIRE_BLOCK, BlockCampfire.class); //464
        register(LAVA_CAULDRON, BlockCauldronLava.class); //465
        register(WOOD_BARK, BlockWoodBark.class); //467
        register(COMPOSTER, BlockComposter.class); //468
        register(LIT_BLAST_FURNACE, BlockBlastFurnaceLit.class); //469
        register(LIGHT_BLOCK, BlockLightBlock.class); //470
        register(WITHER_ROSE, BlockWitherRose.class); //471
        register(PISTON_HEAD_STICKY, BlockPistonHeadSticky.class); //472
        register(BEE_NEST, BlockBeeNest.class); //473
        register(BEEHIVE, BlockBeehive.class); //474
        register(HONEY_BLOCK, BlockHoneyBlock.class); //475
        register(HONEYCOMB_BLOCK, BlockHoneycombBlock.class); //476
        register(LODESTONE, BlockLodestone.class); //477
        register(CRIMSON_ROOTS, BlockRootsCrimson.class); //478
        register(WARPED_ROOTS, BlockRootsWarped.class); //479
        register(CRIMSON_STEM, BlockStemCrimson.class); //480
        register(WARPED_STEM, BlockStemWarped.class); //481
        register(WARPED_WART_BLOCK, BlockWarpedWartBlock.class); //482
        register(CRIMSON_FUNGUS, BlockFungusCrimson.class); //483
        register(WARPED_FUNGUS, BlockFungusWarped.class); //484
        register(SHROOMLIGHT, BlockShroomlight.class); //485
        register(WEEPING_VINES, BlockWeepingVines.class); //486
        register(CRIMSON_NYLIUM, BlockNyliumCrimson.class); //487
        register(WARPED_NYLIUM, BlockNyliumWarped.class); //488
        register(BASALT, BlockBasalt.class); //489
        register(POLISHED_BASALT, BlockPolishedBasalt.class); //490
        register(SOUL_SOIL, BlockSoulSoil.class); //491
        register(SOUL_FIRE, BlockSoulFire.class); //492
        register(NETHER_SPROUTS_BLOCK, BlockNetherSprouts.class); //493
        register(TARGET, BlockTarget.class); //494
        register(STRIPPED_CRIMSON_STEM, BlockStemStrippedCrimson.class); //495
        register(STRIPPED_WARPED_STEM, BlockStemStrippedWarped.class); //496
        register(CRIMSON_PLANKS, BlockPlanksCrimson.class); //497
        register(WARPED_PLANKS, BlockPlanksWarped.class); //498
        register(CRIMSON_DOOR_BLOCK, BlockDoorCrimson.class); //499
        register(WARPED_DOOR_BLOCK, BlockDoorWarped.class); //500
        register(CRIMSON_TRAPDOOR, BlockTrapdoorCrimson.class); //501
        register(WARPED_TRAPDOOR, BlockTrapdoorWarped.class); //502

        register(CRIMSON_STANDING_SIGN, BlockCrimsonSignPost.class); //505
        register(WARPED_STANDING_SIGN, BlockWarpedSignPost.class); //506
        register(CRIMSON_WALL_SIGN, BlockCrimsonWallSign.class); //507
        register(WARPED_WALL_SIGN, BlockWarpedWallSign.class); //508
        register(CRIMSON_STAIRS, BlockStairsCrimson.class); //509
        register(WARPED_STAIRS, BlockStairsWarped.class); //510
        register(CRIMSON_FENCE, BlockFenceCrimson.class); //511
        register(WARPED_FENCE, BlockFenceWarped.class); //512
        register(CRIMSON_FENCE_GATE, BlockFenceGateCrimson.class); //513
        register(WARPED_FENCE_GATE, BlockFenceGateWarped.class); //514
        register(CRIMSON_BUTTON, BlockButtonCrimson.class); //515
        register(WARPED_BUTTON, BlockButtonWarped.class); //516
        register(CRIMSON_PRESSURE_PLATE, BlockPressurePlateCrimson.class); //517
        register(WARPED_PRESSURE_PLATE, BlockPressurePlateWarped.class); //518
        register(CRIMSON_SLAB, BlockSlabCrimson.class); //519
        register(WARPED_SLAB, BlockSlabWarped.class); //520
        register(CRIMSON_DOUBLE_SLAB, BlockDoubleSlabCrimson.class); //521
        register(WARPED_DOUBLE_SLAB, BlockDoubleSlabWarped.class); //522
        register(SOUL_TORCH, BlockSoulTorch.class); //523
        register(SOUL_LANTERN, BlockSoulLantern.class); //524
        register(NETHERITE_BLOCK, BlockNetheriteBlock.class); //525
        register(ANCIENT_DEBRIS, BlockAncientDebris.class); //526
        register(RESPAWN_ANCHOR, BlockRespawnAnchor.class); //527
        register(BLACKSTONE, BlockBlackstone.class); //528
        register(POLISHED_BLACKSTONE_BRICKS, BlockBricksBlackstonePolished.class); //529
        register(POLISHED_BLACKSTONE_BRICK_STAIRS, BlockStairsBrickBlackstonePolished.class); //530
        register(BLACKSTONE_STAIRS, BlockStairsBlackstone.class); //531
        register(BLACKSTONE_WALL, BlockWallBlackstone.class); //532
        register(POLISHED_BLACKSTONE_BRICK_WALL, BlockWallBrickBlackstonePolished.class); //533
        register(CHISELED_POLISHED_BLACKSTONE, BlockBlackstonePolishedChiseled.class); //534
        register(CRACKED_POLISHED_BLACKSTONE_BRICKS, BlockBricksBlackstonePolishedCracked.class); //535
        register(GILDED_BLACKSTONE, BlockBlackstoneGilded.class); //536
        register(BLACKSTONE_SLAB, BlockSlabBlackstone.class); //537
        register(BLACKSTONE_DOUBLE_SLAB, BlockDoubleSlabBlackstone.class); //538
        register(POLISHED_BLACKSTONE_BRICK_SLAB, BlockSlabBrickBlackstonePolished.class); //539
        register(POLISHED_BLACKSTONE_BRICK_DOUBLE_SLAB, BlockDoubleSlabBrickBlackstonePolished.class); //540
        register(CHAIN_BLOCK, BlockChain.class); //541
        register(TWISTING_VINES, BlockVinesTwisting.class); //542
        register(NETHER_GOLD_ORE, BlockOreGoldNether.class); //543
        register(CRYING_OBSIDIAN, BlockCryingObsidian.class); //544
        register(SOUL_CAMPFIRE_BLOCK, BlockCampfireSoul.class); //545
        register(POLISHED_BLACKSTONE, BlockBlackstonePolished.class); //546
        register(POLISHED_BLACKSTONE_STAIRS, BlockStairsBlackstonePolished.class); //547
        register(POLISHED_BLACKSTONE_SLAB, BlockSlabBlackstonePolished.class); //548
        register(POLISHED_BLACKSTONE_DOUBLE_SLAB, BlockDoubleSlabBlackstonePolished.class); //549
        register(POLISHED_BLACKSTONE_PRESSURE_PLATE, BlockPressurePlateBlackstonePolished.class); //550
        register(POLISHED_BLACKSTONE_BUTTON, BlockButtonBlackstonePolished.class); //551
        register(POLISHED_BLACKSTONE_WALL, BlockWallBlackstonePolished.class); //552
        register(WARPED_HYPHAE, BlockHyphaeWarped.class); //553
        register(CRIMSON_HYPHAE, BlockHyphaeCrimson.class); //554
        register(STRIPPED_CRIMSON_HYPHAE, BlockHyphaeStrippedCrimson.class); //555
        register(STRIPPED_WARPED_HYPHAE, BlockHyphaeStrippedWarped.class); //556
        register(CHISELED_NETHER_BRICKS, BlockBricksNetherChiseled.class); //557
        register(CRACKED_NETHER_BRICKS, BlockBricksNetherCracked.class); //558
        register(QUARTZ_BRICKS, BlockBricksQuartz.class); //559

        register(POWDER_SNOW, BlockPowderSnow.class); // 561
        register(SCULK_SENSOR, BlockSculkSensor.class); // 562
        register(POINTED_DRIPSTONE, BlockPointedDripstone.class); // 563

        register(COPPER_ORE, BlockOreCopper.class); //566
        register(LIGHTNING_ROD, BlockLightningRod.class); //567

        register(HEAVY_CORE, BlockHeavyCore.class); //571
        register(DRIPSTONE_BLOCK, BlockDripstone.class); //572
        register(ROOTED_DIRT, BlockDirtRooted.class); //573
        register(HANGING_ROOTS, BlockRootsHanging.class); //574
        register(MOSS_BLOCK, BlockMoss.class); //575
        register(SPORE_BLOSSOM, BlockSporeBlossom.class); //576
        register(CAVE_VINES, BlockCaveVines.class); //577
        register(BIG_DRIPLEAF, BlockDripleafBig.class); //578
        register(AZALEA_LEAVES, BlockAzaleaLeaves.class); //579
        register(AZALEA_LEAVES_FLOWERED, BlockAzaleaLeavesFlowered.class); //580
        register(CALCITE, BlockCalcite.class); //581
        register(AMETHYST_BLOCK, BlockAmethyst.class); //582
        register(BUDDING_AMETHYST, BlockBuddingAmethyst.class); //583
        register(AMETHYST_CLUSTER, BlockAmethystCluster.class); //584
        register(LARGE_AMETHYST_BUD, BlockAmethystBudLarge.class); //585
        register(MEDIUM_AMETHYST_BUD, BlockAmethystBudMedium.class); //586
        register(SMALL_AMETHYST_BUD, BlockAmethystBudSmall.class); //587
        register(TUFF, BlockTuff.class); //588
        register(TINTED_GLASS, BlockGlassTinted.class); //589
        register(MOSS_CARPET, BlockMossCarpet.class); //590
        register(SMALL_DRIPLEAF, BlockDripleafSmall.class); //591
        register(AZALEA, BlockAzalea.class); //592
        register(FLOWERING_AZALEA, BlockAzaleaFlowering.class); //593
        register(GLOW_FRAME, BlockItemFrameGlow.class); //594
        register(COPPER_BLOCK, BlockCopper.class); //595
        register(EXPOSED_COPPER, BlockCopperExposed.class); //596
        register(WEATHERED_COPPER, BlockCopperWeathered.class); //597
        register(OXIDIZED_COPPER, BlockCopperOxidized.class); //598
        register(WAXED_COPPER, BlockCopperWaxed.class); //599
        register(WAXED_EXPOSED_COPPER, BlockCopperExposedWaxed.class); //600
        register(WAXED_WEATHERED_COPPER, BlockCopperWeatheredWaxed.class); //601
        register(CUT_COPPER, BlockCopperCut.class); //602
        register(EXPOSED_CUT_COPPER, BlockCopperCutExposed.class); //603
        register(WEATHERED_CUT_COPPER, BlockCopperCutWeathered.class); //604
        register(OXIDIZED_CUT_COPPER, BlockCopperCutOxidized.class); //605
        register(WAXED_CUT_COPPER, BlockCopperCutWaxed.class); //606
        register(WAXED_EXPOSED_CUT_COPPER, BlockCopperCutExposedWaxed.class); //607
        register(WAXED_WEATHERED_CUT_COPPER, BlockCopperCutWeatheredWaxed.class); //608
        register(CUT_COPPER_STAIRS, BlockStairsCutCopper.class); //609
        register(EXPOSED_CUT_COPPER_STAIRS, BlockStairsCutCopperExposed.class); //610
        register(WEATHERED_CUT_COPPER_STAIRS, BlockStairsCutCopperWeathered.class); //611
        register(OXIDIZED_CUT_COPPER_STAIRS, BlockStairsCutCopperOxidized.class); //612
        register(WAXED_CUT_COPPER_STAIRS, BlockStairsCutCopperWaxed.class); //613
        register(WAXED_EXPOSED_CUT_COPPER_STAIRS, BlockStairsCutCopperExposedWaxed.class); //614
        register(WAXED_WEATHERED_CUT_COPPER_STAIRS, BlockStairsCutCopperWeatheredWaxed.class); //615
        register(CUT_COPPER_SLAB, BlockSlabCutCopper.class); //616
        register(EXPOSED_CUT_COPPER_SLAB, BlockSlabExposedCutCopper.class); //617
        register(WEATHERED_CUT_COPPER_SLAB, BlockSlabWeatheredCutCopper.class); //618
        register(OXIDIZED_CUT_COPPER_SLAB, BlockSlabOxidizedCutCopper.class); //619
        register(WAXED_CUT_COPPER_SLAB, BlockSlabCutCopperWaxed.class); //620
        register(WAXED_EXPOSED_CUT_COPPER_SLAB, BlockSlabExposedCutCopperWaxed.class); //621
        register(WAXED_WEATHERED_CUT_COPPER_SLAB, BlockSlabWeatheredCutCopperWaxed.class); //622
        register(DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabCutCopper.class); //623
        register(EXPOSED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabExposedCutCopper.class); //624
        register(WEATHERED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabWeatheredCutCopper.class); //625
        register(OXIDIZED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabOxidizedCutCopper.class); //626
        register(WAXED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabCutCopperWaxed.class); //627
        register(WAXED_EXPOSED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabExposedCutCopperWaxed.class); //628
        register(WAXED_WEATHERED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabWeatheredCutCopperWaxed.class); //629

        register(CAVE_VINES_BODY_WITH_BERRIES, BlockCaveVinesBerriesBody.class); //630
        register(CAVE_VINES_HEAD_WITH_BERRIES, BlockCaveVinesBerriesHead.class); //631
        register(SMOOTH_BASALT, BlockBasaltSmooth.class); //632
        register(DEEPSLATE, BlockDeepslate.class); //633
        register(COBBLED_DEEPSLATE, BlockDeepslateCobbled.class); //634
        register(COBBLED_DEEPSLATE_SLAB, BlockSlabDeepslateCobbled.class); //635
        register(COBBLED_DEEPSLATE_STAIRS, BlockStairsDeepslateCobbled.class); //636
        register(COBBLED_DEEPSLATE_WALL, BlockWallDeepslateCobbled.class); //637
        register(POLISHED_DEEPSLATE, BlockDeepslatePolished.class); //638
        register(POLISHED_DEEPSLATE_SLAB, BlockSlabDeepslatePolished.class); //639
        register(POLISHED_DEEPSLATE_STAIRS, BlockStairsDeepslatePolished.class); //640
        register(POLISHED_DEEPSLATE_WALL, BlockWallDeepslatePolished.class); //641
        register(DEEPSLATE_TILES, BlockTilesDeepslate.class); //642
        register(DEEPSLATE_TILE_SLAB, BlockSlabDeepslateTile.class); //643
        register(DEEPSLATE_TILE_STAIRS, BlockStairsDeepslateTile.class); //644
        register(DEEPSLATE_TILE_WALL, BlockWallDeepslateTile.class); //645
        register(DEEPSLATE_BRICKS, BlockBricksDeepslate.class); //646
        register(DEEPSLATE_BRICK_SLAB, BlockSlabDeepslateBrick.class); //647
        register(DEEPSLATE_BRICK_STAIRS, BlockStairsDeepslateBrick.class); //648
        register(DEEPSLATE_BRICK_WALL, BlockWallDeepslateBrick.class); //649
        register(CHISELED_DEEPSLATE, BlockDeepslateChiseled.class); //650
        register(COBBLED_DEEPSLATE_DOUBLE_SLAB, BlockDoubleSlabDeepslateCobbled.class); //651
        register(POLISHED_DEEPSLATE_DOUBLE_SLAB, BlockDoubleSlabDeepslatePolished.class); //652
        register(DEEPSLATE_TILE_DOUBLE_SLAB, BlockDoubleSlabDeepslateTile.class); //653
        register(DEEPSLATE_BRICK_DOUBLE_SLAB, BlockDoubleSlabDeepslateBrick.class); //654
        register(DEEPSLATE_LAPIS_ORE, BlockDeepslateLapisOre.class); // 655
        register(DEEPSLATE_IRON_ORE, BlockDeepslateIronOre.class); // 656
        register(DEEPSLATE_GOLD_ORE, BlockDeepslateGoldOre.class); // 657
        register(DEEPSLATE_REDSTONE_ORE, BlockDeepslateRedstoneOre.class); // 658
        register(LIT_DEEPSLATE_REDSTONE_ORE, BlockLitDeepslateRedstoneOre.class); // 659
        register(DEEPSLATE_DIAMOND_ORE, BlockDeepslateDiamondOre.class); // 660
        register(DEEPSLATE_COAL_ORE, BlockDeepslateCoalOre.class); // 661
        register(DEEPSLATE_EMERALD_ORE, BlockDeepslateEmeraldOre.class); // 662
        register(DEEPSLATE_COPPER_ORE, BlockDeepslateCopperOre.class); // 663
        register(CRACKED_DEEPSLATE_TILES, BlockTilesDeepslateCracked.class); //664
        register(CRACKED_DEEPSLATE_BRICKS, BlockBricksDeepslateCracked.class); //665
        register(GLOW_LICHEN, BlockGlowLichen.class); //666
        register(CANDLE, BlockCandle.class); //667
        register(WHITE_CANDLE, BlockCandleWhite.class); //668
        register(ORANGE_CANDLE, BlockCandleOrange.class); //669
        register(MAGENTA_CANDLE, BlockCandleMagenta.class); //670
        register(LIGHT_BLUE_CANDLE, BlockCandleLightBlue.class); //671
        register(YELLOW_CANDLE, BlockCandleYellow.class); //672
        register(LIME_CANDLE, BlockCandleLime.class); //673
        register(PINK_CANDLE, BlockCandlePink.class); //674
        register(GRAY_CANDLE, BlockCandleGray.class); //675
        register(LIGHT_GRAY_CANDLE, BlockCandleLightGray.class); //676
        register(CYAN_CANDLE, BlockCandleCyan.class); //677
        register(PURPLE_CANDLE, BlockCandlePurple.class); //678
        register(BLUE_CANDLE, BlockCandleBlue.class); //679
        register(BROWN_CANDLE, BlockCandleBrown.class); //680
        register(GREEN_CANDLE, BlockCandleGreen.class); //681
        register(RED_CANDLE, BlockCandleRed.class); //682
        register(BLACK_CANDLE, BlockCandleBlack.class); //683
        register(CANDLE_CAKE, BlockCandleCake.class); //684
        register(WHITE_CANDLE_CAKE, BlockCandleCakeWhite.class); //685
        register(ORANGE_CANDLE_CAKE, BlockCandleCakeOrange.class); //686
        register(MAGENTA_CANDLE_CAKE, BlockCandleCakeMagenta.class); //687
        register(LIGHT_BLUE_CANDLE_CAKE, BlockCandleCakeLightBlue.class); //688
        register(YELLOW_CANDLE_CAKE, BlockCandleCakeYellow.class); //689
        register(LIME_CANDLE_CAKE, BlockCandleCakeLime.class); //690
        register(PINK_CANDLE_CAKE, BlockCandleCakePink.class); //691
        register(GRAY_CANDLE_CAKE, BlockCandleCakeGray.class); //692
        register(LIGHT_GRAY_CANDLE_CAKE, BlockCandleCakeLightGray.class); //693
        register(CYAN_CANDLE_CAKE, BlockCandleCakeCyan.class); //694
        register(PURPLE_CANDLE_CAKE, BlockCandleCakePurple.class); //695
        register(BLUE_CANDLE_CAKE, BlockCandleCakeBlue.class); //696
        register(BROWN_CANDLE_CAKE, BlockCandleCakeBrown.class); //697
        register(GREEN_CANDLE_CAKE, BlockCandleCakeGreen.class); //698
        register(RED_CANDLE_CAKE, BlockCandleCakeRed.class); //699
        register(BLACK_CANDLE_CAKE, BlockCandleCakeBlack.class); //700
        register(WAXED_OXIDIZED_COPPER, BlockCopperOxidizedWaxed.class); //701
        register(WAXED_OXIDIZED_CUT_COPPER, BlockCopperCutOxidizedWaxed.class); //702
        register(WAXED_OXIDIZED_CUT_COPPER_STAIRS, BlockStairsCutCopperOxidizedWaxed.class); //703
        register(WAXED_OXIDIZED_CUT_COPPER_SLAB, BlockSlabOxidizedCutCopper.class); //704
        register(WAXED_OXIDIZED_DOUBLE_CUT_COPPER_SLAB, BlockDoubleSlabOxidizedCutCopper.class); //705

        register(RAW_IRON_BLOCK, BlockRawIron.class); //706
        register(RAW_COPPER_BLOCK, BlockRawCopper.class); //707
        register(RAW_GOLD_BLOCK, BlockRawGold.class); //708
        register(INFESTED_DEEPSLATE, BlockInfestedDeepslate.class); //709

        register(SCULK, BlockSculk.class); //713
        register(SCULK_VEIN, BlockSculkVein.class); //714
        register(SCULK_CATALYST, BlockSculkCatalyst.class); //715
        register(SCULK_SHRIEKER, BlockSculkShrieker.class); //716

        register(REINFORCED_DEEPSLATE, BlockReinforcedDeeplsate.class); //721

        register(FROG_SPAWN, BlockFrogSpawn.class); //723
        register(PEARLESCENT_FROGLIGHT, BlockFrogLightPearlescent.class); //724
        register(VERDANT_FROGLIGHT, BlockFrogLightVerdant.class); //725
        register(OCHRE_FROGLIGHT, BlockFrogLightOchre.class); //726
        register(MANGROVE_LEAVES, BlockLeavesMangrove.class); //727
        register(MUD, BlockMud.class); //728
        register(MANGROVE_PROPAGULE, BlockMangrovePropagule.class); //729
        register(MUD_BRICKS, BlockMudBricks.class); //730

        register(PACKED_MUD, BlockPackedMud.class); //732
        register(MUD_BRICK_SLAB, BlockSlabMudBrick.class); //733
        register(MUD_BRICK_DOUBLE_SLAB, BlockDoubleSlabMudBrick.class); //734
        register(MUD_BRICK_STAIRS, BlockStairsMudBrick.class); //735
        register(MUD_BRICK_WALL, BlockWallMudBrick.class); //736
        register(MANGROVE_ROOTS, BlockMangroveRoots.class); //737
        register(MUDDY_MANGROVE_ROOTS, BlockMangroveRootsMuddy.class); //738
        register(MANGROVE_LOG, BlockLogMangrove.class); //739
        register(STRIPPED_MANGROVE_LOG, BlockLogStrippedMangrove.class); //740
        register(MANGROVE_PLANKS, BlockPlanksMangrove.class); //741
        register(MANGROVE_BUTTON, BlockButtonMangrove.class); //742
        register(MANGROVE_STAIRS, BlockStairsMangrove.class); //743
        register(MANGROVE_SLAB, BlockSlabMangrove.class); //744
        register(MANGROVE_PRESSURE_PLATE, BlockPressurePlateMangrove.class); //745
        register(MANGROVE_FENCE, BlockFenceMangrove.class); //746
        register(MANGROVE_FENCE_GATE, BlockFenceGateMangrove.class); //747
        register(MANGROVE_DOOR_BLOCK, BlockDoorMangrove.class); //748
        register(MANGROVE_STANDING_SIGN, BlockMangroveSignPost.class); //749
        register(MANGROVE_WALL_SIGN, BlockMangroveWallSign.class); //750
        register(MANGROVE_TRAPDOOR, BlockTrapdoorMangrove.class); //751
        register(MANGROVE_WOOD, BlockWoodMangrove.class); //752
        register(STRIPPED_MANGROVE_WOOD, BlockWoodStrippedMangrove.class); //753
        register(MANGROVE_DOUBLE_SLAB, BlockDoubleSlabMangrove.class); //754
        register(OAK_HANGING_SIGN, BlockOakHangingSign.class); //755
        register(SPRUCE_HANGING_SIGN, BlockSpruceHangingSign.class); //756
        register(BIRCH_HANGING_SIGN, BlockBirchHangingSign.class); //757
        register(JUNGLE_HANGING_SIGN, BlockJungleHangingSign.class); //758
        register(ACACIA_HANGING_SIGN, BlockAcaciaHangingSign.class); //759
        register(DARK_OAK_HANGING_SIGN, BlockDarkOakHangingSign.class); //760
        register(CRIMSON_HANGING_SIGN, BlockCrimsonHangingSign.class); //761
        register(WARPED_HANGING_SIGN, BlockWarpedHangingSign.class); //762
        register(MANGROVE_HANGING_SIGN, BlockMangroveHangingSign.class); //763
        register(BAMBOO_MOSAIC, BlockBambooMosaic.class); //764
        register(BAMBOO_PLANKS, BlockPlanksBamboo.class); //765
        register(BAMBOO_BUTTON, BlockButtonBamboo.class); //766
        register(BAMBOO_STAIRS, BlockStairsBamboo.class); //777
        register(BAMBOO_SLAB, BlockSlabBamboo.class); //768
        register(BAMBOO_PRESSURE_PLATE, BlockPressurePlateBamboo.class); //769
        register(BAMBOO_FENCE, BlockFenceBamboo.class); //770
        register(BAMBOO_FENCE_GATE, BlockFenceGateBamboo.class); //771
        register(BAMBOO_DOOR, BlockDoorBamboo.class); //772
        register(BAMBOO_STANDING_SIGN, BlockBambooSignPost.class); //773
        register(BAMBOO_WALL_SIGN, BlockBambooWallSign.class); //774
        register(BAMBOO_TRAPDOOR, BlockTrapdoorBamboo.class); //775
        register(BAMBOO_DOUBLE_SLAB, BlockDoubleSlabBamboo.class); //776
        register(BAMBOO_HANGING_SIGN, BlockBambooHangingSign.class); //777
        register(BAMBOO_MOSAIC_STAIRS, BlockStairsBambooMosaic.class); //778
        register(BAMBOO_MOSAIC_SLAB, BlockSlabBambooMosaic.class); //779
        register(BAMBOO_MOSAIC_DOUBLE_SLAB, BlockDoubleSlabBambooMosaic.class); //780
        register(CHISELED_BOOKSHELF, BlockChiseledBookshelf.class); //781
        register(BAMBOO_BLOCK, BlockBambooBlock.class); //782
        register(STRIPPED_BAMBOO_BLOCK, BlockStrippedBambooBlock.class); //783
        register(SUSPICIOUS_SAND, BlockSuspiciousSand.class); //784
        register(CHERRY_BUTTON, BlockButtonCherry.class); //785
        register(CHERRY_DOOR, BlockDoorCherry.class); //786
        register(CHERRY_FENCE, BlockFenceCherry.class); //787
        register(CHERRY_FENCE_GATE, BlockFenceGateCherry.class); //788
        register(CHERRY_HANGING_SIGN, BlockCherryHangingSign.class); //789
        register(STRIPPED_CHERRY_LOG, BlockLogStrippedCherry.class); //790
        register(CHERRY_LOG, BlockCherryLog.class); //791
        register(CHERRY_PLANKS, BlockPlanksCherry.class); //792
        register(CHERRY_PRESSURE_PLATE, BlockPressurePlateCherry.class); //793
        register(CHERRY_SLAB, BlockSlabCherry.class); //794
        register(CHERRY_DOUBLE_SLAB, BlockDoubleSlabCherry.class); //795
        register(CHERRY_STAIRS, BlockStairsCherry.class); //796
        register(CHERRY_STANDING_SIGN, BlockCherrySignPost.class); //797
        register(CHERRY_TRAPDOOR, BlockTrapdoorCherry.class); //798
        register(CHERRY_WALL_SIGN, BlockCherryWallSign.class); //799
        register(STRIPPED_CHERRY_WOOD, BlockWoodStrippedCherry.class); //800
        register(CHERRY_WOOD, BlockWoodCherry.class); //801
        register(CHERRY_SAPLING, BlockCherrySapling.class); //802
        register(CHERRY_LEAVES, BlockCherryLeaves.class); //803
        register(PINK_PETALS, BlockPinkPetals.class); //804

        register(DECORATED_POT, BlockDecoratedPot.class); //806

        register(TORCHFLOWER_CROP, BlockTorchflowerCrop.class); //822
        register(TORCHFLOWER, BlockTorchflower.class); //823

        register(SUSPICIOUS_GRAVEL, BlockSuspiciousGravel.class); //828
        register(PITCHER_CROP, BlockPitcherCrop.class); //829

        register(CALIBRATED_SCULK_SENSOR, BlockCalibratedSculkSensor.class); //835

        register(SNIFFER_EGG, BlockSnifferEgg.class); //851

        register(PITCHER_PLANT, BlockPitcherPlant.class); //867

        register(SPRUCE_PLANKS, BlockSprucePlanks.class); //994
        register(BIRCH_PLANKS, BlockBirchPlanks.class); ///995
        register(JUNGLE_PLANKS, BlockJunglePlanks.class); //996
        register(ACACIA_PLANKS, BlockAcaciaPlanks.class); //997
        register(DARK_OAK_PLANKS, BlockDarkOakPlanks.class); //998

        register(TUFF_SLAB, BlockSlabTuff.class); //999
        register(TUFF_DOUBLE_SLAB, BlockDoubleSlabTuff.class); //1000
        register(TUFF_STAIRS, BlockStairsTuff.class); //1001
        register(TUFF_WALL, BlockTuffWall.class); //1002
        register(POLISHED_TUFF, BlockPolishedTuff.class); //1003
        register(POLISHED_TUFF_SLAB, BlockSlabTuffPolished.class); //1004
        register(POLISHED_TUFF_DOUBLE_SLAB, BlockDoubleSlabTuffPolished.class); //1005
        register(POLISHED_TUFF_STAIRS, BlockStairsTuffPolished.class); //1006
        register(POLISHED_TUFF_WALL, BlockPolishedTuffWall.class); //1007
        register(CHISELED_TUFF, BlockChiseledTuff.class); //1008
        register(TUFF_BRICKS, BlockTuffBricks.class); //1009
        register(TUFF_BRICK_SLAB, BlockSlabTuffBrick.class); //1010
        register(TUFF_BRICK_DOUBLE_SLAB, BlockDoubleSlabTuffBrick.class); //1011
        register(TUFF_BRICK_STAIRS, BlockStairsTuffBrick.class); //1012
        register(TUFF_BRICK_WALL, BlockTuffBrickWall.class); //1013
        register(CHISELED_TUFF_BRICKS, BlockChiseledTuffBricks.class); //1014
        register(CHISELED_COPPER, BlockChiseledCopper.class); //1015
        register(EXPOSED_CHISELED_COPPER, BlockExposedChiseledCopper.class); //1016
        register(WEATHERED_CHISELED_COPPER, BlockWeatheredChiseledCopper.class); //1017
        register(OXIDIZED_CHISELED_COPPER, BlockOxidizedChiseledCopper.class); //1018
        register(WAXED_CHISELED_COPPER, BlockWaxedChiseledCopper.class); //1019
        register(WAXED_EXPOSED_CHISELED_COPPER, BlockWaxedExposedChiseledCopper.class); //1020
        register(WAXED_WEATHERED_CHISELED_COPPER, BlockWaxedWeatheredChiseledCopper.class); //1021
        register(WAXED_OXIDIZED_CHISELED_COPPER, BlockWaxedOxidizedChiseledCopper.class); //1022
        register(COPPER_GRATE, BlockCopperGrate.class); //1023
        register(EXPOSED_COPPER_GRATE, BlockExposedCopperGrate.class); //1024
        register(WEATHERED_COPPER_GRATE, BlockWeatheredCopperGrate.class); //1025
        register(OXIDIZED_COPPER_GRATE, BlockOxidizedCopperGrate.class); //1026
        register(WAXED_COPPER_GRATE, BlockWaxedCopperGrate.class); //1027
        register(WAXED_EXPOSED_COPPER_GRATE, BlockWaxedExposedCopperGrate.class); //1028
        register(WAXED_WEATHERED_COPPER_GRATE, BlockWaxedWeatheredCopperGrate.class); //1029
        register(WAXED_OXIDIZED_COPPER_GRATE, BlockWaxedOxidizedCopperGrate.class); //1030
        register(COPPER_BULB, BlockCopperBulb.class); //1031
        register(EXPOSED_COPPER_BULB, BlockExposedCopperBulb.class); //1032
        register(WEATHERED_COPPER_BULB, BlockWeatheredCopperBulb.class); //1033
        register(OXIDIZED_COPPER_BULB, BlockOxidizedCopperBulb.class); //1034
        register(WAXED_COPPER_BULB, BlockWaxedCopperBulb.class); //1035
        register(WAXED_EXPOSED_COPPER_BULB, BlockWaxedExposedCopperBulb.class); //1036
        register(WAXED_WEATHERED_COPPER_BULB, BlockWaxedWeatheredCopperBulb.class); //1037
        register(WAXED_OXIDIZED_COPPER_BULB, BlockWaxedOxidizedCopperBulb.class); //1038
        register(COPPER_DOOR, BlockCopperDoor.class); //1039
        register(EXPOSED_COPPER_DOOR, BlockExposedCopperDoor.class); //1040
        register(WEATHERED_COPPER_DOOR, BlockWeatheredCopperDoor.class); //1041
        register(OXIDIZED_COPPER_DOOR, BlockOxidizedCopperDoor.class); //1042
        register(WAXED_COPPER_DOOR, BlockWaxedCopperDoor.class); //1043
        register(WAXED_EXPOSED_COPPER_DOOR, BlockWaxedExposedCopperDoor.class); //1044
        register(WAXED_WEATHERED_COPPER_DOOR, BlockWaxedWeatheredCopperDoor.class); //1045
        register(WAXED_OXIDIZED_COPPER_DOOR, BlockWaxedOxidizedCopperDoor.class); //1046
        register(COPPER_TRAPDOOR, BlockCopperTrapdoor.class); //1047
        register(EXPOSED_COPPER_TRAPDOOR, BlockExposedCopperTrapdoor.class); //1048
        register(WEATHERED_COPPER_TRAPDOOR, BlockWeatheredCopperTrapdoor.class); //1049
        register(OXIDIZED_COPPER_TRAPDOOR, BlockOxidizedCopperTrapdoor.class); //1050
        register(WAXED_COPPER_TRAPDOOR, BlockWaxedCopperTrapdoor.class); //1051
        register(WAXED_EXPOSED_COPPER_TRAPDOOR, BlockWaxedExposedCopperTrapdoor.class); //1052
        register(WAXED_WEATHERED_COPPER_TRAPDOOR, BlockWaxedWeatheredCopperTrapdoor.class); //1053
        register(WAXED_OXIDIZED_COPPER_TRAPDOOR, BlockWaxedOxidizedCopperTrapdoor.class); //1054

        register(BLUE_ORCHID, BlockBlueOrchid.class); //1085
        register(ALLIUM, BlockAllium.class); //1086
        register(AZURE_BLUET, BlockAzureBluet.class); //1087
        register(RED_TULIP, BlockRedTulip.class); //1088
        register(ORANGE_TULIP, BlockOrangeTulip.class); //1089
        register(WHITE_TULIP, BlockWhiteTulip.class); //1090
        register(PINK_TULIP, BlockPinkTulip.class); //1091
        register(OXEYE_DAISY, BlockOxeyeDaisy.class); //1092
        register(CORNFLOWER, BlockCornflower.class); //1093
        register(LILY_OF_THE_VALLEY, BlockLilyOfTheValley.class); //1094

        register(CHIPPED_ANVIL, BlockChippedAnvil.class); //1214
        register(DAMAGED_ANVIL, BlockDamagedAnvil.class); //1215
        register(DEPRECATED_ANVIL, BlockDeprecatedAnvil.class); //1216

        register(WITHER_SKELETON_SKULL, BlockSkullWitherSkeleton.class); //1220
        register(ZOMBIE_HEAD, BlockHeadZombie.class); //1221
        register(PLAYER_HEAD, BlockHeadPlayer.class); //1222
        register(CREEPER_HEAD, BlockHeadCreeper.class); //1223
        register(DRAGON_HEAD, BlockHeadDragon.class); //1224
        register(PIGLIN_HEAD, BlockHeadPiglin.class); //1225
        register(MOSSY_COBBLESTONE_WALL, BlockMossyCobblestoneWall.class); //1226
        register(GRANITE_WALL, BlockGraniteWall.class); //1227
        register(DIORITE_WALL, BlockDioriteWall.class); //1228
        register(ANDESITE_WALL, BlockAndesiteWall.class); //1229
        register(SANDSTONE_WALL, BlockSandstoneWall.class); //1230
        register(BRICK_WALL, BlockBrickWall.class); //1231
        register(STONE_BRICK_WALL, BlockStoneBrickWall.class); //1232
        register(MOSSY_STONE_BRICK_WALL, BlockMossyStoneBrickWall.class); //1233
        register(NETHER_BRICK_WALL, BlockNetherBrickWall.class); //1234
        register(ENDSTONE_BRICK_WALL, BlockEndstoneBrickWall.class); //1235
        register(PRISMARINE_WALL, BlockPrismarineWall.class); //1236
        register(RED_SANDSTONE_WALL, BlockRedSandstoneBlock.class); //1237
        register(RED_NETHER_BRICK_WALL, BlockRedNetherBrickWall.class); //1238

        register(PALE_OAK_BUTTON, BlockButtonPaleOak.class); //1244
        register(PALE_OAK_DOOR, BlockDoorPaleOak.class); //1245
        register(PALE_OAK_FENCE, BlockFencePaleOak.class); //1246
        register(PALE_OAK_FENCE_GATE, BlockFenceGatePaleOak.class); //1247
        register(PALE_OAK_HANGING_SIGN, BlockPaleOakHangingSign.class); //1248
        register(STRIPPED_PALE_OAK_LOG, BlockLogStrippedPaleOak.class); //1249
        register(PALE_OAK_LOG, BlockLogPaleOak.class); //1250
        register(PALE_OAK_PLANKS, BlockPlanksPaleOak.class); //1251
        register(PALE_OAK_PRESSURE_PLATE, BlockPressurePlatePaleOak.class); //1252
        register(PALE_OAK_SLAB, BlockSlabPaleOak.class); //1253
        register(PALE_OAK_DOUBLE_SLAB, BlockDoubleSlabPaleOak.class); //1254
        register(PALE_OAK_STAIRS, BlockStairsPaleOak.class); //1255
        register(PALE_OAK_STANDING_SIGN, BlockPaleOakSignPost.class); //1256
        register(PALE_OAK_TRAPDOOR, BlockTrapdoorPaleOak.class); //1257
        register(PALE_OAK_WALL_SIGN, BlockPaleOakWallSign.class); //1258
        register(STRIPPED_PALE_OAK_WOOD, BlockWoodStrippedPaleOak.class); //1259
        register(PALE_OAK_WOOD, BlockWoodPaleOak.class); //1260
        register(PALE_OAK_SAPLING, BlockPaleOakSapling.class); //1261
        register(PALE_OAK_LEAVES, BlockLeavesPaleOak.class); //1262

        register(PALE_MOSS_BLOCK, BlockPaleMoss.class); //1264
        register(PALE_MOSS_CARPET, BlockPaleMossCarpet.class); //1265
        register(PALE_HANGING_MOSS, BlockPaleHangingMoss.class); //1266
        register(CREAKING_HEART, BlockCreakingHeart.class); //1267
        register(RESIN_BRICKS, BlockResinBricks.class); //1268
        register(RESIN_BRICK_SLAB, BlockSlabResinBrick.class); //1269
        register(RESIN_BRICK_DOUBLE_SLAB, BlockDoubleSlabResinBrick.class); //1270
        register(RESIN_BRICK_STAIRS, BlockStairsResinBricks.class); //1271
        register(RESIN_BRICK_WALL, BlockResinBrickWall.class); //1272
        register(OPEN_EYEBLOSSOM, BlockOpenEyeblossom.class); //1273
        register(CLOSED_EYEBLOSSOM, BlockClosedEyeblossom.class); //1274
        register(CHISELED_RESIN_BRICKS, BlockChiseledResinBricks.class); //1275
        register(RESIN_BLOCK, BlockResinBlock.class); //1276
        register(RESIN_CLUMP, BlockResinClump.class); //1277
        register(BUSH, BlockBush.class); //1278
        register(WILDFLOWERS, BlockWildflowers.class); //1279
        register(FIREFLY_BUSH, BlockFireflyBush.class); //1280
        register(LEAF_LITTER, BlockLeafLitter.class); //1281
        register(DRIED_GHAST, BlockDriedGhast.class); //1282
        register(SHORT_DRY_GRASS, BlockShortDryGrass.class); //1283
        register(TALL_DRY_GRASS, BlockTallDryGrass.class); //1284
        register(CACTUS_FLOWER, BlockCactusFlower.class); //1285

        IntStream idStream = IntStream.range(0, Block.MAX_BLOCK_ID);
        idStream.parallel().forEach(id -> {
            Class<?> c = LIST[id];
            if (c != null) {
                Block block;
                try {
                    block = (Block) c.getDeclaredConstructor().newInstance();
                    try {
                        @SuppressWarnings("rawtypes")
                        Constructor constructor = c.getDeclaredConstructor(int.class);
                        constructor.setAccessible(true);
                        for (int data = 0; data < (1 << Block.DATA_BITS); ++data) {
                            int fullId = (id << Block.DATA_BITS) | data;
                            Block b;
                            try {
                                b = (Block) constructor.newInstance(data);
                                if (b.getDamage() != data) {
                                    b = BLOCK_UNKNOWN;
                                }
                            } catch (Exception e) {
                                Server.getInstance().getLogger().error("Error while registering " + c.getName(), e);
                                b = BLOCK_UNKNOWN;
                            }
                            FULL_LIST[fullId] = b;
                        }
                    } catch (NoSuchMethodException ignore) {
                        for (int data = 0; data < Block.DATA_SIZE; ++data) {
                            int fullId = (id << Block.DATA_BITS) | data;
                            FULL_LIST[fullId] = block;
                        }
                    }
                } catch (Exception e) {
                    Server.getInstance().getLogger().error("Error while registering " + c.getName(), e);
                    for (int data = 0; data < Block.DATA_SIZE; ++data) {
                        FULL_LIST[(id << Block.DATA_BITS) | data] = BLOCK_UNKNOWN;
                    }
                    return;
                }

                SOLID[id] = block.isSolid();
                TRANSPARENT[id] = block.isTransparent();
                DIFFUSES_SKY_LIGHT[id] = block.diffusesSkyLight();
                LIGHT[id] = block.getLightLevel();

                if (block.isSolid()) {
                    if (block.isTransparent()) {
                        if (block instanceof BlockLiquid || block instanceof BlockIce) {
                            LIGHT_FILTER[id] = 2;
                        } else {
                            LIGHT_FILTER[id] = 1;
                        }
                    } else if (block instanceof BlockSlime) {
                        LIGHT_FILTER[id] = 1;
                    } else if (id == CAULDRON_BLOCK) {
                        LIGHT_FILTER[id] = 3;
                    } else {
                        LIGHT_FILTER[id] = 15;
                    }
                } else {
                    LIGHT_FILTER[id] = 1;
                }
            } else {
                LIGHT_FILTER[id] = 1;
                for (int data = 0; data < Block.DATA_SIZE; ++data) {
                    FULL_LIST[(id << Block.DATA_BITS) | data] = BLOCK_UNKNOWN;
                }
            }
        });
    }

    public void initCustomBlocks() {
        if (!HASHED_SORTED_CUSTOM_BLOCK.isEmpty()) {
            VanillaPaletteUpdater.updateAllProtocols();

            for (var entry : HASHED_SORTED_CUSTOM_BLOCK.entrySet()) {
                final CustomBlock customBlock = entry.getValue();
                final BlockProperties properties = customBlock.getBlockProperties();

                final String identifier = customBlock.getIdentifier();
                final int id = nextBlockId++;

                CUSTOM_BLOCK_ID_MAP.put(entry.getKey(), id);//自定义方块标识符->自定义方块id
                ID_TO_CUSTOM_BLOCK.put(id, customBlock);//自定义方块id->自定义方块
                CUSTOM_BLOCK_DEFINITIONS.add(customBlock.getDefinition());//行为包数据

                CustomBlockUtil.generateVariants(properties, properties.getNames().toArray(new String[0]))
                        .forEach(states -> {
                            int meta = 0;

                            for (String name : states.keySet()) {
                                meta = properties.setValue(meta, name, states.get(name));
                            }

                            final int itemId = 255 - id;
                            for (RuntimeItemMapping mapping : RuntimeItems.VALUES) {
                                mapping.registerCustomBlockItem(customBlock.getIdentifier(), itemId, meta);
                                Registries.ITEM.addToCustom(customBlock.getIdentifier(), Item.get(itemId, meta));
                            }

                            CustomBlockUtil.CustomBlockState state;
                            try {
                                state = CustomBlockUtil.createBlockState(identifier, (id << Block.DATA_BITS) | meta, properties, customBlock);
                            } catch (InvalidBlockPropertyMetaException e) {
                                //log.error(e);
                                return; // Nukkit has more states than our block
                            }

                            BlockRegistry.LEGACY_2_CUSTOM_STATE.computeIfAbsent(identifier, (key) -> new ArrayList<>()).add(state);
                        });
            }

            final BlockPalette storagePalette = GlobalBlockPalette.getPaletteByProtocol(LevelDBConstants.PALETTE_VERSION);
            final ObjectSet<BlockPalette> set = new ObjectArraySet<>();

            for (int protocol : ProtocolInfo.SUPPORTED_PROTOCOLS) {
                if (protocol < Server.getInstance().getSettings().general().multiversion().minProtocol()) {
                    continue;
                }

                BlockPalette palette = GlobalBlockPalette.getPaletteByProtocol(protocol);
                if (set.contains(palette)) {
                    continue;
                }
                set.add(palette);

                if (palette.getProtocol() == storagePalette.getProtocol()) {
                    CustomBlockUtil.recreateBlockPalette(palette, new ObjectArrayList<>(NukkitLegacyMapper.loadBlockPalette()));
                } else {
                    Path path = CustomBlockUtil.getVanillaPalettePath(palette.getProtocol());
                    if (!Files.exists(path)) {
                        //log.warn("No vanilla palette found for {}.", Utils.getVersionByProtocol(palette.getProtocol()));
                        continue;
                    }
                    try {
                        CustomBlockUtil.recreateBlockPalette(palette);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            ID_TO_CUSTOM_BLOCK.forEach((id, block) -> {
                LIGHT[id] = block.getLightLevel();
                LIGHT_FILTER[id] = block.getLightFilter();
                SOLID[id] = ((Block) block).isSolid();
                TRANSPARENT[id] = ((Block) block).isTransparent();
                DIFFUSES_SKY_LIGHT[id] = ((Block) block).diffusesSkyLight();

                // Registering custom block type
                BlockTypes.register(new CustomBlockType(block));

                if (block.shouldBeRegisteredInCreative()) {
                    Registries.CREATIVE_ITEM.register(Item.get(block.getIdentifier()));
                }
            });
        }
    }

    @Override
    public void register(Integer key, Class<? extends Block> value) {
        LIST[key] = value;
    }

    public void registerCustom(@NotNull List<Class<? extends CustomBlock>> blockClassList) {
        for (Class<? extends CustomBlock> blockClass : blockClassList) {
            registerCustom(blockClass);
        }
    }

    public void registerCustom(@NotNull Class<? extends CustomBlock> clazz) {
        if (!Server.getInstance().getSettings().features().enableExperimentMode()) {
            throw new RegisterException("The server does not have the experiment mode feature enabled.Unable to register custom block!");
        }

        CustomBlock block;
        try {
            var method = clazz.getDeclaredConstructor();
            method.setAccessible(true);
            block = method.newInstance();
            if (!HASHED_SORTED_CUSTOM_BLOCK.containsKey(block.getIdentifier())) {
                HASHED_SORTED_CUSTOM_BLOCK.put(block.getIdentifier(), block);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RegisterException(e);
        } catch (NoSuchMethodException e) {
            throw new RegisterException("Cannot find the parameterless constructor for this custom block:" + clazz.getCanonicalName());
        }
    }

    public void registerCustom(@NotNull Map<String, Class<? extends CustomBlock>> blockNamespaceClassMap) {
        if (!Server.getInstance().getSettings().features().enableExperimentMode()) {
            throw new RegisterException("The server does not have the experiment mode feature enabled.Unable to register custom block!");
        }
        for (var entry : blockNamespaceClassMap.entrySet()) {
            if (!HASHED_SORTED_CUSTOM_BLOCK.containsKey(entry.getKey())) {
                try {
                    var method = entry.getValue().getDeclaredConstructor();
                    method.setAccessible(true);
                    var block = method.newInstance();
                    HASHED_SORTED_CUSTOM_BLOCK.put(entry.getKey(), block);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RegisterException(e);
                } catch (NoSuchMethodException e) {
                    throw new RegisterException("Cannot find the parameterless constructor for this custom block:" + entry.getValue().getCanonicalName());
                }
            }
        }
    }

    @Override
    public Block get(Integer key) {
        return FULL_LIST[key];
    }

    public Class<? extends Block> getClass(int key) {
        return LIST[key];
    }

    public CustomBlock getCustom(int key) {
        return ID_TO_CUSTOM_BLOCK.get(key);
    }

    public int getCustomId(String key) {
        return CUSTOM_BLOCK_ID_MAP.get(key);
    }

    public int getLight(int blockId) {
        return LIGHT[blockId];
    }

    public int getLightFilter(int blockId) {
        return LIGHT_FILTER[blockId];
    }

    public boolean isSolid(int blockId) {
        return SOLID[blockId];
    }

    public boolean isTransparent(int blockId) {
        return TRANSPARENT[blockId];
    }

    public boolean isDiffusesSkyLight(int blockId) {
        return DIFFUSES_SKY_LIGHT[blockId];
    }

    public int getListSize() {
        return LIST.length;
    }

    public int getFullListSize() {
        return FULL_LIST.length;
    }

    public List<CustomBlockDefinition> getCustomBlockDefinitionList() {
        return new ArrayList<>(CUSTOM_BLOCK_DEFINITIONS);
    }

    public Map<String, List<CustomBlockUtil.CustomBlockState>> getLegacy2CustomState() {
        return LEGACY_2_CUSTOM_STATE;
    }

    @Override
    public void trim() {

    }

    @Override
    public void reload() {
        isLoad.set(false);
        LIST = new Class[Block.MAX_BLOCK_ID];
        FULL_LIST = new Block[Block.MAX_BLOCK_ID * (1 << Block.DATA_BITS)];
        LIGHT = new int[65536];
        LIGHT_FILTER = new int[65536];
        SOLID = new boolean[65536];
        TRANSPARENT = new boolean[65536];
        DIFFUSES_SKY_LIGHT = new boolean[65536];

        CUSTOM_BLOCK_DEFINITIONS.clear();
        ID_TO_CUSTOM_BLOCK.clear();
        CUSTOM_BLOCK_ID_MAP.clear();
        LEGACY_2_CUSTOM_STATE.clear();

        init();
    }
}
