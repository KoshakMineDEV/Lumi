package cn.nukkit.block;


public class BlockMangroveShelf extends BlockShelf {
    public BlockMangroveShelf() {
        this(0);
    }

    public BlockMangroveShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Mangrove Shelf";
    }

    @Override
    public int getId() {
        return MANGROVE_SHELF;
    }
}
