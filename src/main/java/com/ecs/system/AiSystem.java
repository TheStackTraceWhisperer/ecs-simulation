package com.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.ecs.component.AiBehavior;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;

/**
 * System for executing AI behaviors.
 */
@Singleton
@Order(3)
public class AiSystem extends IteratingSystem {

    private ComponentMapper<AiBehavior> aiBehaviorMapper;

    public AiSystem() {
        super(Aspect.all(AiBehavior.class));
    }

    @Override
    protected void process(int entityId) {
        AiBehavior behavior = aiBehaviorMapper.get(entityId);
        if (behavior != null && behavior.rootNode != null) {
            behavior.rootNode.execute(world, entityId);
        }
    }
}
