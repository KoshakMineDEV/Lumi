package cn.nukkit.block.material.tags;

import cn.nukkit.block.material.BlockTypes;
import cn.nukkit.block.material.tags.impl.SimpleBlockTag;

public interface InternalBlockTags {
    BlockTag WOOL = BlockTags.register("lumi:wool", new SimpleBlockTag(
            BlockTypes.WOOL,
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
}
