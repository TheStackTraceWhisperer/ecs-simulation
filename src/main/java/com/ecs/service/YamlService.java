package com.ecs.service;

import jakarta.inject.Singleton;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Service for YAML serialization and deserialization.
 */
@Singleton
public class YamlService {

    private final Yaml yaml;

    public YamlService() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
    }

    /**
     * Loads an object from a YAML file.
     *
     * @param filename the file to load from
     * @param clazz    the class type to deserialize to
     * @param <T>      the type
     * @return the deserialized object
     * @throws IOException if file reading fails
     */
    public <T> T load(String filename, Class<T> clazz) throws IOException {
        try (FileReader reader = new FileReader(filename)) {
            return yaml.loadAs(reader, clazz);
        }
    }

    /**
     * Dumps an object to a YAML file.
     *
     * @param filename the file to write to
     * @param data     the object to serialize
     * @throws IOException if file writing fails
     */
    public void dump(String filename, Object data) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            yaml.dump(data, writer);
        }
    }

    /**
     * Gets the underlying Yaml instance for custom operations.
     *
     * @return the Yaml instance
     */
    public Yaml getYaml() {
        return yaml;
    }
}
