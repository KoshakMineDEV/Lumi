package cn.nukkit.entity.passive;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.item.data.DyeColor;
import cn.nukkit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EntitySheep extends EntityCreature {

    public static final int NETWORK_ID = 13;

    public EntitySheep(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 1.3f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(8);

        super.initEntity();
        this.setColor(randomColor());
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();
        drops.add(Item.get(Item.WOOL, 0, 1));
        drops.add(Item.get(this.isOnFire() ? Item.COOKED_MUTTON : Item.RAW_MUTTON, 0, Utils.rand(1, 2)));

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    private int randomColor() {
        int rand = Utils.rand(1, 200);

        if (rand == 1) {
            return DyeColor.PINK.getWoolData();
        } else if (rand < 8) {
            return DyeColor.BROWN.getWoolData();
        } else if (rand < 18) {
            return DyeColor.GRAY.getWoolData();
        } else if (rand < 28) {
            return DyeColor.LIGHT_GRAY.getWoolData();
        } else if (rand < 38) {
            return DyeColor.BLACK.getWoolData();
        } else {
            return DyeColor.WHITE.getWoolData();
        }
    }

    public void setColor(int woolColor) {
        this.namedTag.putByte("Color", woolColor);
        this.setDataProperty(new ByteEntityData(DATA_COLOUR, woolColor));
    }
}
