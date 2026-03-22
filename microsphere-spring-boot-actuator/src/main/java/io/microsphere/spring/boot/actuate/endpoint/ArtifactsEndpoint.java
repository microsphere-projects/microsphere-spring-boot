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

    /**
     * Constructs an {@link ArtifactsEndpoint} with the specified {@link ClassLoader}
     * used to detect artifacts.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     *   ArtifactsEndpoint endpoint = new ArtifactsEndpoint(classLoader);
     * }</pre>
     *
     * @param classLoader the {@link ClassLoader} to scan for artifacts
     */
    public ArtifactsEndpoint(ClassLoader classLoader) {
        this.artifactDetector = new ArtifactDetector(classLoader);
    }

    /**
     * Returns the list of detected {@link Artifact} metadata from the class path.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ArtifactsEndpoint endpoint = new ArtifactsEndpoint(classLoader);
     *   List<Artifact> artifacts = endpoint.getArtifactMetaInfoList();
     *   artifacts.forEach(artifact -> System.out.println(artifact));
     * }</pre>
     *
     * @return a list of {@link Artifact} instances detected on the class path
     */
    @ReadOperation
    public List<Artifact> getArtifactMetaInfoList() {
        return artifactDetector.detect(false);
    }
}