package cn.nukkit.item;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class ItemBeetroot extends ItemFood {

    public ItemBeetroot() {
        this(0, 1);
    }

    public ItemBeetroot(Integer meta) {
        this(meta, 1);
    }

    public ItemBeetroot(Integer meta, int count) {
        super(BEETROOT, meta, count, "Beetroot");
    }

    @Override
    public int getFoodRestore() {
        return 1;
    }

    @Override
    public float getSaturationRestore() {
        return 1.2F;
    }
}
