package cn.nukkit.block.material.tags.impl;

import cn.nukkit.block.material.BlockType;
import cn.nukkit.block.material.tags.BlockTag;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Data
public class SimpleBlockTag implements BlockTag {

    private final Set<BlockType> blockTypes;

    public SimpleBlockTag(BlockType... blockTypes) {
        this(Arrays.asList(blockTypes));
    }

    public SimpleBlockTag(Collection<BlockType> blockTypes) {
        this.blockTypes = Collections.unmodifiableSet(new ObjectOpenHashSet<>(blockTypes));
    }
}
