package cn.nukkit.item.enchantment;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityCombustByEntityEvent;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class EnchantmentFireAspect extends Enchantment {

    public EnchantmentFireAspect() {
        super(ID_FIRE_ASPECT, NAME_FIRE_ASPECT, "fire", EnchantmentRarity.RARE, EnchantmentType.SWORD);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 10 + (level - 1) * 20;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return super.getMinEnchantAbility(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public void doAttack(Entity attacker, Entity entity) {
        if (!entity.isPlayer || !((Player) entity).isCreative()) {
            int duration = Math.max(entity.fireTicks / 20, getLevel() << 2);

            EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(attacker, entity, duration);

            if (event.call()) {
                entity.setOnFire(event.getDuration());
            }
        }
    }
}
