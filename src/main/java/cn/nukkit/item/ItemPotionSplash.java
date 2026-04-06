package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

/**
 * Created on 2015/12/27 by xtypr.
 * Package cn.nukkit.item in project Nukkit .
 */
public class ItemPotionSplash extends ItemProjectile {

    public ItemPotionSplash(Integer meta) {
        this(meta, 1);
    }

    public ItemPotionSplash(Integer meta, int count) {
        super(SPLASH_POTION, meta, count, "Splash Potion");
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public String getEntityType() {
        return "ThrownPotion";
    }

    @Override
    public float getThrowForce() {
        return 0.50f;
    }

    @Override
    public void onThrown(Player player, EntityProjectile projectile) {
        player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BOW);
    }

    @Override
    protected CompoundTag correctNBT(CompoundTag nbt) {
        nbt.putInt("PotionId", this.meta);
        return nbt;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        if(protocolId < ProtocolInfo.v1_21_0) {
            if(getDamage() > 40) return false;
        }
        return true;
    }
}
