package com.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.ecs.component.Stats;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * System for handling entity death when health reaches zero.
 */
@Singleton
@Order(5)
@Slf4j
public class DeathSystem extends IteratingSystem {

    private ComponentMapper<Stats> statsMapper;

    public DeathSystem() {
        super(Aspect.all(Stats.class));
    }

    @Override
    protected void process(int entityId) {
        Stats stats = statsMapper.get(entityId);
        if (stats.health <= 0) {
            log.debug("Entity {} died.", entityId);
            world.delete(entityId);
        }
    }
}
