package cn.nukkit.entity.ai.behavior;

import lombok.Getter;
import lombok.Setter;
import cn.nukkit.entity.ai.behavior.Behavior;
import cn.nukkit.entity.ai.behavior.BehaviorState;

/**
 * Base class for behaviors that provides default state management.
 *
 * @author daoge_cmd
 */
public abstract class AbstractBehavior implements Behavior {

    @Getter
    @Setter
    protected BehaviorState behaviorState = BehaviorState.STOP;
}
