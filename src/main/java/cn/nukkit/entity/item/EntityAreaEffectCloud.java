package cn.nukkit.entity.item;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.data.FloatEntityData;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.PotionType;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EntityAreaEffectCloud extends Entity {

    public static final int NETWORK_ID = 95;

    protected List<Effect> cloudEffects;
    protected int reapplicationDelay;
    protected int durationOnUse;
    protected float initialRadius;
    protected float radiusOnUse;
    protected int nextApply;
    private int lastAge;
    protected long ownerId;

    @Nullable
    protected Entity owner;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    public EntityAreaEffectCloud(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public List<Effect> getCloudEffects() {
        return cloudEffects;
    }

    public int getWaitTime() {
        return this.getDataPropertyInt(DATA_AREA_EFFECT_CLOUD_WAITING);
    }

    public void setWaitTime(int waitTime) {
        setWaitTime(waitTime, true);
    }

    public void setWaitTime(int waitTime, boolean send) {
        this.setDataProperty(new IntEntityData(DATA_AREA_EFFECT_CLOUD_WAITING, waitTime), send);
    }

    public int getPotionId() {
        return this.getDataPropertyShort(DATA_AUX_VALUE_DATA);
    }

    public void setPotionId(int potionId) {
        setPotionId(potionId, true);
    }

    public void setPotionId(int potionId, boolean send) {
        this.setDataProperty(new ShortEntityData(DATA_AUX_VALUE_DATA, potionId & 0xFFFF), send);
    }

    public void recalculatePotionColor() {
        recalculatePotionColor(true);
    }

    public void recalculatePotionColor(boolean send) {
        int[] color = new int[4];
        int count = 0;

        if (namedTag.contains("ParticleColor")) {
            int effectColor = namedTag.getInt("ParticleColor");
            color[0] = (effectColor & 0xFF000000) >> 24;
            color[1] = (effectColor & 0x00FF0000) >> 16;
            color[2] = (effectColor & 0x0000FF00) >> 8;
            color[3] = effectColor & 0x000000FF;
        } else {
            color[0] = 255;

            PotionType potion = PotionType.get(getPotionId());
            for (Effect effect : potion.getEffects(true)) {
                Color effectColor = effect.getColor();
                color[1] += effectColor.getRed() * effect.getLevel();
                color[2] += effectColor.getGreen() * effect.getLevel();
                color[3] += effectColor.getBlue() * effect.getLevel();
                count += effect.getLevel();
            }
        }

        // Hardcoded color for area effect cloud from potions with water
        // to get rid of diving by zero
        if (count == 0) {
            setPotionColor(0xFF385DC6, send);
            return;
        }

        int a = (color[0] / count) & 0xff;
        int r = (color[1] / count) & 0xff;
        int g = (color[2] / count) & 0xff;
        int b = (color[3] / count) & 0xff;

        setPotionColor(a, r, g, b, send);
    }

    public int getPotionColor() {
        return this.getDataPropertyInt(DATA_EFFECT_COLOR);
    }

    public void setPotionColor(int argp) {
        setPotionColor(argp, true);
    }

    public void setPotionColor(int alpha, int red, int green, int blue, boolean send) {
        setPotionColor(((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff), send);
    }

    public void setPotionColor(int argp, boolean send) {
        this.setDataProperty(new IntEntityData(DATA_EFFECT_COLOR, argp), send);
    }

    public int getPickupCount() {
        return this.getDataPropertyInt(DATA_AREA_EFFECT_CLOUD_PICKUP_COUNT);
    }

    public void setPickupCount(int pickupCount) {
        setPickupCount(pickupCount, true);
    }

    public void setPickupCount(int pickupCount, boolean send) {
        this.setDataProperty(new IntEntityData(DATA_AREA_EFFECT_CLOUD_PICKUP_COUNT, pickupCount), send);
    }

    public float getRadiusChangeOnPickup() {
        return this.getDataPropertyFloat(DATA_AREA_EFFECT_CLOUD_CHANGE_ON_PICKUP);
    }

    public void setRadiusChangeOnPickup(float radiusChangeOnPickup) {
        setRadiusChangeOnPickup(radiusChangeOnPickup, true);
    }

    public void setRadiusChangeOnPickup(float radiusChangeOnPickup, boolean send) {
        this.setDataProperty(new FloatEntityData(DATA_AREA_EFFECT_CLOUD_CHANGE_ON_PICKUP, radiusChangeOnPickup), send);
    }

    public float getRadiusPerTick() {
        return this.getDataPropertyFloat(DATA_AREA_EFFECT_CLOUD_CHANGE_RATE);
    }

    public void setRadiusPerTick(float radiusPerTick) {
        setRadiusPerTick(radiusPerTick, true);
    }

    public void setRadiusPerTick(float radiusPerTick, boolean send) {
        this.setDataProperty(new FloatEntityData(DATA_AREA_EFFECT_CLOUD_CHANGE_RATE, radiusPerTick), send);
    }

    public long getSpawnTick() {
        return this.getDataPropertyInt(DATA_AREA_EFFECT_CLOUD_SPAWN_TIME);
    }

    public void setSpawnTick(long spawnTick) {
        setSpawnTick(spawnTick, true);
    }

    public void setSpawnTick(long spawnTick, boolean send) {
        this.setDataProperty(new IntEntityData(DATA_AREA_EFFECT_CLOUD_SPAWN_TIME, (int) spawnTick), send);
    }

    private long getTicksAlive() {
        return Math.max(0, level.getCurrentTick() - this.getSpawnTick());
    }

    public int getDuration() {
        return this.getDataPropertyInt(DATA_AREA_EFFECT_CLOUD_DURATION);
    }

    public void setDuration(int duration) {
        setDuration(duration, true);
    }

    public void setDuration(int duration, boolean send) {
        this.setDataProperty(new IntEntityData(DATA_AREA_EFFECT_CLOUD_DURATION, duration), send);
    }

    public float getRadius() {
        return this.getDataPropertyFloat(DATA_AREA_EFFECT_CLOUD_RADIUS);
    }

    public void setRadius(float radius) {
        setRadius(radius, true);
    }

    public void setRadius(float radius, boolean send) {
        this.setDataProperty(new FloatEntityData(DATA_AREA_EFFECT_CLOUD_RADIUS, radius), send);
    }

    public int getParticleId() {
        return this.getDataPropertyInt(DATA_AREA_EFFECT_CLOUD_PARTICLE_ID);
    }

    public void setParticleId(int particleId) {
        setParticleId(particleId, true);
    }

    public void setParticleId(int particleId, boolean send) {
        this.setDataProperty(new IntEntityData(DATA_AREA_EFFECT_CLOUD_PARTICLE_ID, particleId), send);
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Nullable
    public Entity getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable Entity owner) {
        this.owner = owner;
        if (owner != null) {
            this.ownerId = owner.getId();
        }
    }

    @Override
    protected void initEntity() {
        super.initEntity();
        this.invulnerable = true;

        this.setDataFlag(DATA_FLAGS, DATA_FLAG_FIRE_IMMUNE, true);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_IMMOBILE, true);

        this.setPickupCount(namedTag.getInt("PickupCount"), false);

        if (this.namedTag.contains("Duration")) {
            this.setDuration(this.namedTag.getInt("Duration"), false);
        } else {
            this.setDuration(600, false);
        }

        if (this.namedTag.contains("DurationOnUse")) {
            this.durationOnUse = this.namedTag.getInt("DurationOnUse");
        } else {
            this.durationOnUse = -100;
        }

        if (this.namedTag.contains("ReapplicationDelay")) {
            this.reapplicationDelay = this.namedTag.getInt("ReapplicationDelay");
        } else {
            this.reapplicationDelay = 0;
        }

        if (this.namedTag.contains("InitialRadius")) {
            this.initialRadius = this.namedTag.getFloat("InitialRadius");
        } else {
            this.initialRadius = 3.0F;
        }

        if (this.namedTag.contains("Radius")) {
            this.setRadius(this.namedTag.getFloat("Radius"), false);
        } else {
            this.setRadius(initialRadius, false);
        }

        if (this.namedTag.contains("RadiusOnUse")) {
            this.radiusOnUse = this.namedTag.getFloat("RadiusOnUse");
        } else {
            this.radiusOnUse = -0.5F;
        }

        if (this.namedTag.contains("RadiusChangeOnPickup")) {
            this.setRadiusChangeOnPickup(this.namedTag.getFloat("RadiusChangeOnPickup"), false);
        } else {
            this.setRadiusChangeOnPickup(-0.5F, false);
        }

        if (this.namedTag.contains("RadiusPerTick")) {
            this.setRadiusPerTick(namedTag.getFloat("RadiusPerTick"), false);
        } else {
            this.setRadiusPerTick(-0.005F, false);
        }

        if (this.namedTag.contains("WaitTime")) {
            this.setWaitTime(namedTag.getInt("WaitTime"), false);
        } else {
            this.setWaitTime(10, false);
        }

        this.setPotionId(this.namedTag.getShort("PotionId"), false);

        ListTag<CompoundTag> mobEffects = this.namedTag.getList("mobEffects", CompoundTag.class);
        this.cloudEffects = new ArrayList<>();
        for (CompoundTag effectTag : mobEffects.getAll()) {
            Effect effect = Effect.get(effectTag.getByte("Id"))
                    .setAmbient(effectTag.getBoolean("Ambient"))
                    .setAmplifier(effectTag.getByte("Amplifier"))
                    .setVisible(effectTag.getBoolean("DisplayOnScreenTextureAnimation"))
                    .setDuration(effectTag.getInt("Duration"));
            if (effect != null) {
                this.cloudEffects.add(effect);
            }
        }

        this.recalculatePotionColor(false);

        int particleId = this.namedTag.getInt("ParticleId");
        if (particleId == 0) {
            particleId = Particle.TYPE_MOB_SPELL_AMBIENT;
        }
        this.setParticleId(particleId, false);

        this.setSpawnTick(this.namedTag.getLong("SpawnTick"), false);

        this.ownerId = namedTag.getLong("OwnerId");

        setMaxHealth(1);
        setHealth(1);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt("Duration", this.getDuration());
        this.namedTag.putInt("DurationOnUse", this.durationOnUse);
        this.namedTag.putInt("ReapplicationDelay", this.reapplicationDelay);
        this.namedTag.putFloat("InitialRadius", this.initialRadius);
        this.namedTag.putFloat("RadiusOnUse", this.radiusOnUse);
        this.namedTag.putFloat("RadiusPerTick", this.getRadiusPerTick());
        this.namedTag.putFloat("RadiusChangeOnPickup", this.getRadiusChangeOnPickup());
        this.namedTag.putInt("PickupCount", this.getPickupCount());
        this.namedTag.putInt("WaitTime", this.getWaitTime());
        this.namedTag.putShort("PotionId", this.getPotionId());
        this.namedTag.putInt("ParticleColor", this.getPotionColor());
        this.namedTag.putLong("SpawnTick", this.getSpawnTick());
        this.namedTag.putLong("OwnerId", this.ownerId);
        this.namedTag.putFloat("Radius", this.getRadius());

        ListTag<CompoundTag> list = new ListTag<>("mobEffects");
        for (Effect effect : this.cloudEffects) {
            list.add(new CompoundTag().putByte("Id", effect.getId())
                    .putBoolean("Ambient", effect.isAmbient())
                    .putByte("Amplifier", effect.getAmplifier())
                    .putBoolean("DisplayOnScreenTextureAnimation", effect.isVisible())
                    .putInt("Duration", effect.getDuration())
            );
        }
        this.namedTag.putList(list);

        if (this.getParticleId() != Particle.TYPE_MOB_SPELL_AMBIENT) {
            this.namedTag.putInt("ParticleId", this.getParticleId());
        } else {
            this.namedTag.remove("ParticleId");
        }
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        super.onUpdate(currentTick);

        boolean sendRadius = age % 10 == 0;

        int age = this.age;
        float radius = getRadius();
        int waitTime = getWaitTime();
        if (age < waitTime) {
            radius = initialRadius;
        } else if (age > waitTime + getDuration()) {
            kill();
        } else {
            int tickDiff = age - lastAge;
            radius += getRadiusPerTick() * tickDiff;
            if ((nextApply -= tickDiff) <= 0) {
                nextApply = reapplicationDelay + 10;

                Entity[] collidingEntities = level.getCollidingEntities(getBoundingBox());
                if (collidingEntities.length > 0) {
                    radius += radiusOnUse;
                    radiusOnUse /= 2;

                    setDuration(getDuration() + durationOnUse);

                    for (Entity collidingEntity : collidingEntities) {
                        if (collidingEntity == this || !(collidingEntity instanceof EntityLiving)) continue;

                        for (Effect effect : cloudEffects) {
                            collidingEntity.addEffect(effect);
                        }
                    }
                }
            }
        }

        this.lastAge = age;

        if (radius <= 1.5 && age >= waitTime) {
            setRadius(radius, false);
            kill();
        } else {
            setRadius(radius, sendRadius);
        }

        float height = getHeight();
        boundingBox.setBounds(x - radius, y - height, z - radius, x + radius, y + height, z + radius);

        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return entity instanceof EntityLiving;
    }

    @Override
    public float getHeight() {
        return 0.3F + (getRadius() / 2F);
    }

    @Override
    public float getWidth() {
        return getRadius();
    }

    @Override
    public float getLength() {
        return getRadius();
    }
}
