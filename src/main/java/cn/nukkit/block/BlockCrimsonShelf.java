package cn.nukkit.block;


public class BlockCrimsonShelf extends BlockShelf {
    public BlockCrimsonShelf() {
        this(0);
    }

    public BlockCrimsonShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Crimson Shelf";
    }

    @Override
    public int getId() {
        return CRIMSON_SHELF;
    }
}
