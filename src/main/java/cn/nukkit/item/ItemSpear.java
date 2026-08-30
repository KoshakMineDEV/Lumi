package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class ItemSpear extends StringItemToolBase {

    private static final double MIN_REACH = 2.0;
    private static final double MAX_REACH = 4.5;
    private static final double CREATIVE_MAX_REACH = 7.5;
    private static final double TARGET_MARGIN = 0.125;
    private static final double MIN_RELATIVE_SPEED = 4.6;
    private static final double MIN_KNOCKBACK_SPEED = 5.1;
    private static final int MINIMUM_LUNGE_FOOD = 7;
    private static final int CHARGE_CONTACT_COOLDOWN = 10;
    private static final float BASE_CHARGE_DAMAGE = 1;
    private static final float BASE_KNOCKBACK = 0.3f;
    private static final float KNOCKBACK_PER_LEVEL = 0.1f;
    private static final double LUNGE_IMPULSE_PER_LEVEL = 0.458;
    private static final double LUNGE_EXHAUSTION_PER_LEVEL = 4;
    private static final double NO_COLLISION_DISTANCE = -1;
    private static final Map<Player, SpearAttackState> PLAYER_ATTACK_STATES = new WeakHashMap<>();

    public ItemSpear(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean isSpear() {
        return true;
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        player.getLevel().addLevelSoundEvent(player, this.getUseSound());
        return true;
    }

    @Override
    public boolean canRelease() {
        return true;
    }

    @Override
    public boolean onRelease(Player player, int ticksUsed) {
        return true;
    }

    public double getMaximumReach(boolean creative) {
        return creative ? CREATIVE_MAX_REACH : MAX_REACH;
    }

    public abstract int getChargeDelay();

    public abstract int getJabCooldown();

    protected abstract double getChargeDamageMultiplier();

    protected abstract int getChargeDamageDuration();

    protected abstract int getChargeKnockbackDuration();

    protected abstract int getChargeDismountDuration();

    protected abstract double getChargeDismountSpeed();

    public boolean canDealChargeDamage(int ticksUsed, double relativeSpeed) {
        int activeTicks = this.getActiveChargeTicks(ticksUsed);
        return activeTicks >= 0
                && activeTicks <= this.getChargeDamageDuration()
                && relativeSpeed >= MIN_RELATIVE_SPEED;
    }

    public boolean canChargeKnockBack(int ticksUsed, double forwardSpeed) {
        int activeTicks = this.getActiveChargeTicks(ticksUsed);
        return activeTicks >= 0
                && activeTicks <= this.getChargeKnockbackDuration()
                && forwardSpeed >= MIN_KNOCKBACK_SPEED;
    }

    public boolean canChargeDismount(int ticksUsed, double forwardSpeed) {
        int activeTicks = this.getActiveChargeTicks(ticksUsed);
        return activeTicks >= 0
                && activeTicks <= this.getChargeDismountDuration()
                && forwardSpeed >= this.getChargeDismountSpeed();
    }

    private int getActiveChargeTicks(int ticksUsed) {
        return ticksUsed - this.getChargeDelay();
    }

    public int getChargeDamage(double relativeSpeed) {
        return (int) (BASE_CHARGE_DAMAGE + Math.floor(relativeSpeed * this.getChargeDamageMultiplier()));
    }

    public int jabAttackInView(Player player) {
        int currentTick = player.getServer().getTick();
        SpearAttackState state = getAttackState(player);
        if (!state.tryStartJab(currentTick, this.getJabCooldown())) {
            return -1;
        }

        if (this.canLunge(player)) {
            this.applyLunge(player);
        }

        int hitCount = 0;
        for (Entity target : this.getTargetsInView(player)) {
            if (this.attackTarget(player, target, this.getAttackDamage(player), AttackEffects.JAB, AttackType.JAB)) {
                hitCount++;
            }
        }
        return hitCount;
    }

    public int chargeAttackInView(Player player) {
        int currentTick = player.getServer().getTick();
        int ticksUsed = currentTick - player.getStartActionTick();
        double forwardSpeed = player.getHorizontalSpeed() * 20.0; // convert to BPS
        SpearAttackState state = getAttackState(player);
        state.removeExpiredChargeContacts(currentTick);

        int hitCount = 0;

        for (Entity target : this.getTargetsInView(player)) {
            if (state.hasRecentChargeContact(target, currentTick)) {
                continue;
            }

            double targetSpeed = target.getHorizontalSpeed() * 20.0; // convert to BPS
            double relativeSpeed = forwardSpeed - targetSpeed;

            AttackEffects effects = this.getChargeEffects(ticksUsed, relativeSpeed, forwardSpeed);

            if (!effects.hasAny()) {
                continue;
            }

            state.rememberChargeContact(target, currentTick);
            float damage = effects.dealsDamage() ? this.getChargeDamage(relativeSpeed) : 0;
            if (this.attackTarget(player, target, damage, effects, AttackType.CHARGE)) {
                hitCount++;
            }
        }

        return hitCount;
    }

    private AttackEffects getChargeEffects(int ticksUsed, double relativeSpeed, double forwardSpeed) {
        return new AttackEffects(
                this.canDealChargeDamage(ticksUsed, relativeSpeed),
                this.canChargeKnockBack(ticksUsed, forwardSpeed),
                this.canChargeDismount(ticksUsed, forwardSpeed)
        );
    }

    private List<Entity> getTargetsInView(Player player) {
        Vector3 start = player.getEyePosition();
        Vector3 direction = player.getDirectionVector();
        double maximumReach = this.getMaximumReach(player.isCreative());
        Vector3 end = start.add(direction.multiply(maximumReach));
        double blockDistance = this.getFirstBlockingDistance(player, start, end, direction, maximumReach);
        AxisAlignedBB searchBox = player.getBoundingBox().grow(maximumReach, maximumReach, maximumReach);
        List<Entity> targets = new ArrayList<>();

        for (Entity target : player.getLevel().getNearbyEntities(searchBox, player)) {
            if (!this.isValidTarget(player, target)) {
                continue;
            }

            double hitDistance = this.getTargetHitDistance(target, start, end);
            if (hitDistance < MIN_REACH || hitDistance > blockDistance) {
                continue;
            }

            targets.add(target);
        }

        return targets;
    }

    private boolean isValidTarget(Player attacker, Entity target) {
        if (target == attacker || !target.isAlive() || this.sharesVehicle(attacker, target)) {
            return false;
        }
        return !(target instanceof Player player)
                || (!player.isSpectator() && attacker.getLevel().getGameRules().getBoolean(GameRule.PVP));
    }

    private double getTargetHitDistance(Entity target, Vector3 start, Vector3 end) {
        AxisAlignedBB hitbox = target.boundingBox.grow(TARGET_MARGIN, TARGET_MARGIN, TARGET_MARGIN);
        MovingObjectPosition collision = hitbox.calculateIntercept(start, end);
        return collision == null ? Double.POSITIVE_INFINITY : start.distance(collision.hitVector);
    }

    private double getFirstBlockingDistance(Player player, Vector3 start, Vector3 end,
                                            Vector3 direction, double maximumReach) {
        BlockRayTraversal ray = new BlockRayTraversal(start, direction);
        while (ray.isWithin(maximumReach)) {
            Block block = player.getLevel().getBlock(ray.blockX, ray.blockY, ray.blockZ);
            double collisionDistance = this.getBlockCollisionDistance(block, start, end);
            if (collisionDistance != NO_COLLISION_DISTANCE) {
                return Math.min(maximumReach, collisionDistance);
            }
            ray.advance();
        }
        return maximumReach;
    }

    private double getBlockCollisionDistance(Block block, Vector3 start, Vector3 end) {
        AxisAlignedBB collisionBox = block.getCollisionBoundingBox();
        if (block.canPassThrough() || collisionBox == null) {
            return NO_COLLISION_DISTANCE;
        }
        if (collisionBox.isVectorInside(start)) {
            return 0;
        }

        MovingObjectPosition collision = collisionBox.calculateIntercept(start, end);
        return collision == null ? NO_COLLISION_DISTANCE : start.distance(collision.hitVector);
    }

    private boolean sharesVehicle(Entity attacker, Entity target) {
        Entity attackerRoot = this.getRootVehicle(attacker);
        Entity targetRoot = this.getRootVehicle(target);
        return attackerRoot == targetRoot && (attackerRoot != attacker || targetRoot != target);
    }

    private Entity getRootVehicle(Entity entity) {
        Entity root = entity;
        while (root.getRiding() != null) {
            root = root.getRiding();
        }
        return root;
    }

    private boolean attackTarget(Player player, Entity target, float baseDamage,
                                 AttackEffects effects, AttackType attackType) {
        Enchantment[] enchantments = effects.dealsDamage() ? this.getEnchantments() : Enchantment.EMPTY_ARRAY;
        float damage = this.addEnchantmentDamage(baseDamage, player, target, enchantments);
        float knockBack = this.getKnockBack(effects.knocksBack());
        Map<EntityDamageEvent.DamageModifier, Float> modifiers = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
        modifiers.put(EntityDamageEvent.DamageModifier.BASE, damage);
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, modifiers, knockBack, enchantments
        );
        event.setAttackCooldown(0);
        event.setBreakShield(this.canBreakShield());
        event.setCriticalAllowed(false);

        if (!target.attack(event)) {
            return false;
        }

        if (effects.dismounts() && target.getRiding() != null) {
            target.getRiding().dismountEntity(target);
        }

        this.playHitSound(player, target, attackType);
        if (effects.dealsDamage()) {
            for (Enchantment enchantment : enchantments) {
                enchantment.doPostAttack(player, target);
            }
        }

        player.sendMessage(event.getDamage() + "");

        if (!player.isCreative()) {
            this.damageSpear(player);
        }
        return true;
    }

    private float addEnchantmentDamage(float damage, Player player, Entity target,
                                       Enchantment[] enchantments) {
        for (Enchantment enchantment : enchantments) {
            damage += (float) enchantment.getDamageBonus(target, player);
        }
        return damage;
    }

    private float getKnockBack(boolean knocksBack) {
        if (!knocksBack) {
            return 0;
        }

        Enchantment enchantment = this.getEnchantment(Enchantment.ID_KNOCKBACK);
        return enchantment == null
                ? BASE_KNOCKBACK
                : BASE_KNOCKBACK + enchantment.getLevel() * KNOCKBACK_PER_LEVEL;
    }

    private void playHitSound(Player player, Entity target, AttackType attackType) {
        if (attackType == AttackType.CHARGE) {
            player.getLevel().addSoundToViewers(target, this.getChargeHitSound());
        } else {
            player.getLevel().addLevelSoundEvent(target, this.getAttackHitSound());
        }
    }

    private void damageSpear(Player player) {
        this.useOn((Entity) null);
        if (this.getDamage() >= this.getMaxDurability()) {
            player.getLevel().addSoundToViewers(player, Sound.RANDOM_BREAK);
            player.getLevel().addParticle(new ItemBreakParticle(player, this));
            player.getInventory().setItemInHand(Item.get(0));
        } else {
            player.getInventory().setItemInHand(this);
        }
    }

    public void applyLunge(Player player) {
        int lungeLevel = this.getEnchantmentLevel(Enchantment.ID_LUNGE);
        Vector3 direction = player.getDirectionVector();
        direction.y = 0;

        if (direction.lengthSquared() == 0) {
            return;
        }

        Vector3 impulse = direction.multiply(LUNGE_IMPULSE_PER_LEVEL * lungeLevel);
        player.setMotion(player.getMotion().add(impulse));
        player.getLevel().addLevelSoundEvent(player, getLungeSound(lungeLevel));

        if (player.isSurvival() || player.isAdventure()) {
            this.damageSpear(player);
            player.getFoodData().exhaust(lungeLevel * LUNGE_EXHAUSTION_PER_LEVEL);
        }
    }

    private static int getLungeSound(int lungeLevel) {
        return switch (Math.min(lungeLevel, 3)) {
            case 1 -> LevelSoundEventPacket.SOUND_LUNGE_1;
            case 2 -> LevelSoundEventPacket.SOUND_LUNGE_2;
            default -> LevelSoundEventPacket.SOUND_LUNGE_3;
        };
    }

    public boolean canLunge(Player player) {
        if (player.getRiding() != null || player.isGliding() || player.isSwimming() || player.isInsideOfWater()) {
            return false;
        }

        if ((player.isSurvival() || player.isAdventure())
                && player.getFoodData().getFood() < MINIMUM_LUNGE_FOOD) {
            return false;
        }
        return this.getEnchantmentLevel(Enchantment.ID_LUNGE) > 0;
    }

    private static SpearAttackState getAttackState(Player player) {
        return PLAYER_ATTACK_STATES.computeIfAbsent(player, ignored -> new SpearAttackState());
    }

    private enum AttackType {
        JAB,
        CHARGE
    }

    private record AttackEffects(boolean dealsDamage, boolean knocksBack, boolean dismounts) {

        private static final AttackEffects JAB = new AttackEffects(true, true, false);

        private boolean hasAny() {
            return this.dealsDamage || this.knocksBack || this.dismounts;
        }
    }

    private static final class SpearAttackState {

        private int jabCooldownUntil;
        private final Map<Long, Integer> chargeContacts = new HashMap<>();

        private boolean tryStartJab(int currentTick, int cooldown) {
            if (currentTick < this.jabCooldownUntil) {
                return false;
            }
            this.jabCooldownUntil = currentTick + cooldown;
            return true;
        }

        private void removeExpiredChargeContacts(int currentTick) {
            this.chargeContacts.values().removeIf(
                    lastTick -> currentTick - lastTick >= CHARGE_CONTACT_COOLDOWN
            );
        }

        private boolean hasRecentChargeContact(Entity target, int currentTick) {
            Integer lastTick = this.chargeContacts.get(target.getId());
            return lastTick != null && currentTick - lastTick < CHARGE_CONTACT_COOLDOWN;
        }

        private void rememberChargeContact(Entity target, int currentTick) {
            this.chargeContacts.put(target.getId(), currentTick);
        }
    }

    /** Safely walks through every voxel crossed by a ray. */
    private static final class BlockRayTraversal {

        private int blockX;
        private int blockY;
        private int blockZ;
        private final int stepX;
        private final int stepY;
        private final int stepZ;
        private final double deltaX;
        private final double deltaY;
        private final double deltaZ;
        private double nextX;
        private double nextY;
        private double nextZ;
        private double travelled;

        private BlockRayTraversal(Vector3 start, Vector3 direction) {
            this.blockX = start.getFloorX();
            this.blockY = start.getFloorY();
            this.blockZ = start.getFloorZ();
            this.stepX = Double.compare(direction.x, 0);
            this.stepY = Double.compare(direction.y, 0);
            this.stepZ = Double.compare(direction.z, 0);
            this.deltaX = getBoundaryInterval(direction.x, this.stepX);
            this.deltaY = getBoundaryInterval(direction.y, this.stepY);
            this.deltaZ = getBoundaryInterval(direction.z, this.stepZ);
            this.nextX = getFirstBoundaryDistance(start.x, this.blockX, direction.x, this.stepX);
            this.nextY = getFirstBoundaryDistance(start.y, this.blockY, direction.y, this.stepY);
            this.nextZ = getFirstBoundaryDistance(start.z, this.blockZ, direction.z, this.stepZ);
        }

        private boolean isWithin(double maximumDistance) {
            return this.travelled <= maximumDistance;
        }

        private void advance() {
            double nextBoundary = Math.min(this.nextX, Math.min(this.nextY, this.nextZ));
            if (this.nextX <= nextBoundary) {
                this.blockX += this.stepX;
                this.nextX += this.deltaX;
            }
            if (this.nextY <= nextBoundary) {
                this.blockY += this.stepY;
                this.nextY += this.deltaY;
            }
            if (this.nextZ <= nextBoundary) {
                this.blockZ += this.stepZ;
                this.nextZ += this.deltaZ;
            }
            this.travelled = nextBoundary;
        }

        private static double getBoundaryInterval(double direction, int step) {
            return step == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / direction);
        }

        private static double getFirstBoundaryDistance(double coordinate, int blockCoordinate,
                                                       double direction, int step) {
            if (step > 0) {
                return (blockCoordinate + 1 - coordinate) / direction;
            }
            if (step < 0) {
                return (coordinate - blockCoordinate) / -direction;
            }
            return Double.POSITIVE_INFINITY;
        }
    }

    public int getAttackHitSound() {
        return switch (this.getTier()) {
            case ItemTool.TIER_WOODEN -> LevelSoundEventPacket.SOUND_WOODEN_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_STONE -> LevelSoundEventPacket.SOUND_STONE_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_COPPER -> LevelSoundEventPacket.SOUND_COPPER_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_IRON -> LevelSoundEventPacket.SOUND_IRON_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_GOLD -> LevelSoundEventPacket.SOUND_GOLDEN_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_DIAMOND -> LevelSoundEventPacket.SOUND_DIAMOND_SPEAR_ATTACK_HIT;
            case ItemTool.TIER_NETHERITE -> LevelSoundEventPacket.SOUND_NETHERITE_SPEAR_ATTACK_HIT;
            default -> LevelSoundEventPacket.SOUND_SPEAR_ATTACK_HIT;
        };
    }

    private Sound getChargeHitSound() {
        return this.getTier() == ItemTool.TIER_WOODEN ? Sound.ITEM_WOODEN_SPEAR_HIT : Sound.ITEM_SPEAR_HIT;
    }

    public int getAttackMissSound() {
        return switch (this.getTier()) {
            case ItemTool.TIER_WOODEN -> LevelSoundEventPacket.SOUND_WOODEN_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_STONE -> LevelSoundEventPacket.SOUND_STONE_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_COPPER -> LevelSoundEventPacket.SOUND_COPPER_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_IRON -> LevelSoundEventPacket.SOUND_IRON_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_GOLD -> LevelSoundEventPacket.SOUND_GOLDEN_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_DIAMOND -> LevelSoundEventPacket.SOUND_DIAMOND_SPEAR_ATTACK_MISS;
            case ItemTool.TIER_NETHERITE -> LevelSoundEventPacket.SOUND_NETHERITE_SPEAR_ATTACK_MISS;
            default -> LevelSoundEventPacket.SOUND_SPEAR_ATTACK_MISS;
        };
    }

    private int getUseSound() {
        return switch (this.getTier()) {
            case ItemTool.TIER_WOODEN -> LevelSoundEventPacket.SOUND_WOODEN_SPEAR_USE;
            case ItemTool.TIER_STONE -> LevelSoundEventPacket.SOUND_STONE_SPEAR_USE;
            case ItemTool.TIER_COPPER -> LevelSoundEventPacket.SOUND_COPPER_SPEAR_USE;
            case ItemTool.TIER_IRON -> LevelSoundEventPacket.SOUND_IRON_SPEAR_USE;
            case ItemTool.TIER_GOLD -> LevelSoundEventPacket.SOUND_GOLDEN_SPEAR_USE;
            case ItemTool.TIER_DIAMOND -> LevelSoundEventPacket.SOUND_DIAMOND_SPEAR_USE;
            case ItemTool.TIER_NETHERITE -> LevelSoundEventPacket.SOUND_NETHERITE_SPEAR_USE;
            default -> LevelSoundEventPacket.SOUND_SPEAR_USE;
        };
    }

    @Override
    public boolean isSupportedOn(int protocolId) {
        return protocolId >= ProtocolInfo.v1_21_130;
    }
}
