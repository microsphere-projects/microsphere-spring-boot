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
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.ApplicationContextAssertProvider;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;

import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ClassUtils.newInstance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.core.ResolvableType.forClass;

/**
 * Abstract class for auto-configuration class tests based on {@link AbstractApplicationContextRunner}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractApplicationContextRunner
 * @see AutoConfigurationTest
 * @see WebAutoConfigurationTest
 * @since 1.0.0
 */
public abstract class AbstractAutoConfigurationTest<A, R extends AbstractApplicationContextRunner> {

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

    protected final ResolvableType abstractAutoConfigurationTestType;

    /**
     * The {@link Class class} of auto-configuration
     */
    @Nonnull
    protected Class<A> autoConfigurationClass;

    /**
     * The {@link Class class} of {@link AbstractApplicationContextRunner}
     */
    @Nonnull
    protected Class<R> runnerClass;

    @Nonnull
    protected final R runner;

    protected AbstractAutoConfigurationTest() {
        Class<?> type = getClass();
        SpringBootTest springBootTest = type.getAnnotation(SPRING_BOOT_TEST_CLASS);
        assertNotNull(springBootTest, "The @SpringBootTest must not be annotated on " + type);
        this.springBootTestAttributes = GenericAnnotationAttributes.of(springBootTest);
        this.isWebApplication = !NONE.equals(springBootTestAttributes.get("webEnvironment"));
        this.abstractAutoConfigurationTestType = forClass(getClass()).as(AbstractAutoConfigurationTest.class);
        this.autoConfigurationClass = (Class<A>) abstractAutoConfigurationTestType.resolveGeneric(0);
        this.runnerClass = (Class<R>) abstractAutoConfigurationTestType.resolveGeneric(1);
        this.runner = newRunner();
        if (!isWebApplication) {
            assertInstanceOf(ApplicationContextRunner.class, this.runner,
                    format("The runner class[{}] must be ApplicationContextRunner when the auto-configuration test is not a web application", this.runner.getClass().getName()));
        }
    }

    protected R newRunner() {
        R runner = newInstance(runnerClass);
        return initRunner(runner);
    }

    protected R initRunner(R runner) {
        String[] propertyValues = getPropertyValues();
        Class<?>[] classes = getClasses();
        return (R) runner.withPropertyValues(propertyValues)
                .withConfiguration(of(this.autoConfigurationClass))
                .withConfiguration(of(classes));
    }

    @Test
    protected void testAutoConfiguredClasses() {
        for (Class<?> autoConfiguredClass : getAutoConfiguredClasses()) {
            assertAutoConfiguredClass(autoConfiguredClass);
        }
    }

    @Test
    protected void testOnGlobalDisabledProperty() {
        for (String propertyValue : getGlobalDisabledPropertyValues()) {
            assertDisabledProperty(propertyValue, getAutoConfiguredClasses());
        }
    }

    @Test
    protected void testOnGlobalMissingClass() {
        for (Class<?> missingClass : getGlobalMissingClasses()) {
            assertFilteredClass(missingClass.getName(), getAutoConfiguredClasses());
        }
    }

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

    protected void assertAutoConfiguredClass(Class<?> autoConfiguredClass) {
        assertAutoConfiguredClass(this.runner, autoConfiguredClass);
    }

    protected void assertDisabledProperty(String propertyValue, Class<?>... beanClasses) {
        assertDisabledProperty(this.runner, propertyValue, beanClasses);
    }

    protected void assertFilteredClass(String filteredClass, Class<?>... beanClasses) {
        assertFilteredClass(this.runner, filteredClass, beanClasses);
    }

    public static <R extends AbstractApplicationContextRunner<R, C, A>, C extends ConfigurableApplicationContext, A extends ApplicationContextAssertProvider<C>>
    void assertAutoConfiguredClass(R runner, Class<?> autoConfiguredClass) {
        runner.run(context -> {
            assertThat(context).hasSingleBean(autoConfiguredClass);
        });
    }

    public static <R extends AbstractApplicationContextRunner<R, C, A>, C extends ConfigurableApplicationContext, A extends ApplicationContextAssertProvider<C>>
    void assertDisabledProperty(R runner, String propertyValue, Class<?>... beanClasses) {
        runner.withPropertyValues(propertyValue).run(context -> {
            for (Class<?> beanClass : beanClasses) {
                assertThat(context).doesNotHaveBean(beanClass);
            }
        });
    }

    public static <R extends AbstractApplicationContextRunner<R, C, A>, C extends ConfigurableApplicationContext, A extends ApplicationContextAssertProvider<C>>
    void assertFilteredClass(R runner, String filteredClass, Class<?>... beanClasses) {
        runner.withClassLoader(new FilteredClassLoader(filteredClass)).run(context -> {
            for (Class<?> beanClass : beanClasses) {
                assertThat(context).doesNotHaveBean(beanClass);
            }
        });
    }
}
