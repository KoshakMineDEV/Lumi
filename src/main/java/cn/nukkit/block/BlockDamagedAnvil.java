package cn.nukkit.block;

/**
 * Created by Pub4Game on 27.12.2015.
 */
public class BlockDamagedAnvil extends BlockAnvil {
    public BlockDamagedAnvil() {
        this(0);
    }

    public BlockDamagedAnvil(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return DAMAGED_ANVIL;
    }

    @Override
    public String getName() {
        return "Damaged Anvil";
    }
}
