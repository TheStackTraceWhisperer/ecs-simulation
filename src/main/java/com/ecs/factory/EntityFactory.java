package com.ecs.factory;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.ecs.component.Position;
import com.ecs.registry.TemplateRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating entities from templates or custom builders.
 */
@Singleton
public class EntityFactory {

    private final TemplateRegistry templateRegistry;

    @Inject
    public EntityFactory(TemplateRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    /**
     * Prepares a new entity builder.
     *
     * @param id the entity identifier
     * @return the builder
     */
    public EntityBuilder prepare(String id) {
        return new EntityBuilder(id);
    }

    /**
     * Builder for fluent entity creation.
     */
    public class EntityBuilder {
        private final String id;
        private Float x;
        private Float y;
        private final List<Component> components = new ArrayList<>();

        private EntityBuilder(String id) {
            this.id = id;
        }

        /**
         * Sets the position of the entity.
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @return this builder
         */
        public EntityBuilder at(float x, float y) {
            this.x = x;
            this.y = y;
            return this;
        }

        /**
         * Adds a component to the entity.
         *
         * @param component the component to add
         * @return this builder
         */
        public EntityBuilder with(Component component) {
            this.components.add(component);
            return this;
        }

        /**
         * Builds and spawns the entity in the world.
         *
         * @param world the world to spawn in
         * @return the entity ID
         */
        public int build(World world) {
            int entityId = world.create();

            // Add components from template if id matches a template
            List<Component> templateComponents = templateRegistry.getTemplate(id);
            if (templateComponents != null) {
                for (Component templateComp : templateComponents) {
                    Component copy = copyComponent(templateComp);
                    world.edit(entityId).add(copy);
                }
            }

            // Add custom components
            for (Component component : components) {
                world.edit(entityId).add(component);
            }

            // Apply position override if specified
            if (x != null && y != null) {
                ComponentMapper<Position> positionMapper = world.getMapper(Position.class);
                Position posComp = positionMapper.get(entityId);
                if (posComp == null) {
                    posComp = world.edit(entityId).create(Position.class);
                }
                posComp.x = x;
                posComp.y = y;
            }

            return entityId;
        }

        /**
         * Copies a component using reflection.
         *
         * @param source the source component
         * @return the copied component
         */
        private Component copyComponent(Component source) {
            try {
                Component copy = source.getClass().getDeclaredConstructor().newInstance();
                for (Field field : source.getClass().getFields()) {
                    field.set(copy, field.get(source));
                }
                return copy;
            } catch (Exception e) {
                throw new RuntimeException("Failed to copy component: " + e.getMessage(), e);
            }
        }
    }
}
