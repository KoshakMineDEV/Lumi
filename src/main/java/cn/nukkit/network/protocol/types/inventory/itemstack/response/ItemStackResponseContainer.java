package cn.nukkit.network.protocol.types.inventory.itemstack.response;

import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.inventory.ContainerSlotType;
import lombok.Value;

import java.util.List;

/**
 * ContainerEntry holds information on what slots in a container have what item stack in them.
 */
@Value
public class ItemStackResponseContainer {
    /**
     * container that the slots that follow are in.
     */
    ContainerSlotType container;

    /**
     * items holds information on what item stack should be present in specific slots in the container.
     */
    List<ItemStackResponseSlot> items;

    Integer dynamicId;

    public FullContainerName getContainerName() {
        return new FullContainerName(this.container, this.dynamicId);
    }

    public ItemStackResponseContainer(ContainerSlotType container, List<ItemStackResponseSlot> items) {
        this(container, items, null);
    }

    public ItemStackResponseContainer(ContainerSlotType container, List<ItemStackResponseSlot> items, Integer dynamicId) {
        this.container = container;
        this.items = items;
        this.dynamicId = dynamicId;
    }
}
