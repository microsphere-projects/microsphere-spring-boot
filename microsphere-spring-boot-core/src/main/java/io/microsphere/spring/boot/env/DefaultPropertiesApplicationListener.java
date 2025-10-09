package io.microsphere.spring.boot.env;

import io.microsphere.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getDefaultPropertiesResources;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getResourceLoader;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getDefaultProperties;
import static io.microsphere.spring.core.io.ResourceLoaderUtils.getResourcePatternResolver;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;
import static org.springframework.util.ObjectUtils.containsElement;

/**
 * Listable {@link ApplicationEnvironmentPreparedEvent} {@link ApplicationListener} Class
 * {@link SpringApplication#setDefaultProperties(Properties) "defaultProperties"}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SpringApplication#setDefaultProperties(Properties)
 * @see DefaultPropertiesPostProcessor
 * @since 1.0.0
 */
public class DefaultPropertiesApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    public static final int DEFAULT_ORDER = LoggingApplicationListener.LOWEST_PRECEDENCE - 1;

    private static final Logger logger = getLogger(DefaultPropertiesApplicationListener.class);

    private int order;

    public DefaultPropertiesApplicationListener() {
        this.setOrder(DEFAULT_ORDER);
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        SpringApplication springApplication = event.getSpringApplication();
        processDefaultProperties(environment, springApplication);
    }

    private void processDefaultProperties(ConfigurableEnvironment environment, SpringApplication springApplication) {
        Map<String, Object> defaultProperties = getDefaultProperties(environment);
        postProcessDefaultProperties(springApplication, defaultProperties);
        logDefaultProperties(springApplication, defaultProperties);
    }

    private void postProcessDefaultProperties(SpringApplication springApplication, Map<String, Object> defaultProperties) {
        ResourceLoader resourceLoader = getResourceLoader(springApplication);
        ClassLoader classLoader = resourceLoader.getClassLoader();
        List<PropertySourceLoader> propertySourceLoaders = loadFactories(PropertySourceLoader.class, classLoader);
        List<DefaultPropertiesPostProcessor> defaultPropertiesPostProcessors = loadFactories(DefaultPropertiesPostProcessor.class, classLoader);
        // DefaultPropertiesPostProcessor execute
        for (DefaultPropertiesPostProcessor defaultPropertiesPostProcessor : defaultPropertiesPostProcessors) {
            postProcessDefaultProperties(defaultPropertiesPostProcessor, propertySourceLoaders, resourceLoader, defaultProperties);
        }

        // Compatible SpringApplicationUtils#getDefaultPropertiesResources way
        loadDefaultPropertiesResources(propertySourceLoaders, resourceLoader, defaultProperties);
    }

    private void loadDefaultPropertiesResources(List<PropertySourceLoader> propertySourceLoaders,
                                                ResourceLoader resourceLoader,
                                                Map<String, Object> defaultProperties) {
        Set<String> defaultPropertiesResources = getDefaultPropertiesResources();
        logger.debug("Start loading from SpringApplicationUtils.loadDefaultPropertiesResources() 'defaultProperties resources: {}", defaultPropertiesResources);
        loadDefaultProperties(defaultPropertiesResources, propertySourceLoaders, resourceLoader, defaultProperties);
    }

    private void postProcessDefaultProperties(DefaultPropertiesPostProcessor defaultPropertiesPostProcessor,
                                              List<PropertySourceLoader> propertySourceLoaders,
                                              ResourceLoader resourceLoader,
                                              Map<String, Object> defaultProperties) {
        Set<String> defaultPropertiesResources = new LinkedHashSet<>();

        String processorClassName = defaultPropertiesPostProcessor.getClass().getName();

        logger.debug("DefaultPropertiesPostProcessor '{}' start processing 'defaultProperties: {}", processorClassName, defaultPropertiesResources);
        defaultPropertiesPostProcessor.initializeResources(defaultPropertiesResources);

        // load "defaultProperties"
        loadDefaultProperties(defaultPropertiesResources, propertySourceLoaders, resourceLoader, defaultProperties);

        defaultPropertiesPostProcessor.postProcess(defaultProperties);
        logger.debug("DefaultPropertiesPostProcessor '{}' end processing 'defaultProperties: {}", processorClassName, defaultPropertiesResources);
    }

    private void loadDefaultProperties(Collection<String> defaultPropertiesResources,
                                       List<PropertySourceLoader> propertySourceLoaders,
                                       ResourceLoader resourceLoader,
                                       Map<String, Object> defaultProperties) {
        logger.debug("Start loading the 'defaultProperties' resource path list: {}", defaultPropertiesResources);
        ResourcePatternResolver resourcePatternResolver = getResourcePatternResolver(resourceLoader);
        for (String defaultPropertiesResource : defaultPropertiesResources) {
            try {
                for (Resource resource : resourcePatternResolver.getResources(defaultPropertiesResource)) {
                    if (!loadDefaultProperties(defaultPropertiesResource, resource, propertySourceLoaders, defaultProperties)) {
                        logger.warn("'defaultProperties' resource [location: {}] failed to load, please confirm the resource can be processed!", resource.getURL());
                    }
                }
            } catch (IOException e) {
                logger.warn("'defaultProperties' resource [location: {}] does not exist, please make sure the resource is correct!", defaultPropertiesResource, e);
            }
        }
    }

    private boolean loadDefaultProperties(String defaultPropertiesResource, Resource resource,
                                          List<PropertySourceLoader> propertySourceLoaders,
                                          Map<String, Object> defaultProperties) throws IOException {
        boolean loaded = false;
        for (PropertySourceLoader propertySourceLoader : propertySourceLoaders) {
            if (loadDefaultProperties(defaultPropertiesResource, resource, propertySourceLoader, defaultProperties)) {
                loaded = true;
                break;
            }
        }
        return loaded;
    }

    private boolean loadDefaultProperties(String defaultPropertiesResource, Resource resource,
                                          PropertySourceLoader propertySourceLoader,
                                          Map<String, Object> defaultProperties) {
        boolean loaded = false;
        try {
            URL url = resource.getURL();
            String resourceLocation = url.getPath();
            String fileExtension = getExtension(resourceLocation);
            String[] fileExtensions = propertySourceLoader.getFileExtensions();
            if (matches(fileExtension, fileExtensions)) {

                List<PropertySource<?>> propertySources = propertySourceLoader.load(resourceLocation, resource);
                logger.debug("'defaultProperties' resource [location: {}] loads into {} PropertySource", resourceLocation, propertySources.size());
                for (PropertySource propertySource : propertySources) {
                    if (propertySource instanceof EnumerablePropertySource) {
                        merge((EnumerablePropertySource) propertySource, defaultProperties);
                        loaded = true;
                    }
                }

            }
        } catch (IOException e) {
            logger.error("'defaultProperties' resource [{}] failed to load", defaultPropertiesResource, e);
        }
        return loaded;
    }

    private void merge(EnumerablePropertySource<?> propertySource, Map<String, Object> defaultProperties) {
        logger.debug("'defaultProperties' PropertySource[{}] Try merging!", propertySource);
        String[] propertyNames = propertySource.getPropertyNames();
        for (String propertyName : propertyNames) {
            Object propertyValue = propertySource.getProperty(propertyName);
            Object oldPropertyValue = defaultProperties.putIfAbsent(propertyName, propertyValue);
            if (oldPropertyValue == null) {
                logger.debug("'defaultProperties' attribute [name: {}, value: {}] added successfully!", propertyName, propertyValue);
            } else {
                logger.warn("'defaultProperties' attribute [name: {}, old-value: {}] already exists, new-value[{}] will not be merged!",
                        propertyName, oldPropertyValue, propertyValue);
            }
        }
    }

    private boolean matches(String fileExtension, String[] fileExtensions) {
        return containsElement(fileExtensions, fileExtension);
    }

    private String getExtension(String resourceLocation) {
        int index = resourceLocation.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        String extension = resourceLocation.substring(index + 1);
        return extension;
    }

    private void logDefaultProperties(SpringApplication springApplication, Map<String, Object> defaultProperties) {
        logger.debug("SpringApplication[sources:{}] defaultProperties:", springApplication.getSources());
        defaultProperties.forEach((key, value) -> {
            logger.debug("'{}' = {}", key, value);
        });
    }


    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}