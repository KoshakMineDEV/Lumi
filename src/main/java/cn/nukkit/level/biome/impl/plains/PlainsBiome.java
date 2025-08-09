package cn.nukkit.level.biome.impl.plains;

import cn.nukkit.block.BlockDoublePlant;
import cn.nukkit.block.BlockFlower;
import cn.nukkit.block.BlockSapling;
import cn.nukkit.level.biome.type.GrassyBiome;
import cn.nukkit.level.generator.populator.impl.PopulatorFlower;
import cn.nukkit.level.generator.populator.impl.PopulatorPumpkin;
import cn.nukkit.level.generator.populator.impl.PopulatorTree;

/**
 * @author DaPorkchop_
 * Nukkit Project
 */
public class PlainsBiome extends GrassyBiome {

    public PlainsBiome() {
        super();

        PopulatorTree trees = new PopulatorTree(BlockSapling.OAK);
        trees.setRandomAmount(1);
        this.addPopulator(trees);

        this.addPopulator(new PopulatorPumpkin());

        PopulatorFlower flower = new PopulatorFlower();
        flower.setRandomAmount(3);
        flower.addType(DANDELION, 0);
        flower.addType(POPPY, 0);
        flower.addType(AZURE_BLUET, 0);
        flower.addType(RED_TULIP, 0);
        flower.addType(ORANGE_TULIP, 0);
        flower.addType(WHITE_TULIP, 0);
        flower.addType(PINK_TULIP, 0);
        flower.addType(OXEYE_DAISY, 0);
        flower.addType(CORNFLOWER, 0);
        flower.addType(DOUBLE_PLANT, BlockDoublePlant.LILAC);
        this.addPopulator(flower);

        this.setBaseHeight(0.125f);
        this.setHeightVariation(0.05f);
    }

    @Override
    public String getName() {
        return "Plains";
    }
}
