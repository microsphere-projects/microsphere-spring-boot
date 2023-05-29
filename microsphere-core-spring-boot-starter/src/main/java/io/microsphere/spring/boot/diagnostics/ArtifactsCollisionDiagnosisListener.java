package io.microsphere.spring.boot.diagnostics;

import io.microsphere.classloading.ArtifactCollisionResourceDetector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;

import java.util.HashSet;
import java.util.Set;

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

    protected void diagnose(ResourceLoader resourceLoader) throws ArtifactsCollisionException {
        ArtifactCollisionResourceDetector detector = new ArtifactCollisionResourceDetector(resourceLoader.getClassLoader());
        //  Artifacts conflict set
        Set<String> artifactsCollisionSet = new HashSet<>(detector.detect().values());

        if (!artifactsCollisionSet.isEmpty()) {
            throw new ArtifactsCollisionException("Artifacts conflict. The list is as follows:" + artifactsCollisionSet, artifactsCollisionSet);
        }
    }
}
