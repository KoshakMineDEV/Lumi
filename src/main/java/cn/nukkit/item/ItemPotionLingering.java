package cn.nukkit.item;

import  cn.nukkit.Player;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemPotionLingering extends ItemProjectile {

    public ItemPotionLingering() {
        this(0, 1);
    }

    public ItemPotionLingering(Integer meta) {
        this(meta, 1);
    }

    public ItemPotionLingering(Integer meta, int count) {
        super(LINGERING_POTION, meta, count, "Lingering Potion");
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
        return "ThrownLingeringPotion";
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
