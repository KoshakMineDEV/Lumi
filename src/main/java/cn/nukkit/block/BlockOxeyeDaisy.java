package cn.nukkit.block;

public class BlockOxeyeDaisy extends BlockFlower {
    public BlockOxeyeDaisy() {
        this(0);
    }

    public BlockOxeyeDaisy(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Oxeye Daisy";
    }

    @Override
    public int getId() {
        return OXEYE_DAISY;
    }
}
