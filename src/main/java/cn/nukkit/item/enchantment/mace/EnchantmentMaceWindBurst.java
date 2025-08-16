package cn.nukkit.item.enchantment.mace;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.level.particle.HugeExplodeParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

public class EnchantmentMaceWindBurst extends EnchantmentMace {

    public EnchantmentMaceWindBurst() {
        super(ID_WIND_BURST, NAME_WIND_BURST, "heavy_weapon.windburst", EnchantmentRarity.RARE);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void doAttack(Entity attacker, Entity entity) {
        double fallDistance = attacker.highestPosition - entity.y;
        if (fallDistance <= 0 || attacker.isOnGround()) {
            return;
        }
        if (attacker instanceof Player player && player.getAdventureSettings().get(AdventureSettings.Type.FLYING)) {
            return;
        }
        double knockbackScaling = (this.getLevel() + 1) * 0.25;
        this.knockBack(attacker, knockbackScaling);
        attacker.resetFallDistance();
    }

    protected void knockBack(Entity entity, double knockbackScaling) {
        Vector3 knockback = new Vector3(entity.motionX, entity.motionY, entity.motionZ);
        knockback.x /= 2d;
        knockback.y /= 2d;
        knockback.z /= 2d;
        knockback.y += 2.0f * knockbackScaling;

        entity.setMotion(knockback);
        entity.getLevel().addParticle(new HugeExplodeParticle(entity));
        entity.getLevel().addLevelSoundEvent(entity, LevelSoundEventPacket.SOUND_MACE_SMASH_HEAVY_GROUND);
    }
}