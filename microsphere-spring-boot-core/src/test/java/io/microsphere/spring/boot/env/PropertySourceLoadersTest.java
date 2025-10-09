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
package io.microsphere.spring.boot.env;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockPropertySource;

import java.io.IOException;
import java.util.List;

import static io.microsphere.collection.Sets.ofSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PropertySourceLoaders}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertySourceLoaders
 * @since 1.0.0
 */
public class PropertySourceLoadersTest {

    private static final PropertySourceLoaders propertySourceLoaders = new PropertySourceLoaders();

    private static final String TEST_PROPERTY_NAME = "core";

    private static final String TEST_RESOURCE_LOCATION = "classpath:/config/default/core.properties";

    @Test
    void testGetFileExtensions() {
        String[] fileExtensions = propertySourceLoaders.getFileExtensions();
        assertEquals(ofSet("properties", "xml", "yml", "yaml"), ofSet(fileExtensions));
    }

    @Test
    void testLoad() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(TEST_RESOURCE_LOCATION);
        List<PropertySource<?>> propertySources = propertySourceLoaders.load(TEST_PROPERTY_NAME, resource);
        assertEquals(2, propertySources.size());

        PropertySource propertySource = propertySources.get(1);
        assertPropertySource(propertySource);
    }

    @Test
    void testLoadAsOriginTracked() throws IOException {
        PropertySource propertySource = propertySourceLoaders.loadAsOriginTracked(TEST_PROPERTY_NAME, TEST_RESOURCE_LOCATION);
        assertTrue(propertySource instanceof OriginLookup);
        assertPropertySource(propertySource);
    }

    @Test
    void testLoadAsOriginTrackedOnNull() throws IOException {
        PropertySource propertySource = propertySourceLoaders.loadAsOriginTracked(TEST_PROPERTY_NAME, "classpath:/META-INF/spring.factories");
        assertNull(propertySource);
    }

    @Test
    void testLoadAsOriginTrackedOnIOException() throws IOException {
        assertThrows(IOException.class, () -> propertySourceLoaders.loadAsOriginTracked(TEST_PROPERTY_NAME, TEST_RESOURCE_LOCATION + ".test"));
    }

    @Test
    void testReloadAsOriginTracked() throws IOException {
        PropertySource propertySource = propertySourceLoaders.loadAsOriginTracked(TEST_PROPERTY_NAME, TEST_RESOURCE_LOCATION);
        assertSame(propertySource, propertySourceLoaders.reloadAsOriginTracked(propertySource));
    }

    @Test
    void testReloadAsOriginTrackedOnClassPathResource() throws IOException {
        MockPropertySource mockPropertySource = new MockPropertySource("[" + TEST_RESOURCE_LOCATION + "]");
        PropertySource propertySource = propertySourceLoaders.reloadAsOriginTracked(mockPropertySource);
        assertTrue(propertySource instanceof OriginTrackedMapPropertySource);
    }

    private void assertPropertySource(PropertySource<?> propertySource) {
        assertTrue(propertySource instanceof OriginTrackedMapPropertySource);
        assertEquals(TEST_PROPERTY_NAME, propertySource.getName());
        assertEquals("graceful", propertySource.getProperty("server.shutdown"));
    }
}