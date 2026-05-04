package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Utils;

public class EntitySlime extends EntityCreature {

    public static final int NETWORK_ID = 37;

    private int size = 1;

    public EntitySlime(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getHeight() {
        return 0.51f + size * 0.51f;
    }

    @Override
    public float getLength() {
        return 0.51f + size * 0.51f;
    }

    @Override
    protected void initEntity() {
        if (this.namedTag.contains("Size")) {
            this.size = this.namedTag.getInt("Size");
        } else {
            this.size = Utils.rand(1, 3);
            if (this.size == 3) {
                this.size = 4;
            }
        }

        this.setMaxHealth(1);
        super.initEntity();

        this.setScale(0.51f + size * 0.51f);
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.SLIMEBALL, 0, Utils.rand(0, 2))};
    }
}
