package com.ecs.ai;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.ecs.component.AttackIntent;
import com.ecs.component.Body;
import com.ecs.component.CombatStats;
import com.ecs.component.Position;

/**
 * Behavior node that adds attack intent if target is in range.
 */
public class CombatNode implements BehaviorNode {

    private final int targetId;

    public CombatNode(int targetId) {
        this.targetId = targetId;
    }

    @Override
    public Status execute(World world, int entityId) {
        ComponentMapper<Position> positionMapper = world.getMapper(Position.class);
        ComponentMapper<Body> bodyMapper = world.getMapper(Body.class);
        ComponentMapper<CombatStats> combatStatsMapper = world.getMapper(CombatStats.class);
        ComponentMapper<AttackIntent> attackIntentMapper = world.getMapper(AttackIntent.class);

        Position myPosition = positionMapper.get(entityId);
        Position targetPosition = positionMapper.get(targetId);
        CombatStats myCombatStats = combatStatsMapper.get(entityId);

        if (myPosition == null || targetPosition == null || myCombatStats == null) {
            return Status.FAILURE;
        }

        // Calculate distance
        float dx = targetPosition.x - myPosition.x;
        float dy = targetPosition.y - myPosition.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Subtract radii if bodies exist
        Body myBody = bodyMapper.get(entityId);
        Body targetBody = bodyMapper.get(targetId);
        if (myBody != null && targetBody != null) {
            distance -= (myBody.radius + targetBody.radius);
        }

        // Check if in range
        if (distance <= myCombatStats.range) {
            // Add attack intent if not already present
            AttackIntent intent = attackIntentMapper.get(entityId);
            if (intent == null) {
                intent = world.edit(entityId).create(AttackIntent.class);
            }
            intent.targetId = targetId;
            return Status.SUCCESS;
        }

        return Status.FAILURE;
    }
}
