package io.microsphere.spring.boot.classloading;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.classloading.BannedArtifactClassLoadingExecutor;
import io.microsphere.logging.Logger;
import io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.getBoolean;
import static java.lang.Thread.currentThread;

/**
 * {@link ApplicationStartingEvent ApplicationStartingEvent} {@link ApplicationListener Listener} bans
 * the load of Artifacts collision class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class BannedArtifactClassLoadingListener extends SpringApplicationRunListenerAdapter implements Ordered {

    private static final Logger logger = getLogger(BannedArtifactClassLoadingListener.class);

    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = "false",
            description = "Whether to ban the artifacts from class loading",
            source = SYSTEM_PROPERTIES_SOURCE
    )
    public static final String BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "banned-artifacts.enabled";

    private static final ConcurrentMap<SpringApplication, Boolean> processedMap = new ConcurrentHashMap<>();

    static {
        addShutdownHookCallback(processedMap::clear);
    }

    public BannedArtifactClassLoadingListener(SpringApplication springApplication, String... args) {
        super(springApplication, args);
        setOrder(HIGHEST_PRECEDENCE);
    }

    @Override
    public void starting() {
        if (isProcessed()) {
            logger.trace("Current application's artifacts have been processed!");
            return;
        }

        if (bannedArtifactsEnabled()) {
            banArtifacts();
        } else {
            logger.trace("The artifacts will not be banned, caused by the JDK System property('{}') is missing or 'false'",
                    BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
        }
        markProcessed();
    }

    boolean isProcessed() {
        Boolean processed = processedMap.getOrDefault(getSpringApplication(), FALSE);
        return TRUE.equals(processed);
    }

    private boolean bannedArtifactsEnabled() {
        return getBoolean(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
    }

    private void markProcessed() {
        processedMap.put(getSpringApplication(), TRUE);
    }

    private void banArtifacts() {

        SpringApplication springApplication = getSpringApplication();

        logger.trace("Current SpringApplication(Main class: '{}', arguments: {}) tries to ban the artifacts!",
                springApplication.getMainApplicationClass(), arrayToString(args));

        ClassLoader classLoader = springApplication.getClassLoader();

        ClassLoader contextClassLoader = currentThread().getContextClassLoader();

        if (classLoader != contextClassLoader) {
            logger.info("The artifacts will not be banned, caused by the SpringApplication's ClassLoader[{}] is different " +
                    "from the current thread context ClassLoader[{}].", classLoader, contextClassLoader);
            return;
        }

        BannedArtifactClassLoadingExecutor discoverer = new BannedArtifactClassLoadingExecutor(classLoader);

        discoverer.execute();
    }
}