package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.ai.behavior.BehaviorEvaluator;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.math.Vector3;

/**
 * Checks if the block at the entity's location plus an offset
 * matches the expected block type.
 *
 * @author daoge_cmd
 */
public class BlockCheckEvaluator implements BehaviorEvaluator {

    protected final int blockId;
    protected final Vector3 offset;

    public BlockCheckEvaluator(int blockId, Vector3 offset) {
        this.blockId = blockId;
        this.offset = offset;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity) {
        var loc = entity.getLocation();
        int x = (int) Math.floor(loc.x + offset.x);
        int y = (int) Math.floor(loc.y + offset.y);
        int z = (int) Math.floor(loc.z + offset.z);
        return entity.getLevel().getBlockIdAt(x, y, z) == blockId;
    }
}
