package cn.nukkit.block;

public class BlockAllium extends BlockFlower {
    public BlockAllium() {
        this(0);
    }

    public BlockAllium(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Allium";
    }

    @Override
    public int getId() {
        return ALLIUM;
    }
}
