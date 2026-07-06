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

package io.microsphere.spring.boot.test;

import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.core.annotation.GenericAnnotationAttributes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.core.ResolvableType.forClass;

/**
 * Abstract class for auto-configuration class tests
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutoConfigurationTest
 * @since 1.0.0
 */
public abstract class AbstractAutoConfigurationTest<A> {

    /**
     * The {@link Class class} of {@link SpringBootTest}
     */
    public static final Class<SpringBootTest> SPRING_BOOT_TEST_CLASS = SpringBootTest.class;

    /**
     * The {@link GenericAnnotationAttributes} of {@link SpringBootTest}
     */
    @Nonnull
    protected final GenericAnnotationAttributes<SpringBootTest> springBootTestAttributes;

    /**
     * Is web application ?
     */
    protected final boolean isWebApplication;

    /**
     * The {@link Class class} of auto-configuration
     */
    @Nonnull
    protected Class<A> autoConfigurationClass;

    protected AbstractAutoConfigurationTest() {
        Class<?> type = getClass();
        SpringBootTest springBootTest = type.getAnnotation(SPRING_BOOT_TEST_CLASS);
        assertNotNull(springBootTest, "The @SpringBootTest must not be annotated on " + type);
        this.springBootTestAttributes = GenericAnnotationAttributes.of(springBootTest);
        this.isWebApplication = !NONE.equals(springBootTestAttributes.get("webEnvironment"));
        this.autoConfigurationClass = (Class<A>) forClass(getClass())
                .as(AbstractAutoConfigurationTest.class).resolveGeneric(0);
    }

    @Test
    protected abstract void testAutoConfiguredClasses();

    @Test
    protected abstract void testOnGlobalDisabledProperty();

    @Test
    protected abstract void testOnGlobalMissingClass();

    @Nonnull
    protected final String[] getPropertyValues() {
        return this.springBootTestAttributes.getStringArray("properties");
    }

    @Nonnull
    protected final Class<?>[] getAutoConfiguredClasses() {
        Set<Class<?>> autoConfiguredClasses = newLinkedHashSet();
        autoConfiguredClasses.add(this.autoConfigurationClass);
        configureAutoConfiguredClasses(autoConfiguredClasses);
        return autoConfiguredClasses.toArray(EMPTY_CLASS_ARRAY);
    }

    @Nonnull
    protected final Set<String> getGlobalDisabledPropertyValues() {
        Set<String> globalDisabledPropertyValues = newLinkedHashSet();
        configureGlobalDisabledPropertyValues(globalDisabledPropertyValues);
        return globalDisabledPropertyValues;
    }

    @Nonnull
    protected final Set<Class<?>> getGlobalMissingClasses() {
        Set<Class<?>> globalMissingClasses = newLinkedHashSet();
        configureGlobalMissingClasses(globalMissingClasses);
        return globalMissingClasses;
    }

    protected final Class<?>[] getClasses() {
        return this.springBootTestAttributes.getClassArray("classes");
    }

    /**
     * Configure auto-configured classes
     *
     * @param autoConfiguredClasses the auto-configured classes
     */
    protected abstract void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses);

    /**
     * Configure global disabled property values
     *
     * @param globalDisabledPropertyValues the global disabled property values
     */
    protected abstract void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues);

    /**
     * Configure global missing classes
     *
     * @param globalMissingClasses the global missing classes
     */
    protected abstract void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses);
}
