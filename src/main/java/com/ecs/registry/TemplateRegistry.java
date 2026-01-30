package com.ecs.registry;

import com.artemis.Component;
import com.ecs.service.YamlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * Registry for entity templates loaded from YAML prefab files.
 */
@Singleton
@Slf4j
public class TemplateRegistry {

    private final YamlService yamlService;
    private final Map<String, List<Component>> templates = new HashMap<>();

    @Inject
    public TemplateRegistry(YamlService yamlService) {
        this.yamlService = yamlService;
        loadTemplates();
    }

    /**
     * Scans the prefabs directory via classpath and loads all YAML templates.
     * 
     * <p><strong>Note:</strong> Current implementation requires manual listing of template names.
     * A future enhancement could use ClassGraph or Reflections library to dynamically discover
     * all YAML files in the prefabs directory.</p>
     */
    private void loadTemplates() {
        try {
            // Use ClassLoader to get resources from classpath (works in JAR)
            ClassLoader classLoader = getClass().getClassLoader();
            URL prefabsUrl = classLoader.getResource("prefabs");
            
            if (prefabsUrl == null) {
                log.info("Prefabs directory not found in classpath, skipping template loading.");
                return;
            }

            // TODO: Use resource scanner library to dynamically discover YAML files
            String[] templateNames = {"example"}; // Manually maintained list
            
            for (String templateName : templateNames) {
                try {
                    String resourcePath = "prefabs/" + templateName + ".yml";
                    InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
                    
                    if (inputStream == null) {
                        resourcePath = "prefabs/" + templateName + ".yaml";
                        inputStream = classLoader.getResourceAsStream(resourcePath);
                    }
                    
                    if (inputStream != null) {
                        loadTemplate(templateName, inputStream);
                        inputStream.close();
                    }
                } catch (Exception e) {
                    log.error("Failed to load template {}: {}", templateName, e.getMessage());
                }
            }
            
            log.info("Loaded {} templates", templates.size());
        } catch (Exception e) {
            log.error("Failed to load templates: {}", e.getMessage());
        }
    }

    /**
     * Loads a single template from an input stream.
     */
    @SuppressWarnings("unchecked")
    private void loadTemplate(String templateName, InputStream inputStream) {
        try (InputStream stream = inputStream) {
            Map<String, Object> templateData = (Map<String, Object>) yamlService.getYaml().load(stream);
            
            if (templateData == null || !templateData.containsKey("components")) {
                log.warn("Template {} has no components", templateName);
                return;
            }

            List<Map<String, Object>> componentsData = (List<Map<String, Object>>) templateData.get("components");
            List<Component> components = new ArrayList<>();

            for (Map<String, Object> componentData : componentsData) {
                try {
                    Component component = instantiateComponent(componentData);
                    components.add(component);
                } catch (Exception e) {
                    log.error("Failed to instantiate component in template {}: {}", templateName, e.getMessage());
                }
            }

            templates.put(templateName, components);
            log.info("Loaded template '{}' with {} components", templateName, components.size());
        } catch (Exception e) {
            log.error("Failed to parse template {}: {}", templateName, e.getMessage());
        }
    }

    /**
     * Instantiates a component from YAML data.
     */
    @SuppressWarnings("unchecked")
    private Component instantiateComponent(Map<String, Object> data) throws Exception {
        String typeName = (String) data.get("type");
        
        if (typeName == null) {
            throw new IllegalArgumentException("Component type is missing");
        }

        // Restrict to known component classes
        if (!typeName.startsWith("com.ecs.component.")) {
            throw new IllegalArgumentException("Disallowed component type: " + typeName);
        }

        Class<?> rawClass = Class.forName(typeName);
        if (!Component.class.isAssignableFrom(rawClass)) {
            throw new IllegalArgumentException("Type is not a valid Component: " + typeName);
        }

        Component component = ((Class<? extends Component>) rawClass).getDeclaredConstructor().newInstance();

        // Set field values
        Map<String, Object> fields = (Map<String, Object>) data.get("fields");
        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                try {
                    Field field = component.getClass().getField(entry.getKey());
                    field.set(component, entry.getValue());
                } catch (NoSuchFieldException e) {
                    log.warn("Field {} not found in component {}", entry.getKey(), typeName);
                }
            }
        }

        return component;
    }

    /**
     * Gets a template by name.
     *
     * @param name the template name
     * @return the list of components, or null if not found
     */
    public List<Component> getTemplate(String name) {
        return templates.get(name);
    }

    /**
     * Registers a template manually.
     *
     * @param name       the template name
     * @param components the list of components
     */
    public void registerTemplate(String name, List<Component> components) {
        templates.put(name, components);
    }
}
