package io.microsphere.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.spring.util.ObjectUtils.of;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

/**
 * Configurable {@link AutoConfigurationImportFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConfigurableAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware, Ordered {

    public static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "microsphere.autoconfigure.exclude";

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
        MutablePropertySources propertySources = getPropertySources(environment);
        Set<String> allExcludedClasses = new TreeSet<>();
        for (PropertySource propertySource : propertySources) {
            Object property = propertySource.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE);
            if (property instanceof String) {
                String exclude = (String) property;
                String resolvedExclude = environment.resolvePlaceholders(exclude);
                Set<String> excludedClasses = StringUtils.commaDelimitedListToSet(resolvedExclude);
                allExcludedClasses.addAll(excludedClasses);
            }
        }
        return unmodifiableSet(allExcludedClasses);
    }

    public static void addExcludedAutoConfigurationClass(Environment environment, String className) {
        addExcludedAutoConfigurationClasses(environment, of(className));
    }

    public static void addExcludedAutoConfigurationClasses(Environment environment, String... classNames) {
        addExcludedAutoConfigurationClasses(environment, Arrays.asList(classNames));
    }

    public static void addExcludedAutoConfigurationClasses(Environment environment, Iterable<String> classNames) {
        ExcludedAutoConfigurationClassPropertySource propertySource = ExcludedAutoConfigurationClassPropertySource.get(environment);
        propertySource.addClasses(classNames);
    }

    static MutablePropertySources getPropertySources(Environment environment) {
        return getConfigurableEnvironment(environment).getPropertySources();
    }

    static ConfigurableEnvironment getConfigurableEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        return (ConfigurableEnvironment) environment;
    }

    static class ExcludedAutoConfigurationClassPropertySource extends PropertySource<Set<String>> {

        private static final String NAME = PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE;

        private ExcludedAutoConfigurationClassPropertySource() {
            super(NAME, new LinkedHashSet<>());
        }

        @Override
        public Object getProperty(String name) {
            if (PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE.equals(name)) {
                return collectionToCommaDelimitedString(getSource());
            }
            return null;
        }

        public void addClasses(Iterable<String> classNames) {
            Set<String> allClassNames = getSource();
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

    private boolean isExcluded(String autoConfigurationClass) {
        return excludedAutoConfigurationClasses.contains(autoConfigurationClass);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 99;
    }
}
