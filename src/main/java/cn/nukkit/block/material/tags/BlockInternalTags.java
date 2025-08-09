package cn.nukkit.block.material.tags;

import cn.nukkit.block.material.BlockTypes;
import cn.nukkit.block.material.tags.impl.SimpleBlockTag;

public interface BlockInternalTags {
    BlockTag VIBRATION_DAMPER = BlockTags.register("lumi:vibration_damper", new SimpleBlockTag(
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
            BlockTypes.YELLOW_WOOL,
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
}
