package com.ecs.ai;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.ecs.component.Position;
import com.ecs.component.Velocity;

/**
 * Behavior node that chases a target by setting velocity towards it.
 */
public class ChaseNode implements BehaviorNode {

    private final int targetId;
    private final float speed;

    public ChaseNode(int targetId, float speed) {
        this.targetId = targetId;
        this.speed = speed;
    }

    @Override
    public Status execute(World world, int entityId) {
        ComponentMapper<Position> positionMapper = world.getMapper(Position.class);
        ComponentMapper<Velocity> velocityMapper = world.getMapper(Velocity.class);

        Position myPosition = positionMapper.get(entityId);
        Position targetPosition = positionMapper.get(targetId);
        Velocity myVelocity = velocityMapper.get(entityId);

        if (myPosition == null || targetPosition == null || myVelocity == null) {
            return Status.FAILURE;
        }

        // Calculate direction to target
        float dx = targetPosition.x - myPosition.x;
        float dy = targetPosition.y - myPosition.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.1f) {
            // Already at target
            myVelocity.dx = 0;
            myVelocity.dy = 0;
            return Status.SUCCESS;
        }

        // Normalize and apply speed
        myVelocity.dx = (dx / distance) * speed;
        myVelocity.dy = (dy / distance) * speed;

        return Status.RUNNING;
    }

    @Override
    public BehaviorNode deepCopy() {
        return new ChaseNode(targetId, speed);
    }
}
