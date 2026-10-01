package cn.nukkit.block;

public class BlockShelfMushroom extends BlockTransparentMeta {
    public BlockShelfMushroom() {
        this(0);
    }

    public BlockShelfMushroom(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Shelf Mushroom";
    }

    @Override
    public int getId() {
        return SHELF_MUSHROOM;
    }
}
