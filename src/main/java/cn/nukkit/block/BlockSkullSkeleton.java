package cn.nukkit.block;

import cn.nukkit.block.data.Faceable;
import cn.nukkit.blockentity.impl.BlockEntitySkull;

/**
 * @author Justin
 */
public class BlockSkullSkeleton extends BlockSkull implements Faceable, BlockEntityHolder<BlockEntitySkull> {

    public BlockSkullSkeleton() {
        this(0);
    }

    public BlockSkullSkeleton(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return SKELETON_SKULL_BLOCK;
    }

    public SkullType getSkullType() {
        return SkullType.SKELETON;
    }
}
