package cn.nukkit.block;

import cn.nukkit.block.data.BlockColor;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemNamespaceId;

public class BlockPoplarWallSign extends BlockWallSign {

    public BlockPoplarWallSign() {
        this(0);
    }

    public BlockPoplarWallSign(int meta) {
        super(meta);
    }

    @Override
    public int getId() {
        return POPLAR_WALL_SIGN;
    }

    @Override
    protected int getPostId() {
        return POPLAR_STANDING_SIGN;
    }

    @Override
    public String getName() {
        return "Poplar Wall Sign";
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
