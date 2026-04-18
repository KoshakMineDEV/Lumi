package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.customblock.properties.BlockProperties;
import cn.nukkit.block.customblock.properties.BooleanBlockProperty;
import cn.nukkit.block.customblock.properties.IntBlockProperty;
import cn.nukkit.block.data.Faceable;
import cn.nukkit.block.properties.BlockPropertiesHelper;
import cn.nukkit.block.properties.VanillaProperties;
import cn.nukkit.blockentity.BlockEntityID;
import cn.nukkit.blockentity.impl.BlockEntityShelf;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BlockShelf extends BlockTransparentMeta implements Faceable, BlockEntityHolder<BlockEntityShelf>, BlockPropertiesHelper {
    private static final BooleanBlockProperty POWERED_BIT = new BooleanBlockProperty("powered_bit", false);
    private static final IntBlockProperty POWERED_SHELF_TYPE = new IntBlockProperty("powered_shelf_type", false, 3, 0);

    private static final BlockProperties PROPERTIES = new BlockProperties(VanillaProperties.CARDINAL_DIRECTION, POWERED_BIT, POWERED_SHELF_TYPE);

    public BlockShelf() {
        this(0);
    }

    public BlockShelf(int meta) {
        super(meta);
    }

    @Override
    public BlockProperties getBlockProperties() {
        return PROPERTIES;
    }

    @Override
    public double getHardness() {
        return 2;
    }

    @Override
    public double getResistance() {
        return 3;
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, Player player) {
        setBlockFace(player != null ? player.getDirection().getOpposite() : BlockFace.SOUTH);
        this.getLevel().setBlock(this, this, true);
        this.getOrCreateBlockEntity();
        this.updateConnection(this);
        return true;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        return true;
    }

    @Override
    public int onTouch(@NotNull Vector3 vector, @NotNull Item item, @NotNull BlockFace blockFace, float fx, float fy, float fz, @Nullable Player player, PlayerInteractEvent.Action action) {
        if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (player != null && !player.isSneaking() && blockFace == getBlockFace() && fy > 0.25f && fy < 0.75f) {
                if (!isGettingPower()) {
                    Vector3 faceValues = blockFace.rotateYCCW().getUnitVector();
                    double distance = (fx * faceValues.x) + (fz * faceValues.z);
                    if (distance < 0) distance++;
                    int slot = distance < (1 / 3f) ? 0 : distance < (2 / 3f) ? 1 : 2;
                    BlockEntityShelf blockEntity = this.getOrCreateBlockEntity();
                    Inventory inventory = blockEntity.getInventory();
                    if (!player.isCreative()) {
                        player.getInventory().setItemInHand(inventory.getItem(slot)); //Overwrites the players item. So no need to remove it.
                    }
                    inventory.setItem(slot, item);
                    blockEntity.setDirty();
                } else {
                    List<BlockShelf> shelves = getConnectedBlocks();
                    for (int i = 0; i < shelves.size(); i++) {
                        BlockEntityShelf shelf = shelves.get(i).getOrCreateBlockEntity();
                        for (int j = 0; j < shelf.getSize(); j++) {
                            Item shelfItem = shelf.getItem(j);
                            int playerSlot = (i * shelf.getSize()) + j;
                            Item playerItem = player.getInventory().getItem(playerSlot);
                            shelf.getInventory().setItem(j, playerItem);
                            player.getInventory().setItem(playerSlot, shelfItem);
                        }
                        shelf.setDirty();
                    }
                }
                return 1;
            }
        }
        return 0;
    }

    @Override
    public void setBlockFace(BlockFace face) {
        this.setPropertyValue(VanillaProperties.CARDINAL_DIRECTION, face);
    }

    @Override
    public BlockFace getBlockFace() {
        return this.getPropertyValue(VanillaProperties.CARDINAL_DIRECTION);
    }

    @Override
    public @NotNull Class<? extends BlockEntityShelf> getBlockEntityClass() {
        return BlockEntityShelf.class;
    }

    @Override
    public @NotNull String getBlockEntityType() {
        return BlockEntityID.SHELF;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride() {
        Map<Integer, Item> items = this.getOrCreateBlockEntity().getInventory().getContents();
        int overwrite = 0;
        for(var entry : items.entrySet()) {
            if(!entry.getValue().isNull()) overwrite |= (1 << entry.getKey());
        }
        return overwrite;
    }

    @Override
    public int onUpdate(int type) {
        if(type == Level.BLOCK_UPDATE_REDSTONE) {
            updateConnection(this);
            this.setPropertyValue(POWERED_BIT, this.isGettingPower());
            this.level.setBlock(this, this);
        }
        return super.onUpdate(type);
    }

    public boolean canConnect(@NotNull BlockShelf shelf) {
        if (!isGettingPower()) return false;

        return switch (getType()) {
            case LEFT -> canConnectToSide(shelf, getBlockFace().rotateYCCW(), PoweredShelfType.RIGHT);
            case RIGHT -> canConnectToSide(shelf, getBlockFace().rotateY(), PoweredShelfType.LEFT);
            default -> true;
        };
    }

    private boolean canConnectToSide(BlockShelf shelf, BlockFace sideFace, PoweredShelfType expectedType) {
        Block sideBlock = getSide(sideFace);
        if (shelf.equals(sideBlock)) return true;
        if (sideBlock instanceof BlockShelf other) {
            return other.getType() == expectedType;
        }
        return false;
    }

    public void updateConnection(@NotNull Block origin) {
        PoweredShelfType newType = PoweredShelfType.UNCONNECTED;

        BlockFace face = getBlockFace().rotateY();
        Block right = getSide(face);
        Block left = getSide(face, -1);

        if (isGettingPower()) {
            boolean connectRight = right instanceof BlockShelf s && s.canConnect(this);
            boolean connectLeft = left instanceof BlockShelf s && s.canConnect(this);

            if (connectLeft && !connectRight) newType = PoweredShelfType.LEFT;
            else if (!connectLeft && connectRight) newType = PoweredShelfType.RIGHT;
            else if (connectLeft) newType = determineCenterType(left, right);
        }

        if (newType != getType()) {
            setPropertyValue(POWERED_SHELF_TYPE, newType.ordinal());
            level.setBlock(this, this);

            if (right != origin && right instanceof BlockShelf s) s.updateConnection(this);
            if (left != origin && left instanceof BlockShelf s) s.updateConnection(this);
        }
    }

    private PoweredShelfType determineCenterType(Block left, Block right) {
        if (right instanceof BlockShelf rs && rs.getType() == PoweredShelfType.UNCONNECTED && rs.canConnect(this))
            return PoweredShelfType.LEFT;
        if (left instanceof BlockShelf ls && ls.getType() == PoweredShelfType.UNCONNECTED && ls.canConnect(this))
            return PoweredShelfType.RIGHT;

        boolean rightIsRight = right instanceof BlockShelf rs2 && rs2.getType() == PoweredShelfType.RIGHT;
        boolean leftIsLeft = left instanceof BlockShelf ls2 && ls2.getType() == PoweredShelfType.LEFT;
        if (rightIsRight && leftIsLeft) return PoweredShelfType.RIGHT;

        boolean rightIsCenter = right instanceof BlockShelf rs3 && rs3.getType() == PoweredShelfType.CENTER;
        boolean leftIsCenter = left instanceof BlockShelf ls3 && ls3.getType() == PoweredShelfType.CENTER;
        if (rightIsCenter) return PoweredShelfType.RIGHT;
        if (leftIsCenter) return PoweredShelfType.LEFT;

        return PoweredShelfType.CENTER;
    }

    protected List<BlockShelf> getConnectedBlocks() {
        if(this.getType() == PoweredShelfType.UNCONNECTED || !this.isGettingPower()) {
            return new ArrayList<>(Collections.singletonList(this));
        } else {
            List<BlockShelf> shelves = new ArrayList<>();
            BlockFace face = getBlockFace().rotateY();
            Block right = getSide(face);
            Block left = getSide(face, -1);
           switch (getType()) {
                case CENTER -> {
                    if(right instanceof BlockShelf shelf) shelves.add(shelf);
                    shelves.add(this);
                    if(left instanceof BlockShelf shelf) shelves.add(shelf);
                }
               case RIGHT -> {
                    shelves.add(this);
                    if(right instanceof BlockShelf shelf) {
                        shelves.add(shelf);
                        if(shelf.getType() == PoweredShelfType.CENTER) {
                            Block right1 = getSide(face, 2);
                            if (right1 instanceof BlockShelf shelf1) shelves.add(shelf1);
                        }
                    }
                   Collections.reverse(shelves);
               }
               case LEFT -> {
                    shelves.add(this);
                    if(left instanceof BlockShelf shelf) {
                        shelves.add(shelf);
                        if(shelf.getType() == PoweredShelfType.CENTER) {
                            Block left1 = getSide(face, -2);
                            if (left1 instanceof BlockShelf shelf1) shelves.add(shelf1);
                        }
                    }
                }
            }
            return shelves;
        }
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{
                new ItemBlock(this, 0)
        };
    }

    public PoweredShelfType getType() {
        return PoweredShelfType.values()[getPropertyValue(POWERED_SHELF_TYPE)];
    }

    private boolean isGettingPower() {
        return this.level.isBlockPowered(this);
    }

    public enum PoweredShelfType {
        UNCONNECTED,
        RIGHT,
        CENTER,
        LEFT
    }
}