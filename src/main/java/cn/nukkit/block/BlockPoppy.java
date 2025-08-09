package cn.nukkit.block;

public class BlockPoppy extends BlockFlower {
    public BlockPoppy() {
        this(0);
    }

    public BlockPoppy(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Poppy";
    }

    @Override
    public int getId() {
        return POPPY;
    }
}
