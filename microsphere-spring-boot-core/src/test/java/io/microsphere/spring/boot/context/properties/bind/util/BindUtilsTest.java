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

package io.microsphere.spring.boot.context.properties.bind.util;


import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.bind;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.isBoundProperty;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.isConfigurationPropertiesBean;
import static io.microsphere.spring.boot.util.AbstractTest.assertServerPropertiesPort;
import static io.microsphere.spring.core.env.EnvironmentUtils.getProperties;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.context.properties.bind.Bindable.of;

/**
 * {@link BindUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BindUtils
 * @since 1.0.0
 */
class BindUtilsTest {

    private Bindable<ServerProperties> bindable;

    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        this.bindable = of(ServerProperties.class)
                .withAnnotations(ServerProperties.class.getAnnotations());
        this.environment = new MockEnvironment();
        this.environment.setProperty("server.port", "12345");
    }

    @Test
    void test() {
        String propertyNamePrefix = "server";
        ServerProperties serverProperties = bind(this.environment, propertyNamePrefix, ServerProperties.class, new BindListener() {
            @Override
            public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                if (name.equals(ConfigurationPropertyName.of(propertyNamePrefix))) {
                    assertFalse(isConfigurationPropertiesBean(target, context));
                    assertTrue(isConfigurationPropertiesBean(bindable, context));
                    assertFalse(isBoundProperty(context));
                } else {
                    assertFalse(isConfigurationPropertiesBean(target, context));
                    assertFalse(isConfigurationPropertiesBean(bindable, context));
                    assertTrue(isBoundProperty(context));
                }
            }
        });
        assertServerPropertiesPort(this.environment, serverProperties);
    }

    @Test
    void testIsConfigurationPropertiesBeanWithoutAnnotations() {
        assertFalse(isConfigurationPropertiesBean(of(ServerProperties.class), null));
    }

    @Test
    void testIsConfigurationPropertiesBeanOnNullArguments() {
        assertFalse(isConfigurationPropertiesBean(null, null));
        assertFalse(isConfigurationPropertiesBean(null));
    }

    @Test
    void testIsBoundPropertyWithNullBindContextContext() {
        assertFalse(isBoundProperty(null));
    }

    @Test
    void testBindWithMap() {
        Map<String, String> properties = getProperties(this.environment, "server.port");
        ServerProperties serverProperties = bind(properties, "server", ServerProperties.class);
        assertServerPropertiesPort(this.environment, serverProperties);
    }
}