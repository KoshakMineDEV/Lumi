package cn.nukkit.registry;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.item.*;
import cn.nukkit.entity.mob.*;
import cn.nukkit.entity.passive.*;
import cn.nukkit.entity.projectile.*;
import cn.nukkit.entity.weather.EntityLightning;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

public class EntityRegistry implements IRegistry<String, Class<? extends Entity>, Class<? extends Entity>> {

    private static final Object2ObjectOpenHashMap<String, Class<? extends Entity>> KNOWN_ENTITIES = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, String> SHORT_NAMES = new Object2ObjectOpenHashMap<>();
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        //Items
        register("Item", EntityItem.class);
        register("Painting", EntityPainting.class);
        register("XpOrb", EntityXPOrb.class);
        register("ArmorStand", EntityArmorStand.class);
        register("EndCrystal", EntityEndCrystal.class);
        register("FallingSand", EntityFallingBlock.class);
        register("PrimedTnt", EntityPrimedTNT.class);
        register("Firework", EntityFirework.class);
        //Projectiles
        register("Arrow", EntityArrow.class);
        register("Snowball", EntitySnowball.class);
        register("EnderPearl", EntityEnderPearl.class);
        register("EnderEye", EntityEnderEye.class);
        register("ThrownExpBottle", EntityExpBottle.class);
        register("ThrownPotion", EntityPotionSplash.class);
        register("Egg", EntityEgg.class);
        register("SmallFireBall", EntitySmallFireBall.class);
        register("GhastFireBall", EntityGhastFireBall.class);
        register("ShulkerBullet", EntityShulkerBullet.class);
        register("ThrownLingeringPotion", EntityPotionLingering.class);
        register("ThrownTrident", EntityThrownTrident.class);
        register("WitherSkull", EntityWitherSkull.class);
        register("BlueWitherSkull", EntityBlueWitherSkull.class);
        register("LlamaSpit", EntityLlamaSpit.class);
        register("EvocationFangs", EntityEvocationFangs.class);
        register("EnderCharge", EntityEnderCharge.class);
        register("FishingHook", EntityFishingHook.class);
        //Monsters
        register("Blaze", EntityBlaze.class);
        register("Creeper", EntityCreeper.class);
        register("CaveSpider", EntityCaveSpider.class);
        register("Drowned", EntityDrowned.class);
        register("ElderGuardian", EntityElderGuardian.class);
        register("EnderDragon", EntityEnderDragon.class);
        register("Enderman", EntityEnderman.class);
        register("Endermite", EntityEndermite.class);
        register("Evoker", EntityEvoker.class);
        register("Ghast", EntityGhast.class);
        register("Guardian", EntityGuardian.class);
        register("Husk", EntityHusk.class);
        register("MagmaCube", EntityMagmaCube.class);
        register("Phantom", EntityPhantom.class);
        register("Ravager", EntityRavager.class);
        register("Shulker", EntityShulker.class);
        register("Silverfish", EntitySilverfish.class);
        register("Skeleton", EntitySkeleton.class);
        register("SkeletonHorse", EntitySkeletonHorse.class);
        register("Slime", EntitySlime.class);
        register("Spider", EntitySpider.class);
        register("Stray", EntityStray.class);
        register("Vindicator", EntityVindicator.class);
        register("Warden", EntityWarden.class);
        register("Vex", EntityVex.class);
        register("WitherSkeleton", EntityWitherSkeleton.class);
        register("Wither", EntityWither.class);
        register("Witch", EntityWitch.class);
        register("ZombiePigman", EntityZombiePigman.class);
        register("ZombieVillager", EntityZombieVillager.class);
        register("Zombie", EntityZombie.class);
        register("Pillager", EntityPillager.class);
        register("ZombieVillagerV2", EntityZombieVillagerV2.class);
        register("Hoglin", EntityHoglin.class);
        register("Piglin", EntityPiglin.class);
        register("Zoglin", EntityZoglin.class);
        register("PiglinBrute", EntityPiglinBrute.class);
        //register("Breeze", EntityBreeze.class);
        //register("Bogged", EntityBogged.class);
        register("Creaking", EntityCreaking.class);
        //Passive
        register("Bat", EntityBat.class);
        register("Cat", EntityCat.class);
        register("Chicken", EntityChicken.class);
        register("Cod", EntityCod.class);
        register("Cow", EntityCow.class);
        register("Dolphin", EntityDolphin.class);
        register("Donkey", EntityDonkey.class);
        register("Horse", EntityHorse.class);
        register("IronGolem", EntityIronGolem.class);
        register("Llama", EntityLlama.class);
        register("Mooshroom", EntityMooshroom.class);
        register("Mule", EntityMule.class);
        register("Panda", EntityPanda.class);
        register("Parrot", EntityParrot.class);
        register("PolarBear", EntityPolarBear.class);
        register("Pig", EntityPig.class);
        register("Pufferfish", EntityPufferfish.class);
        register("Rabbit", EntityRabbit.class);
        register("Salmon", EntitySalmon.class);
        register("Sheep", EntitySheep.class);
        register("Squid", EntitySquid.class);
        register("SnowGolem", EntitySnowGolem.class);
        register("TropicalFish", EntityTropicalFish.class);
        register("Turtle", EntityTurtle.class);
        register("Wolf", EntityWolf.class);
        register("Ocelot", EntityOcelot.class);
        register("Villager", EntityVillager.class);
        register("ZombieHorse", EntityZombieHorse.class);
        register("WanderingTrader", EntityWanderingTrader.class);
        register("VillagerV2", EntityVillagerV2.class);
        register("Fox", EntityFox.class);
        register("Frog", EntityFrog.class);
        register("Goat", EntityGoat.class);
        register("Bee", EntityBee.class);
        register("Strider", EntityStrider.class);
        register("Tadpole", EntityTadpole.class);
        register("Axolotl", EntityAxolotl.class);
        register("GlowSquid", EntityGlowSquid.class);
        register("Allay", EntityAllay.class);
        register("Npc", EntityNPCEntity.class);
        register("Camel", EntityCamel.class);
        //Vehicles
        register("MinecartRideable", EntityMinecartEmpty.class);
        register("MinecartChest", EntityMinecartChest.class);
        register("MinecartHopper", EntityMinecartHopper.class);
        register("MinecartTnt", EntityMinecartTNT.class);
        register("Boat", EntityBoat.class);
        register("ChestBoat", EntityChestBoat.class);
        //Others
        register("Human", EntityHuman.class, true);
        register("Lightning", EntityLightning.class);
        register("AreaEffectCloud", EntityAreaEffectCloud.class);
        register("WindCharge", EntityWindCharge.class);
    }

    @Override
    public void register(String key, Class<? extends Entity> value) {
        register(key, value, false);
    }

    public void register(String key, Class<? extends Entity> value, boolean force) {
        if (value == null) {
            throw new RegisterException("Tried to register null as BlockEntity with identifier: " + key);
        }
        try {
            int networkId = value.getField("NETWORK_ID").getInt(null);
            KNOWN_ENTITIES.put(String.valueOf(networkId), value);
        } catch (Exception e) {
            if (!force) {
                throw new RegisterException("Failed to register " + key, e);
            }
        }

        KNOWN_ENTITIES.put(key, value);
        SHORT_NAMES.put(value.getSimpleName(), key);
    }

    @Override
    public Class<? extends Entity> get(String key) {
        return KNOWN_ENTITIES.get(key);
    }

    public String getSaveId(Entity entity) {
        if(entity instanceof CustomEntity) {
            EntityDefinition entityDefinition = ((CustomEntity) entity).getEntityDefinition();
            return entityDefinition == null ? "" : entityDefinition.getIdentifier();
        }
        return SHORT_NAMES.getOrDefault(entity.getClass().getSimpleName(), "");
    }

    public boolean isRegistered(String name) {
        return KNOWN_ENTITIES.containsKey(name);
    }

    @Override
    public void trim() {}

    @Override
    public void reload() {
        isLoad.set(false);
        KNOWN_ENTITIES.clear();
        SHORT_NAMES.clear();
        init();
    }
}
