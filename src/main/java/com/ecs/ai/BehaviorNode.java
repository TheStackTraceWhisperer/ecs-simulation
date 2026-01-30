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
}
