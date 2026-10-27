package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.math.BlockFace;
import cn.nukkit.block.data.BlockColor;
import cn.nukkit.utils.Utils;

public class BlockChorusPlant extends BlockTransparentMeta implements BlockPropertiesHelper {

    private static final BlockProperties PROPERTIES = new BlockProperties(
            VanillaProperties.CONNECTION_WEST, VanillaProperties.CONNECTION_SOUTH,
            VanillaProperties.CONNECTION_NORTH, VanillaProperties.CONNECTION_EAST,
            VanillaProperties.CONNECTION_UP, VanillaProperties.CONNECTION_DOWN);

    public BlockChorusPlant() {
        this(0);
    }

    public BlockChorusPlant(int meta) {
        super(meta);
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    public boolean updateConnections() {
        int previous = this.getDamage();
        this.setDamage(connectionMeta(this.level, this.getFloorX(), this.getFloorY(), this.getFloorZ()));
        return previous != this.getDamage();
    }

    public static int connectionMeta(ChunkManager level, int x, int y, int z) {
        int meta = 0;
        for (BlockFace face : BlockFace.values()) {
            int id = level.getBlockIdAt(x + face.getXOffset(), y + face.getYOffset(), z + face.getZOffset());
            boolean connected = id == CHORUS_PLANT || id == CHORUS_FLOWER
                    || face == BlockFace.DOWN && id == END_STONE;
            meta = PROPERTIES.setBooleanValue(meta, "minecraft:connection_" + face.getName(), connected);
        }
        return meta;
    }

    @Override
    public int getId() {
        return CHORUS_PLANT;
    }

    @Override
    public String getName() {
        return "Chorus Plant";
    }

    @Override
    public double getHardness() {
        return 0.4;
    }

    @Override
    public double getResistance() {
        return 0.4;
    }

    @Override
    public boolean breaksWhenMoved() {
        return true;
    }

    @Override
    public boolean sticksToPiston() {
        return false;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, double fx, double fy, double fz, Player player) {
        Block down = block.down();
        if (!(down instanceof BlockEndStone) &&
                !(down instanceof BlockChorusPlant) &&
                !(block.north() instanceof BlockChorusPlant) &&
                !(block.east() instanceof BlockChorusPlant) &&
                !(block.south() instanceof BlockChorusPlant) &&
                !(block.west() instanceof BlockChorusPlant)) {
            return false;
        }

        this.updateConnections();
        this.level.setBlock(block, this, true, true);
        return true;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (this.down().isTransparent() &&
                    !(this.down() instanceof BlockChorusPlant) &&
                    !(this.north() instanceof BlockChorusPlant) &&
                    !(this.east() instanceof BlockChorusPlant) &&
                    !(this.south() instanceof BlockChorusPlant) &&
                    !(this.west() instanceof BlockChorusPlant)) {
                this.getLevel().useBreakOn(this, null, null, true);
                return Level.BLOCK_UPDATE_NORMAL;
            }
            if (this.updateConnections()) {
                this.level.setBlock(this, this, true, false);
            }
        }

        return 0;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.PURPLE_BLOCK_COLOR;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.hasEnchantment(Enchantment.ID_SILK_TOUCH)) {
            return new Item[]{this.toItem()};
        }
        return Utils.rand() ? new Item[]{Item.get(ItemID.CHORUS_FRUIT, 0, 1)} : Item.EMPTY_ARRAY;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public boolean diffusesSkyLight() {
        return true;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(this, 0, 1);
    }
}
