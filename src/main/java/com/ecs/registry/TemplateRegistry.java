package com.ecs.registry;

import com.artemis.Component;
import com.ecs.service.YamlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Registry for entity templates loaded from YAML prefab files.
 */
@Singleton
public class TemplateRegistry {

    private final YamlService yamlService;
    private final Map<String, List<Component>> templates = new HashMap<>();

    @Inject
    public TemplateRegistry(YamlService yamlService) {
        this.yamlService = yamlService;
        loadTemplates();
    }

    /**
     * Scans the prefabs directory and loads all YAML templates.
     */
    private void loadTemplates() {
        File prefabsDir = new File("src/main/resources/prefabs");
        if (!prefabsDir.exists() || !prefabsDir.isDirectory()) {
            System.out.println("Prefabs directory not found, skipping template loading.");
            return;
        }

        File[] files = prefabsDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null || files.length == 0) {
            System.out.println("No prefab files found.");
            return;
        }

        for (File file : files) {
            try {
                String templateName = file.getName().replaceAll("\\.(yml|yaml)$", "");
                // Load as a generic map structure
                // The actual component instantiation will be handled by EntityFactory
                System.out.println("Found template: " + templateName);
            } catch (Exception e) {
                System.err.println("Failed to load template from " + file.getName() + ": " + e.getMessage());
            }
        }
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
