package cn.nukkit.blockentity.impl;

import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;
import cn.nukkit.item.ItemRecord;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

import java.util.Objects;

/**
 * @author CreeperFace
 */
public class BlockEntityJukebox extends BlockEntitySpawnable {

    private Item recordItem;

    public BlockEntityJukebox(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        if (namedTag.contains("RecordItem")) {
            this.recordItem = NBTIO.getItemHelper(namedTag.getCompound("RecordItem"));
        } else {
            this.recordItem = Item.get(0);
        }

        super.initBlockEntity();
    }

    public Item getRecordItem() {
        return recordItem;
    }

    public void setRecordItem(Item recordItem) {
        Objects.requireNonNull(recordItem, "Record item cannot be null");
        this.recordItem = recordItem;
    }

    public void play() {
        if (this.recordItem instanceof ItemRecord) {
            this.getLevel().addLevelSoundEvent(this, switch (this.recordItem.getId()) {
                case Item.RECORD_13 -> LevelSoundEventPacket.SOUND_RECORD_13;
                case Item.RECORD_CAT -> LevelSoundEventPacket.SOUND_RECORD_CAT;
                case Item.RECORD_BLOCKS -> LevelSoundEventPacket.SOUND_RECORD_BLOCKS;
                case Item.RECORD_CHIRP -> LevelSoundEventPacket.SOUND_RECORD_CHIRP;
                case Item.RECORD_FAR -> LevelSoundEventPacket.SOUND_RECORD_FAR;
                case Item.RECORD_MALL -> LevelSoundEventPacket.SOUND_RECORD_MALL;
                case Item.RECORD_MELLOHI -> LevelSoundEventPacket.SOUND_RECORD_MELLOHI;
                case Item.RECORD_STAL -> LevelSoundEventPacket.SOUND_RECORD_STAL;
                case Item.RECORD_STRAD -> LevelSoundEventPacket.SOUND_RECORD_STRAD;
                case Item.RECORD_WARD -> LevelSoundEventPacket.SOUND_RECORD_WARD;
                case Item.RECORD_11 -> LevelSoundEventPacket.SOUND_RECORD_11;
                case Item.RECORD_WAIT -> LevelSoundEventPacket.SOUND_RECORD_WAIT;
                case Item.RECORD_PIGSTEP -> LevelSoundEventPacket.SOUND_RECORD_PIGSTEP;
                case Item.RECORD_OTHERSIDE -> LevelSoundEventPacket.SOUND_RECORD_OTHERSIDE;
                case Item.RECORD_5 -> LevelSoundEventPacket.SOUND_RECORD_5;
                case Item.RECORD_RELIC -> LevelSoundEventPacket.SOUND_RECORD_RELIC;
                case Item.STRING_IDENTIFIED_ITEM -> switch (this.recordItem.getNamespaceId()) {
                    case ItemNamespaceId.MUSIC_DISC_CREATOR -> LevelSoundEventPacket.SOUND_RECORD_CREATOR;
                    case ItemNamespaceId.MUSIC_DISC_CREATOR_MUSIC_BOX -> LevelSoundEventPacket.SOUND_RECORD_CREATOR_MUSIC_BOX;
                    case ItemNamespaceId.MUSIC_DISC_PRECIPICE -> LevelSoundEventPacket.SOUND_RECORD_PRECIPICE;
                    case ItemNamespaceId.MUSIC_DISC_LAVA_CHICKEN -> LevelSoundEventPacket.SOUND_RECORD_LAVA_CHICKEN;
                    case ItemNamespaceId.MUSIC_DISC_TEARS -> LevelSoundEventPacket.SOUND_RECORD_TEARS;
                    default ->
                            throw new IllegalStateException("Sound is not implemented for item: " + this.recordItem.getNamespaceId());
                };
                default ->
                        throw new IllegalStateException("Sound is not implemented for item: " + this.recordItem.getId());
            });
        }
    }

    public void stop() {
        this.getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_STOP_RECORD);
    }

    public void dropItem() {
        if (this.recordItem.getId() != 0) {
            stop();
            this.level.dropItem(this.up(), this.recordItem);
            this.recordItem = Item.get(0);
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putCompound("RecordItem", NBTIO.putItemHelper(this.recordItem));
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return getDefaultCompound(this, JUKEBOX);
    }

    @Override
    public void onBreak() {
        this.dropItem();
    }

    /**
     * 获取唱片机对应的比较器信号强度。
     * Get the comparator signal strength corresponding to the jukebox.
     *
     * <p>该方法根据唱片机中当前放置的唱片类型返回对应的比较器信号强度值。
     * 如果唱片机中没有放置唱片或者放置的物品不是唱片，将返回 0。</p>
     * <p>This method returns the corresponding comparator signal strength value based on the type of the record currently placed in the jukebox.
     * If there is no record in the jukebox or the placed item is not a record, it will return 0.</p>
     *
     * @return 比较器信号强度值，范围从 0 到 15。
     * The comparator signal strength value, ranging from 0 to 15.
     */
    public int getComparatorSignal() {
        if (this.recordItem instanceof ItemRecord) {
            return switch (this.recordItem.getId()) {
                case Item.RECORD_13 -> 1;
                case Item.RECORD_CAT -> 2;
                case Item.RECORD_BLOCKS -> 3;
                case Item.RECORD_CHIRP -> 4;
                case Item.RECORD_FAR -> 5;
                case Item.RECORD_MALL -> 6;
                case Item.RECORD_MELLOHI -> 7;
                case Item.RECORD_STAL -> 8;
                case Item.RECORD_STRAD -> 9;
                case Item.RECORD_WARD -> 10;
                case Item.RECORD_11 -> 11;
                case Item.RECORD_WAIT -> 12;
                case Item.RECORD_PIGSTEP -> 13;
                case Item.RECORD_OTHERSIDE -> 14;
                case Item.RECORD_5, Item.RECORD_RELIC -> 15;
                default -> 0;
            };
        }
        return 0;
    }
}