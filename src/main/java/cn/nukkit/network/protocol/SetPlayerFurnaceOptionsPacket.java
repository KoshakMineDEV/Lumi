package cn.nukkit.network.protocol;

import lombok.ToString;

/**
 * Sent by the client to sync furnace UI options (left tab, filtering, layout).
 *
 * @since v2192
 */
@ToString
public class SetPlayerFurnaceOptionsPacket extends DataPacket {

    public static final int NETWORK_ID = ProtocolInfo.SET_PLAYER_FURNACE_OPTIONS_PACKET;

    public FurnaceType type = FurnaceType.NONE;
    public FurnaceLeftTabIndex leftTabIndex = FurnaceLeftTabIndex.NONE;
    public boolean filtering;
    public FurnaceLayout layout = FurnaceLayout.NONE;

    @Override
    @Deprecated
    public byte pid() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.type = FurnaceType.values()[this.getByte() & 0xff];
        this.leftTabIndex = FurnaceLeftTabIndex.values()[this.getVarInt()];
        this.filtering = this.getBoolean();
        this.layout = FurnaceLayout.values()[this.getVarInt()];
    }

    @Override
    public void encode() {
        this.encodeUnsupported();
    }

    public enum FurnaceType {
        NONE,
        FURNACE,
        BLAST_FURNACE,
        SMOKER
    }

    public enum FurnaceLeftTabIndex {
        NONE,
        RECIPE_FOOD,
        RECIPE_ITEMS,
        RECIPE_BLOCKS,
        RECIPE_SEARCH,
        INVENTORY
    }

    public enum FurnaceLayout {
        NONE,
        INVENTORY_ONLY,
        DEFAULT
    }
}