package cn.nukkit.item;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.trim.ItemTrimMaterialType;
import cn.nukkit.item.trim.ItemTrimPatternType;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
abstract public class ItemArmor extends Item implements ItemDurable {

    public static final int TIER_LEATHER = 1;
    public static final int TIER_IRON = 2;
    public static final int TIER_CHAIN = 3;
    public static final int TIER_GOLD = 4;
    public static final int TIER_DIAMOND = 5;
    public static final int TIER_NETHERITE = 6;
    public static final int TIER_OTHER = 7;

    public ItemArmor(int id) {
        super(id);
    }

    public ItemArmor(int id, Integer meta) {
        super(id, meta);
    }

    public ItemArmor(int id, Integer meta, int count) {
        super(id, meta, count);
    }

    public ItemArmor(int id, Integer meta, int count, String name) {
        super(id, meta, count, name);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isArmor() {
        return true;
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        boolean equip = false;
        Item oldSlotItem = Item.get(AIR);
        if (this.isHelmet()) {
            oldSlotItem = player.getInventory().getHelmetFast();
            if (player.getInventory().setHelmet(this)) {
                equip = true;
            }
        } else if (this.isChestplate()) {
            oldSlotItem = player.getInventory().getChestplateFast();
            if (player.getInventory().setChestplate(this)) {
                equip = true;
            }
        } else if (this.isLeggings()) {
            oldSlotItem = player.getInventory().getLeggingsFast();
            if (player.getInventory().setLeggings(this)) {
                equip = true;
            }
        } else if (this.isBoots()) {
            oldSlotItem = player.getInventory().getBootsFast();
            if (player.getInventory().setBoots(this)) {
                equip = true;
            }
        }
        if (equip) {
            player.getInventory().setItem(player.getInventory().getHeldItemIndex(), oldSlotItem);
            switch (this.getTier()) {
                case TIER_CHAIN:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_CHAIN);
                    break;
                case TIER_DIAMOND:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_DIAMOND);
                    break;
                case TIER_GOLD:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_GOLD);
                    break;
                case TIER_IRON:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_IRON);
                    break;
                case TIER_LEATHER:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_LEATHER);
                    break;
                case TIER_NETHERITE:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_NETHERITE);
                    break;
                case TIER_OTHER:
                default:
                    player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_GENERIC);
            }

            return this.getCount() == 0;
        }
        return false;
    }

    @Override
    public int getEnchantAbility() {
        switch (this.getTier()) {
            case TIER_CHAIN:
                return 12;
            case TIER_LEATHER:
                return 15;
            case TIER_DIAMOND:
                return 10;
            case TIER_GOLD:
                return 25;
            case TIER_IRON:
                return 9;
            case TIER_NETHERITE:
                return 10; //TODO
        }

        return 0;
    }

    @Override
    public boolean canBePutInHelmetSlot() {
        return this.isHelmet();
    }

    public void setTrim(ItemTrimPatternType pattern, ItemTrimMaterialType material) {
        CompoundTag tag = new CompoundTag();
        if (this.hasCompoundTag()) {
            tag = this.getNamedTag();
        }
        tag = tag.putCompound("Trim", new CompoundTag()
                .putString("Pattern", pattern.getTrimPattern())
                .putString("Material", material.getMaterialName()));
        this.setCompoundTag(tag);
    }
}
