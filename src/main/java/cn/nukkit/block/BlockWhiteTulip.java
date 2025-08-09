package cn.nukkit.block;

public class BlockWhiteTulip extends BlockFlower {
    public BlockWhiteTulip() {
        this(0);
    }

    public BlockWhiteTulip(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "White Tulip";
    }

    @Override
    public int getId() {
        return WHITE_TULIP;
    }
}
