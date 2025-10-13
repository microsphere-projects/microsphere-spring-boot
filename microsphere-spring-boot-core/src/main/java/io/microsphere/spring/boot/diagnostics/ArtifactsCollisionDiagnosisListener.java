package io.microsphere.spring.boot.diagnostics;

import io.microsphere.classloading.Artifact;
import io.microsphere.classloading.ArtifactDetector;
import io.microsphere.classloading.MavenArtifact;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getResourceLoader;

/**
 * Artifacts Collision diagnosis listener
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactsCollisionDiagnosisListener implements ApplicationListener<ApplicationContextInitializedEvent> {

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) throws ArtifactsCollisionException {
        SpringApplication springApplication = event.getSpringApplication();
        diagnose(springApplication);
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
        //  Artifacts conflict set
        return getArtifactsCollisionSet(artifacts);
    }

    Set<String> getArtifactsCollisionSet(List<Artifact> artifacts) {
        int size = size(artifacts);
        Set<String> artifactsCollisionSet = newLinkedHashSet();
        Set<String> ids = newLinkedHashSet(size);
        for (int i = 0; i < size; i++) {
            Artifact artifact = artifacts.get(i);
            String id = buildId(artifact);
            if (!ids.add(id)) {
                artifactsCollisionSet.add(id);
            }
        }
        return artifactsCollisionSet;
    }

    private String buildId(Artifact artifact) {
        if (artifact instanceof MavenArtifact mavenArtifact) {
            return mavenArtifact.getGroupId() + ":" + mavenArtifact.getArtifactId();
        }
        return artifact.getArtifactId();
    }
}
