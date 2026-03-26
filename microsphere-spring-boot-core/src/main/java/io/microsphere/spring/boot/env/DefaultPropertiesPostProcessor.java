package io.microsphere.spring.boot.env;

import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"} Post Processor.
 *
 * <h3>Example Usage</h3>
 * <p>Example usage in a custom {@link DefaultPropertiesPostProcessor}:</p>
 *
 * <pre>{@code
 * public class CustomDefaultPropertiesPostProcessor implements DefaultPropertiesPostProcessor {
 *
 *     @Override
 *     public void initializeResources(Set<String> defaultPropertiesResources) {
 *         defaultPropertiesResources.add("classpath*:META-INF/custom-default.properties");
 *     }
 *
 *     @Override
 *     public void postProcess(Map<String> defaultProperties) {
 *         // Add or modify default properties
 *         defaultProperties.put("custom.property", "defaultValue");
 *     }
 * }
 * }</pre>
 *
 * <p>Register the processor in META-INF/spring.factories:</p>
 *
 * <pre>{@code
 * io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor=\
 * com.example.CustomDefaultPropertiesPostProcessor
 * }</pre>
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