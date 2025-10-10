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

package io.microsphere.spring.boot.env.config;


import io.microsphere.spring.boot.env.config.OriginTrackedConfigurationPropertyInitializer.NamedOrigin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.boot.util.TestUtils.application;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.origin.OriginTrackedValue.of;

/**
 * {@link OriginTrackedConfigurationPropertyInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OriginTrackedConfigurationPropertyInitializer
 * @since 1.0.0
 */
class OriginTrackedConfigurationPropertyInitializerTest {

    private ConfigurableApplicationContext context;

    private OriginTrackedConfigurationPropertyInitializer initializer;

    @BeforeEach
    void setUp() {
        this.context = new GenericApplicationContext();
        this.initializer = new OriginTrackedConfigurationPropertyInitializer();
        this.initializer.initialize(context);
    }

    @Test
    void testInitialize() {
        SpringApplication springApplication = application();
        ConfigurableApplicationContext context = springApplication.run();
        assertNotNull(context);
        context.close();
    }

    @Test
    void testInitializePropertySourcesOnIOException() throws IOException {
        MutablePropertySources propertySources = new MutablePropertySources();

        String name = "[classpath:/not-found.yaml]";
        EncodedResource resource = new EncodedResource(new ClassPathResource("META-INF/config/default/test.properties"));
        ResourcePropertySource propertySource = new ResourcePropertySource(name, resource);

        propertySources.addLast(propertySource);
        this.initializer.initializePropertySources(propertySources);
        assertSame(propertySource, propertySources.get(name));
    }

    @Test
    void testCreateOriginTrackedPropertySource() throws IOException {
        String propertySourceName = "mockPropertySource";
        Map<String, Object> source = new HashMap<>();
        MapPropertySource propertySource = new MapPropertySource(propertySourceName, source);
        String propertyName = "test-name";
        Object propertyValue = new Object();
        OriginTrackedValue originTrackedValue = of(propertyValue, new NamedOrigin("mock"));
        source.put(propertyName, originTrackedValue);
        source.put("not-test-name", null);
        source.put("test-name-2", "test-value-2");
        PropertySource originTrackedPropertySource = this.initializer.createOriginTrackedPropertySource(propertySource);
        assertEquals(propertySourceName, originTrackedPropertySource.getName());
        assertEquals(propertyValue, originTrackedPropertySource.getProperty(propertyName));
    }

    @Test
    void testNamedOrigin() {
        String name = "test";
        NamedOrigin namedOrigin = new NamedOrigin(name);
        assertEquals(name, namedOrigin.toString());
    }
}