package cn.nukkit.item.enchantment;

import lombok.Getter;

@Getter
public enum EnchantmentRarity {
    COMMON(10),
    UNCOMMON(5),
    RARE(2),
    VERY_RARE(1);

    private final int weight;

    EnchantmentRarity(int weight) {
        this.weight = weight;
    }

    public static EnchantmentRarity fromWeight(int weight) {
        if (weight < 2) {
            return VERY_RARE;
        } else if (weight < 5) {
            return RARE;
        } else if (weight < 10) {
            return UNCOMMON;
        }
        return COMMON;
    }
}
