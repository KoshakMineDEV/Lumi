package cn.nukkit.item;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class ItemBeetrootSoup extends ItemFood {

    public ItemBeetrootSoup() {
        this(0, 1);
    }

    public ItemBeetrootSoup(Integer meta) {
        this(meta, 1);
    }

    public ItemBeetrootSoup(Integer meta, int count) {
        super(BEETROOT_SOUP, 0, count, "Beetroot Soup");
    }

    @Override
    public int getFoodRestore() {
        return 6;
    }

    @Override
    public float getSaturationRestore() {
        return 7.2F;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
