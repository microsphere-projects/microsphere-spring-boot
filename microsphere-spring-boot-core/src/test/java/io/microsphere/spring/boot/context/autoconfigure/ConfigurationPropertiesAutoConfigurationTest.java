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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurationPropertiesAutoConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesAutoConfiguration
 * @since 1.0.0
 */
@SpringJUnitConfig
@EnableAutoConfiguration
class ConfigurationPropertiesAutoConfigurationTest {

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