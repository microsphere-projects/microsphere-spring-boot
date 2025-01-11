package io.microsphere.spring.boot.classloading;

import io.microsphere.classloading.BannedArtifactClassLoadingExecutor;
import io.microsphere.logging.Logger;
import io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.util.Arrays;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.util.ClassUtils.isPresent;

/**
 * {@link ApplicationStartingEvent ApplicationStartingEvent} {@link ApplicationListener Listener} bans
 * the load of Artifacts collision class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class BannedArtifactClassLoadingListener extends SpringApplicationRunListenerAdapter implements Ordered {

    private static final Logger logger = getLogger(BannedArtifactClassLoadingListener.class);

    private static final boolean artifactsBanned = Boolean.getBoolean("microsphere.artifacts.banned");

    private static final String SPRING_BOOT_LAUNCHER_CLASS_NAME = "org.springframework.boot.loader.Launcher";

    static final boolean SPRING_BOOT_LAUNCHER_CLASS_PRESENT = isPresent(SPRING_BOOT_LAUNCHER_CLASS_NAME, null);

    private static boolean banned = false;

    public BannedArtifactClassLoadingListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
        setOrder(HIGHEST_PRECEDENCE);
    }

    @Override
    public void starting() {
        if (!banned) {
            banArtifacts();
            banned = true;
        }
    }

    private void banArtifacts() {

        logger.debug("SpringApplication[Main class: {}, start parameter: {}] Try to ban the artifacts!",
                springApplication.getMainApplicationClass(), Arrays.asList(args));

        if (SPRING_BOOT_LAUNCHER_CLASS_PRESENT) {
            logger.debug("The current application is booted by the Spring Boot Launcher. No Artifacts to be banned!");
            return;
        }

        if (artifactsBanned) {
            logger.debug("Current application Artifacts have been marked and banned!");
            return;
        }

        ClassLoader classLoader = springApplication.getClassLoader();

        if (classLoader != Thread.currentThread().getContextClassLoader()) {
            logger.debug("The ClassLoader used by the current application is different from the current thread context ClassLoader. The artifacts will not be banned!");
            return;
        }

        BannedArtifactClassLoadingExecutor discoverer = new BannedArtifactClassLoadingExecutor(classLoader);

        discoverer.execute();
    }
}
