package cn.nukkit.block;


public class BlockAcaciaShelf extends BlockShelf {
    public BlockAcaciaShelf() {
        this(0);
    }

    public BlockAcaciaShelf(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Acacia Shelf";
    }

    @Override
    public int getId() {
        return ACACIA_SHELF;
    }
}
