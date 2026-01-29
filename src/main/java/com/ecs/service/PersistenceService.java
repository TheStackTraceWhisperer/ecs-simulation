package com.ecs.service;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.ecs.component.Persistent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for persisting and loading entities to/from YAML files.
 */
@Singleton
public class PersistenceService {

    private final YamlService yamlService;
    private final World world;

    @Inject
    public PersistenceService(YamlService yamlService, World world) {
        this.yamlService = yamlService;
        this.world = world;
    }

    /**
     * Saves all entities with the Persistent component to a YAML file.
     *
     * @param filename the file to save to
     * @throws IOException if file writing fails
     */
    public void save(String filename) throws IOException {
        List<Map<String, Object>> entities = new ArrayList<>();

        // Query all entities with Persistent component
        IntBag entityIds = world.getAspectSubscriptionManager()
                .get(Aspect.all(Persistent.class))
                .getEntities();

        for (int i = 0; i < entityIds.size(); i++) {
            int entityId = entityIds.get(i);
            Map<String, Object> entityData = new HashMap<>();

            // Serialize all components
            List<Map<String, Object>> componentsData = new ArrayList<>();
            
            // Iterate over all component types registered in the world
            for (Class<? extends Component> componentType : getAllComponentTypes()) {
                // Skip transient components
                if (componentType.isAnnotationPresent(com.artemis.annotations.Transient.class)) {
                    continue;
                }
                
                try {
                    Component component = world.getMapper(componentType).get(entityId);
                    if (component != null) {
                        Map<String, Object> componentData = serializeComponent(component);
                        componentsData.add(componentData);
                    }
                } catch (Exception e) {
                    // Skip components that can't be accessed
                }
            }
            
            entityData.put("components", componentsData);
            entities.add(entityData);
        }

        yamlService.dump(filename, entities);
    }
    
    /**
     * Gets all component types to serialize.
     * Returns all known component types excluding transient ones.
     */
    private List<Class<? extends Component>> getAllComponentTypes() {
        List<Class<? extends Component>> types = new ArrayList<>();
        // Include all persistent component types
        types.add(com.ecs.component.Position.class);
        types.add(com.ecs.component.Velocity.class);
        types.add(com.ecs.component.Identity.class);
        types.add(com.ecs.component.Body.class);
        types.add(com.ecs.component.Stats.class);
        types.add(com.ecs.component.CombatStats.class);
        types.add(com.ecs.component.Persistent.class);
        types.add(com.ecs.component.AiBehavior.class);
        // Note: SwingTimer, AttackIntent, and SpatialNode are @Transient and handled by annotation check
        return types;
    }

    /**
     * Loads entities from a YAML file and creates them in the world.
     *
     * @param filename the file to load from
     * @throws IOException if file reading fails
     */
    @SuppressWarnings("unchecked")
    public void load(String filename) throws IOException {
        List<Map<String, Object>> entities;
        try (java.io.FileReader reader = new java.io.FileReader(filename)) {
            entities = (List<Map<String, Object>>) yamlService.getYaml().load(reader);
        }

        if (entities == null) {
            return;
        }

        for (Map<String, Object> entityData : entities) {
            int entityId = world.create();
            List<Map<String, Object>> componentsData = 
                    (List<Map<String, Object>>) entityData.get("components");

            if (componentsData != null) {
                for (Map<String, Object> componentData : componentsData) {
                    try {
                        Component component = deserializeComponent(componentData);
                        world.edit(entityId).add(component);
                    } catch (Exception e) {
                        System.err.println("Failed to deserialize component: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Serializes a component to a map.
     *
     * @param component the component to serialize
     * @return the serialized data
     */
    private Map<String, Object> serializeComponent(Component component) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", component.getClass().getName());

        Map<String, Object> fields = new HashMap<>();
        for (Field field : component.getClass().getFields()) {
            try {
                fields.put(field.getName(), field.get(component));
            } catch (IllegalAccessException e) {
                System.err.println("Failed to serialize field " + field.getName() + ": " + e.getMessage());
            }
        }
        data.put("fields", fields);
        return data;
    }

    /**
     * Deserializes a component from a map.
     *
     * @param data the serialized data
     * @return the component instance
     * @throws Exception if deserialization fails
     */
    @SuppressWarnings("unchecked")
    private Component deserializeComponent(Map<String, Object> data) throws Exception {
        String typeName = (String) data.get("type");
        
        if (typeName == null) {
            throw new IllegalArgumentException("Component type is missing");
        }

        // Restrict deserialization to known-safe component classes
        if (!typeName.startsWith("com.ecs.component.")) {
            throw new IllegalArgumentException("Disallowed component type: " + typeName);
        }

        Class<?> rawClass = Class.forName(typeName);
        if (!Component.class.isAssignableFrom(rawClass)) {
            throw new IllegalArgumentException("Type is not a valid Component: " + typeName);
        }

        Class<? extends Component> componentClass = (Class<? extends Component>) rawClass;
        Component component = componentClass.getDeclaredConstructor().newInstance();

        Map<String, Object> fields = (Map<String, Object>) data.get("fields");
        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                Field field = componentClass.getField(entry.getKey());
                field.set(component, entry.getValue());
            }
        }

        return component;
    }
}
