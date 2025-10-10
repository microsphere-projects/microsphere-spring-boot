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

package io.microsphere.spring.boot.util;


import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.spring.boot.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.addDefaultPropertiesResource;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.addDefaultPropertiesResources;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getDefaultPropertiesResources;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getLoggingLevel;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getResourceLoader;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.log;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.WebApplicationType.NONE;

/**
 * {@link SpringApplicationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringApplicationUtils
 * @since 1.0.0
 */
class SpringApplicationUtilsTest {

    @Test
    void testAddDefaultPropertiesResource() {
        addDefaultPropertiesResource(null);
        addDefaultPropertiesResource("");
        addDefaultPropertiesResource(" ");
        addDefaultPropertiesResource("test.properties");
    }

    @Test
    void testAddDefaultPropertiesResources() {
        addDefaultPropertiesResources(null);
        addDefaultPropertiesResources();
        addDefaultPropertiesResources("a", "", null);
    }

    @Test
    void testGetDefaultPropertiesResources() {
        addDefaultPropertiesResources("a", "b", "c");
        assertEquals(ofSet("a", "b", "c"), getDefaultPropertiesResources());
    }

    @Test
    void testGetResourceLoader() {
        SpringApplication springApplication = new SpringApplication();
        ResourceLoader resourceLoader = getResourceLoader(springApplication);
        assertNotNull(resourceLoader);

        springApplication.setResourceLoader(resourceLoader);
        assertSame(resourceLoader, getResourceLoader(springApplication));
    }

    @Test
    void testGetLoggingLevel() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        assertEquals(DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL, getLoggingLevel(context));
    }

    @Test
    void testGetLoggingLevelFromPropertyResolver() {
        testGetLoggingLevel("TRACE");
        testGetLoggingLevel("DEBUG");
        testGetLoggingLevel("INFO");
        testGetLoggingLevel("WARN");
        testGetLoggingLevel("ERROR");
        testGetLoggingLevel("OFF");
    }

    @Test
    void testLog() {
        testLog("TRACE");
        testLog("DEBUG");
        testLog("INFO");
        testLog("WARN");
        testLog("ERROR");
        testLog("OFF");
    }

    void testGetLoggingLevel(String level) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME, level);
        assertEquals(level, getLoggingLevel(environment));
    }

    void testLog(String level) {
        SpringApplication springApplication = new SpringApplication(SpringApplicationUtilsTest.class);
        springApplication.setWebApplicationType(NONE);
        String name = MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME;
        String arg = "--" + name + "=" + level;
        ConfigurableApplicationContext context = springApplication.run(arg);
        log(springApplication, context, level, arg);
    }
}