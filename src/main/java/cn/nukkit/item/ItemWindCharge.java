package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

public class ItemWindCharge extends StringItemProjectileBase {

    public ItemWindCharge() {
        super("minecraft:wind_charge", "Wind Charge");
    }

    @Override
    public String getEntityType() {
        return "WindCharge";
    }

    @Override
    public float getThrowForce() {
        return 1.5f;
    }

    @Override
    public boolean canThrow(Player player) {
        return player.getServer().getTick() - player.getLastWindChargeThrowingTick() >= 10;
    }

    @Override
    public void onThrown(Player player, EntityProjectile projectile) {
        player.setLastWindChargeThrowingTick();
        player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_BOW);
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_0;
    }
}