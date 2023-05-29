package io.microsphere.spring.boot.util;

import org.springframework.boot.SpringApplication;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link SpringApplication} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SpringApplicationUtils {

    private static final Set<String> defaultPropertiesResources = new LinkedHashSet<>();

    private SpringApplicationUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    /**
     * Add "defaultProperties" resource path
     *
     * @param resourceLocation "defaultProperties" resource path
     */
    public static void addDefaultPropertiesResource(String resourceLocation) {
        if (StringUtils.hasText(resourceLocation)) {
            defaultPropertiesResources.add(resourceLocation);
        }
    }

    /**
     * Add one or more "defaultProperties" resource paths
     *
     * @param resourceLocations one or more "defaultProperties" resource paths
     */
    public static void addDefaultPropertiesResources(String... resourceLocations) {
        if (resourceLocations == null) {
            return;
        }
        for (String resource : resourceLocations) {
            addDefaultPropertiesResource(resource);
        }
    }

    /**
     * @return read-only {@link #defaultPropertiesResources}
     */
    public static Set<String> getDefaultPropertiesResources() {
        return Collections.unmodifiableSet(defaultPropertiesResources);
    }

    public static ResourceLoader getResourceLoader(SpringApplication springApplication) {
        ResourceLoader resourceLoader = springApplication.getResourceLoader();
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader(springApplication.getClassLoader());
        }
        return resourceLoader;
    }
}
