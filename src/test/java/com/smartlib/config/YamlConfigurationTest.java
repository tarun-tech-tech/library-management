package com.smartlib.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class YamlConfigurationTest {

    @Test
    void applicationConfigurationFilesAreValidYaml() {
        Yaml yaml = new Yaml();
        assertDoesNotThrow(() -> load(yaml, "application.yml"));
        assertDoesNotThrow(() -> load(yaml, "application-prod.yml"));
    }

    private static void load(Yaml yaml, String resource) {
        try (InputStream stream = YamlConfigurationTest.class.getClassLoader()
                .getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing resource " + resource);
            yaml.load(stream);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid YAML resource " + resource, exception);
        }
    }
}
