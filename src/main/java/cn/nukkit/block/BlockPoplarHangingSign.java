package cn.nukkit.block;

public class BlockPoplarHangingSign extends BlockHangingSign {

    public BlockPoplarHangingSign() {
        this(0);
    }

    public BlockPoplarHangingSign(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_HANGING_SIGN;
    }

    @Override
    public String getName() {
        return "Poplar Hanging Sign";
    }
}
