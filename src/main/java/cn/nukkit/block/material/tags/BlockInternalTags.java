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
}
