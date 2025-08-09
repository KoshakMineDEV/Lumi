package cn.nukkit.level.biome.impl.forest;

import cn.nukkit.block.BlockDoublePlant;
import cn.nukkit.block.BlockFlower;
import cn.nukkit.level.generator.populator.impl.PopulatorFlower;

/**
 * @author DaPorkchop_
 * Nukkit Project
 */
public class FlowerForestBiome extends ForestBiome {
    public FlowerForestBiome() {
        this(TYPE_NORMAL);
    }

    public FlowerForestBiome(int type) {
        super(type);

        //https://minecraft.wiki/w/Flower#Natural_generation
        PopulatorFlower flower = new PopulatorFlower();
        flower.setBaseAmount(10);
        flower.addType(POPPY, 0);
        flower.addType(ALLIUM, 0);
        flower.addType(AZURE_BLUET, 0);
        flower.addType(RED_TULIP, 0);
        flower.addType(ORANGE_TULIP, 0);
        flower.addType(WHITE_TULIP, 0);
        flower.addType(PINK_TULIP, 0);
        flower.addType(OXEYE_DAISY, 0);
        flower.addType(CORNFLOWER, 0);
        flower.addType(LILY_OF_THE_VALLEY, 0);
        flower.addType(DOUBLE_PLANT, BlockDoublePlant.LILAC);
        flower.addType(DOUBLE_PLANT, BlockDoublePlant.ROSE_BUSH);
        flower.addType(DOUBLE_PLANT, BlockDoublePlant.PEONY);
        this.addPopulator(flower);

        this.setHeightVariation(0.4f);
    }

    @Override
    public String getName() {
        return this.type == TYPE_BIRCH ? "Birch Forest" : "Forest";
    }
}
