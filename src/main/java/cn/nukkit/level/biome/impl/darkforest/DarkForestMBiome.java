package cn.nukkit.level.biome.impl.darkforest;

public class DarkForestMBiome extends DarkForestBiome {

    public DarkForestMBiome() {
        super();

        this.setBaseHeight(0.2f);
        this.setHeightVariation(0.4f);
    }

    @Override
    public String getName() {
        return "Roofed Forest M";
    }
}
