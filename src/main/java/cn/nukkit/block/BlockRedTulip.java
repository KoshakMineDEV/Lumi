package cn.nukkit.block;

public class BlockRedTulip extends BlockFlower {
    public BlockRedTulip() {
        this(0);
    }

    public BlockRedTulip(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Red Tulip";
    }

    @Override
    public int getId() {
        return RED_TULIP;
    }
}
