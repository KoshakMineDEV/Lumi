package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.BlockColor;

public class BlockFenceGateCrimson extends BlockFenceGate {

    public BlockFenceGateCrimson() {
        this(0);
    }

    public BlockFenceGateCrimson(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return CRIMSON_FENCE_GATE;
    }

    @Override
    public String getName() {
        return "Crimson Fence Gate";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.NETHERRACK_BLOCK_COLOR;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(this.getId(), 0), 0);
    }

    @Override
    public void playOpenSound() {
        level.addSound(this, Sound.OPEN_NETHER_WOOD_FENCE_GATE);
    }

    @Override
    public void playCloseSound() {
        level.addSound(this, Sound.CLOSE_NETHER_WOOD_FENCE_GATE);
    }
}