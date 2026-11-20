package cn.nukkit.item;

import java.util.concurrent.atomic.AtomicInteger;

public final class ItemStackNetManager {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    public static int allocate() {
        return NEXT_ID.getAndUpdate(id -> id == Integer.MAX_VALUE ? 1 : id + 1);
    }
}
