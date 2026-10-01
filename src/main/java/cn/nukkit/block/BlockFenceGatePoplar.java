package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockFenceGatePoplar extends BlockFenceGate {

    public BlockFenceGatePoplar() {
        this(0);
    }

    public BlockFenceGatePoplar(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_FENCE_GATE;
    }

    @Override
    public String getName() {
        return "Poplar Fence Gate";
    }

    @Override
    public Item toItem() {
        return Item.get(ItemNamespaceId.POPLAR_FENCE_GATE, 0, 1);
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
