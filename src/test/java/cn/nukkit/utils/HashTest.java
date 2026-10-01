package cn.nukkit.utils;

import cn.nukkit.nbt.tag.CompoundTag;
import org.cloudburstmc.nbt.NbtMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author LT_Name
 */
public class HashTest {

    @Test
    public void test() {
        testHashBlock(-15000000, -64, -15000000);
        testHashBlock(-15000000, -64, 15000000);
        testHashBlock(-15000000, 319, -15000000);
        testHashBlock(-15000000, 319, 15000000);
        testHashBlock(15000000, -64, -15000000);
        testHashBlock(15000000, -64, 15000000);
        testHashBlock(15000000, 319, -15000000);
        testHashBlock(15000000, 319, 15000000);

        //测试随机
        for (int i = 0; i < 1000; i++) {
            int x = (int) (Math.random() * 30000000 - 15000000);
            int y = (int) (Math.random() * 384) - 64;
            int z = (int) (Math.random() * 30000000 - 15000000);

            testHashBlock(x, y, z);
        }
    }

    public static void testHashBlock(int x, int y, int z) {
        long triple = Hash.hashBlock(x, y, z);

        int x1 = Hash.hashBlockX(triple);
        int y1 = Hash.hashBlockY(triple);
        int z1 = Hash.hashBlockZ(triple);

        Assertions.assertEquals(x, x1);
        Assertions.assertEquals(y, y1);
        Assertions.assertEquals(z, z1);
    }

    @Test
    public void testBlockStateHashAcrossNbtImplementations() {
        CompoundTag nukkitState = new CompoundTag()
                .putString("name", "minecraft:test_block")
                .putCompound("states", new CompoundTag()
                        .putInt("second", 2)
                        .putString("first", "value"))
                .putInt("version", 1);

        NbtMap cloudburstState = NbtMap.builder()
                .putString("name", "minecraft:test_block")
                .putCompound("states", NbtMap.builder()
                        .putString("first", "value")
                        .putInt("second", 2)
                        .build())
                .putInt("version", 2)
                .build();

        Assertions.assertEquals(Hash.hashBlock(nukkitState), Hash.hashBlock(cloudburstState));
    }

}
