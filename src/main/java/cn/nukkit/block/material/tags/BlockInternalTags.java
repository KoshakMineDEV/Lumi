package cn.nukkit.block.material.tags;

import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.BlockTypes;
import cn.nukkit.block.material.tags.impl.SimpleBlockTag;

public interface BlockInternalTags {
    BlockTag WOOL = BlockTags.register("lumi:wool", new SimpleBlockTag(
            BlockTypes.WHITE_WOOL,
            BlockTypes.BLACK_WOOL,
            BlockTypes.BLUE_WOOL,
            BlockTypes.BROWN_WOOL,
            BlockTypes.CYAN_WOOL,
            BlockTypes.GRAY_WOOL,
            BlockTypes.GREEN_WOOL,
            BlockTypes.LIGHT_BLUE_WOOL,
            BlockTypes.LIGHT_GRAY_WOOL,
            BlockTypes.LIME_WOOL,
            BlockTypes.MAGENTA_WOOL,
            BlockTypes.ORANGE_WOOL,
            BlockTypes.PINK_WOOL,
            BlockTypes.PURPLE_WOOL,
            BlockTypes.RED_WOOL,
            BlockTypes.YELLOW_WOOL
    ));

    BlockTag CARPET = BlockTags.register("lumi:carpet", new SimpleBlockTag(
            BlockTypes.CARPET,
            BlockTypes.WHITE_CARPET,
            BlockTypes.BLACK_CARPET,
            BlockTypes.BLUE_CARPET,
            BlockTypes.BROWN_CARPET,
            BlockTypes.CYAN_CARPET,
            BlockTypes.GRAY_CARPET,
            BlockTypes.GREEN_CARPET,
            BlockTypes.LIGHT_BLUE_CARPET,
            BlockTypes.LIGHT_GRAY_CARPET,
            BlockTypes.LIME_CARPET,
            BlockTypes.MAGENTA_CARPET,
            BlockTypes.ORANGE_CARPET,
            BlockTypes.PINK_CARPET,
            BlockTypes.PURPLE_CARPET,
            BlockTypes.RED_CARPET,
            BlockTypes.YELLOW_CARPET
    ));

    BlockTag VIBRATION_DAMPER = BlockTags.register("lumi:vibration_damper",
            WOOL.copyWith(CARPET.getBlockTypes().toArray(new BlockType[0]))
    );

    BlockTag JUNGLE = BlockTags.register("lumi:jungle", new SimpleBlockTag(
            BlockTypes.JUNGLE_WOOD,
            BlockTypes.JUNGLE_LOG,
            BlockTypes.STRIPPED_JUNGLE_WOOD,
            BlockTypes.STRIPPED_JUNGLE_LOG
    ));

    BlockTag POTTABLE = BlockTags.register("lumi:pottable", new SimpleBlockTag(
            BlockTypes.DANDELION,
            BlockTypes.POPPY,
            BlockTypes.BLUE_ORCHID,
            BlockTypes.ALLIUM,
            BlockTypes.AZURE_BLUET,
            BlockTypes.RED_TULIP,
            BlockTypes.ORANGE_TULIP,
            BlockTypes.WHITE_TULIP,
            BlockTypes.PINK_TULIP,
            BlockTypes.OXEYE_DAISY,
            BlockTypes.CORNFLOWER,
            BlockTypes.LILY_OF_THE_VALLEY,
            BlockTypes.WITHER_ROSE,
            BlockTypes.TORCHFLOWER,
            BlockTypes.OAK_SAPLING,
            BlockTypes.SPRUCE_SAPLING,
            BlockTypes.BIRCH_SAPLING,
            BlockTypes.JUNGLE_SAPLING,
            BlockTypes.ACACIA_SAPLING,
            BlockTypes.DARK_OAK_SAPLING,
            BlockTypes.CHERRY_SAPLING,
            BlockTypes.RED_MUSHROOM,
            BlockTypes.BROWN_MUSHROOM,
            BlockTypes.FERN,
            BlockTypes.DEADBUSH,
            BlockTypes.CACTUS,
            BlockTypes.BAMBOO,
            BlockTypes.AZALEA,
            BlockTypes.FLOWERING_AZALEA,
            BlockTypes.CRIMSON_FUNGUS,
            BlockTypes.WARPED_FUNGUS,
            BlockTypes.WARPED_ROOTS,
            BlockTypes.CRIMSON_ROOTS,
            BlockTypes.MANGROVE_PROPAGULE,
            BlockTypes.PALE_OAK_SAPLING,
            BlockTypes.OPEN_EYEBLOSSOM,
            BlockTypes.CLOSED_EYEBLOSSOM
    ));

    BlockTag DYED_SHULKER_BOX = BlockTags.register("lumi:dyed_shulker_box", new SimpleBlockTag(
            BlockTypes.WHITE_SHULKER_BOX,
            BlockTypes.LIGHT_GRAY_SHULKER_BOX,
            BlockTypes.GRAY_SHULKER_BOX,
            BlockTypes.BLACK_SHULKER_BOX,
            BlockTypes.BROWN_SHULKER_BOX,
            BlockTypes.RED_SHULKER_BOX,
            BlockTypes.ORANGE_SHULKER_BOX,
            BlockTypes.YELLOW_SHULKER_BOX,
            BlockTypes.LIME_SHULKER_BOX,
            BlockTypes.GREEN_SHULKER_BOX,
            BlockTypes.CYAN_SHULKER_BOX,
            BlockTypes.LIGHT_BLUE_SHULKER_BOX,
            BlockTypes.BLUE_SHULKER_BOX,
            BlockTypes.PURPLE_SHULKER_BOX,
            BlockTypes.MAGENTA_SHULKER_BOX,
            BlockTypes.PINK_SHULKER_BOX
    ));

    BlockTag SHULKER_BOX = BlockTags.register("lumi:shulker_box",
            DYED_SHULKER_BOX.copyWith(BlockTypes.UNDYED_SHULKER_BOX)
    );

    BlockTag WOODEN_DOOR = BlockTags.register("lumi:wooden_door", new SimpleBlockTag(
            BlockTypes.ACACIA_DOOR,
            BlockTypes.WOODEN_DOOR,
            BlockTypes.BIRCH_DOOR,
            BlockTypes.CRIMSON_DOOR,
            BlockTypes.DARK_OAK_DOOR,
            BlockTypes.JUNGLE_DOOR,
            BlockTypes.SPRUCE_DOOR,
            BlockTypes.WARPED_DOOR,
            BlockTypes.MANGROVE_DOOR,
            BlockTypes.CHERRY_DOOR,
            BlockTypes.PALE_OAK_DOOR
    ));

    BlockTag WOODEN_STAIRS = BlockTags.register("lumi:wooden_stairs", new SimpleBlockTag(
            BlockTypes.ACACIA_STAIRS,
            BlockTypes.OAK_STAIRS,
            BlockTypes.BIRCH_STAIRS,
            BlockTypes.CRIMSON_STAIRS,
            BlockTypes.DARK_OAK_STAIRS,
            BlockTypes.JUNGLE_STAIRS,
            BlockTypes.SPRUCE_STAIRS,
            BlockTypes.WARPED_STAIRS,
            BlockTypes.MANGROVE_STAIRS,
            BlockTypes.CHERRY_STAIRS,
            BlockTypes.PALE_OAK_STAIRS
    ));

    BlockTag WOODEN_PRESSURE_PLATE = BlockTags.register("lumi:wooden_pressure_plate", new SimpleBlockTag(
            BlockTypes.ACACIA_PRESSURE_PLATE,
            BlockTypes.WOODEN_PRESSURE_PLATE,
            BlockTypes.BIRCH_PRESSURE_PLATE,
            BlockTypes.CRIMSON_PRESSURE_PLATE,
            BlockTypes.DARK_OAK_PRESSURE_PLATE,
            BlockTypes.JUNGLE_PRESSURE_PLATE,
            BlockTypes.SPRUCE_PRESSURE_PLATE,
            BlockTypes.WARPED_PRESSURE_PLATE,
            BlockTypes.MANGROVE_PRESSURE_PLATE,
            BlockTypes.CHERRY_PRESSURE_PLATE,
            BlockTypes.PALE_OAK_PRESSURE_PLATE
    ));

    BlockTag WOODEN_BUTTON = BlockTags.register("lumi:wooden_button", new SimpleBlockTag(
            BlockTypes.ACACIA_BUTTON,
            BlockTypes.WOODEN_BUTTON,
            BlockTypes.BIRCH_BUTTON,
            BlockTypes.CRIMSON_BUTTON,
            BlockTypes.DARK_OAK_BUTTON,
            BlockTypes.JUNGLE_BUTTON,
            BlockTypes.SPRUCE_BUTTON,
            BlockTypes.WARPED_BUTTON,
            BlockTypes.MANGROVE_BUTTON,
            BlockTypes.CHERRY_BUTTON,
            BlockTypes.PALE_OAK_BUTTON
    ));

    BlockTag WOODEN_TRAPDOOR = BlockTags.register("lumi:wooden_trapdoor", new SimpleBlockTag(
            BlockTypes.ACACIA_TRAPDOOR,
            BlockTypes.TRAPDOOR,
            BlockTypes.BIRCH_TRAPDOOR,
            BlockTypes.CRIMSON_TRAPDOOR,
            BlockTypes.DARK_OAK_TRAPDOOR,
            BlockTypes.JUNGLE_TRAPDOOR,
            BlockTypes.SPRUCE_TRAPDOOR,
            BlockTypes.WARPED_TRAPDOOR,
            BlockTypes.MANGROVE_TRAPDOOR,
            BlockTypes.CHERRY_TRAPDOOR,
            BlockTypes.PALE_OAK_TRAPDOOR
    ));

    BlockTag WOODEN_FENCE = BlockTags.register("lumi:wooden_fence", new SimpleBlockTag(
            BlockTypes.ACACIA_FENCE,
            BlockTypes.FENCE,
            BlockTypes.BIRCH_FENCE,
            BlockTypes.CRIMSON_FENCE,
            BlockTypes.DARK_OAK_FENCE,
            BlockTypes.JUNGLE_FENCE,
            BlockTypes.SPRUCE_FENCE,
            BlockTypes.WARPED_FENCE,
            BlockTypes.MANGROVE_FENCE,
            BlockTypes.CHERRY_FENCE,
            BlockTypes.PALE_OAK_FENCE
    ));

    BlockTag WOODEN_FENCE_GATE = BlockTags.register("lumi:wooden_fence_gate", new SimpleBlockTag(
            BlockTypes.ACACIA_FENCE_GATE,
            BlockTypes.FENCE_GATE,
            BlockTypes.BIRCH_FENCE_GATE,
            BlockTypes.CRIMSON_FENCE_GATE,
            BlockTypes.DARK_OAK_FENCE_GATE,
            BlockTypes.JUNGLE_FENCE_GATE,
            BlockTypes.SPRUCE_FENCE_GATE,
            BlockTypes.WARPED_FENCE_GATE,
            BlockTypes.MANGROVE_FENCE_GATE,
            BlockTypes.CHERRY_FENCE_GATE,
            BlockTypes.PALE_OAK_FENCE_GATE
    ));

    BlockTag SAPLING = BlockTags.register("lumi:sapling", new SimpleBlockTag(
            BlockTypes.ACACIA_SAPLING,
            BlockTypes.OAK_SAPLING,
            BlockTypes.BIRCH_SAPLING,
            BlockTypes.DARK_OAK_SAPLING,
            BlockTypes.JUNGLE_SAPLING,
            BlockTypes.SPRUCE_SAPLING,
            BlockTypes.MANGROVE_PROPAGULE,
            BlockTypes.CHERRY_SAPLING,
            BlockTypes.PALE_OAK_SAPLING
    ));
}
