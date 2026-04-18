package cn.nukkit.block;


public class BlockDarkOakShelf extends BlockShelf {
    public BlockDarkOakShelf() {
        this(0);
    }

    public BlockDarkOakShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Dark Oak Shelf";
    }

    @Override
    public int getId() {
        return DARK_OAK_SHELF;
    }
}
