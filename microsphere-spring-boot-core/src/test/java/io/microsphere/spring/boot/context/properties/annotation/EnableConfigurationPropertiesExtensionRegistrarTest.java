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

package io.microsphere.spring.boot.context.properties.annotation;


import io.microsphere.spring.boot.context.properties.ListenableConfigurationPropertiesBindHandlerAdvisor;
import io.microsphere.spring.boot.context.properties.TestConfigurationPropertiesBindHandlerAdvisor;
import io.microsphere.spring.boot.context.properties.bind.EventPublishingConfigurationPropertiesBeanPropertyChangedListener;
import io.microsphere.spring.boot.context.properties.bind.TestBindListener;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnableConfigurationPropertiesExtensionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableConfigurationPropertiesExtensionRegistrar
 * @since 1.0.0
 */
class EnableConfigurationPropertiesExtensionRegistrarTest {

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension
    class DefaultTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertTrue(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertTrue(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertTrue(isBeanPresent(this.context, TestBindListener.class));
            assertTrue(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension(adviseBindListener = false)
    class DisabledAdviseBindListenerTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertFalse(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertFalse(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertFalse(isBeanPresent(this.context, TestBindListener.class));
            assertTrue(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension(publishEvents = false)
    class DisabledPublishEventsTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertTrue(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertFalse(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertTrue(isBeanPresent(this.context, TestBindListener.class));
            assertTrue(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension(sources = {BEAN_FACTORY})
    class OnlyBeanFactorySourceTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertTrue(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertTrue(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertFalse(isBeanPresent(this.context, TestBindListener.class));
            assertFalse(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension(sources = {SPRING_FACTORIES})
    class OnlySpringFactoriesSourceTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertTrue(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertTrue(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertTrue(isBeanPresent(this.context, TestBindListener.class));
            assertFalse(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension(sources = {JAVA_SERVICE_PROVIDER})
    class OnlyJavaServiceProviderSourceTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertTrue(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertTrue(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertFalse(isBeanPresent(this.context, TestBindListener.class));
            assertTrue(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

    @Nested
    @SpringJUnitConfig
    @EnableConfigurationPropertiesExtension(adviseBindListener = false, publishEvents = false, sources = {})
    class DisabledTest {

        @Autowired
        private ConfigurableApplicationContext context;

        @Test
        void test() {
            assertFalse(isBeanPresent(this.context, ListenableConfigurationPropertiesBindHandlerAdvisor.class));
            assertFalse(isBeanPresent(this.context, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
            assertFalse(isBeanPresent(this.context, TestBindListener.class));
            assertFalse(isBeanPresent(this.context, TestConfigurationPropertiesBindHandlerAdvisor.class));
        }
    }

}