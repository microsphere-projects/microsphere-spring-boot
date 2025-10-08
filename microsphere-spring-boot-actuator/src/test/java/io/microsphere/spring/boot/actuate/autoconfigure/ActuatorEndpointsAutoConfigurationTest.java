package io.microsphere.spring.boot.actuate.autoconfigure;

import io.microsphere.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationMetadataEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationPropertiesEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationPropertiesEndpoint.ConfigurationPropertiesDescriptor;
import io.microsphere.spring.boot.actuate.endpoint.WebEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.WebApplicationType.SERVLET;

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
        },
        properties = {
                "management.endpoint.loggers.enabled=false"
        }
)
@PropertySource(value = "classpath:META-INF/config/default/endpoints.properties")
@TestPropertySource(value = "classpath:META-INF/config/default/endpoints.properties")
@EnableAutoConfiguration
public class ActuatorEndpointsAutoConfigurationTest {

    @Autowired
    private ArtifactsEndpoint artifactsEndpoint;

    @Autowired
    private WebEndpoints webEndpoints;

    @Autowired
    private ConfigurationMetadataEndpoint configurationMetadataEndpoint;

    @Autowired
    private ConfigurationPropertiesEndpoint configurationPropertiesEndpoint;

    @Test
    void testArtifactsEndpoint() {
        assertFalse(artifactsEndpoint.getArtifactMetaInfoList().isEmpty());
    }

    @Test
    void testInvokeReadOperations() {
        Map<String, Object> aggregatedResults = webEndpoints.invokeReadOperations();
        assertFalse(aggregatedResults.isEmpty());
    }

    @Test
    void testGetConfigurationMetadata() {
        ConfigurationMetadataEndpoint.ConfigurationMetadataDescriptor configurationMetadata = configurationMetadataEndpoint.getConfigurationMetadata();
        assertFalse(configurationMetadata.getGroups().isEmpty());
        assertFalse(configurationMetadata.getProperties().isEmpty());
    }

    @Test
    void testGetConfigurationProperties() {
        ConfigurationPropertiesDescriptor descriptor = configurationPropertiesEndpoint.getConfigurationProperties();
        assertNotNull(descriptor);
        assertFalse(descriptor.getConfigurationProperties().isEmpty());
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ActuatorEndpointsAutoConfigurationTest.class)
                .web(SERVLET)
                .run(args);
    }

}
