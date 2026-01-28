package cn.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.block.BlockGrindstone;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class GrindstoneInventory extends FakeBlockUIComponent {

    private static final int EQUIPMENT = 0;
    private static final int INGREDIENT = 1;

    public static final int GRINDSTONE_EQUIPMENT_UI_SLOT = 16;
    public static final int GRINDSTONE_INGREDIENT_UI_SLOT = 17;

    public GrindstoneInventory(PlayerUIInventory playerUI, Position position) {
        super(playerUI, InventoryType.GRINDSTONE, 16, position);
    }

    public Item getResult() {
        Item eq = getEquipment();
        Item iq = getIngredient();

        if(eq == null || eq.isNull()) {
            eq = iq.clone();
            iq = Item.get(Item.AIR);
        }

        if (!(eq instanceof ItemDurable)) {
            if (eq.getId() == Item.ENCHANTED_BOOK) {
                return Item.get(Item.BOOK);
            }
            return Item.get(Item.AIR);
        }

        CompoundTag tag = eq.getNamedTag();
        if (tag == null) tag = new CompoundTag();
        eq.setNamedTag(tag.remove("ench").putInt("RepairCost", 0));
        if (eq.getNamespaceId().equals(iq.getNamespaceId())) {
            // Output durability is the sum of the durabilities of the two input items
            // plus 5% of the maximum durability of the output item (rounded down)
            // capped so it does not exceed the maximum durability of the output item
            // https://minecraft.wiki/w/Grindstone#Repairing_and_disenchanting
            eq.setDamage(Math.max(eq.getMaxDurability() - ((eq.getMaxDurability() - eq.getDamage()) + (iq.getMaxDurability() - iq.getDamage()) + NukkitMath.floorDouble(eq.getMaxDurability() * 0.05)) + 1, 0));
        }
        if(holder instanceof Position position) {
            if(position.getLevelBlock() instanceof BlockGrindstone grindstone) {
                grindstone.releaseExperience(recalculateResultExperience());
            }
        }
        return eq;
    }

    public int recalculateResultExperience() {
        Item firstItem = this.getEquipment();
        Item secondItem = this.getIngredient();
        if (!firstItem.hasEnchantments() && !secondItem.hasEnchantments()) {
            return 0;
        }

        int resultExperience = Stream.of(firstItem, secondItem)
                .flatMap(item -> {
                    // Support stacks of enchanted items and skips invalid stacks (e.g. negative stacks, enchanted air)
                    if (item.isNull()) {
                        return Stream.empty();
                    } else if (item.getCount() == 1) {
                        return Arrays.stream(item.getEnchantments());
                    } else {
                        Enchantment[][] enchantments = new Enchantment[item.getCount()][];
                        Arrays.fill(enchantments, item.getEnchantments());
                        return Arrays.stream(enchantments).flatMap(Arrays::stream);
                    }
                })
                .mapToInt(enchantment -> enchantment.getMinEnchantAbility(enchantment.getLevel()))
                .sum();

        resultExperience = ThreadLocalRandom.current().nextInt(
                NukkitMath.ceilDouble((double) resultExperience / 2),
                resultExperience + 1
        );
        return resultExperience;
    }

    public Item getEquipment() {
        return getItem(EQUIPMENT);
    }

    public void setEquipment(Item equipment) {
        setItem(EQUIPMENT, equipment);
    }

    public Item getIngredient() {
        return getItem(INGREDIENT);
    }

    public void setIngredient(Item ingredient) {
        setItem(INGREDIENT, ingredient);
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        who.craftingType = Player.GRINDSTONE_WINDOW_ID;
    }
}