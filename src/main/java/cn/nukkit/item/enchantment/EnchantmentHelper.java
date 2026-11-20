package cn.nukkit.item.enchantment;

import cn.nukkit.block.BlockID;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.network.protocol.PlayerEnchantOptionsPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EnchantmentHelper {

    public static final int MAX_BOOKSHELF_COUNT = 15;

    private static final String[] OPTION_NAMES = {
            "ancient", "arcane", "bane", "breath", "cold", "conjuring",
            "craft", "enchant", "free", "greater", "lesser", "light",
            "magic", "marvel", "mirror", "mystery", "primal", "rune",
            "secret", "shadow", "spell", "temporal"
    };

    public static List<PlayerEnchantOptionsPacket.EnchantOptionData> generateOptions(Position tablePos, Item input, long seed) {
        if (input == null || input.isNull() || input.hasEnchantments()) {
            return Collections.emptyList();
        }
        NukkitRandom random = new NukkitRandom(seed);
        int bookshelfCount = countBookshelves(tablePos);
        int selected = random.nextRange(1, 8) + (bookshelfCount >> 1) + random.nextRange(0, bookshelfCount);
        int[] requiredLevels = {
                Math.max(selected / 3, 1),
                selected * 2 / 3 + 1,
                Math.max(selected, bookshelfCount * 2)
        };
        List<PlayerEnchantOptionsPacket.EnchantOptionData> options = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            options.add(createOption(random, input, requiredLevels[i], i));
        }
        return options;
    }

    private static PlayerEnchantOptionsPacket.EnchantOptionData createOption(NukkitRandom random, Item input, int requiredLevel, int slot) {
        int enchantability = Math.max(1, input.getEnchantAbility());
        int power = requiredLevel + random.nextBoundedInt(enchantability >> 1 > 0 ? (enchantability >> 1) + 1 : 2);
        List<PlayerEnchantOptionsPacket.EnchantData> chosen = new ArrayList<>();
        List<Enchantment> applicable = filterApplicable(input, power);
        if (!applicable.isEmpty()) {
            Enchantment first = applicable.get(random.nextBoundedInt(applicable.size()));
            chosen.add(new PlayerEnchantOptionsPacket.EnchantData(first.getId(), first.getLevel()));

            int remainingPower = power / 2;
            while (remainingPower > 0 && !applicable.isEmpty() && random.nextRange(0, 50) <= remainingPower) {
                Enchantment pick = applicable.get(random.nextBoundedInt(applicable.size()));
                boolean compatible = true;
                for (PlayerEnchantOptionsPacket.EnchantData picked : chosen) {
                    if (picked.getType() == pick.getId()) {
                        compatible = false;
                        break;
                    }
                    Enchantment other = Enchantment.getEnchantment(picked.getType());
                    if (other != null && !other.isCompatibleWith(pick)) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible) {
                    chosen.add(new PlayerEnchantOptionsPacket.EnchantData(pick.getId(), pick.getLevel()));
                }
                remainingPower /= 2;
            }
        }

        String name = OPTION_NAMES[random.nextBoundedInt(OPTION_NAMES.length)];
        int netId = 0;
        PlayerEnchantOptionsPacket.EnchantOptionData option = new PlayerEnchantOptionsPacket.EnchantOptionData(
                requiredLevel, slot, chosen,
                Collections.emptyList(), Collections.emptyList(),
                name, netId
        );
        return option;
    }

    private static List<Enchantment> filterApplicable(Item input, int power) {
        List<Enchantment> result = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.getEnchantments()) {
            if (enchantment == null
                    || enchantment.isTreasure()
                    || !enchantment.canEnchant(input)) {
                continue;
            }
            for (int lvl = enchantment.getMaxLevel(); lvl > 0; lvl--) {
                if (power >= enchantment.getMinEnchantAbility(lvl) && power <= enchantment.getMaxEnchantAbility(lvl)) {
                    result.add(enchantment.clone().setLevel(lvl));
                    break;
                }
            }
        }
        return result;
    }

    private static int countBookshelves(Position tablePos) {
        if (tablePos == null) {
            return 0;
        }
        Level level = tablePos.getLevel();
        if (level == null) {
            return 0;
        }
        int tableX = tablePos.getFloorX();
        int tableY = tablePos.getFloorY();
        int tableZ = tablePos.getFloorZ();
        int bookshelfCount = 0;
        outer:
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Only positions exactly 2 blocks away on X or Z qualify.
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }

                // The middle block between bookshelf and table must be air (transmitter).
                // Integer division by 2 (truncating toward zero) matches JE
                // EnchantingTableBlock.isValidBookShelf's offset.getX()/2: ±2→±1, ±1/0→0.
                int midX = x / 2;
                int midZ = z / 2;
                for (int y = 0; y <= 1; y++) {
                    if (level.getBlockIdAt(tableX + midX, tableY + y, tableZ + midZ) != BlockID.AIR) {
                        continue outer;
                    }
                }

                // Count bookshelves at y=0 and y=1 on this column.
                for (int y = 0; y <= 1; y++) {
                    if (level.getBlockIdAt(tableX + x, tableY + y, tableZ + z) == BlockID.BOOKSHELF) {
                        bookshelfCount++;
                        if (bookshelfCount >= MAX_BOOKSHELF_COUNT) {
                            return bookshelfCount;
                        }
                    }
                }
            }
        }
        return bookshelfCount;
    }
}
