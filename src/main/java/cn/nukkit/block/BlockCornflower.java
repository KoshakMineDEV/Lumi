package cn.nukkit.block;

public class BlockCornflower extends BlockFlower {
    public BlockCornflower() {
        this(0);
    }

    public BlockCornflower(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Cornflower";
    }

    @Override
    public int getId() {
        return CORNFLOWER;
    }
}
