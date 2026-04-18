package cn.nukkit.block;


public class BlockJungleShelf extends BlockShelf {
    public BlockJungleShelf() {
        this(0);
    }

    public BlockJungleShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Jungle Shelf";
    }

    @Override
    public int getId() {
        return JUNGLE_SHELF;
    }
}
