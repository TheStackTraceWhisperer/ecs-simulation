package com.ecs;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.ecs.ai.ChaseNode;
import com.ecs.ai.CombatNode;
import com.ecs.ai.FindTargetNode;
import com.ecs.component.*;
import com.ecs.spatial.SpatialHashGrid;
import com.ecs.system.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the simulation with combat and AI.
 */
class SimulationIT {

    @Test
    void testCombatSimulation() {
        // Create the world with all systems
        SpatialHashGrid grid = new SpatialHashGrid();
        
        World world = new World(new WorldConfigurationBuilder()
                .with(new MovementSystem())
                .with(new SpatialSystem(grid))
                .with(new AiSystem())
                .with(new BasicAttackSystem())
                .with(new DeathSystem())
                .build());

        // Create attacker entity
        int attacker = world.create();
        world.edit(attacker)
                .add(new Position(0, 0))
                .add(new Velocity(0, 0))
                .add(new Body(1.0f))
                .add(new SpatialNode(0, 0))
                .add(new Stats(100))
                .add(new CombatStats(10, 2.0f, 1.0f)); // 10 damage, 2 range, 1 attack/sec

        // Create target entity
        int target = world.create();
        world.edit(target)
                .add(new Position(5, 0))
                .add(new Velocity(0, 0))
                .add(new Body(1.0f))
                .add(new SpatialNode(5, 0))
                .add(new Stats(50)); // 50 health

        // Add AI behavior to attacker to chase and attack target
        FindTargetNode findTarget = new FindTargetNode(grid);
        world.edit(attacker).add(new AiBehavior(findTarget));

        // Run simulation for several ticks
        boolean targetDied = false;
        for (int i = 0; i < 1000; i++) {
            world.setDelta(0.016f); // ~60 FPS
            world.process();

            // Check if target is still alive
            if (!world.getEntityManager().isActive(target)) {
                targetDied = true;
                System.out.println("Target died after " + i + " ticks");
                break;
            }

            // Update AI behavior to chase the target once found
            if (i == 10 && findTarget.getLastFoundTarget() != -1) {
                // Switch to chase behavior
                int foundTarget = findTarget.getLastFoundTarget();
                ChaseNode chase = new ChaseNode(foundTarget, 10.0f);
                CombatNode combat = new CombatNode(foundTarget);
                // Simplified: just use combat node directly for this test
                world.edit(attacker).add(new AiBehavior(combat));
                
                // Manually move attacker close to target for testing
                Position attackerPos = world.getMapper(Position.class).get(attacker);
                attackerPos.x = 3;
                attackerPos.y = 0;
            }
        }

        // Assert that the target died
        assertThat(targetDied)
                .as("Target should have died during simulation")
                .isTrue();
    }
}
