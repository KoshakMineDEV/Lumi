package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.BlockColor;

public class BlockFenceGateBamboo extends BlockFenceGate {

    public BlockFenceGateBamboo() {
        this(0);
    }

    public BlockFenceGateBamboo(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return BAMBOO_FENCE_GATE;
    }

    @Override
    public String getName() {
        return "Bamboo Fence Gate";
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BAMBOO_BLOCK_COLOR;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(this.getId(), 0), 0);
    }

    @Override
    public void playOpenSound() {
        level.addSound(this, Sound.OPEN_BAMBOO_WOOD_FENCE_GATE);
    }

    @Override
    public void playCloseSound() {
        level.addSound(this, Sound.CLOSE_BAMBOO_WOOD_FENCE_GATE);
    }
}