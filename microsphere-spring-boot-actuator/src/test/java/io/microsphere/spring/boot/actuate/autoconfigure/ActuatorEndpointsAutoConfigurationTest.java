package io.microsphere.spring.boot.actuate.autoconfigure;

import io.microsphere.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.WebEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link ActuatorEndpointsAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ActuatorEndpointsAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                ActuatorEndpointsAutoConfigurationTest.class,
        })
@PropertySource(value = "classpath:META-INF/config/default/endpoints.properties")
@EnableAutoConfiguration
class ActuatorEndpointsAutoConfigurationTest {

    @Autowired
    private ArtifactsEndpoint artifactsEndpoint;

    @Autowired
    private WebEndpoints webEndpoints;

    @Test
    void testArtifactsEndpoint() {
        assertFalse(artifactsEndpoint.getArtifactMetaInfoList().isEmpty());
    }

    @Test
    public void testInvokeReadOperations() {
        Map<String, Object> aggregatedResults = webEndpoints.invokeReadOperations();
        assertFalse(aggregatedResults.isEmpty());
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ActuatorEndpointsAutoConfigurationTest.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

}
