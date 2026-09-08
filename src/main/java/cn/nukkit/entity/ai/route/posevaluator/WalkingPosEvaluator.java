package cn.nukkit.entity.ai.route.posevaluator;

import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityIntelligent;

/**
 * Evaluates walkable positions for ground entities.
 * Avoids lava, cactus, and other hazardous blocks.
 * Treats water as a valid standing surface.
 *
 * @author daoge_cmd
 */
public class WalkingPosEvaluator implements GroundPosEvaluator {

    private final boolean allowWater;

    public WalkingPosEvaluator() {
        this(true);
    }

    public WalkingPosEvaluator(boolean allowWater) {
        this.allowWater = allowWater;
    }

    @Override
    public boolean evaluate(EntityIntelligent entity, Block block) {
        int id = block.getId();
        if (id == Block.LAVA || id == Block.STILL_LAVA || id == Block.CACTUS) {
            return false;
        }
        return (allowWater && block.isWater())
                || (block.isSolid() && block.getCollisionBoundingBox() != null);
    }
}
