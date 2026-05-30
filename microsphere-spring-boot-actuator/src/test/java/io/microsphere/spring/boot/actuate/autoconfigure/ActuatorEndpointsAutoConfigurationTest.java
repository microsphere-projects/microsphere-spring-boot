package io.microsphere.spring.boot.actuate.autoconfigure;

import io.microsphere.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationMetadataEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationPropertiesEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.WebEndpoints;
import org.junit.jupiter.api.ClassOrderer.OrderAnnotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * {@link ActuatorEndpointsAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ActuatorEndpointsAutoConfiguration
 * @since 1.0.0
 */
@TestClassOrder(OrderAnnotation.class)
class ActuatorEndpointsAutoConfigurationTest {

    @Order(1)
    @Nested
    @DisplayName("test on defaults configuration")
    @SpringBootTest(
            webEnvironment = RANDOM_PORT,
            classes = {
                    ActuatorEndpointsAutoConfigurationTest.class,
            },
            properties = {
                    "management.endpoint.loggers.enabled=false"
            }
    )
    @EnableAutoConfiguration
    class Defaults {

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
            ConfigurationPropertiesEndpoint.ConfigurationPropertiesDescriptor descriptor = configurationPropertiesEndpoint.getConfigurationProperties();
            assertNotNull(descriptor);
            assertFalse(descriptor.getConfigurationProperties().isEmpty());
        }
    }

    @Order(2)
    @Nested
    @DisplayName("test on disabled configuration")
    @SpringBootTest(
            webEnvironment = RANDOM_PORT,
            classes = {
                    ActuatorEndpointsAutoConfigurationTest.class,
            },
            properties = {
                    "management.endpoint.artifacts.enabled=false",
                    "management.endpoint.webEndpoints.enabled=false",
                    "management.endpoint.configMetadata.enabled=false",
                    "management.endpoint.configProperties.enabled=false"
            }
    )
    @EnableAutoConfiguration
    class Disalbed {

        @Autowired(required = false)
        private ArtifactsEndpoint artifactsEndpoint;

        @Autowired(required = false)
        private WebEndpoints webEndpoints;

        @Autowired(required = false)
        private ConfigurationMetadataEndpoint configurationMetadataEndpoint;

        @Autowired(required = false)
        private ConfigurationPropertiesEndpoint configurationPropertiesEndpoint;

        @Test
        void test() {
            assertNull(this.artifactsEndpoint);
            assertNull(this.webEndpoints);
            assertNull(this.configurationMetadataEndpoint);
            assertNull(this.configurationPropertiesEndpoint);
        }
    }

}