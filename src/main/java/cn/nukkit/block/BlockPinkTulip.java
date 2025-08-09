package cn.nukkit.block;

public class BlockPinkTulip extends BlockFlower {
    public BlockPinkTulip() {
        this(0);
    }

    public BlockPinkTulip(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Pink Tulip";
    }

    @Override
    public int getId() {
        return PINK_TULIP;
    }
}
