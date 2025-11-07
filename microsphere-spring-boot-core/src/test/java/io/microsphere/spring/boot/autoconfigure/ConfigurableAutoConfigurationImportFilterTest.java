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
package io.microsphere.spring.boot.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME;
import static io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass;
import static io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurableAutoConfigurationImportFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurableAutoConfigurationImportFilter
 * @since 1.0.0
 */
class ConfigurableAutoConfigurationImportFilterTest {

    private static final String TEST_CLASS_NAME_1 = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration";

    private static final String TEST_CLASS_NAME_2 = "org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration";

    private static final String TEST_CLASS_NAME_3 = "io.microsphere.logging.spring.boot.autoconfigure.WebMvcLoggingAutoConfiguration";

    private MockEnvironment environment;

    private String[] autoConfigurationClasses;

    private ConfigurableAutoConfigurationImportFilter filter;

    @BeforeEach
    void setUp() {
        this.environment = new MockEnvironment();
        this.autoConfigurationClasses = ofArray(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3);
        this.filter = new ConfigurableAutoConfigurationImportFilter();
    }

    @Test
    void testConstants() {
        assertEquals("microsphere.autoconfigure.exclude", AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME);
    }

    @Test
    void testMatch() {
        this.filter.setEnvironment(this.environment);
        boolean[] result = this.filter.match(this.autoConfigurationClasses, null);
        assertTrue(result[0]);
        assertTrue(result[1]);
        assertTrue(result[2]);

        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1);
        this.filter.setEnvironment(this.environment);
        result = this.filter.match(this.autoConfigurationClasses, null);
        assertFalse(result[0]);
        assertTrue(result[1]);
        assertTrue(result[2]);

        MutablePropertySources propertySources = this.environment.getPropertySources();
        MapPropertySource mapPropertySource1 = new MapPropertySource("map-property-source-1",
                ofMap(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_2));
        propertySources.addLast(mapPropertySource1);
        this.filter.setEnvironment(this.environment);
        result = this.filter.match(this.autoConfigurationClasses, null);
        assertFalse(result[0]);
        assertFalse(result[1]);
        assertTrue(result[2]);

        MapPropertySource mapPropertySource2 = new MapPropertySource("map-property-source-2",
                ofMap(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_3));
        propertySources.addLast(mapPropertySource2);
        this.filter.setEnvironment(this.environment);
        result = this.filter.match(this.autoConfigurationClasses, null);
        assertFalse(result[0]);
        assertFalse(result[1]);
        assertFalse(result[2]);
    }

    @Test
    void testMatchFromBinder() {
        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME + "[0]", TEST_CLASS_NAME_1);
        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME + "[1]", TEST_CLASS_NAME_2);
        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME + "[2]", TEST_CLASS_NAME_3);
        this.filter.setEnvironment(this.environment);
        boolean[] result = this.filter.match(autoConfigurationClasses, null);
        assertFalse(result[0]);
        assertFalse(result[1]);
        assertFalse(result[2]);
    }

    @Test
    void testGetExcludedAutoConfigurationClasses() {
        Set<String> classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertTrue(classNames.isEmpty());

        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1);
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(singleton(TEST_CLASS_NAME_1), classNames);

        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1 + "," + TEST_CLASS_NAME_2);
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2)), classNames);

        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1 + "," + TEST_CLASS_NAME_2 + "," + TEST_CLASS_NAME_3);
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3)), classNames);

        // Test the placeholders
        this.environment.setProperty("exclude", TEST_CLASS_NAME_1 + "," + TEST_CLASS_NAME_3);
        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, "${exclude}");
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_3)), classNames);

        this.environment.setProperty("exclude1", TEST_CLASS_NAME_1);
        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_2 + ",${exclude1}");
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_2, TEST_CLASS_NAME_1)), classNames);

        this.environment.setProperty("exclude1", TEST_CLASS_NAME_1);
        this.environment.setProperty("exclude2", TEST_CLASS_NAME_2);
        this.environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, "${exclude1},${exclude2}");
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2)), classNames);
    }

    @Test
    void testAddExcludedAutoConfigurationClass() {
        Set<String> classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertTrue(classNames.isEmpty());

        addExcludedAutoConfigurationClass(this.environment, TEST_CLASS_NAME_1);
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(singleton(TEST_CLASS_NAME_1), classNames);

        addExcludedAutoConfigurationClass(this.environment, TEST_CLASS_NAME_2);
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2)), classNames);

        // Test the placeholders
        this.environment.setProperty("exclude3", TEST_CLASS_NAME_3);
        addExcludedAutoConfigurationClass(this.environment, "${exclude3}");
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3)), classNames);

        // Test the duplicated elements
        addExcludedAutoConfigurationClass(this.environment, TEST_CLASS_NAME_3);
        classNames = getExcludedAutoConfigurationClasses(this.environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3)), classNames);
    }

    @Test
    void testIsExcluded() {
        this.filter.setEnvironment(this.environment);

        assertFalse(this.filter.isExcluded(null));
        assertFalse(this.filter.isExcluded(""));
        assertFalse(this.filter.isExcluded(" "));

        assertFalse(this.filter.isExcluded(TEST_CLASS_NAME_1));

        addExcludedAutoConfigurationClass(this.environment, TEST_CLASS_NAME_1);
        this.filter.setEnvironment(this.environment);
        assertTrue(this.filter.isExcluded(TEST_CLASS_NAME_1));
    }
}