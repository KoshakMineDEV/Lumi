package cn.nukkit.level.generator;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockStone;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeSelector;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.noise.vanilla.f.NoiseGeneratorOctavesF;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.level.generator.populator.impl.*;
import cn.nukkit.level.generator.populator.overworld.*;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.level.generator.task.ChunkPopulationTask;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

/**
 * Nukkit's terrain generator
 * Originally adapted from the PocketMine-MP generator by NycuRO and CreeperFace
 * Mostly rewritten by DaPorkchop_
 * <p>
 * The generator classes, and others related to terrain generation are theirs and are intended for NUKKIT USAGE and should not be copied/translated to other server software
 * such as BukkitPE, ClearSky, Genisys, PocketMine-MP, or others
 */
public class Normal extends Generator {
    public static final int BEDROCK_LAYER = -64;

    private static final float[] biomeWeights = new float[25];

    static {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                biomeWeights[i + 2 + (j + 2) * 5] = (float) (10.0F / Math.sqrt((float) (i * i + j * j) + 0.2F));
            }
        }
    }

    private List<Populator> generationPopulators = ImmutableList.of(
            new PopulatorDeepslate(BEDROCK_LAYER),
            new PopulatorGroundCover()
    );
    private List<Populator> populators = ImmutableList.of(
            new PopulatorOre(STONE, new OreType[]{
                    new OreType(Block.get(BlockID.COAL_ORE), 20, 17, 0, 128),
                    new OreType(Block.get(BlockID.COPPER_ORE), 17, 9, 0, 64),
                    new OreType(Block.get(BlockID.IRON_ORE), 20, 9, 0, 64),
                    new OreType(Block.get(BlockID.REDSTONE_ORE), 8, 8, 0, 16),
                    new OreType(Block.get(BlockID.LAPIS_ORE), 1, 7, 0, 30),
                    new OreType(Block.get(BlockID.GOLD_ORE), 2, 9, 0, 32),
                    new OreType(Block.get(BlockID.DIAMOND_ORE), 1, 8, 0, 16),
                    new OreType(Block.get(BlockID.DIRT), 10, 33, 0, 128),
                    new OreType(Block.get(BlockID.GRAVEL), 8, 33, 0, 128),
                    new OreType(Block.get(BlockID.STONE, BlockStone.GRANITE), 10, 33, 0, 80),
                    new OreType(Block.get(BlockID.STONE, BlockStone.DIORITE), 10, 33, 0, 80),
                    new OreType(Block.get(BlockID.STONE, BlockStone.ANDESITE), 10, 33, 0, 80),
                    new OreType(Block.get(BlockID.DEEPSLATE), 20, 33, 0, 8)
            }),
            new PopulatorOre(BlockID.DEEPSLATE, new OreType[]{
                    new OreType(Block.get(BlockID.DEEPSLATE_COAL_ORE), 1, 13, -4, 8, BlockID.DEEPSLATE),
                    new OreType(Block.get(BlockID.DEEPSLATE_COPPER_ORE), 5, 9, -64, 8, BlockID.DEEPSLATE),
                    new OreType(Block.get(BlockID.DEEPSLATE_IRON_ORE), 5, 9, -64, 8, BlockID.DEEPSLATE),
                    new OreType(Block.get(BlockID.DEEPSLATE_REDSTONE_ORE), 8, 8, -64, 8, BlockID.DEEPSLATE),
                    new OreType(Block.get(BlockID.DEEPSLATE_LAPIS_ORE), 6, 6, -64, 8, BlockID.DEEPSLATE),
                    new OreType(Block.get(BlockID.DEEPSLATE_GOLD_ORE), 2, 9, -64, 8, BlockID.DEEPSLATE),
                    new OreType(Block.get(BlockID.DEEPSLATE_DIAMOND_ORE), 4, 5, -64, 8, BlockID.DEEPSLATE)
            }),
            new PopulatorCaves(BEDROCK_LAYER),
            new PopulatorSpring(BlockID.WATER, BlockID.STONE, 15, 8, 255),
            new PopulatorSpring(BlockID.LAVA, BlockID.STONE, 10, 16, 255),
            new PopulatorBedrock(BEDROCK_LAYER)
    );
    private List<Populator> structurePopulators = ImmutableList.of(
            new PopulatorFossil(),
            new PopulatorShipwreck(),
            new PopulatorSwampHut(),
            new PopulatorDesertPyramid(),
            new PopulatorJungleTemple(),
            new PopulatorIgloo(),
            new PopulatorPillagerOutpost(),
            new PopulatorOceanRuin(),
            new PopulatorStronghold(),
            new PopulatorMineshaft(),
            new PopulatorDesertWell(),
            new PopulatorDungeon()
    );
    public static final int seaHeight = 64; // should be 62
    public NoiseGeneratorOctavesF scaleNoise;
    public NoiseGeneratorOctavesF depthNoise;
    private ChunkManager level;
    private NukkitRandom nukkitRandom;
    private long localSeed1;
    private long localSeed2;
    private BiomeSelector selector;
    private ThreadLocal<float[]> depthRegion = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<float[]> mainNoiseRegion = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<float[]> minLimitRegion = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<float[]> maxLimitRegion = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<float[]> heightMap = ThreadLocal.withInitial(() -> new float[825]);
    private NoiseGeneratorOctavesF minLimitPerlinNoise;
    private NoiseGeneratorOctavesF maxLimitPerlinNoise;
    private NoiseGeneratorOctavesF mainPerlinNoise;

    public Normal() {
        this(Collections.emptyMap());
    }

    public Normal(Map<String, Object> options) {
    }

    @Override
    public int getId() {
        return TYPE_INFINITE;
    }

    @Override
    public ChunkManager getChunkManager() {
        return level;
    }

    @Override
    public String getName() {
        return "normal";
    }

    @Override
    public Map<String, Object> getSettings() {
        return Collections.emptyMap();
    }

    public Biome pickBiome(int x, int z) {
        return this.selector.pickBiome(x, z);
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.nukkitRandom = random;
        SplittableRandom random1 = new SplittableRandom();
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.localSeed1 = random1.nextLong();
        this.localSeed2 = random1.nextLong();
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.selector = new BiomeSelector(this.nukkitRandom);

        this.minLimitPerlinNoise = new NoiseGeneratorOctavesF(random, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctavesF(random, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctavesF(random, 8);
        this.scaleNoise = new NoiseGeneratorOctavesF(random, 10);
        this.depthNoise = new NoiseGeneratorOctavesF(random, 16);
    }

    @Override
    public void populateStructure(final int chunkX, final int chunkZ) {
        final BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);
        for (final Populator populator : structurePopulators) {
            Server.getInstance().computeThreadPool.submit(new ChunkPopulationTask(level, chunk, populator));
        }
    }

    @Override
    public void generateChunk(final int chunkX, final int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        this.nukkitRandom.setSeed(chunkX * localSeed1 ^ chunkZ * localSeed2 ^ this.level.getSeed());

        BaseFullChunk chunk = level.getChunk(chunkX, chunkZ);

        //generate base noise values
        float[] depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion.get(), chunkX << 2, chunkZ << 2, 5, 5, 200f, 200f, 0.5f);
        this.depthRegion.set(depthRegion);
        float[] mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion.get(), chunkX << 2, 0, chunkZ << 2, 5, 33, 5, 11.406866f, 4.277575f, 11.406866f);
        this.mainNoiseRegion.set(mainNoiseRegion);
        float[] minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion.get(), chunkX << 2, 0, chunkZ << 2, 5, 33, 5, 684.412f, 684.412f, 684.412f);
        this.minLimitRegion.set(minLimitRegion);
        float[] maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion.get(), chunkX << 2, 0, chunkZ << 2, 5, 33, 5, 684.412f, 684.412f, 684.412f);
        this.maxLimitRegion.set(maxLimitRegion);
        float[] heightMap = this.heightMap.get();

        //generate heightmap and smooth biome heights
        int horizCounter = 0;
        int vertCounter = 0;
        for (int xSeg = 0; xSeg < 5; ++xSeg) {
            for (int zSeg = 0; zSeg < 5; ++zSeg) {
                float heightVariationSum = 0.0F;
                float baseHeightSum = 0.0F;
                float biomeWeightSum = 0.0F;
                Biome biome = pickBiome(baseX + (xSeg << 2), baseZ + (zSeg << 2));

                for (int xSmooth = -2; xSmooth <= 2; ++xSmooth) {
                    for (int zSmooth = -2; zSmooth <= 2; ++zSmooth) {
                        Biome biome1 = pickBiome(baseX + (xSeg << 2) + xSmooth, baseZ + (zSeg << 2) + zSmooth);
                        float baseHeight = biome1.getBaseHeight();
                        float heightVariation = biome1.getHeightVariation();

                        float scaledWeight = biomeWeights[xSmooth + 2 + (zSmooth + 2) * 5] / (baseHeight + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight()) {
                            scaledWeight /= 2.0F;
                        }

                        heightVariationSum += heightVariation * scaledWeight;
                        baseHeightSum += baseHeight * scaledWeight;
                        biomeWeightSum += scaledWeight;
                    }
                }

                heightVariationSum = heightVariationSum / biomeWeightSum;
                baseHeightSum = baseHeightSum / biomeWeightSum;
                heightVariationSum = heightVariationSum * 0.9F + 0.1F;
                baseHeightSum = (baseHeightSum * 4.0F - 1.0F) / 8.0F;
                float depthNoise = depthRegion[vertCounter] / 8000.0f;

                if (depthNoise < 0.0f) {
                    depthNoise = -depthNoise * 0.3f;
                }

                depthNoise = depthNoise * 3.0f - 2.0f;

                if (depthNoise < 0.0f) {
                    depthNoise = depthNoise / 2.0f;

                    if (depthNoise < -1.0f) {
                        depthNoise = -1.0f;
                    }

                    depthNoise = depthNoise / 1.4f;
                    depthNoise = depthNoise / 2.0f;
                } else {
                    if (depthNoise > 1.0f) {
                        depthNoise = 1.0f;
                    }

                    depthNoise = depthNoise / 8.0f;
                }

                ++vertCounter;
                float baseHeightClone = baseHeightSum;
                float heightVariationClone = heightVariationSum;
                baseHeightClone = baseHeightClone + depthNoise * 0.2f;
                baseHeightClone = baseHeightClone * 8.5f / 8.0f;
                float baseHeightFactor = 8.5f + baseHeightClone * 4.0f;

                for (int ySeg = 0; ySeg < 33; ++ySeg) {
                    float baseScale = ((float) ySeg - baseHeightFactor) * 12f * 128.0f / 256.0f / heightVariationClone;

                    if (baseScale < 0.0f) {
                        baseScale *= 4.0f;
                    }

                    float minScaled = minLimitRegion[horizCounter] / 512f;
                    float maxScaled = maxLimitRegion[horizCounter] / 512f;
                    float noiseScaled = (mainNoiseRegion[horizCounter] / 10.0f + 1.0f) / 2.0f;
                    float clamp = MathHelper.denormalizeClamp(minScaled, maxScaled, noiseScaled) - baseScale;

                    if (ySeg > 29) {
                        float yScaled = ((float) (ySeg - 29) / 3.0F);
                        clamp = clamp * (1.0f - yScaled) + -10.0f * yScaled;
                    }

                    heightMap[horizCounter] = clamp;
                    ++horizCounter;
                }
            }
        }

        //place blocks
        for (int xSeg = 0; xSeg < 4; ++xSeg) {
            int xScale = xSeg * 5;
            int xScaleEnd = (xSeg + 1) * 5;

            for (int zSeg = 0; zSeg < 4; ++zSeg) {
                int zScale1 = (xScale + zSeg) * 33;
                int zScaleEnd1 = (xScale + zSeg + 1) * 33;
                int zScale2 = (xScaleEnd + zSeg) * 33;
                int zScaleEnd2 = (xScaleEnd + zSeg + 1) * 33;

                for (int ySeg = 0; ySeg < 32; ++ySeg) {
                    double height1 = heightMap[zScale1 + ySeg];
                    double height2 = heightMap[zScaleEnd1 + ySeg];
                    double height3 = heightMap[zScale2 + ySeg];
                    double height4 = heightMap[zScaleEnd2 + ySeg];
                    double height5 = (heightMap[zScale1 + ySeg + 1] - height1) * 0.125f;
                    double height6 = (heightMap[zScaleEnd1 + ySeg + 1] - height2) * 0.125f;
                    double height7 = (heightMap[zScale2 + ySeg + 1] - height3) * 0.125f;
                    double height8 = (heightMap[zScaleEnd2 + ySeg + 1] - height4) * 0.125f;

                    for (int yIn = 0; yIn < 8; ++yIn) {
                        double baseIncr = height1;
                        double baseIncr2 = height2;
                        double scaleY = (height3 - height1) * 0.25f;
                        double scaleY2 = (height4 - height2) * 0.25f;

                        for (int zIn = 0; zIn < 4; ++zIn) {
                            double scaleZ = (baseIncr2 - baseIncr) * 0.25f;
                            double scaleZ2 = baseIncr - scaleZ;

                            for (int xIn = 0; xIn < 4; ++xIn) {
                                if ((scaleZ2 += scaleZ) > 0.0f) {
                                    chunk.setBlockId((xSeg << 2) + zIn, (ySeg << 3) + yIn, (zSeg << 2) + xIn, STONE);
                                } else if ((ySeg << 3) + yIn <= seaHeight) {
                                    chunk.setBlockId((xSeg << 2) + zIn, (ySeg << 3) + yIn, (zSeg << 2) + xIn, STILL_WATER);
                                }
                            }

                            baseIncr += scaleY;
                            baseIncr2 += scaleY2;
                        }

                        height1 += height5;
                        height2 += height6;
                        height3 += height7;
                        height4 += height8;
                    }
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBiome(x, z, selector.pickBiome(baseX | x, baseZ | z));
            }
        }

        //populate chunk
        for (Populator populator : this.generationPopulators) {
            populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk);
        }
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        for (Populator populator : this.populators) {
            populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, level.getChunk(chunkX, chunkZ));
        }

        Biome biome = EnumBiome.getBiome(level.getChunk(chunkX, chunkZ).getBiomeId(7, 7));
        biome.populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(0.5, 256, 0.5);
    }
}