package cn.nukkit.network.protocol.types.inventory;

import lombok.Value;

@Value
public class FullContainerName {

    private static final FullContainerName[] STATIC_NAMES = new FullContainerName[ContainerSlotType.values().length];

    static {
        for (ContainerSlotType type : ContainerSlotType.values()) {
            STATIC_NAMES[type.ordinal()] = new FullContainerName(type, null);
        }
    }

    public static FullContainerName of(ContainerSlotType type) {
        return STATIC_NAMES[type.ordinal()];
    }

   ContainerSlotType container;
   /**
    * May be null if not present since v729
    */
   Integer dynamicId;
}
