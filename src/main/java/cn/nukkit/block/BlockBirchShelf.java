package cn.nukkit.block;


public class BlockBirchShelf extends BlockShelf {
    public BlockBirchShelf() {
        this(0);
    }

    public BlockBirchShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Birch Shelf";
    }

    @Override
    public int getId() {
        return BIRCH_SHELF;
    }
}
