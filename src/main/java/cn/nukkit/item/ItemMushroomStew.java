package cn.nukkit.item;

import cn.nukkit.Player;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class ItemMushroomStew extends ItemFood {

    public ItemMushroomStew() {
        this(0, 1);
    }

    public ItemMushroomStew(Integer meta) {
        this(meta, 1);
    }

    public ItemMushroomStew(Integer meta, int count) {
        super(MUSHROOM_STEW, 0, count, "Mushroom Stew");
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
    public boolean onEaten(Player player) {
        player.getInventory().addItem(new ItemBowl());

        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
