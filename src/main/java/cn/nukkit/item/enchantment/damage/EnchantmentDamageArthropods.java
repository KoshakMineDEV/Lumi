package cn.nukkit.item.enchantment.damage;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityArthropod;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.event.entity.EntityEffectUpdateEvent;
import cn.nukkit.item.enchantment.EnchantmentRarity;
import cn.nukkit.utils.Utils;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentDamageArthropods extends EnchantmentDamage {

    public EnchantmentDamageArthropods() {
        super(ID_DAMAGE_ARTHROPODS, NAME_DAMAGE_ARTHROPODS, "arthropods", EnchantmentRarity.UNCOMMON, Type.SMITE);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 5 + ((level - 1) << 3);
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return this.getMinEnchantAbility(level) + 20;
    }

    @Override
    public double getDamageBonus(Entity entity, Entity attacker) {
        if (entity instanceof EntityArthropod) {
            return getLevel() * 2.5;
        }

        return 0;
    }

    @Override
    public void doPostAttack(Entity attacker, Entity entity) {
        if (entity instanceof EntityArthropod) {
            entity.addEffect(
                    Effect.get(EffectType.SLOWNESS)
                            .setDuration(20 + Utils.random.nextInt(10 * this.level))
                            .setAmplifier(3),
                    EntityEffectUpdateEvent.Cause.ATTACK
            );
        }
    }
}
