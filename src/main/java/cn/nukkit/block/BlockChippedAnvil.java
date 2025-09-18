package cn.nukkit.block;

/**
 * Created by Pub4Game on 27.12.2015.
 */
public class BlockChippedAnvil extends BlockAnvil {
    public BlockChippedAnvil() {
        this(0);
    }

    public BlockChippedAnvil(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CHIPPED_ANVIL;
    }

    @Override
    public String getName() {
        return "Chipped Anvil";
    }
}
