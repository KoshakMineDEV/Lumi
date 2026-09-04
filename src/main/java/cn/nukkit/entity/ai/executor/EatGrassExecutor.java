package cn.nukkit.entity.ai.executor;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.ai.behavior.BehaviorExecutor;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.network.protocol.EntityEventPacket;

/**
 * Sheep-specific grass eating executor. Plays the eat animation,
 * then destroys short grass or converts grass block to dirt.
 *
 * @author daoge_cmd
 */
public class EatGrassExecutor implements BehaviorExecutor {

    protected final int duration;
    protected int tickCounter;

    public EatGrassExecutor(int duration) {
        this.duration = duration;
    }

    public EatGrassExecutor() {
        this(40);
    }

    @Override
    public void onStart(EntityIntelligent entity) {
        tickCounter = 0;
        var packet = new EntityEventPacket();
        packet.eid = entity.getRuntimeId();
        packet.event = EntityEventPacket.EAT_GRASS_ANIMATION;
        Server.broadcastPacket(entity.getLivingEntity().getViewers().values(), packet);
    }

    @Override
    public boolean execute(EntityIntelligent entity) {
        tickCounter++;
        return tickCounter < duration;
    }

    @Override
    public void onStop(EntityIntelligent entity) {
        var level = entity.getLevel();
        var loc = entity.getLocation();
        int x = (int) Math.floor(loc.x);
        int y = (int) Math.floor(loc.y);
        int z = (int) Math.floor(loc.z);

        // Spawn block break particle
        var feetBlock = level.getBlock(x, y, z);
        level.addParticle(new DestroyBlockParticle(loc, feetBlock));

        if (!level.getGameRules().getBoolean(GameRule.MOB_GRIEFING)) {
            return;
        }

        if (feetBlock.getId() == Block.TALL_GRASS) {
            level.setBlock(x, y, z, Block.get(Block.AIR), false, true);
            onEatGrass(entity);
            return;
        }

        var belowBlock = level.getBlock(x, y - 1, z);
        if (belowBlock.getId() == Block.GRASS_BLOCK) {
            level.setBlock(x, y - 1, z, Block.get(Block.DIRT), false, true);
            onEatGrass(entity);
        }
    }

    protected void onEatGrass(EntityIntelligent entity) {
        if (entity instanceof EntityAgeable ageable && ageable.isBaby()) {
            ageable.setBaby(false);
        }
    }
}
