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
     * The property name to exclude the class names of Spring Boot Auto-Configuration.
     * This property is used by {@link EnableAutoConfiguration} and {@link AutoConfigurationImportSelector}
     * to specify auto-configuration classes that should be excluded from loading.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // In application.properties or application.yml
     *   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
     * }</pre>
     * or programmatically:
     * <pre>{@code
     *   Map<String, Object> props = new HashMap<>();
     *   props.put("spring.autoconfigure.exclude", "com.example.ExcludedConfig");
     *   SpringApplication app = new SpringApplication(MyApplication.class);
     *   app.setDefaultProperties(props);
     * }</pre>
     *
     * @see EnableAutoConfiguration
     * @see AutoConfigurationImportSelector#PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE
     * @since 1.0.0
     */
    String SPRING_AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME = "spring.autoconfigure.exclude";

    /**
     * The name of the {@link PropertySource} attached to the {@link Environment} since Spring Boot 2.x.
     * This property source name is used by {@link ConfigurationPropertySources} to identify
     * the attached configuration property source for accessing configuration properties.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Environment env = applicationContext.getEnvironment();
     *   PropertySource<?> ps = env.getPropertySources().get(ATTACHED_PROPERTY_SOURCE_NAME);
     *   if (ps != null) {
     *       // Access configuration properties
     *   }
     * }</pre>
     *
     * @see ConfigurationPropertySources#ATTACHED_PROPERTY_SOURCE_NAME
     * @since 1.0.0
     */
    String ATTACHED_PROPERTY_SOURCE_NAME = "configurationProperties";
}
