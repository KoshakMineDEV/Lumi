package cn.nukkit.item;

/**
 * Created by Snake1999 on 2016/1/14.
 * Package cn.nukkit.item in project nukkit.
 */
public class ItemRabbitRaw extends ItemFood {

    public ItemRabbitRaw() {
        this(0, 1);
    }

    public ItemRabbitRaw(Integer meta) {
        this(meta, 1);
    }

    public ItemRabbitRaw(Integer meta, int count) {
        super(RAW_RABBIT, meta, count, "Raw Rabbit");
    }

    @Override
    public int getFoodRestore() {
        return 3;
    }

    @Override
    public float getSaturationRestore() {
        return 1.8F;
    }
}
