package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockPoplarSignPost extends BlockSignPost {

    public BlockPoplarSignPost() {
        this(0);
    }

    public BlockPoplarSignPost(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_STANDING_SIGN;
    }

    @Override
    public int getWallId() {
        return POPLAR_WALL_SIGN;
    }

    @Override
    public String getName() {
        return "Poplar Sign Post";
    }

    @Override
    public Item toItem() {
        return Item.get(ItemNamespaceId.POPLAR_SIGN);
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.SPRUCE_BLOCK_COLOR;
    }
}
