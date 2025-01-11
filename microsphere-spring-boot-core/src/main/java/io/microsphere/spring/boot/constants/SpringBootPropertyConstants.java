package io.microsphere.spring.boot.constants;

import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * The Constants for Spring Boot Property
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface SpringBootPropertyConstants {

    /**
     * The property name to exclude the class names of Spring Boot Auto-Configuration
     *
     * @see EnableAutoConfiguration
     * @see AutoConfigurationImportSelector#PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE
     */
    String SPRING_AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME = "spring.autoconfigure.exclude";

    /**
     * The name of the {@link PropertySource} {@link #attach(Environment) adapter} since Spring Boot 2
     *
     * @see ConfigurationPropertySources#ATTACHED_PROPERTY_SOURCE_NAME
     */
    String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";

}
