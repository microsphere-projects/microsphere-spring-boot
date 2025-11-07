package io.microsphere.spring.boot.autoconfigure;

import io.microsphere.annotation.ConfigurationProperty;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.combine;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.boot.context.properties.bind.Binder.get;
import static org.springframework.util.Assert.isInstanceOf;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;
import static org.springframework.util.StringUtils.hasText;

/**
 * Configurable {@link AutoConfigurationImportFilter} for excluding specific Spring Boot auto-configuration classes.
 *
 * <h3>Example Usage</h3>
 * <h4>Exclude auto-configuration classes via property</h4>
 * <pre>{@code
 * microsphere.autoconfigure.exclude=com.example.FooAutoConfiguration,com.example.BarAutoConfiguration
 * }</pre>
 * or
 * <pre>{@code
 * microsphere.autoconfigure.exclude[0]=com.example.FooAutoConfiguration
 * microsphere.autoconfigure.exclude[1]=com.example.BarAutoConfiguration
 * }</pre>
 *
 * <h4>Programmatically exclude classes</h4>
 * <pre>{@code
 * ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass(environment, "com.example.FooAutoConfiguration");
 * ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClasses(environment, "com.example.BarAutoConfiguration", "com.example.BazAutoConfiguration");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConfigurableAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware, Ordered {

    @ConfigurationProperty(
            type = String[].class,
            description = "The property to exclude the classes of Spring Boot Auto-Configuration",
            source = APPLICATION_SOURCE
    )
    public static final String AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME = "microsphere.autoconfigure.exclude";

    private Set<String> excludedAutoConfigurationClasses;

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        int size = autoConfigurationClasses.length;
        boolean[] results = new boolean[size];
        for (int i = 0; i < size; i++) {
            String autoConfigurationClass = autoConfigurationClasses[i];
            results[i] = isExcluded(autoConfigurationClass) ? false : true;
        }
        return results;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.excludedAutoConfigurationClasses = getExcludedAutoConfigurationClasses(environment);
    }

    public static Set<String> getExcludedAutoConfigurationClasses(Environment environment) {
        ConfigurableEnvironment configurableEnvironment = asConfigurableEnvironment(environment);
        Set<String> allExcludedClasses = new LinkedHashSet<>();
        addExcludedAutoConfigurationClasses(environment, getExcludedAutoConfigurationClasses(configurableEnvironment), allExcludedClasses);
        addExcludedAutoConfigurationClasses(environment, getExcludedAutoConfigurationClassesFromBinder(configurableEnvironment), allExcludedClasses);
        return allExcludedClasses.isEmpty() ? emptySet() : unmodifiableSet(allExcludedClasses);
    }

    private static void addExcludedAutoConfigurationClasses(Environment environment, String[] excludedClasses,
                                                            Set<String> allExcludedClasses) {
        for (String excludedClass : excludedClasses) {
            String resolvedExcludeClass = environment.resolvePlaceholders(excludedClass);
            allExcludedClasses.addAll(commaDelimitedListToSet(resolvedExcludeClass));
        }
    }

    private static String[] getExcludedAutoConfigurationClasses(ConfigurableEnvironment environment) {
        Set<String> excludedClasses = new LinkedHashSet<>();
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource propertySource : propertySources) {
            Object property = propertySource.getProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME);
            if (property instanceof String) {
                String exclude = (String) property;
                String resolvedExclude = environment.resolvePlaceholders(exclude);
                Set<String> classes = commaDelimitedListToSet(resolvedExclude);
                excludedClasses.addAll(classes);
            }
        }
        return excludedClasses.isEmpty() ? EMPTY_STRING_ARRAY : excludedClasses.toArray(EMPTY_STRING_ARRAY);
    }

    private static String[] getExcludedAutoConfigurationClassesFromBinder(ConfigurableEnvironment environment) {
        Binder binder = get(environment);
        return binder.bind(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, String[].class).orElse(EMPTY_STRING_ARRAY);
    }

    /**
     * Add a class name to exclude Spring Boot Auto-Configuration
     *
     * @param environment {@link Environment}
     * @param className   the name of class
     */
    public static void addExcludedAutoConfigurationClass(Environment environment, String className) {
        addExcludedAutoConfigurationClasses(environment, className);
    }

    /**
     * Add one or more class names to exclude Spring Boot Auto-Configuration
     *
     * @param environment     {@link Environment}
     * @param className       one class name
     * @param otherClassNames more class names
     */
    public static void addExcludedAutoConfigurationClasses(Environment environment, String className, String... otherClassNames) {
        addExcludedAutoConfigurationClasses(environment, asList(combine(className, otherClassNames)));
    }

    /**
     * Add the class names to exclude Spring Boot Auto-Configuration
     *
     * @param environment {@link Environment}
     * @param classNames  the class names
     */
    public static void addExcludedAutoConfigurationClasses(Environment environment, Iterable<String> classNames) {
        ExcludedAutoConfigurationClassPropertySource propertySource = ExcludedAutoConfigurationClassPropertySource.get(environment);
        propertySource.addClasses(classNames);
    }

    private static MutablePropertySources getPropertySources(Environment environment) {
        return getConfigurableEnvironment(environment).getPropertySources();
    }

    private static ConfigurableEnvironment getConfigurableEnvironment(Environment environment) {
        isInstanceOf(ConfigurableEnvironment.class, environment);
        return (ConfigurableEnvironment) environment;
    }

    private static class ExcludedAutoConfigurationClassPropertySource extends PropertySource<Set<String>> {

        private static final String NAME = AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME;

        private ExcludedAutoConfigurationClassPropertySource() {
            super(NAME, new LinkedHashSet<>());
        }

        @Override
        public Object getProperty(String name) {
            if (AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME.equals(name)) {
                return collectionToCommaDelimitedString(this.source);
            }
            return null;
        }

        public void addClasses(Iterable<String> classNames) {
            Set<String> allClassNames = this.source;
            classNames.forEach(allClassNames::add);
        }

        static ExcludedAutoConfigurationClassPropertySource get(Environment environment) {
            MutablePropertySources propertySources = getPropertySources(environment);
            ExcludedAutoConfigurationClassPropertySource propertySource = (ExcludedAutoConfigurationClassPropertySource) propertySources.get(NAME);
            if (propertySource == null) {
                propertySource = new ExcludedAutoConfigurationClassPropertySource();
                propertySources.addFirst(propertySource);
            }
            return propertySource;
        }
    }

    boolean isExcluded(String autoConfigurationClassName) {
        return hasText(autoConfigurationClassName) && excludedAutoConfigurationClasses.contains(autoConfigurationClassName);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 99;
    }
}