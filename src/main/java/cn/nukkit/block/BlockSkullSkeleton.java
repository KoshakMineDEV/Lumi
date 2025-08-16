package cn.nukkit.block;

import cn.nukkit.blockentity.impl.BlockEntitySkull;
import cn.nukkit.utils.Faceable;

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
