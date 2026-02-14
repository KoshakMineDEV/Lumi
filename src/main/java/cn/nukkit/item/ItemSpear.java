package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerSpearStabEvent;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.Getter;

public abstract class ItemSpear extends StringItemToolBase {

    @Getter
    public float minimumSpeed = 0.13f;
    public int minimumLungeFood = 6;
    public int baseLungeExhaust = 4;

    public ItemSpear(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean isSpear() {
        return true;
    }

    public void onSpearStab(Player player, float movementSpeed) {
        if (player.getServer().getTick() - player.getLastSpearUse() < 20) return;

        player.setItemCoolDown(20, this.getIdentifier());

        PlayerSpearStabEvent event = new PlayerSpearStabEvent(player, this, movementSpeed);

        if (!event.call()) return;

        this.applyLunge(player);

        if (movementSpeed < getMinimumSpeed() || !player.isSprinting()) {
            player.getLevel().addSound(player.getPosition(), Sound.ITEM_SPEAR_ATTACK_MISS);
            player.setLastSpearUse();
            return;
        }

        Level level = player.getLevel();
        Location loc = player.getLocation();

        Vector3 eyePos = loc.add(0, player.getEyeHeight(), 0);
        Vector3 direction = player.getDirectionVector().normalize();

        double maxDistance = 5.0;
        double minDot = 0.866;
        double bestScore = -1;

        Entity target = null;

        AxisAlignedBB searchBox = player.getBoundingBox().grow(maxDistance, maxDistance, maxDistance);

        for (Entity entity : level.getNearbyEntities(searchBox, player)) {
            if (!(entity instanceof EntityLiving living) || !living.isAlive()) continue;

            Vector3 targetPos = entity.getPosition().add(0, living.getEyeHeight() * 0.5, 0);

            double distance = eyePos.distance(targetPos);
            if (distance > maxDistance) continue;

            Vector3 toEntity = targetPos.subtract(eyePos).normalize();
            double dot = direction.dot(toEntity);

            if (dot < minDot) continue;

            double score = dot - (distance / maxDistance) * 0.1;
            if (score <= bestScore) continue;

            bestScore = score;
            target = entity;
        }

        if (target != null) {
            EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                    player,
                    target,
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                    getJabDamage()
            );

            target.attack(damageEvent);
            level.addSound(player.getPosition(), Sound.ITEM_SPEAR_ATTACK_HIT);
        } else {
            level.addSound(player.getPosition(), Sound.ITEM_SPEAR_ATTACK_MISS);
        }

        player.setLastSpearUse();
    }

    public float getJabDamage() {
        float damage = getAttackDamage();

        int level = getEnchantmentLevel(Enchantment.ID_LUNGE);
        damage += level * 1.5f;

        return damage;
    }

    public void applyLunge(Player player) {
        if (this.canLunge(player)){
            int lungeLevel = getEnchantmentLevel(Enchantment.ID_LUNGE);
            Vector3 dir = player.getDirectionVector();
            dir.y = 0;

            if (dir.lengthSquared() == 0) return;

            dir = dir.normalize().multiply(0.5 + (lungeLevel * 0.4));

            player.setMotion(player.getMotion().add(dir));
            // TODO: lunge sound
//            player.getLevel().addSound(player.getPosition(), Sound.ITEM_SPEAR_LUNGE);
            player.getFoodData().exhaust(baseLungeExhaust * lungeLevel);
        }
    }

    public boolean canLunge(Player player) {
        int playerGamemode = player.getGamemode();
        int enchantmentLevel = getEnchantmentLevel(Enchantment.ID_LUNGE);

        if (player.isGliding() || player.isSwimming() || player.isInsideOfWater() || player.isInsideOfWater()) {
            return false;
        }

        if ((playerGamemode == Player.SURVIVAL || playerGamemode == Player.ADVENTURE) && player.getFoodData().getFood() < minimumLungeFood) {
            return false;
        }
        return enchantmentLevel > 0;
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_130;
    }
}