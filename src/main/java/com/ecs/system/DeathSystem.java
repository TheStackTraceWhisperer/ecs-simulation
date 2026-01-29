package com.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.ecs.component.Stats;
import jakarta.inject.Singleton;

/**
 * System for handling entity death when health reaches zero.
 */
@Singleton
public class DeathSystem extends IteratingSystem {

    private ComponentMapper<Stats> statsMapper;

    public DeathSystem() {
        super(Aspect.all(Stats.class));
    }

    @Override
    protected void process(int entityId) {
        Stats stats = statsMapper.get(entityId);
        if (stats.health <= 0) {
            System.out.println("Entity " + entityId + " died.");
            world.delete(entityId);
        }
    }
}
