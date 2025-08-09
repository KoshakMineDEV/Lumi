package cn.nukkit.block;

public class BlockBlueOrchid extends BlockFlower {
    public BlockBlueOrchid() {
        this(0);
    }

    public BlockBlueOrchid(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Blue Orchid";
    }

    @Override
    public int getId() {
        return BLUE_ORCHID;
    }
}
