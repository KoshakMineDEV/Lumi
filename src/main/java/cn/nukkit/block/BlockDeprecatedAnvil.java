package cn.nukkit.block;

/**
 * Created by Pub4Game on 27.12.2015.
 */
public class BlockDeprecatedAnvil extends BlockAnvil {
    public BlockDeprecatedAnvil() {
        this(0);
    }

    public BlockDeprecatedAnvil(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return DEPRECATED_ANVIL;
    }

    @Override
    public String getName() {
        return "Deprecated Anvil";
    }
}
