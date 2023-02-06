package io.github.microsphere.spring.boot.classloading;

import io.github.microsphere.classloading.ArtifactCollisionResourceDetector;
import io.github.microsphere.spring.boot.listener.SpringApplicationRunListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import sun.misc.URLClassPath;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@link ApplicationStartingEvent ApplicationStartingEvent} {@link ApplicationListener Listener} resolves
 * the load of Artifacts collision class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ResolvingArtifactsCollisionClassLoadingListener extends SpringApplicationRunListenerAdapter implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ResolvingArtifactsCollisionClassLoadingListener.class);

    private static final boolean artifactsCollisionResolved = Boolean.getBoolean("artifacts.collision.resolved");

    private static final String SPRING_BOOT_LAUNCHER_CLASS_NAME = "org.springframework.boot.loader.Launcher";

    private static final String URL_CLASS_PATH_CLASS_NAME = "sun.misc.URLClassPath";

    private static final ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();

    static final boolean SPRING_BOOT_LAUNCHER_CLASS_PRESENT =
            ClassUtils.isPresent(SPRING_BOOT_LAUNCHER_CLASS_NAME, defaultClassLoader);

    private static final boolean URL_CLASS_PATH_CLASS_PRESENT = ClassUtils.isPresent(URL_CLASS_PATH_CLASS_NAME, defaultClassLoader);

    private static final Class<?> URL_CLASS_PATH_CLASS;

    private static final Field ucpField;

    private static final Field loadersFields;

    private static final Class<?> loaderClass;

    private static final Field baseField;

    private static final boolean SUN_JDK_SUPPORTED;

    static {

        if (URL_CLASS_PATH_CLASS_PRESENT) {
            URL_CLASS_PATH_CLASS = ClassUtils.resolveClassName(URL_CLASS_PATH_CLASS_NAME, defaultClassLoader);
            ucpField = FieldUtils.getField(URLClassLoader.class, "ucp", true);
            loadersFields = FieldUtils.getField(URL_CLASS_PATH_CLASS, "loaders", true);
            loaderClass = ClassUtils.resolveClassName("sun.misc.URLClassPath.Loader", defaultClassLoader);
            baseField = FieldUtils.getField(loaderClass, "base", true);
        } else {
            URL_CLASS_PATH_CLASS = null;
            ucpField = null;
            loadersFields = null;
            loaderClass = null;
            baseField = null;
        }

        SUN_JDK_SUPPORTED = URL_CLASS_PATH_CLASS != null &&
                ucpField != null &&
                loadersFields != null &&
                loaderClass != null &&
                baseField != null;
    }

    private static boolean resolved = false;

    public ResolvingArtifactsCollisionClassLoadingListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
        setOrder(HIGHEST_PRECEDENCE);
    }

    @Override
    public void starting() {
        if (!resolved) {
            ResourceLoader resourceLoader = getResourceLoader(springApplication);
            resolveArtifactsCollision(resourceLoader);
            resolved = true;
        }
    }

    private void resolveArtifactsCollision(ResourceLoader resourceLoader) {

        logger.debug("SpringApplication[Main class: {}, start parameter: {}] Try to resolve possible collisions from Artifacts!",
                springApplication.getMainApplicationClass(), Arrays.asList(args));

        if (SPRING_BOOT_LAUNCHER_CLASS_PRESENT) {
            logger.debug("The current application is booted by the Spring Boot Launcher. There is no need to handle Artifacts collisions!");
            return;
        }

        if (!SUN_JDK_SUPPORTED) {
            logger.debug("Artifacts collisions will not be handled if the current application uses non-Sun JDK!");
            return;
        }

        if (artifactsCollisionResolved) {
            logger.debug("Current application Artifacts collisions have been marked and resolved!");
            return;
        }

        ClassLoader classLoader = resourceLoader.getClassLoader();

        if (!(classLoader instanceof URLClassLoader)) {
            logger.debug("The current ClassLoader[type: {}] is not URLClassLoader. Artifacts collisions will not be processed!", classLoader.getClass());
            return;
        }

        if (classLoader != Thread.currentThread().getContextClassLoader()) {
            logger.debug("The ClassLoader used by the current application is different from the current thread context ClassLoader. Artifacts collisions will not be handled!");
            return;
        }

        URLClassLoader currentClassLoader = (URLClassLoader) classLoader;

        ArtifactCollisionResourceDetector discoverer = new ArtifactCollisionResourceDetector(currentClassLoader);
        Map<URL, String> artifactCollisionResources = discoverer.detect();

        if (artifactCollisionResources.isEmpty()) {
            logger.debug("No Artifacts resource collisions are found. No need to handle Artifacts collisions!");
            return;
        }
        try {
            resolveArtifactsCollision(currentClassLoader, artifactCollisionResources.keySet());
        } catch (Throwable e) {
            logger.error("Serious error occurred in handling Artifacts collisions", e);
        }
    }

    private void resolveArtifactsCollision(URLClassLoader currentClassLoader, Set<URL> artifactCollisionResources) throws Throwable {

        URLClassPath urlClassPath = (URLClassPath) ucpField.get(currentClassLoader);
        // Remove collision loaders
        ArrayList<Object> loaders = (ArrayList<Object>) loadersFields.get(urlClassPath);
        Iterator<Object> iterator = loaders.iterator();
        while (iterator.hasNext()) {
            Object loader = iterator.next();
            URL base = (URL) baseField.get(loader);
            String protocol = base.getProtocol();
            if ("jar".equals(protocol)) {
                String path = StringUtils.substringBetween(base.toString(), "jar:", "!/");
                if (path != null) {
                    try {
                        if (artifactCollisionResources.contains(new URL(path))) {
                            logger.debug("ClassPath contains Artifacts to exclude: {}", base);
                            iterator.remove();
                        }
                    } catch (MalformedURLException e) {
                        logger.error("URLClassPath.Loader base URL path parsing error: {}", base);
                    }
                }
            }
        }
    }

    private ResourceLoader getResourceLoader(SpringApplication springApplication) {
        ResourceLoader resourceLoader = springApplication.getResourceLoader();
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader(springApplication.getClassLoader());
        }
        return resourceLoader;
    }
}
