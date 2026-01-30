package com.ecs.registry;

import com.artemis.Component;
import com.ecs.service.YamlService;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.reflect.Field;
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
     * Uses ClassGraph for dynamic discovery of YAML files.
     */
    private void loadTemplates() {
        try {
            // Use ClassGraph to scan for YAML files in the prefabs directory
            try (ScanResult scanResult = new ClassGraph()
                    .acceptPaths("prefabs")
                    .scan()) {
                
                // Find all .yml and .yaml files
                for (Resource resource : scanResult.getResourcesWithExtension("yml")) {
                    loadTemplateFromResource(resource);
                }
                
                for (Resource resource : scanResult.getResourcesWithExtension("yaml")) {
                    loadTemplateFromResource(resource);
                }
            }
            
            log.info("Loaded {} templates", templates.size());
        } catch (Exception e) {
            log.error("Failed to load templates: {}", e.getMessage());
        }
    }

    /**
     * Loads a template from a ClassGraph resource.
     */
    private void loadTemplateFromResource(Resource resource) {
        try {
            String path = resource.getPath();
            String templateName = extractTemplateName(path);
            
            try (InputStream inputStream = resource.open()) {
                loadTemplate(templateName, inputStream);
            }
        } catch (Exception e) {
            log.error("Failed to load template from {}: {}", resource.getPath(), e.getMessage());
        }
    }

    /**
     * Extracts the template name from a resource path.
     * Example: "prefabs/orc.yml" -> "orc"
     */
    private String extractTemplateName(String path) {
        String fileName = path;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            fileName = path.substring(lastSlash + 1);
        }
        
        // Remove extension
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0) {
            fileName = fileName.substring(0, lastDot);
        }
        
        return fileName;
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
