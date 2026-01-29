package com.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.ecs.component.*;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * System for processing basic attacks.
 */
@Singleton
@Order(4)
@Slf4j
public class BasicAttackSystem extends IteratingSystem {

    private ComponentMapper<CombatStats> combatStatsMapper;
    private ComponentMapper<SwingTimer> swingTimerMapper;
    private ComponentMapper<AttackIntent> attackIntentMapper;
    private ComponentMapper<Position> positionMapper;
    private ComponentMapper<Body> bodyMapper;
    private ComponentMapper<Stats> statsMapper;

    public BasicAttackSystem() {
        super(Aspect.all(CombatStats.class));
    }

    @Override
    protected void process(int entityId) {
        CombatStats combatStats = combatStatsMapper.get(entityId);

        // Decrement swing timer
        SwingTimer timer = swingTimerMapper.get(entityId);
        if (timer != null) {
            timer.cooldown -= world.getDelta();
            if (timer.cooldown <= 0) {
                world.edit(entityId).remove(SwingTimer.class);
                timer = null;
            }
        }

        // Check for attack intent
        AttackIntent intent = attackIntentMapper.get(entityId);
        if (intent != null && timer == null) {
            // Execute attack
            executeAttack(entityId, intent.targetId, combatStats);

            // Set cooldown
            float cooldown = 1.0f / combatStats.attackSpeed;
            SwingTimer newTimer = world.edit(entityId).create(SwingTimer.class);
            newTimer.cooldown = cooldown;

            // Remove intent
            world.edit(entityId).remove(AttackIntent.class);
        }
    }

    /**
     * Executes an attack from attacker to target.
     */
    private void executeAttack(int attackerId, int targetId, CombatStats attackerStats) {
        Position attackerPos = positionMapper.get(attackerId);
        Position targetPos = positionMapper.get(targetId);
        Stats targetStats = statsMapper.get(targetId);

        if (attackerPos == null || targetPos == null || targetStats == null) {
            return;
        }

        // Calculate distance
        float dx = targetPos.x - attackerPos.x;
        float dy = targetPos.y - attackerPos.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Subtract radii
        Body attackerBody = bodyMapper.get(attackerId);
        Body targetBody = bodyMapper.get(targetId);
        if (attackerBody != null && targetBody != null) {
            distance -= (attackerBody.radius + targetBody.radius);
        }

        // Check range and apply damage
        if (distance <= attackerStats.range) {
            targetStats.health -= attackerStats.damage;
            log.debug("Entity {} attacked {} for {} damage. Target health: {}", 
                     attackerId, targetId, attackerStats.damage, targetStats.health);
        }
    }
}
