package io.microsphere.spring.boot.diagnostics;

import java.util.Set;

/**
 * Artifacts Collision {@link RuntimeException}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactsCollisionException extends RuntimeException {

    private final Set<String> artifacts;

    /**
     * Construct a new {@link ArtifactsCollisionException} with the specified message and set of colliding artifacts.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Set<String> artifacts = new LinkedHashSet<>();
     *   artifacts.add("com.example:my-lib");
     *   throw new ArtifactsCollisionException("Duplicate artifacts found", artifacts);
     * }</pre>
     *
     * @param message the detail message describing the collision
     * @param artifacts the set of colliding artifact identifiers
     */
    public ArtifactsCollisionException(String message, Set<String> artifacts) {
        super(message);
        this.artifacts = artifacts;
    }

    /**
     * Return the set of colliding artifact identifiers.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   try {
     *       // application startup
     *   } catch (ArtifactsCollisionException e) {
     *       Set<String> collisions = e.getArtifacts();
     *       collisions.forEach(System.err::println);
     *   }
     * }</pre>
     *
     * @return the set of colliding artifact identifiers
     */
    public Set<String> getArtifacts() {
        return artifacts;
    }
}