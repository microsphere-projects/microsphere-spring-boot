package io.microsphere.spring.boot.util;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.boot.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static java.util.Collections.unmodifiableSet;
import static java.util.Locale.ENGLISH;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link SpringApplication} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SpringApplicationUtils implements Utils {

    private static final Logger logger = getLogger(SpringApplicationUtils.class);

    private static final Set<String> defaultPropertiesResources = new LinkedHashSet<>();

    static {
        addShutdownHookCallback(defaultPropertiesResources::clear);
    }

    /**
     * Add "defaultProperties" resource path
     *
     * @param resourceLocation "defaultProperties" resource path
     */
    public static void addDefaultPropertiesResource(String resourceLocation) {
        if (hasText(resourceLocation)) {
            defaultPropertiesResources.add(resourceLocation);
        }
    }

    /**
     * Add one or more "defaultProperties" resource paths
     *
     * @param resourceLocations one or more "defaultProperties" resource paths
     */
    public static void addDefaultPropertiesResources(String... resourceLocations) {
        if (resourceLocations != null) {
            for (String resource : resourceLocations) {
                addDefaultPropertiesResource(resource);
            }
        }
    }

    /**
     * @return read-only {@link #defaultPropertiesResources}
     */
    public static Set<String> getDefaultPropertiesResources() {
        return unmodifiableSet(defaultPropertiesResources);
    }

    public static ResourceLoader getResourceLoader(SpringApplication springApplication) {
        ResourceLoader resourceLoader = springApplication.getResourceLoader();
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader(springApplication.getClassLoader());
        }
        return resourceLoader;
    }

    /**
     * Get logging level with upper case from the Spring {@link PropertySources}
     *
     * @param context {@link ConfigurableApplicationContext} context
     * @return logging level with upper case
     */
    public static String getLoggingLevel(@Nullable ConfigurableApplicationContext context) {
        return getLoggingLevel(context == null ? null : context.getEnvironment());
    }

    /**
     * Get logging level(Upper case) from the Spring {@link PropertySources}
     *
     * @param propertyResolver {@link PropertyResolver}
     * @return logging level with upper case
     */
    public static String getLoggingLevel(@Nullable PropertyResolver propertyResolver) {
        String level = propertyResolver == null ? DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL :
                propertyResolver.getProperty(MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME, DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL);
        return level.toUpperCase(ENGLISH);
    }

    /**
     * Log {@link SpringApplication}
     *
     * @param springApplication {@link SpringApplication}
     * @param args              the command line arguments
     * @param pattern           the logging message pattern
     * @param patternArgs       the logging message arguments
     */
    public static void log(SpringApplication springApplication, String[] args, String pattern, Object... patternArgs) {
        log(springApplication, args, null, pattern, patternArgs);
    }

    /**
     * Log {@link SpringApplication}
     *
     * @param springApplication {@link SpringApplication}
     * @param args              the command line arguments
     * @param context           {@link ConfigurableApplicationContext}
     * @param pattern           the logging message pattern
     * @param patternArgs       the logging message arguments
     */
    public static void log(SpringApplication springApplication, String[] args, @Nullable ConfigurableApplicationContext context,
                           String pattern, Object... patternArgs) {
        String messagePattern = "SpringApplication: " +
                "    main class : '{}' ," +
                "    web type : '{}' ," +
                "    sources : {} ," +
                "    all sources : {} ," +
                "    initializers : {} ," +
                "    listeners : {}," +
                "    args : {}," +
                "    context id : '{}'," +
                "    log : {},";

        Object[] arguments = ofArray(springApplication.getMainApplicationClass(),
                springApplication.getWebApplicationType(),
                springApplication.getSources(),
                springApplication.getAllSources(),
                springApplication.getInitializers(),
                springApplication.getListeners(),
                arrayToString(args),
                context == null ? "N/A" : context.getId(),
                format(pattern, patternArgs));

        String level = getLoggingLevel(context);

        switch (level) {
            case "TRACE": {
                logger.trace(messagePattern, arguments);
                break;
            }
            case "DEBUG": {
                logger.debug(messagePattern, arguments);
                break;
            }
            case "INFO": {
                logger.info(messagePattern, arguments);
                break;
            }
            case "WARN": {
                logger.warn(messagePattern, arguments);
                break;
            }
            case "ERROR": {
                logger.error(messagePattern, arguments);
                break;
            }
            default: {
                logger.trace("Logging is off");
                break;
            }
        }
    }

    private SpringApplicationUtils() {
    }
}