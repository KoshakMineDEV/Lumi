package cn.nukkit.block;

public class BlockStairsPaleOak extends BlockStairsOak {

    public BlockStairsPaleOak() {
        this(0);
    }

    public BlockStairsPaleOak(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return PALE_OAK_STAIRS;
    }

    @Override
    public String getName() {
        return "Pale Oak Wood Stairs";
    }
}
