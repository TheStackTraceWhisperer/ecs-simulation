package com.ecs.factory;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Comparator;
import java.util.List;

/**
 * Factory for creating and configuring the Artemis World instance.
 */
@Factory
public class ArtemisFactory {

    private final List<BaseSystem> systems;

    @Inject
    public ArtemisFactory(List<BaseSystem> systems) {
        this.systems = systems;
    }

    /**
     * Creates a configured World instance with all registered systems.
     * Systems are registered in order determined by @Order annotations.
     *
     * @return the configured World
     */
    @Singleton
    public World createWorld() {
        WorldConfigurationBuilder builder = new WorldConfigurationBuilder();

        // Sort systems by @Order annotation (lower values = higher priority)
        systems.stream()
                .sorted(Comparator.comparingInt(this::getOrder))
                .forEach(builder::with);

        return new World(builder.build());
    }

    /**
     * Gets the order value for a system, defaulting to 0 if not annotated.
     */
    private int getOrder(BaseSystem system) {
        Order orderAnnotation = system.getClass().getAnnotation(Order.class);
        return orderAnnotation != null ? orderAnnotation.value() : 0;
    }
}
