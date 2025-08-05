package cn.nukkit.block.material.tags;

import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.tags.impl.SimpleBlockTag;

import java.util.Set;

public interface BlockTag {

    Set<BlockType> getBlockTypes();

    default boolean has(BlockType blockType) {
        return this.getBlockTypes().contains(blockType);
    }

    static BlockTag of(BlockType... blockTypes) {
        return new SimpleBlockTag(blockTypes);
    }
}
