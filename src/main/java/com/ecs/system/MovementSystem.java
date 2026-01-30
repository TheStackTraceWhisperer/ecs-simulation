package com.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.ecs.component.Position;
import com.ecs.component.Velocity;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;

/**
 * System for applying velocity to entity positions.
 */
@Singleton
@Order(1)
public class MovementSystem extends IteratingSystem {

    private ComponentMapper<Position> positionMapper;
    private ComponentMapper<Velocity> velocityMapper;

    public MovementSystem() {
        super(Aspect.all(Position.class, Velocity.class));
    }

    @Override
    protected void process(int entityId) {
        Position position = positionMapper.get(entityId);
        Velocity velocity = velocityMapper.get(entityId);

        // Apply velocity with delta time
        position.x += velocity.dx * world.getDelta();
        position.y += velocity.dy * world.getDelta();
    }
}
