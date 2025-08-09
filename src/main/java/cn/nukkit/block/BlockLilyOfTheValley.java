package cn.nukkit.block;

public class BlockLilyOfTheValley extends BlockFlower {
    public BlockLilyOfTheValley() {
        this(0);
    }

    public BlockLilyOfTheValley(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Lily of the Valley";
    }

    @Override
    public int getId() {
        return LILY_OF_THE_VALLEY;
    }
}
