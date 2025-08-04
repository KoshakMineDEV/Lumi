package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.level.vibration.VanillaVibrationTypes;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.math.Vector3;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public abstract class ItemFood extends Item {

    public ItemFood(int id, Integer meta, int count, String name) {
        super(id, meta, count, name);
    }

    public ItemFood(int id) {
        super(id);
    }

    public ItemFood(int id, Integer meta) {
        super(id, meta);
    }

    public ItemFood(int id, Integer meta, int count) {
        super(id, meta, count);
    }

    public int getFoodRestore() {
        return 0;
    }

    public float getSaturationRestore() {
        return 0;
    }

    public boolean isRequiresHunger() {
        return true;
    }

    public int getEatingTicks() {
        return 31;
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        if (player.getFoodData().isHungry() || !this.isRequiresHunger() || player.isCreative()) {
            return true;
        }
        player.getFoodData().sendFood();
        return false;
    }

    @Override
    public boolean onUse(Player player, int ticksUsed) {
        if (ticksUsed < getEatingTicks()) {
            return false;
        }

        PlayerItemConsumeEvent event = new PlayerItemConsumeEvent(player, this);
        event.call();

        if (event.isCancelled()) {
            player.getInventory().sendContents(player);
            return false;
        }

        if (this.onEaten(player)) {
            player.getFoodData().addFood(this);

            if (player.isAdventure() || player.isSurvival()) {
                --this.count;
                player.getInventory().setItemInHand(this);

                player.getLevel().addSound(player, Sound.RANDOM_BURP);
            }
        }

        player.getLevel().getVibrationManager().callVibrationEvent(new VibrationEvent(player, player.add(0, player.getEyeHeight()), VanillaVibrationTypes.EAT));

        return true;
    }

    /*
     * Used for additional behaviour in Food like: Chorus, Suspicious Stew and etc.
     */
    public boolean onEaten(Player player) {
        return true;
    }

    @Override
    public boolean canRelease() {
        return true;
    }
}
