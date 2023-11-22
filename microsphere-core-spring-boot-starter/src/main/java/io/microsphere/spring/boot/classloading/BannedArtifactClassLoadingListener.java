package io.microsphere.spring.boot.classloading;

import io.microsphere.classloading.BannedArtifactClassLoadingExecutor;
import io.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * {@link ApplicationStartingEvent ApplicationStartingEvent} {@link ApplicationListener Listener} bans
 * the load of Artifacts collision class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class BannedArtifactClassLoadingListener extends SpringApplicationRunListenerAdapter implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(BannedArtifactClassLoadingListener.class);

    private static final boolean artifactsBanned = Boolean.getBoolean("microsphere.artifacts.banned");

    private static final String SPRING_BOOT_LAUNCHER_CLASS_NAME = "org.springframework.boot.loader.Launcher";

    private static final ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();

    static final boolean SPRING_BOOT_LAUNCHER_CLASS_PRESENT = ClassUtils.isPresent(SPRING_BOOT_LAUNCHER_CLASS_NAME, defaultClassLoader);

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
