package cn.nukkit.event.block;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class AnvilDamageEvent extends BlockEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private Block newBlock;
    private DamageCause cause;
    private final Player player;

    /**
     * This event is called when an anvil is damaged.
     * @param oldBlock The block (anvil) that has been damaged.
     * @param newBlock New anvil state
     * @param cause Cause of the anvil being damaged.
     * @param player The player who used the anvil.
     */
    public AnvilDamageEvent(Block oldBlock, Block newBlock, DamageCause cause, Player player) {
        super(oldBlock);
        this.newBlock = newBlock;
        this.cause = cause;
        this.player = player;
    }

    public Block getNewBlock() {
        return this.newBlock;
    }

    public void getNewBlock(Block newBlock) {
        this.newBlock = newBlock;
    }

    public DamageCause getCause() {
        return this.cause;
    }

    public Player getPlayer() {
        return this.player;
    }

    public enum DamageCause {
        USE,
        FALL
    }
}
