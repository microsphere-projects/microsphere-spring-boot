package io.microsphere.spring.boot.env;

import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;

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
public interface DefaultPropertiesPostProcessor extends Ordered {

    /**
     * Initialize {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties}" resources
     *
     * @param defaultPropertiesResources {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"} Resource path
     */
    void initializeResources(Set<String> defaultPropertiesResources);

    /**
     * Post handle {@link SpringApplication#setDefaultProperties (Properties) "defaultProperties"},
     * after {@link #initializeResources(Set)}
     *
     * @param defaultProperties {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"}
     */
    default void postProcess(Map<String, Object> defaultProperties) {
    }

    /**
     * Get the order of {@link DefaultPropertiesPostProcessor}
     *
     * @return {@link #LOWEST_PRECEDENCE} as default
     */
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
