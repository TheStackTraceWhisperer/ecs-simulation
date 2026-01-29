package com.ecs.factory;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
     *
     * @return the configured World
     */
    @Singleton
    public World createWorld() {
        WorldConfigurationBuilder builder = new WorldConfigurationBuilder();

        // Register all injected systems
        for (BaseSystem system : systems) {
            builder.with(system);
        }

        return new World(builder.build());
    }
}
