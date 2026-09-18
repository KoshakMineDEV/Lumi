package cn.nukkit.network.protocol;

import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.*;
import cn.nukkit.network.protocol.types.inventory.itemstack.request.ItemStackRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@ToString
@Setter
@Getter
public class PlayerAuthInputPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.PLAYER_AUTH_INPUT_PACKET;

    private float yaw;
    private float pitch;
    private float headYaw;
    private Vector3f position;
    private Vector2 motion;
    private Set<AuthInputAction> inputData = EnumSet.noneOf(AuthInputAction.class);
    private InputMode inputMode;
    private ClientPlayMode playMode;
    private AuthInteractionModel interactionModel;
    /**
     * @deprecated since v748
     */
    private Vector3f vrGazeDirection;
    private long tick;
    private Vector3f delta;
    private InventoryTransactionPacket itemUseTransaction;
    private ItemStackRequest itemStackRequest;
    private Map<PlayerActionType, PlayerBlockActionData> blockActionData = new EnumMap<>(PlayerActionType.class);
    /**
     * @since v748
     */
    private Vector2f interactRotation;
    /**
     * @since 575
     */
    private Vector2f analogMoveVector;
    /**
     * @since 649
     */
    private long predictedVehicle;
    /**
     * @since v662 1.20.70
     */
    private Vector2f vehicleRotation;
    /**
     * @since v748
     */
    private Vector3f cameraOrientation;
    /**
     * @since v766
     */
    private Vector2f rawMoveVector;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.pitch = this.getLFloat();
        this.yaw = this.getLFloat();
        this.position = this.getVector3f();
        this.motion = new Vector2(this.getLFloat(), this.getLFloat());
        this.headYaw = this.getLFloat();

        boolean v2168 = this.protocol >= ProtocolInfo.v1_26_40;
        if (v2168) {
            if (this.getBoolean()) {
                int count = (int) this.getUnsignedVarInt();
                for (int i = 0; i < Math.min(count, 256); i++) {
                    int ordinal = this.getVarInt();
                    if (ordinal >= 0 && ordinal < AuthInputAction.size()) {
                        this.inputData.add(AuthInputAction.from(ordinal));
                    }
                }
            }
        } else {
            long inputFlags = this.getUnsignedVarLong();
            for (int i = 0; i < Math.min(AuthInputAction.size(), Long.SIZE); i++) {
                if ((inputFlags & (1L << i)) != 0) this.inputData.add(AuthInputAction.from(i));
            }
        }

        this.inputMode = InputMode.fromOrdinal((int) this.getUnsignedVarInt());
        this.playMode = ClientPlayMode.fromOrdinal((int) this.getUnsignedVarInt());
        this.interactionModel = AuthInteractionModel.fromOrdinal(v2168 ? this.getVarInt() : (int) this.getUnsignedVarInt());
        if (protocol >= ProtocolInfo.v1_21_40) {
            this.interactRotation = this.getVector2f();
        } else if (this.playMode == ClientPlayMode.REALITY) {
            this.vrGazeDirection = this.getVector3f();
        }

        this.tick = this.getUnsignedVarLong();
        this.delta = this.getVector3f();

        if (v2168) {
            if (this.getBoolean() && this.getBoolean()) this.itemUseTransaction = this.readItemUseTransaction();
            if (this.getBoolean() && this.getBoolean()) this.itemStackRequest = this.readItemStackRequest(this.protocol);
            if (this.getBoolean() && this.getBoolean()) {
                int size = (int) this.getUnsignedVarInt();
                if (size > 256) throw new IllegalArgumentException("PlayerAuthInputPacket block actions are too long: " + size);
                this.decodeBlockActions(size);
            }
            if (this.getBoolean() && this.getBoolean()) this.vehicleRotation = this.getVector2f();
            if (this.getBoolean() && this.getBoolean()) this.predictedVehicle = this.getVarLong();
        } else {
            if (this.inputData.contains(AuthInputAction.PERFORM_ITEM_INTERACTION)) this.itemUseTransaction = this.readItemUseTransaction();
            if (this.inputData.contains(AuthInputAction.PERFORM_ITEM_STACK_REQUEST)) this.itemStackRequest = this.readItemStackRequest(this.protocol);
            if (this.inputData.contains(AuthInputAction.PERFORM_BLOCK_ACTIONS)) {
                int size = this.getVarInt();
                if (size > 256) throw new IllegalArgumentException("PlayerAuthInputPacket block actions are too long: " + size);
                this.decodeBlockActions(size);
            }
            if (protocol >= ProtocolInfo.v1_20_60 && this.inputData.contains(AuthInputAction.IN_CLIENT_PREDICTED_IN_VEHICLE)) {
                if (protocol >= ProtocolInfo.v1_20_70) this.vehicleRotation = this.getVector2f();
                this.predictedVehicle = this.getVarLong();
            }
        }

        this.analogMoveVector = this.getVector2f();
        if (protocol >= ProtocolInfo.v1_21_40) this.cameraOrientation = this.getVector3f();
        if (protocol >= ProtocolInfo.v1_21_50) this.rawMoveVector = this.getVector2f();
    }

    private void decodeBlockActions(int arraySize) {
        for (int i = 0; i < arraySize; i++) {
            PlayerActionType type = PlayerActionType.fromOrNull(this.getVarInt());
            if (type == null) throw new UnsupportedOperationException("Unknown player block action type");
            switch (type) {
                case START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, CRACK_BLOCK, PREDICT_DESTROY_BLOCK, CONTINUE_DESTROY_BLOCK ->
                        this.blockActionData.put(type, new PlayerBlockActionData(type, this.getSignedBlockPosition(), this.getVarInt()));
                default -> this.blockActionData.put(type, new PlayerBlockActionData(type, null, -1));
            }
        }
    }
    private InventoryTransactionPacket readItemUseTransaction() {
        InventoryTransactionPacket packet = new InventoryTransactionPacket();
        packet.protocol = this.protocol;
        packet.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
        packet.setBuffer(this.getBufferUnsafe());
        packet.setCount(this.getCount());
        packet.setOffset(this.getOffset());

        boolean v2168 = packet.protocol >= ProtocolInfo.v1_26_40;
        packet.legacyRequestId = packet.getVarInt();
        boolean hasLegacySlots = v2168
                ? packet.getBoolean() && packet.legacyRequestId < -1 && (packet.legacyRequestId & 1) == 0
                : packet.legacyRequestId < -1 && (packet.legacyRequestId & 1) == 0;
        if (hasLegacySlots) {
            int legacySlotsCount = Math.min((int) packet.getUnsignedVarInt(), 256);
            for (int i = 0; i < legacySlotsCount; i++) {
                packet.getByte();
                packet.get((int) packet.getUnsignedVarInt());
            }
        }
        if (v2168 && !(packet.getBoolean() && packet.getBoolean())) {
            throw new IllegalStateException("Expected InventoryActionData");
        }

        int actionCount = Math.min((int) packet.getUnsignedVarInt(), 4096);
        packet.actions = new NetworkInventoryAction[actionCount];
        for (int i = 0; i < packet.actions.length; i++) {
            packet.actions[i] = new NetworkInventoryAction().read(packet, v2168);
        }

        UseItemData itemData = new UseItemData();
        itemData.actionType = v2168 ? packet.getVarInt() : (int) packet.getUnsignedVarInt();
        if (packet.protocol >= ProtocolInfo.v1_21_20) itemData.triggerType = v2168 ? packet.getByte() & 0xff : (int) packet.getUnsignedVarInt();
        itemData.blockPos = packet.getBlockVector3();
        itemData.face = v2168 ? BlockFace.fromIndex(packet.getByte() & 0xff) : packet.getBlockFace();
        itemData.hotbarSlot = packet.getVarInt();
        itemData.itemInHand = v2168 ? packet.getNetworkItemStackDescriptor(packet.protocol) : packet.getSlot(packet.protocol);
        itemData.playerPos = packet.getVector3f().asVector3();
        itemData.clickPos = packet.getVector3f();
        itemData.blockRuntimeId = (int) packet.getUnsignedVarInt();
        if (packet.protocol >= ProtocolInfo.v1_21_20) itemData.clientInteractPrediction = v2168 ? packet.getByte() & 0xff : (int) packet.getUnsignedVarInt();
        if (packet.protocol >= ProtocolInfo.v1_26_10) itemData.clientCooldownState = (byte) packet.getByte();

        packet.transactionData = itemData;
        this.setOffset(packet.getOffset());
        return packet;
    }
    @Override
    public void encode() {
        // Noop
    }
}