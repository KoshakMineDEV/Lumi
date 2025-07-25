package cn.nukkit.entity.effect;

import cn.nukkit.entity.Entity;
import cn.nukkit.nbt.tag.CompoundTag;

import java.awt.Color;

public abstract class Effect implements Cloneable {

    private final EffectType type;
    private final String name;
    private int duration = 600;
    private int amplifier;
    private boolean visible = true;
    private boolean ambient = false;
    private final boolean bad;
    private Color color;

    public Effect(EffectType type, String name, Color color) {
        this(type, name, color, false);
    }

    public Effect(EffectType type, String name, Color color, boolean bad) {
        this.type = type;
        this.name = name;
        this.color = color;
        this.bad = bad;
    }

    public static Effect get(EffectType type) {
        return EffectRegistry.get(type);
    }

    public static Effect get(String id) {
        return get(EffectType.get(id));
    }

    public static Effect get(int id) {
        return get(EffectType.get(id));
    }

    public EffectType getType() {
        return type;
    }

    public Integer getId() {
        return type.id();
    }

    public String getName() {
        return name;
    }

    /**
     * get the duration of this potion in 1 tick = 0.05 s
     */
    public int getDuration() {
        return duration;
    }

    /**
     * set the duration of this potion , 1 tick = 0.05 s
     *
     * @param duration the duration
     * @return the duration
     */
    public Effect setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Get amplifier.
     */
    public int getAmplifier() {
        return amplifier;
    }

    /**
     * Sets amplifier.
     *
     * @param amplifier the amplifier
     * @return the amplifier
     */
    public Effect setAmplifier(int amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    /**
     * Get the level of potion,level = amplifier + 1.
     *
     * @return the level
     */
    public int getLevel() {
        return amplifier + 1;
    }

    public boolean isVisible() {
        return visible;
    }

    public Effect setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public Effect setAmbient(boolean ambient) {
        this.ambient = ambient;
        return this;
    }

    public boolean isBad() {
        return bad;
    }

    public Color getColor() {
        return color;
    }

    public Effect setColor(Color color) {
        this.color = color;
        return this;
    }

    public boolean canTick() {
        return false;
    }

    public void onApply(Entity entity, double tickCount) {

    }

    public void onAdd(Entity entity) {

    }

    public void onRemove(Entity entity) {

    }

    public CompoundTag saveNBT() {
        return new CompoundTag()
                .putByte("Id", this.getType().id())
                .putByte("Amplifier", this.amplifier)
                .putInt("Duration", this.duration)
                .putInt("DurationEasy", this.duration)
                .putInt("DurationNormal", this.duration)
                .putInt("DurationHard", this.duration)
                .putBoolean("Ambient", this.ambient)
                .putBoolean("ShowParticles", this.visible)
                .putBoolean("DisplayOnScreenTextureAnimation", false);
    }

    public static Effect fromNBT(CompoundTag compoundTag) {
        Effect effect = Effect.get(compoundTag.getByte("Id"));
        if (effect == null) {
            return null;
        }
        return effect
                .setAmplifier(compoundTag.getByte("Amplifier"))
                .setDuration(compoundTag.getInt("Duration"))
                .setAmbient(compoundTag.getBoolean("Ambient"))
                .setVisible(compoundTag.getBoolean("ShowParticles"));
    }

    @Override
    public Effect clone() {
        try {
            return (Effect) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
