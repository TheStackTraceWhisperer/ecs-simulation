package com.ecs.ai;

import com.artemis.World;

/**
 * Interface for behavior tree nodes.
 */
public interface BehaviorNode {
    /**
     * Executes the behavior node.
     *
     * @param world    the world
     * @param entityId the entity ID executing this behavior
     * @return the execution status
     */
    Status execute(World world, int entityId);

    /**
     * Creates a deep copy of this behavior node.
     * This is essential for ensuring entities maintain independent AI state.
     *
     * @return a new instance with the same configuration but independent state
     */
    BehaviorNode deepCopy();
}
