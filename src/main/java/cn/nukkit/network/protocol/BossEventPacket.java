package cn.nukkit.network.protocol;

import lombok.ToString;

/**
 * Created by CreeperFace on 30. 10. 2016.
 */
@ToString
public class BossEventPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.BOSS_EVENT_PACKET;

    /** Shows the bossbar to the player. */
    public static final int TYPE_SHOW = 0;
    /** Registers a player to a boss fight. */
    public static final int TYPE_REGISTER_PLAYER = 1;
    /** Not sure on this. */
    public static final int TYPE_UPDATE = 1;
    /** Removes the bossbar from the client. */
    public static final int TYPE_HIDE = 2;
    /** Unregisters a player from a boss fight. */
    public static final int TYPE_UNREGISTER_PLAYER = 3;
    /** Sets the bar percentage. */
    public static final int TYPE_HEALTH_PERCENT = 4;
    /** Sets title of the bar. */
    public static final int TYPE_TITLE = 5;
    /** Not sure on this. Includes color and overlay fields, plus an unknown short. */
    public static final int TYPE_UPDATE_PROPERTIES  = 6;
    /** Sets color and overlay of the bar. */
    public static final int TYPE_TEXTURE = 7;
    public static final int TYPE_QUERY = 8;

    public long bossEid;
    public int type;
    public long playerEid;
    public float healthPercent;
    public String title = "";
    /**
     * @since v776 1.21.60
     */
    public String filteredTitle = "";
    public short unknown;
    public int color;
    public int overlay;
    
    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.bossEid = this.getEntityUniqueId();
        this.type = (int) this.getUnsignedVarInt();
        switch (this.type) {
            case TYPE_REGISTER_PLAYER:
            case TYPE_UNREGISTER_PLAYER:
            case TYPE_QUERY:
                this.playerEid = this.getEntityUniqueId();
                break;
            case TYPE_SHOW:
                this.title = this.getString();
                if (this.protocol >= ProtocolInfo.v1_21_60) {
                    this.filteredTitle = this.getString();
                }
                this.healthPercent = this.getLFloat();
            case TYPE_UPDATE_PROPERTIES:
                this.unknown = (short) this.getShort();
            case TYPE_TEXTURE:
                this.color = (int) this.getUnsignedVarInt();
                this.overlay = (int) this.getUnsignedVarInt();
                break;
            case TYPE_HEALTH_PERCENT:
                this.healthPercent = this.getLFloat();
                break;
            case TYPE_TITLE:
                this.title = this.getString();
                if (this.protocol >= ProtocolInfo.v1_21_60) {
                    this.filteredTitle = this.getString();
                }
                break;
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.bossEid);
        this.putUnsignedVarInt(this.type);
        switch (this.type) {
            case TYPE_REGISTER_PLAYER:
            case TYPE_UNREGISTER_PLAYER:
            case TYPE_QUERY:
                this.putEntityUniqueId(this.playerEid);
                break;
            case TYPE_SHOW:
                this.putString(this.title);
                if (this.protocol >= ProtocolInfo.v1_21_60) {
                    this.putString(this.filteredTitle);
                }
                this.putLFloat(this.healthPercent);
            case TYPE_UPDATE_PROPERTIES:
                this.putShort(this.unknown);
            case TYPE_TEXTURE:
                this.putUnsignedVarInt(this.color);
                this.putUnsignedVarInt(this.overlay);
                break;
            case TYPE_HEALTH_PERCENT:
                this.putLFloat(this.healthPercent);
                break;
            case TYPE_TITLE:
                this.putString(this.title);
                if (this.protocol >= ProtocolInfo.v1_21_60) {
                    this.putString(this.filteredTitle);
                }
                break;
        }
    }
}
