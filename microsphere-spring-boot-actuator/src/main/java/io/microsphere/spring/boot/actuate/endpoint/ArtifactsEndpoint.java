package io.microsphere.spring.boot.actuate.endpoint;

import io.microsphere.classloading.Artifact;
import io.microsphere.classloading.ArtifactDetector;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * {@link Artifact Artifacts} {@link Endpoint @Endpoint}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Artifact
 * @see Endpoint
 * @since 1.0.0
 */
@Endpoint(id = "artifacts")
public class ArtifactsEndpoint {

    private final ArtifactDetector artifactDetector;

    public ArtifactsEndpoint(ClassLoader classLoader) {
        this.artifactDetector = new ArtifactDetector(classLoader);
    }

    @ReadOperation
    public List<Artifact> getArtifactMetaInfoList() {
        return artifactDetector.detect(false);
    }
}