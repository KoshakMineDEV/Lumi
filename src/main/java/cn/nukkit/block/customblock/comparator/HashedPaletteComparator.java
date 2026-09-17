package cn.nukkit.block.customblock.comparator;

import cn.nukkit.utils.Hash;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class HashedPaletteComparator implements Comparator<String> {
    public static final HashedPaletteComparator INSTANCE = new HashedPaletteComparator();

    @Override
    public int compare(String o1, String o2) {
        byte[] b1 = o1.getBytes(StandardCharsets.UTF_8);
        byte[] b2 = o2.getBytes(StandardCharsets.UTF_8);
        long hash1 = Hash.fnv1_64(b1);
        long hash2 = Hash.fnv1_64(b2);
        return Long.compareUnsigned(hash1, hash2);
    }
}
