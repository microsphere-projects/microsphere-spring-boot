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

package io.microsphere.spring.boot.context.autoconfigure;


import io.microsphere.spring.boot.context.properties.ListenableConfigurationPropertiesBindHandlerAdvisor;
import io.microsphere.spring.boot.context.properties.TestConfigurationPropertiesBindHandlerAdvisor;
import io.microsphere.spring.boot.context.properties.bind.EventPublishingConfigurationPropertiesBeanPropertyChangedListener;
import io.microsphere.spring.boot.context.properties.bind.TestBindListener;
import io.microsphere.spring.boot.test.AutoConfigurationTest;
import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link ConfigurationPropertiesAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = ConfigurationPropertiesAutoConfigurationTest.class,
        webEnvironment = NONE
)
class ConfigurationPropertiesAutoConfigurationTest extends AutoConfigurationTest<ConfigurationPropertiesAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(ListenableConfigurationPropertiesBindHandlerAdvisor.class);
        autoConfiguredClasses.add(EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class);
        autoConfiguredClasses.add(TestBindListener.class);
        autoConfiguredClasses.add(TestConfigurationPropertiesBindHandlerAdvisor.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(BeanCapableImportCandidate.class);
    }
}