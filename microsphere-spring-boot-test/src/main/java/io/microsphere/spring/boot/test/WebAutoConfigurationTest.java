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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

/**
 * Abstract class for auto-configuration class tests
 *
 * @param <A> the type of auto-configuration class
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationContextRunner
 * @see SpringBootTest
 * @since 1.0.0
 */
public abstract class WebAutoConfigurationTest<A> extends AbstractAutoConfigurationTest<A> {

    @Nonnull
    protected WebApplicationContextRunner webApplicationContextRunner;

    protected WebAutoConfigurationTest() {
        super();
    }

    @BeforeEach
    void setUp() {
        String[] propertyValues = getPropertyValues();
        Class<?>[] classes = getClasses();
        this.webApplicationContextRunner = new WebApplicationContextRunner()
                .withSystemProperties(propertyValues)
                .withConfiguration(of(this.autoConfigurationClass))
                .withConfiguration(of(classes));
    }

    @Test
    @Override
    protected void testAutoConfiguredClasses() {
        for (Class<?> autoConfiguredClass : getAutoConfiguredClasses()) {
            assertAutoConfiguredClass(this.webApplicationContextRunner, autoConfiguredClass);
        }
    }

    @Test
    @Override
    protected void testOnGlobalDisabledProperty() {
        for (String propertyValue : getGlobalDisabledPropertyValues()) {
            assertDisabledProperty(this.webApplicationContextRunner, propertyValue, getAutoConfiguredClasses());
        }
    }

    @Test
    @Override
    protected void testOnGlobalMissingClass() {
        for (Class<?> missingClass : getGlobalMissingClasses()) {
            assertFilteredClass(this.webApplicationContextRunner, missingClass.getName(), getAutoConfiguredClasses());
        }
    }

    public static void assertAutoConfiguredClass(WebApplicationContextRunner runner, Class<?> autoConfiguredClass) {
        runner.run(context -> assertThat(context).hasSingleBean(autoConfiguredClass));
    }

    public static void assertDisabledProperty(WebApplicationContextRunner runner, String propertyValue, Class<?>... beanClasses) {
        runner.withPropertyValues(propertyValue)
                .run(context -> {
                    for (Class<?> beanClass : beanClasses) {
                        assertThat(context).doesNotHaveBean(beanClass);
                    }
                });
    }

    public static void assertFilteredClass(WebApplicationContextRunner runner, String filteredClass, Class<?>... beanClasses) {
        runner.withClassLoader(new FilteredClassLoader(filteredClass))
                .run(context -> {
                    for (Class<?> beanClass : beanClasses) {
                        assertThat(context).doesNotHaveBean(beanClass);
                    }
                });
    }
}
