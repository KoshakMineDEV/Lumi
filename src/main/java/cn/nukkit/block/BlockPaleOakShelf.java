package cn.nukkit.block;


public class BlockPaleOakShelf extends BlockShelf {
    public BlockPaleOakShelf() {
        this(0);
    }

    public BlockPaleOakShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Pale Oak Shelf";
    }

    @Override
    public int getId() {
        return PALE_OAK_SHELF;
    }
}
