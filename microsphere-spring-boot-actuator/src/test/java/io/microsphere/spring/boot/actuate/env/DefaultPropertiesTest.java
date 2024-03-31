/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.boot.actuate.env;

import io.microsphere.spring.boot.env.SpringApplicationDefaultPropertiesPostProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring Boot Default Properties Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SpringApplicationDefaultPropertiesPostProcessor
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {DefaultPropertiesTest.class})
public class DefaultPropertiesTest {

    @Autowired
    private Environment environment;

    @Test
    public void testEndpointsDefaultProperties() {
        assertProperty("management.endpoints.enabled-by-default", "false");
        assertProperty("management.endpoints.web.exposure.include", "*");
        assertProperty("management.endpoints.jmx.exposure.exclude", "*");

        assertProperty("management.endpoint.auditevents.enabled", "false");
        assertProperty("management.endpoint.beans.enabled", "false");
        assertProperty("management.endpoint.caches.enabled", "false");
        assertProperty("management.endpoint.conditions.enabled", "false");
        assertProperty("management.endpoint.configprops.enabled", "false");
        assertProperty("management.endpoint.env.enabled", "true");
        assertProperty("management.endpoint.flyway.enabled", "false");
        assertProperty("management.endpoint.health.enabled", "true");
        assertProperty("management.endpoint.httptrace.enabled", "false");
        assertProperty("management.endpoint.info.enabled", "true");
        assertProperty("management.endpoint.integrationgraph.enabled", "false");
        assertProperty("management.endpoint.loggers.enabled", "true");
        assertProperty("management.endpoint.liquibase.enabled", "false");
        assertProperty("management.endpoint.metrics.enabled", "true");
        assertProperty("management.endpoint.mappings.enabled", "true");
        assertProperty("management.endpoint.scheduledtasks.enabled", "false");
        assertProperty("management.endpoint.sessions.enabled", "false");
        assertProperty("management.endpoint.shutdown.enabled", "false");
        assertProperty("management.endpoint.threaddump.enabled", "false");
        assertProperty("management.endpoint.heapdump.enabled", "false");
        assertProperty("management.endpoint.jolokia.enabled", "true");
        assertProperty("management.endpoint.logfile.enabled", "false");
        assertProperty("management.endpoint.prometheus.enabled", "true");


    }

    private void assertProperty(String propertyName, String expectedValue) {
        assertEquals(environment.getRequiredProperty(propertyName), expectedValue);
    }
}
