package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.item.data.DyeColor;

public class EntityWolf extends EntityCreature {

    public static final int NETWORK_ID = 14;

    private static final String NBT_KEY_COLLAR_COLOR = "CollarColor";

    private final Vector3 tempVector = new Vector3();
    private DyeColor collarColor = DyeColor.RED;

    public EntityWolf(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.8f;
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(8);
        super.initEntity();

        if (this.namedTag.contains(NBT_KEY_COLLAR_COLOR)) {
            this.collarColor = DyeColor.getByDyeData(this.namedTag.getByte(NBT_KEY_COLLAR_COLOR));
            if (this.collarColor == null) {
                this.collarColor = DyeColor.RED;
            }

            this.setDataProperty(new ByteEntityData(DATA_COLOUR, collarColor.getWoolData()));
        }
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.getNameTag() : "Wolf";
    }
}
