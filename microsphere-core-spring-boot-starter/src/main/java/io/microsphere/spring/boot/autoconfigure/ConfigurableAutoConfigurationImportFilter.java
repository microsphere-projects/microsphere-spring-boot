package io.microsphere.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Configurable {@link AutoConfigurationImportFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConfigurableAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware, Ordered {

    public static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "microsphere.autoconfigure.exclude";

    private List<String> excludeAutoConfigurationClasses;

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
        this.excludeAutoConfigurationClasses = getExcludeAutoConfigurationClasses(environment);
    }

    private List<String> getExcludeAutoConfigurationClasses(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
        PropertySources propertySources = env.getPropertySources();
        Set<String> allExcludedClasses = new TreeSet<>();
        for (PropertySource propertySource : propertySources) {
            Object property = propertySource.getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE);
            if (property instanceof String) {
                String exclude = (String) property;
                String resolvedExclude = env.resolvePlaceholders(exclude);
                Set<String> excludedClasses = StringUtils.commaDelimitedListToSet(resolvedExclude);
                allExcludedClasses.addAll(excludedClasses);
            }
        }
        return new LinkedList<>(allExcludedClasses);
    }

    private boolean isExcluded(String autoConfigurationClass) {
        return excludeAutoConfigurationClasses.contains(autoConfigurationClass);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 99;
    }
}
