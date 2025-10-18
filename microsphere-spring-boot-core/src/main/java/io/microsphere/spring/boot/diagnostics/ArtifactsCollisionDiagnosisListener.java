package io.microsphere.spring.boot.diagnostics;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.classloading.Artifact;
import io.microsphere.classloading.ArtifactDetector;
import io.microsphere.classloading.MavenArtifact;
import io.microsphere.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getResourceLoader;

/**
 * {@link ApplicationListener} to detect and diagnose artifacts collision in the application classpath.
 * <p>
 * This listener will be triggered when {@link ApplicationContextInitializedEvent} is published,
 * and it will scan all artifacts in the classpath to detect if there are any duplicate artifacts
 * (same group ID and artifact ID) existing.
 *
 * <h3>Example Usage</h3>
 * <p>
 * To enable this diagnosis, set the property:
 * <pre>
 * microsphere.spring.boot.artifacts-collision.enabled=true
 * </pre>
 * <p>
 * Example configuration in application.properties:
 * <pre>
 * microsphere.spring.boot.artifacts-collision.enabled=true
 * </pre>
 * <p>
 * Example configuration in application.yml:
 * <pre>
 * microsphere:
 *   spring:
 *     boot:
 *       artifacts-collision:
 *         enabled: true
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactsCollisionDiagnosisListener implements ApplicationListener<ApplicationContextInitializedEvent> {

    private static final Logger logger = getLogger(ArtifactsCollisionDiagnosisListener.class);

    /**
     * Whether to enable the Artifacts Collision diagnosis : "microsphere.spring.boot.artifacts-collision.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = "false",
            source = APPLICATION_SOURCE
    )
    public static final String ENABLED_PROPERTY_NAME = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "artifacts-collision.enabled";

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) throws ArtifactsCollisionException {
        if (isEnabled(event)) {
            SpringApplication springApplication = event.getSpringApplication();
            diagnose(springApplication);
        }
    }

    private boolean isEnabled(ApplicationContextInitializedEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        ConfigurableEnvironment environment = context.getEnvironment();
        return environment.getProperty(ENABLED_PROPERTY_NAME, Boolean.class, false);
    }

    private void diagnose(SpringApplication springApplication) throws ArtifactsCollisionException {
        ResourceLoader resourceLoader = getResourceLoader(springApplication);
        diagnose(resourceLoader);
    }

    private void diagnose(ResourceLoader resourceLoader) throws ArtifactsCollisionException {
        Set<String> artifactsCollisionSet = diagnose(resourceLoader.getClassLoader());
        if (!artifactsCollisionSet.isEmpty()) {
            throw new ArtifactsCollisionException("Artifacts conflict. The list is as follows:" + artifactsCollisionSet, artifactsCollisionSet);
        }
    }

    protected Set<String> diagnose(ClassLoader classLoader) {
        ArtifactDetector detector = new ArtifactDetector(classLoader);
        List<Artifact> artifacts = detector.detect(false);
        //  Artifacts conflict Map
        Map<String, Artifact> artifactsCollisionMap = getArtifactsCollisionMap(artifacts);
        if (!artifactsCollisionMap.isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner(LINE_SEPARATOR, "-\t", "");
            logger.error("Artifacts collision detected:");
            for (Artifact artifact : artifactsCollisionMap.values()) {
                stringJoiner.add(artifact.toString());
            }
            logger.error(stringJoiner.toString());
        }
        return artifactsCollisionMap.keySet();
    }

    Map<String, Artifact> getArtifactsCollisionMap(List<Artifact> artifacts) {
        int size = size(artifacts);
        Map<String, Artifact> artifactsCollisionMap = newLinkedHashMap(size);
        Set<String> ids = newLinkedHashSet(size);
        for (int i = 0; i < size; i++) {
            Artifact artifact = artifacts.get(i);
            String id = buildId(artifact);
            if (!ids.add(id)) {
                artifactsCollisionMap.put(id, artifact);
            }
        }
        return artifactsCollisionMap;
    }

    private String buildId(Artifact artifact) {
        if (artifact instanceof MavenArtifact) {
            MavenArtifact mavenArtifact = (MavenArtifact) artifact;
            return mavenArtifact.getGroupId() + ":" + mavenArtifact.getArtifactId();
        }
        return artifact.getArtifactId();
    }
}
