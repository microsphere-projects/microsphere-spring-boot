package io.github.microsphere.spring.boot.env;

import org.springframework.boot.SpringApplication;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"} Post Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultPropertiesApplicationListener
 * @since 1.0.0
 */
public interface DefaultPropertiesPostProcessor {

    /**
     * Initialize {@ link SpringApplicationsetDefaultProperties (Properties) "defaultProperties}" resources
     *
     * @param defaultPropertiesResources {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"} Resource path
     */
    void initializeResources(Set<String> defaultPropertiesResources);

    /**
     * Rear handle {@ link SpringApplication#setDefaultProperties (Properties) "defaultProperties"},
     * After {@link #initializeResources(Set)}
     *
     * @param defaultProperties {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"}
     */
    default void postProcess(Map<String, Object> defaultProperties) {
    }
}
