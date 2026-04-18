package cn.nukkit.block;


public class BlockWarpedShelf extends BlockShelf {
    public BlockWarpedShelf() {
        this(0);
    }

    public BlockWarpedShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Warped Shelf";
    }

    @Override
    public int getId() {
        return WARPED_SHELF;
    }
}
