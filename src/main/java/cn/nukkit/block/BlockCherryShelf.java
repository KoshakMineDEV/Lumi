package cn.nukkit.block;


public class BlockCherryShelf extends BlockShelf {
    public BlockCherryShelf() {
        this(0);
    }

    public BlockCherryShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Cherry Shelf";
    }

    @Override
    public int getId() {
        return CHERRY_SHELF;
    }
}
