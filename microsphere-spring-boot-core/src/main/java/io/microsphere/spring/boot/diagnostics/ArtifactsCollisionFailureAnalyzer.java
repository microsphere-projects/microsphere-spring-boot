package io.microsphere.spring.boot.diagnostics;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;

import java.util.Set;

import static java.lang.System.lineSeparator;

/**
 * Artifacts Collision {@link FailureAnalyzer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ArtifactsCollisionFailureAnalyzer extends AbstractFailureAnalyzer<ArtifactsCollisionException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, ArtifactsCollisionException cause) {
        return new FailureAnalysis(cause.getMessage(), buildAction(cause), cause);
    }

    private String buildAction(ArtifactsCollisionException cause) {
        Set<String> artifacts = cause.getArtifacts();
        StringBuilder actionBuilder = new StringBuilder("Analyze conflict Artifacts by running the following Maven command in the root directory of the project source code:")
                .append(lineSeparator())
                .append("mvn dependency:tree -Dincludes=");
        for (String artifact : artifacts) {
            actionBuilder.append(artifact).append(",");
        }

        actionBuilder.append(lineSeparator()).append("After analyzing the results, exclude them in the pom.xml file one by one!");

        return actionBuilder.toString();
    }
}
