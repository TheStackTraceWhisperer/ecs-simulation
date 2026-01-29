package com.ecs.ai;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.ecs.component.Body;
import com.ecs.component.Position;
import com.ecs.spatial.SpatialHashGrid;

/**
 * Behavior node that finds the nearest target entity.
 */
public class FindTargetNode implements BehaviorNode {

    private final SpatialHashGrid grid;
    private int lastFoundTarget = -1;

    public FindTargetNode(SpatialHashGrid grid) {
        this.grid = grid;
    }

    @Override
    public Status execute(World world, int entityId) {
        ComponentMapper<Position> positionMapper = world.getMapper(Position.class);
        ComponentMapper<Body> bodyMapper = world.getMapper(Body.class);

        Position myPosition = positionMapper.get(entityId);
        if (myPosition == null) {
            return Status.FAILURE;
        }

        // Get nearby entities
        IntBag nearby = grid.getNearby(myPosition.x, myPosition.y);

        float closestDistance = Float.MAX_VALUE;
        int closestTarget = -1;

        for (int i = 0; i < nearby.size(); i++) {
            int potentialTarget = nearby.get(i);
            if (potentialTarget == entityId) {
                continue; // Skip self
            }

            Position targetPos = positionMapper.get(potentialTarget);
            if (targetPos == null) {
                continue;
            }

            float dx = targetPos.x - myPosition.x;
            float dy = targetPos.y - myPosition.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = potentialTarget;
            }
        }

        if (closestTarget != -1) {
            lastFoundTarget = closestTarget;
            return Status.SUCCESS;
        }

        return Status.FAILURE;
    }

    public int getLastFoundTarget() {
        return lastFoundTarget;
    }
}
