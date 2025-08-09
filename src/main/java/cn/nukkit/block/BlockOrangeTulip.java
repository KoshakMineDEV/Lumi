package cn.nukkit.block;

public class BlockOrangeTulip extends BlockFlower {
    public BlockOrangeTulip() {
        this(0);
    }

    public BlockOrangeTulip(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Orange Tulip";
    }

    @Override
    public int getId() {
        return ORANGE_TULIP;
    }
}
