package cn.nukkit.item;

/**
 * Created by PetteriM1
 */
public class ItemDriedKelp extends ItemFood {

    public ItemDriedKelp() {
        this(0, 1);
    }

    public ItemDriedKelp(Integer meta) {
        this(meta, 1);
    }

    public ItemDriedKelp(Integer meta, int count) {
        super(DRIED_KELP, 0, count, "Dried Kelp");
    }

    @Override
    public int getFoodRestore() {
        return 1;
    }

    @Override
    public float getSaturationRestore() {
        return 0.6F;
    }

    @Override
    public int getEatingTicks() {
        return 17;
    }
}
