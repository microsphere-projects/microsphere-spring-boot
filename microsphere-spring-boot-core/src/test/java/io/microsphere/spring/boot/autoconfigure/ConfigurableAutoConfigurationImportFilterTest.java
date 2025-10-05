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
import org.springframework.mock.env.MockEnvironment;

import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter.AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME;
import static io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter.addExcludedAutoConfigurationClass;
import static io.microsphere.spring.boot.autoconfigure.ConfigurableAutoConfigurationImportFilter.getExcludedAutoConfigurationClasses;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurableAutoConfigurationImportFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurableAutoConfigurationImportFilter
 * @since 1.0.0
 */
public class ConfigurableAutoConfigurationImportFilterTest {

    private static final String TEST_CLASS_NAME_1 = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration";

    private static final String TEST_CLASS_NAME_2 = "org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration";

    private static final String TEST_CLASS_NAME_3 = "io.microsphere.logging.spring.boot.autoconfigure.WebMvcLoggingAutoConfiguration";

    private MockEnvironment environment;

    @BeforeEach
    void before() {
        environment = new MockEnvironment();
    }

    @Test
    public void testConstants() {
        assertEquals("microsphere.autoconfigure.exclude", AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME);
    }

    @Test
    public void testGetExcludedAutoConfigurationClasses() {
        Set<String> classNames = getExcludedAutoConfigurationClasses(environment);
        assertTrue(classNames.isEmpty());

        environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1);
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(singleton(TEST_CLASS_NAME_1), classNames);

        environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1 + "," + TEST_CLASS_NAME_2);
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2)), classNames);

        environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_1 + "," + TEST_CLASS_NAME_2 + "," + TEST_CLASS_NAME_3);
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3)), classNames);

        // Test the placeholders
        environment.setProperty("exclude", TEST_CLASS_NAME_1 + "," + TEST_CLASS_NAME_3);
        environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, "${exclude}");
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_3)), classNames);

        environment.setProperty("exclude1", TEST_CLASS_NAME_1);
        environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, TEST_CLASS_NAME_2 + ",${exclude1}");
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_2, TEST_CLASS_NAME_1)), classNames);

        environment.setProperty("exclude1", TEST_CLASS_NAME_1);
        environment.setProperty("exclude2", TEST_CLASS_NAME_2);
        environment.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY_NAME, "${exclude1},${exclude2}");
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2)), classNames);
    }

    @Test
    public void testAddExcludedAutoConfigurationClass() {
        Set<String> classNames = getExcludedAutoConfigurationClasses(environment);
        assertTrue(classNames.isEmpty());

        addExcludedAutoConfigurationClass(environment, TEST_CLASS_NAME_1);
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(singleton(TEST_CLASS_NAME_1), classNames);

        addExcludedAutoConfigurationClass(environment, TEST_CLASS_NAME_2);
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2)), classNames);

        // Test the placeholders
        environment.setProperty("exclude3", TEST_CLASS_NAME_3);
        addExcludedAutoConfigurationClass(environment, "${exclude3}");
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3)), classNames);

        // Test the duplicated elements
        addExcludedAutoConfigurationClass(environment, TEST_CLASS_NAME_3);
        classNames = getExcludedAutoConfigurationClasses(environment);
        assertEquals(new TreeSet(asList(TEST_CLASS_NAME_1, TEST_CLASS_NAME_2, TEST_CLASS_NAME_3)), classNames);

    }
}
