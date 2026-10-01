package cn.nukkit.block;

public class BlockRedShrub extends BlockBush {

    public BlockRedShrub() {
        this(0);
    }

    public BlockRedShrub(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return RED_SHRUB;
    }

    @Override
    public String getName() {
        return "Red Shrub";
    }
}
