package io.github.microsphere.spring.boot.diagnostics;

import java.util.Set;

/**
 * Artifacts Collision {@link RuntimeException}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactsCollisionException extends RuntimeException {

    private final Set<String> artifacts;

    public ArtifactsCollisionException(String message, Set<String> artifacts) {
        super(message);
        this.artifacts = artifacts;
    }

    public Set<String> getArtifacts() {
        return artifacts;
    }
}
