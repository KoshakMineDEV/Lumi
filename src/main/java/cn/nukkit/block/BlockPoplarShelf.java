package cn.nukkit.block;

public class BlockPoplarShelf extends BlockShelf {

    public BlockPoplarShelf() {
        this(0);
    }

    public BlockPoplarShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Poplar Shelf";
    }

    @Override
    public int getId() {
        return POPLAR_SHELF;
    }
}
