package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.custom.properties.BlockProperties;
import cn.nukkit.block.custom.properties.BooleanBlockProperty;
import cn.nukkit.block.custom.properties.EnumBlockProperty;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.enums.DripstoneThickness;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.event.block.BlockFallEvent;
import cn.nukkit.event.block.BlockGrowEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Faceable;
import it.unimi.dsi.fastutil.ints.IntObjectPair;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class BlockPointedDripstone extends BlockFallable implements BlockPropertiesHelper, Faceable {

    private static final EnumBlockProperty<DripstoneThickness> THICKNESS = new EnumBlockProperty<>("dripstone_thickness", false, DripstoneThickness.class);
    private static final BooleanBlockProperty HANGING = new BooleanBlockProperty("hanging", false);
    private static final BlockProperties PROPERTIES = new BlockProperties(HANGING, THICKNESS);

    public BlockPointedDripstone() {
        this(0);
    }

    public BlockPointedDripstone(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Pointed Dripstone";
    }

    @Override
    public int getId() {
        return POINTED_DRIPSTONE;
    }

    @Override
    public String getIdentifier() {
        return "minecraft:pointed_dripstone";
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM && this.getThickness() == DripstoneThickness.TIP) {
            Random rand = new Random();
            double nextDouble = rand.nextDouble();
            if (nextDouble <= 0.011377778) {
                this.grow();
            }

            drippingLiquid();
        }
        boolean hanging = getPropertyValue(HANGING);
        if (!hanging) {
            Block down = down();
            if (!down.isSolid()) {
                this.getLevel().useBreakOn(this);
            }
        }
        tryDrop(hanging);
        return 0;
    }

    public void tryDrop(boolean hanging) {
        if (!hanging) return;
        boolean AirUp = false;
        Block blockUp = this.clone();
        while (blockUp.getSide(BlockFace.UP).getId() == POINTED_DRIPSTONE) {
            blockUp = blockUp.getSide(BlockFace.UP);
        }
        if (!blockUp.getSide(BlockFace.UP).isSolid())
            AirUp = true;
        if (AirUp) {
            BlockFallEvent event = new BlockFallEvent(this);
            event.call();
            if (event.isCancelled()) {
                return;
            }
            BlockPointedDripstone block = (BlockPointedDripstone) blockUp;
            block.drop(new CompoundTag().putBoolean("BreakOnGround", true));
            while (block.getSide(BlockFace.DOWN).getId()== POINTED_DRIPSTONE) {
                block = (BlockPointedDripstone) block.getSide(BlockFace.DOWN);
                block.drop(new CompoundTag().putBoolean("BreakOnGround", true));
            }
        }
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        int placeX = block.getFloorX();
        int placeY = block.getFloorY();
        int placeZ = block.getFloorZ();
        int upBlockID = level.getBlockIdAt(placeX, placeY + 1, placeZ);
        int downBlockID = level.getBlockIdAt(placeX, placeY - 1, placeZ);
        if (Objects.equals(upBlockID, AIR) && Objects.equals(downBlockID, AIR)) return false;
        /*    "up" define is exist drip stone in block above,"down" is Similarly.
              up   down
          1   yes   yes
          2   yes   no
          3   no    no
          4   no    yes
        */
        int state = (Objects.equals(upBlockID, POINTED_DRIPSTONE)) ?
                (Objects.equals(downBlockID, POINTED_DRIPSTONE) ? 1 : 2) :
                (!Objects.equals(downBlockID, POINTED_DRIPSTONE) ? 3 : 4
                );
        boolean hanging = false;
        switch (state) {
            case 1 -> {
                setMergeBlock(placeX, placeY, placeZ, false);
                setBlockThicknessStateAt(placeX, placeY + 1, placeZ, true, DripstoneThickness.MERGE);
            }
            case 2 -> {
                if (!Objects.equals(level.getBlockIdAt(placeX, placeY - 1, placeZ), AIR)) {
                    if (face.equals(BlockFace.UP)) {
                        setBlockThicknessStateAt(placeX, placeY + 1, placeZ, true, DripstoneThickness.MERGE);
                        setMergeBlock(placeX, placeY, placeZ, false);
                    } else {
                        setTipBlock(placeX, placeY, placeZ, true);
                        setAddChange(placeX, placeY, placeZ, true);
                    }
                    return true;
                }
                hanging = true;
            }
            case 3 -> {
                setTipBlock(placeX, placeY, placeZ, !face.equals(BlockFace.UP));
                return true;
            }
            case 4 -> {
                if (!Objects.equals(level.getBlockIdAt(placeX, placeY + 1, placeZ), AIR)) {
                    if (face.equals(BlockFace.DOWN)) {
                        setMergeBlock(placeX, placeY, placeZ, true);
                        setBlockThicknessStateAt(placeX, placeY - 1, placeZ, false, DripstoneThickness.MERGE);
                    } else {
                        setTipBlock(placeX, placeY, placeZ, false);
                        setAddChange(placeX, placeY, placeZ, false);
                    }
                    return true;
                }
            }
        }
        setAddChange(placeX, placeY, placeZ, hanging);

        if (state == 1) return true;
        setTipBlock(placeX, placeY, placeZ, hanging);
        return true;
    }

    @Override
    public boolean onBreak(Item item) {
        int x = this.getFloorX();
        int y = this.getFloorY();
        int z = this.getFloorZ();
        level.setBlock(x, y, z, Block.get(AIR), true, true);
        boolean hanging = getPropertyValue(HANGING);
        DripstoneThickness thickness = getThickness();
        if (thickness == DripstoneThickness.MERGE) {
            if (!hanging) {
                setBlockThicknessStateAt(x, y + 1, z, true, DripstoneThickness.TIP);
            } else setBlockThicknessStateAt(x, y - 1, z, false, DripstoneThickness.TIP);
        }
        if (!hanging) {
            int length = getPointedDripStoneLength(x, y, z, false);
            if (length > 0) {
                Block downBlock = down();
                for (int i = 0; i <= length - 1; ++i) {
                    level.setBlock(downBlock.down(i), Block.get(AIR), false, false);
                }
                for (int i = length - 1; i >= 0; --i) {
                    place(null, downBlock.down(i), null, BlockFace.DOWN, 0, 0, 0, null);
                }
            }
        }
        if (hanging) {
            int length = getPointedDripStoneLength(x, y, z, true);
            if (length > 0) {
                Block upBlock = up();
                for (int i = 0; i <= length - 1; ++i) {
                    level.setBlock(upBlock.up(i), Block.get(AIR), false, false);
                }
                for (int i = length - 1; i >= 0; --i) {
                    place(null, upBlock.up(i), null, BlockFace.DOWN, 0, 0, 0, null);
                }
            }
        }

        return true;
    }

    /*@Override
    public void onEntityFallOn(Entity entity, float fallDistance) {
        if (this.level.gameRules.getBoolean(GameRule.FALL_DAMAGE) && this.getPropertyValue(DRIPSTONE_THICKNESS) == DripstoneThickness.TIP && !this.getPropertyValue(HANGING)) {
            int jumpBoost = entity.hasEffect(EffectType.JUMP_BOOST) ? Effect.get(EffectType.JUMP_BOOST).getLevel() : 0;
            float damage = (fallDistance - jumpBoost) * 2 - 2;
            if (damage > 0)
                entity.attack(new EntityDamageEvent(entity, EntityDamageEvent.DamageCause.FALL, damage));
        }
    }

    @Override
    public boolean useDefaultFallDamage() {
        return false;
    }*/

    protected void setTipBlock(int x, int y, int z, boolean hanging) {
        setThickness(DripstoneThickness.TIP);
        this.setPropertyValue(HANGING, hanging);
        this.getLevel().setBlock(x, y, z, this, true, true);
    }

    protected void setMergeBlock(int x, int y, int z, boolean hanging) {
        this.setThickness(DripstoneThickness.MERGE);
        this.setPropertyValue(HANGING, hanging);
        this.getLevel().setBlock(x, y, z, this, true, true);
    }

    protected void setBlockThicknessStateAt(int x, int y, int z, boolean hanging, DripstoneThickness thickness) {
        BlockPointedDripstone blockState;
        this.setThickness(thickness);
        this.setPropertyValue(HANGING, hanging);
        blockState = this;
        level.setBlock(x, y, z, blockState, true,false);
    }

    protected int getPointedDripStoneLength(int x, int y, int z, boolean hanging) {
        if (hanging) {
            for (int j = y + 1; j < getLevel().getDimensionData().getMaxHeight(); ++j) {
                int blockId = level.getBlockIdAt(x, j, z);
                if (!Objects.equals(blockId, POINTED_DRIPSTONE)) {
                    return j - y - 1;
                }
            }
        } else {
            for (int j = y - 1; j > getLevel().getDimensionData().getMinHeight(); --j) {
                int blockId = level.getBlockIdAt(x, j, z);
                if (!Objects.equals(blockId, POINTED_DRIPSTONE)) {
                    return y - j - 1;
                }
            }
        }
        return 0;
    }

    protected void setAddChange(int x, int y, int z, boolean hanging) {
        int length = getPointedDripStoneLength(x, y, z, hanging);
        int k2 = !hanging ? -2 : 2;
        int k1 = !hanging ? -1 : 1;
        if (length == 1) {
            setBlockThicknessStateAt(x, y + k1, z, hanging, DripstoneThickness.FRUSTUM);
        }
        if (length == 2) {
            setBlockThicknessStateAt(x, y + k2, z, hanging, DripstoneThickness.BASE);
            setBlockThicknessStateAt(x, y + k1, z, hanging, DripstoneThickness.FRUSTUM);
        }
        if (length >= 3) {
            setBlockThicknessStateAt(x, y + k2, z, hanging, DripstoneThickness.MIDDLE);
            setBlockThicknessStateAt(x, y + k1, z, hanging, DripstoneThickness.FRUSTUM);
        }
    }

    public void grow() {
        BlockFace face = this.isHanging() ? BlockFace.DOWN : BlockFace.UP;
        Block target = this.getSide(face);
        if (target.isAir()) {
            this.place(null, target, null, face, 0, 0, 0, null);
        }
    }

    public void drippingLiquid() {//features according to https://zh.minecraft.wiki/w/%E6%BB%B4%E6%B0%B4%E7%9F%B3%E9%94%A5
        if (this.getLevelBlockAtLayer(1) instanceof BlockLiquid || this.getThickness() != DripstoneThickness.TIP || !this.isHanging()) {
            return;
        }
        Block highestPDS = this;
        int height = 1;
        while (highestPDS.getSide(BlockFace.UP) instanceof BlockPointedDripstone) {
            highestPDS = highestPDS.getSide(BlockFace.UP);
            height++;
        }

        boolean isWaterloggingBlock = false;
        if (height >= 11 ||
                !(highestPDS.getSide(BlockFace.UP, 2) instanceof BlockLiquid ||
                        highestPDS.getSide(BlockFace.UP, 2).getLevelBlockAtLayer(1) instanceof BlockWater)
        ) {
            return;
        }

        if (highestPDS.getSide(BlockFace.UP, 2).getLevelBlockAtLayer(1) instanceof BlockWater) {
            isWaterloggingBlock = true;
        }

        Block tmp = this;
        BlockCauldron cauldron;
        while (tmp.getSide(BlockFace.DOWN) instanceof BlockAir) {
            tmp = tmp.getSide(BlockFace.DOWN);
        }
        if (tmp.getSide(BlockFace.DOWN) instanceof BlockCauldron) {
            cauldron = (BlockCauldron) tmp.getSide(BlockFace.DOWN);
        } else {
            return;
        }

        Random rand = new Random();
        double nextDouble;
        Block filledWith = isWaterloggingBlock ? highestPDS.getSideAtLayer(1, BlockFace.UP, 2) : highestPDS.getSide(BlockFace.UP, 2);
        /*switch (filledWith.getId()) {
            case FLOWING_LAVA -> {
                nextDouble = rand.nextDouble();
                if ((cauldron.getCauldronLiquid() == CauldronLiquid.LAVA || cauldron.isEmpty()) && cauldron.getFillLevel() < 6 && nextDouble <= 15.0 / 256.0) {
                    CauldronFilledByDrippingLiquidEvent event = new CauldronFilledByDrippingLiquidEvent(cauldron, CauldronLiquid.LAVA, 1);
                    Server.getInstance().getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return;
                    cauldron.setCauldronLiquid(event.getLiquid());
                    cauldron.setFillLevel(cauldron.getFillLevel() + event.getLiquidLevelIncrement());
                    cauldron.level.setBlock(cauldron, cauldron, true, true);
                    this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_DRIP_LAVA_POINTED_DRIPSTONE);
                }
            }
            case FLOWING_WATER -> {
                nextDouble = rand.nextDouble();
                if ((cauldron.getCauldronLiquid() == CauldronLiquid.WATER || cauldron.isEmpty()) && cauldron.getFillLevel() < 6 && nextDouble <= 45.0 / 256.0) {
                    CauldronFilledByDrippingLiquidEvent event = new CauldronFilledByDrippingLiquidEvent(cauldron, CauldronLiquid.WATER, 1);
                    Server.getInstance().getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return;
                    cauldron.setCauldronLiquid(event.getLiquid());
                    cauldron.setFillLevel(cauldron.getFillLevel() + event.getLiquidLevelIncrement());
                    cauldron.level.setBlock(cauldron, cauldron, true, true);
                    this.getLevel().addSound(this.add(0.5, 1, 0.5), Sound.CAULDRON_DRIP_WATER_POINTED_DRIPSTONE);
                }
            }
        }*/
    }

    @Override
    public double getHardness() {
        return 1.5;
    }

    @Override
    public double getResistance() {
        return 3;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(this.getId()), 0, 1);
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BROWN_TERRACOTA_BLOCK_COLOR;
    }

    @Override
    public BlockFace getBlockFace() {
        return this.getBooleanValue(HANGING) ? BlockFace.DOWN : BlockFace.UP;
    }

    public boolean isHanging() {
        return this.getBooleanValue(HANGING);
    }

    public void setHanging(boolean hanging) {
        this.setBooleanValue(HANGING, hanging);
    }

    public DripstoneThickness getThickness() {
        return this.getPropertyValue(THICKNESS);
    }

    public void setThickness(DripstoneThickness value) {
        this.setPropertyValue(THICKNESS, value);
    }

    @Override
    public WaterloggingType getWaterloggingType() {
        return WaterloggingType.WHEN_PLACED_IN_WATER;
    }
}
