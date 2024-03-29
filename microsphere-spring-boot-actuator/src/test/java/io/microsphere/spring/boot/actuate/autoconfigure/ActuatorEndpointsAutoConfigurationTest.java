package io.microsphere.spring.boot.actuate.autoconfigure;

import io.microsphere.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

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
@EnableAutoConfiguration
class ActuatorEndpointsAutoConfigurationTest {

    @Autowired
    private ArtifactsEndpoint artifactsEndpoint;

    @Test
    void testArtifactsEndpoint() {
    }

}
